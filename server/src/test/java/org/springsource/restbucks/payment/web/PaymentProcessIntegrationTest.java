/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springsource.restbucks.payment.web;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.hateoas.IanaLinkRelations.SELF;
import static org.springframework.hateoas.MediaTypes.HAL_JSON;
import static org.springframework.http.HttpStatus.NOT_MODIFIED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.UnsupportedEncodingException;

import org.junit.jupiter.api.Test;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.client.LinkDiscoverer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springsource.restbucks.AbstractWebIntegrationTest;
import org.springsource.restbucks.order.Order;

import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;

/**
 * Integration tests modeling the hypermedia-driven interaction flow against the
 * server implementation. Uses the Spring MVC integration test facilities
 * introduced in 3.2. Implements the order process modeled in my presentation on
 * Hypermedia design with Spring.
 *
 * @see http://bit.ly/UIcDvq
 * @author Oliver Gierke
 * @author Peter Fichtner
 */
@Slf4j
class PaymentProcessIntegrationTest extends AbstractWebIntegrationTest {

	private static final LinkRelation ORDER_REL = BUILDER.relation("order");
	private static final LinkRelation RECEIPT_REL = BUILDER.relation("receipt");
	private static final LinkRelation CANCEL_REL = BUILDER.relation("cancel");
	private static final LinkRelation PAYMENT_REL = BUILDER.relation("payment");

	private static final String FIRST_ORDER_EXPRESSION = String.format("$._embedded.%s[0]", ORDERS_REL);

	/**
	 * Processes the first existing {@link Order} found.
	 */
	@Test
	void processFirstExistingOrder() throws Exception {
		var root = accessRootResource();
		createNewOrder(root);

		var orders = discoverOrdersResource(root);
		var order = accessFirstOrder(orders);
		payAndTakeReceipt(order);
	}

	/**
	 * Creates a new {@link Order} and processes that one.
	 */
	@Test
	void processNewOrder() throws Exception {
		var root = accessRootResource();

		var order = createNewOrder(root);
		payAndTakeReceipt(order);
	}

	/**
	 * Creates a new {@link Order} and cancels it right away.
	 */
	@Test
	void cancelOrderBeforePayment() throws Exception {
		var root = accessRootResource();

		var order = createNewOrder(root);
		cancelOrder(order);
	}

	/**
	 * Access the root resource by referencing the well-known URI. Verifies the
	 * orders resource being present.
	 *
	 * @return the response for the orders resource
	 * @throws Exception
	 */
	@Test
	void tryCheatCancelationAfterPayment() throws Exception {
		var root = accessRootResource();

		var order = createNewOrder(root);
		triggerPayment(order);
		verifyCancelationIsNotAllowed(order);

		// wait until ready otherwise Engine will complain because it gets interrupted
		// during it's sleep time
		pollUntilOrderHasReceiptLink(order);
	}

	/**
	 * Verifies that there is no content on HTTP Status code
	 * {@link HttpStatus#NOT_MODIFIED}.
	 */
	@Test
	void verifyNoContent() throws Exception {
		var root = accessRootResource();

		var order = createNewOrder(root);
		var payment = triggerPayment(order);
		order = orderOf(payment);

		var updater = orderStateUpdater(order);
		while (!updater.doUpdate().statusIs(NOT_MODIFIED)) {
			MILLISECONDS.sleep(250);
		}
		assertThat(updater.contentAsString()).isEmpty();
	}

	void payAndTakeReceipt(MockHttpServletResponse order) throws Exception {
		var payment = triggerPayment(order);
		order = orderOf(payment);
		order = pollUntilOrderHasReceiptLink(order);

		var receipt = takeReceipt(order);
		verifyOrderTaken(receipt);
	}

	/**
	 * Follows the {@code orders} link returns the {@link Order}s found.
	 *
	 * @param root
	 * @return
	 * @throws Exception
	 */
	private MockHttpServletResponse discoverOrdersResource(MockHttpServletResponse root) throws Exception {
		String content = root.getContentAsString();
		Link ordersLink = getDiscovererFor(root).findRequiredLinkWithRel(ORDERS_REL, content);

		LOG.info("Root resource returned: " + content);
		LOG.info(String.format("Found orders link pointing to %s… Following…", ordersLink));

		var response = mvc.perform(get(ordersLink.expand().getHref())) //
				.andExpect(status().isOk()) //
				.andReturn().getResponse();

		LOG.info("Found orders: " + response.getContentAsString());
		return response;
	}

