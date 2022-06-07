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
package org.springsource.restbucks.order;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.hateoas.IanaLinkRelations.SELF;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.mediatype.hal.HalLinkRelation;
import org.springframework.hateoas.mediatype.hal.HalLinkRelation.HalLinkRelationBuilder;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springsource.restbucks.AbstractWebIntegrationTest;
import org.springsource.restbucks.Restbucks;

import com.jayway.jsonpath.JsonPath;

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
class OrderProcessIntegrationTest extends AbstractWebIntegrationTest {

	private static final HalLinkRelationBuilder BUILDER = HalLinkRelation.curieBuilder(Restbucks.CURIE_NAMESPACE);

	private static final LinkRelation ORDERS_REL = BUILDER.relation("orders");

	/**
	 * Creates a new {@link Order} and replaces that one.
	 */
	@Test
	void canReplaceOrder() throws Exception {
		var root = accessRootResource();

		var order = createNewOrder(root);
		order = replaceOrder(order);
//		verifyOrderIsXXXXXXXXXXXXXXXXX(order);
	}

	/**
	 * Creates a new {@link Order} and replaces that one.
	 */
	@Test
	void canUpdateOrder() throws Exception {
		var root = accessRootResource();
		
		var order = createNewOrder(root);
		order = updateOrder(order);
//		verifyOrderIsXXXXXXXXXXXXXXXXX(order);
	}

	/**
	 * Access the root resource by referencing the well-known URI.
	 *
	 * @return the response for the orders resource
	 * @throws Exception
	 */
	private MockHttpServletResponse accessRootResource() throws Exception {
		LOG.info("Accessing root resource…");
		return mvc.perform(get("/")).andExpect(status().isOk()).andReturn().getResponse();
	}

	/**
	 * Creates a new {@link Order} by looking up the orders link from the source and
	 * posting the content of {@code orders.json} to it. Verifies we receive a
	 * {@code 201 Created} and a {@code Location} header. Follows the location
	 * header to retrieve and verify the {@link Order} just created.
	 *
	 * @param root
	 * @return
	 * @throws Exception
	 */
	private MockHttpServletResponse createNewOrder(MockHttpServletResponse root) throws Exception {
		String content = root.getContentAsString();

		Link ordersLink = getDiscovererFor(root).findRequiredLinkWithRel(ORDERS_REL, content);

		ClassPathResource resource = new ClassPathResource("order.json");
		byte[] data = Files.readAllBytes(resource.getFile().toPath());

		var response = mvc.perform(post(ordersLink.expand().getHref()).contentType(APPLICATION_JSON).content(data)) //
				.andExpect(status().isCreated()) //
				.andExpect(header().string("Location", is(notNullValue()))) //
				.andReturn().getResponse();

		return mvc.perform(get(response.getHeader("Location"))).andReturn().getResponse();
	}

	/**
	 * Updates an existing {@link Order}
	 *
	 * @param order
	 * @return
	 * @throws Exception
	 */
	private MockHttpServletResponse updateOrder(MockHttpServletResponse order) throws Exception {
		String content = order.getContentAsString();

		String location = JsonPath.parse(content).read(JsonPath.compile("$.location"), String.class).toString();

		Link orderLink = getDiscovererFor(order).findRequiredLinkWithRel(SELF, content);

		byte[] data = ("{ \"location\": \"" + switchLocation(location) + "\" }").getBytes();
		mvc.perform(patch(orderLink.expand().getHref()).contentType(APPLICATION_JSON).content(data)) //
				.andExpect(status().isNoContent()) //
				.andReturn().getResponse();

		return mvc.perform(get(orderLink.expand().getHref())).andExpect(status().isOk()).andReturn().getResponse();
	}

	/**
	 * Replaces an existing {@link Order}
	 *
	 * @param order
	 * @return
	 * @throws Exception
	 */
	private MockHttpServletResponse replaceOrder(MockHttpServletResponse order) throws Exception {
		String content = order.getContentAsString();

		Link orderLink = getDiscovererFor(order).findRequiredLinkWithRel(SELF, content);

		ClassPathResource resource = new ClassPathResource("order.json");
		byte[] data = Files.readAllBytes(resource.getFile().toPath());

		mvc.perform(put(orderLink.expand().getHref()).contentType(APPLICATION_JSON).content(data)) //
				.andExpect(status().isNoContent()) //
				.andReturn().getResponse();

		return mvc.perform(get(orderLink.expand().getHref())).andExpect(status().isOk()).andReturn().getResponse();
	}

	private String switchLocation(String location) {
		List<String> locations = Arrays.asList("To go", "In store");
		int indexOf = locations.indexOf(location);
		if (indexOf < 0) {
			throw new IllegalStateException("Unknown location " + location);
		}
		return locations.get((indexOf + 1) % locations.size());
	}

}
