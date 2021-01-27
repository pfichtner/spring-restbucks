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
package org.springsource.restbucks.customercard.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.client.LinkDiscoverer;
import org.springframework.hateoas.mediatype.hal.HalLinkRelation;
import org.springframework.hateoas.mediatype.hal.HalLinkRelation.HalLinkRelationBuilder;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springsource.restbucks.AbstractWebIntegrationTest;
import org.springsource.restbucks.Restbucks;
import org.springsource.restbucks.order.Order;

import lombok.extern.slf4j.Slf4j;

/**
 * Integration tests modeling the hypermedia-driven interaction flow against the
 * server implementation. Uses the Spring MVC integration test facilities
 * introduced in 3.2. Implements the order process modeled in my presentation on
 * Hypermedia design with Spring.
 *
 * @author Peter Fichtner
 */
@Slf4j
class CustomerCardScansIntegrationTest extends AbstractWebIntegrationTest {

	private static final HalLinkRelationBuilder BUILDER = HalLinkRelation.curieBuilder(Restbucks.CURIE_NAMESPACE);

	private static final LinkRelation ORDERS_REL = BUILDER.relation("orders");
	private static final LinkRelation ORDER_REL = BUILDER.relation("order");
	private static final LinkRelation CUSTOMERCARD_REL = BUILDER.relation("customercard");

	/**
	 * Creates a new {@link Order} and processes that one.
	 */
	@Test
	void processNewOrder() throws Exception {

		MockHttpServletResponse response = accessRootResource();

		response = createNewOrder(response);
		response = scanCustomerCard(response);

	}

	/**
	 * Access the root resource by referencing the well-known URI. Verifies the
	 * orders resource being present.
	 *
	 * @return the response for the orders resource
	 * @throws Exception
	 */
	private MockHttpServletResponse accessRootResource() throws Exception {

		LOG.info("Accessing root resource…");

		MockHttpServletResponse response = mvc.perform(get("/")). //
				andExpect(status().isOk()). //
				andExpect(linkWithRelIsPresent(ORDERS_REL)). //
				andReturn().getResponse();

		return response;
	}

	/**
	 * Creates a new {@link Order} by looking up the orders link from the source and
	 * posting the content of {@code orders.json} to it. Verifies we receive a
	 * {@code 201 Created} and a {@code Location} header. Follows the location
	 * header to retrieve and verify the {@link Order} just created.
	 *
	 * @param source
	 * @return
	 * @throws Exception
	 */
	private MockHttpServletResponse createNewOrder(MockHttpServletResponse source) throws Exception {

		String content = source.getContentAsString();

		Link ordersLink = getDiscovererFor(source).findRequiredLinkWithRel(ORDERS_REL, content);

		ClassPathResource resource = new ClassPathResource("order.json");
		byte[] data = Files.readAllBytes(resource.getFile().toPath());

		MockHttpServletResponse result = mvc
				.perform(post(ordersLink.expand().getHref()).contentType(MediaType.APPLICATION_JSON).content(data)). //
				andExpect(status().isCreated()). //
				andExpect(header().string("Location", is(notNullValue()))). //
				andReturn().getResponse();

		return mvc.perform(get(result.getHeader("Location"))).andReturn().getResponse();
	}

	private MockHttpServletResponse scanCustomerCard(MockHttpServletResponse response) throws Exception {

		String content = response.getContentAsString();
		LinkDiscoverer discoverer = getDiscovererFor(response);
		Link customerCardLink = discoverer.findRequiredLinkWithRel(CUSTOMERCARD_REL, content);

		LOG.info(String.format("Discovered customercard link pointing to %s…", customerCardLink));

		assertThat(customerCardLink).isNotNull();

		LOG.info("Triggering customer card scan…");

		response = mvc.perform(put(customerCardLink.getHref()) //
				.content("{ \"number\" : \"AAFF123456\" }") //
				.contentType(MediaType.APPLICATION_JSON) //
				.accept(MediaTypes.HAL_JSON)).andExpect(status().isCreated()). //
				andExpect(header().string("Location", is(notNullValue()))). //
				andExpect(linkWithRelIsPresent(ORDER_REL)). //
				andReturn().getResponse();

		LOG.info("customer card scaned…");

		// read created resource
		mvc.perform(get(response.getHeader("Location"))) //
				.andExpect(status().isOk()) //
				.andReturn().getResponse();

		// refetch the order resource, CUSTOMERCARD_REL still is existent (can be
		// scanned multiple times, last card wins)
		Link orderLink = getDiscovererFor(response).findRequiredLinkWithRel(ORDER_REL, response.getContentAsString());
		response = mvc.perform(get(orderLink.expand().getHref())). //
				andExpect(status().isOk()). // //
				andExpect(linkWithRelIsPresent(CUSTOMERCARD_REL)). //
				andReturn().getResponse(); //

		return response;
	}

}