	/**
	 * Looks up the first {@link Order} from the orders representation using a
	 * JSONPath expression of {@value #FIRST_ORDER_EXPRESSION}. Looks up the
	 * {@value Link#REL_SELF} link from the nested object and follows it to lookup
	 * the representation. Verifies the {@code self}, {@code cancel}, and
	 * {@code update} link to be present.
	 *
	 * @param orders
	 * @return
	 * @throws Exception
	 */
	private MockHttpServletResponse accessFirstOrder(MockHttpServletResponse orders) throws Exception {
		String content = orders.getContentAsString();
		String order = JsonPath.parse(content).read(JsonPath.compile(FIRST_ORDER_EXPRESSION), JSONObject.class)
				.toString();
		Link orderLink = getDiscovererFor(orders).findRequiredLinkWithRel(SELF, order).expand();

		LOG.info(String.format("Picking first order using JSONPath expression %s…", FIRST_ORDER_EXPRESSION));
		LOG.info(String.format("Discovered self link pointing to %s… Following", orderLink));

		return mvc.perform(get(orderLink.getHref())).andReturn().getResponse();
	}

	/**
	 * Triggers the payment of an {@link Order} by following the {@code payment}
	 * link and submitting a credit card number to it. Verifies that we get a
	 * {@code 201 Created} and the response contains an {@code order} link.
	 *
	 * @param order
	 * @return
	 * @throws Exception
	 */
	private MockHttpServletResponse triggerPayment(MockHttpServletResponse order) throws Exception {
		String content = order.getContentAsString();
		LinkDiscoverer discoverer = getDiscovererFor(order);
		Link paymentLink = discoverer.findRequiredLinkWithRel(PAYMENT_REL, content);

		LOG.info(String.format("Discovered payment link pointing to %s…", paymentLink));

		assertThat(paymentLink).isNotNull();

		LOG.info("Triggering payment…");
		ResultActions action = mvc.perform(put(paymentLink.getHref())//
				.content("{ \"number\" : \"1234123412341234\" }")//
				.contentType(APPLICATION_JSON)//
				.accept(HAL_JSON));

		try {
			return action.andExpect(status().isCreated()).andReturn().getResponse();
		} finally {
			LOG.info("Payment triggered…");
		}
	}

	/**
	 * Fake a cancellation to make sure it is rejected with a
	 * {@link HttpStatus#METHOD_NOT_ALLOWED}.
	 * 
	 * @param order
	 * @throws Exception
	 */
	private void verifyCancelationIsNotAllowed(MockHttpServletResponse order) throws Exception {
		String content = order.getContentAsString();
		LinkDiscoverer discoverer = getDiscovererFor(order);

		// Make sure we cannot cheat and cancel the order after it has been payed
		LOG.info("Faking a cancel request to make sure it's forbidden…");
		Link selfLink = discoverer.findRequiredLinkWithRel(SELF, content);
		mvc.perform(delete(selfLink.getHref())).andExpect(status().isMethodNotAllowed());
	}

	private MockHttpServletResponse orderOf(MockHttpServletResponse payment) throws Exception {
		String content = payment.getContentAsString();
		LinkDiscoverer discoverer = getDiscovererFor(payment);
		Link orderLink = discoverer.findRequiredLinkWithRel(ORDER_REL, content);

		return mvc.perform(get(orderLink.expand().getHref())).andReturn().getResponse();
	}

	private OrderStateUpdater orderStateUpdater(MockHttpServletResponse order) throws UnsupportedEncodingException {
		var discoverer = getDiscovererFor(order);
		var orderLink = discoverer.findRequiredLinkWithRel(SELF, order.getContentAsString());
		return new OrderStateUpdater(mvc, orderLink);
	}

	/**
	 * Polls the order resource every 2 seconds and uses an {@code If-None-Match}
	 * header alongside the {@code ETag} of the first response to avoid sending the
	 * representation over and over again.
	 *
	 * @param payment
	 * @return
	 * @throws Exception
	 */
	private static class OrderStateUpdater {

		private final MockMvc mockMvc;
		private final Link orderLink;

		private MockHttpServletResponse response;

		OrderStateUpdater(MockMvc mockMvc, Link orderLink) throws UnsupportedEncodingException {
			this.mockMvc = mockMvc;
			this.orderLink = orderLink;
		}

		String contentAsString() {
			try {
				return response.getContentAsString();
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		}

		OrderStateUpdater doUpdate() throws Exception {
			response = mockMvc.perform(get(orderLink.expand().getHref()).headers(headers())).andReturn().getResponse();
			LOG.info(String.format("Received %s with ETag of %s…", response.getStatus(), etag()));
			return this;
		}

		String etag() {
			return response == null ? null : response.getHeader("ETag");
		}

		HttpHeaders headers() {
			HttpHeaders headers = new HttpHeaders();
			String etag = etag();
			if (etag != null) {
				headers.setIfNoneMatch(etag);
			}
			return headers;
		}

		boolean statusIs(HttpStatus httpStatus) {
			return response.getStatus() == httpStatus.value();
		}

	}

	private MockHttpServletResponse pollUntilOrderHasReceiptLink(MockHttpServletResponse order) throws Exception {
		var discoverer = getDiscovererFor(order);
		var updater = orderStateUpdater(order);
		while (true) {
			LOG.info("Poll state of order until receipt is ready…");
			updater.doUpdate();
			if (updater.statusIs(OK)
					&& discoverer.findLinkWithRel(RECEIPT_REL, updater.contentAsString()).isPresent()) {
				return updater.response;
			}
			SECONDS.sleep(2);
		}

	}

	/**
	 * Concludes the {@link Order} by looking up the {@code receipt} link from the
	 * response and follows it. Triggers a {@code DELETE} request subsequently.
	 *
	 * @param order
	 * @return
	 * @throws Exception
	 */
	private MockHttpServletResponse takeReceipt(MockHttpServletResponse order) throws Exception {
		Link receiptLink = getDiscovererFor(order).findRequiredLinkWithRel(RECEIPT_REL, order.getContentAsString());

		var receiptResponse = mvc.perform(get(receiptLink.getHref())) //
				.andExpect(status().isOk()) //
				.andReturn().getResponse();

		LOG.info("Accessing receipt, got:" + receiptResponse.getContentAsString());
		LOG.info("Taking receipt…");

		return mvc.perform(delete(receiptLink.getHref()).accept(HAL_JSON)) //
				.andExpect(status().isOk()) //
				.andReturn().getResponse();
	}

	private void verifyOrderTaken(MockHttpServletResponse order) throws Exception {
		Link orderLink = getDiscovererFor(order).findRequiredLinkWithRel(ORDER_REL, order.getContentAsString());
		var response = mvc.perform(get(orderLink.expand().getHref())) //
				.andExpect(status().isOk()) // //
				.andExpect(jsonPath("$.status", is("Delivered"))) //
				.andReturn().getResponse();

		LOG.info("Final order state: " + response.getContentAsString());
	}

	/**
	 * Cancels the order by issuing a delete request. Verifies the resource being
	 * unavailable after that.
	 *
	 * @param order the response that retrieved an order resource
	 * @throws Exception
	 */
	private void cancelOrder(MockHttpServletResponse order) throws Exception {
		String content = order.getContentAsString();

		LinkDiscoverer discoverer = getDiscovererFor(order);
		Link selfLink = discoverer.findRequiredLinkWithRel(SELF, content);
		Link cancellationLink = discoverer.findRequiredLinkWithRel(CANCEL_REL, content);

		mvc.perform(delete(cancellationLink.getHref())).andExpect(status().isNoContent());
		mvc.perform(get(selfLink.getHref())).andExpect(status().isNotFound());
	}

	private void verifyThatOrderCannotBeCanceled(MockHttpServletResponse order) throws Exception {
		String content = order.getContentAsString();
		LinkDiscoverer discoverer = getDiscovererFor(order);
		LOG.info("Faking a cancel request to make sure it's forbidden…");
		Link orderLink = discoverer.findRequiredLinkWithRel(ORDER_REL, content).expand();

		mvc.perform(get(orderLink.expand().getHref())). //
				andExpect(status().isOk()). //
				andExpect(linkWithRelIsNotPresent(CANCEL_REL));

		mvc.perform(delete(orderLink.getHref())).andExpect(status().isMethodNotAllowed());
	}

}
