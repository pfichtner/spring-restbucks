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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.hateoas.IanaLinkRelations.SELF;
import static org.springframework.hateoas.MediaTypes.HAL_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.hateoas.Link;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.ResultActions;
import org.springsource.restbucks.AbstractWebIntegrationTest;

import com.jayway.jsonpath.JsonPath;

/**
 * Integration tests modeling the hypermedia-driven interaction flow against the
 * server implementation. Uses the Spring MVC integration test facilities
 * introduced in 3.2. Implements the order process modeled in my presentation on
 * Hypermedia design with Spring.
 *
 * @author Peter Fichtner
 */
class OrderProcessIntegrationTest extends AbstractWebIntegrationTest {

	/**
	 * Creates a new {@link Order} and replaces that one.
	 */
	@Test
	void canReplaceOrder() throws Exception {
		var root = accessRootResource();
		var order = createNewOrder(root);
		replaceOrder(order) //
				.andExpect(jsonPath("$.lineItems.length()", is(0))) //
				.andExpect(jsonPath("$.location", is("In store")));
	}

	/**
	 * Creates a new {@link Order} and replaces that one.
	 */
	@Test
	void canUpdateOrder() throws Exception {
		var root = accessRootResource();
		var order = createNewOrder(root);
		updateOrder(order) //
				.andExpect(jsonPath("$.lineItems.length()", is(0))) //
				.andExpect(jsonPath("$.location", is("In store")));
	}

	/**
	 * Updates an existing {@link Order}
	 *
	 * @param order
	 * @return
	 * @throws Exception
	 */
	private ResultActions updateOrder(MockHttpServletResponse order) throws Exception {
		String content = order.getContentAsString();

		String location = JsonPath.parse(content).read(JsonPath.compile("$.location"), String.class).toString();

		Link orderLink = getDiscovererFor(order).findRequiredLinkWithRel(SELF, content);

		byte[] data;
		
		data = ("{ \"lineItems\": [] }").getBytes();
		mvc.perform(patch(orderLink.expand().getHref()).contentType(APPLICATION_JSON).accept(HAL_JSON).content(data)) //
				.andExpect(status().isOk()) //
				.andReturn().getResponse();

		data = ("{ \"location\": \"" + switchLocation(location) + "\" }").getBytes();
		var response = mvc
				.perform(patch(orderLink.expand().getHref()).contentType(APPLICATION_JSON).accept(HAL_JSON)
						.content(data)) //
				.andExpect(status().isOk()) //
				.andReturn().getResponse();
		assertThatSelfLinkHasntChanged(order, orderLink, response);
		return readOrder(orderLink);
	}

	/**
	 * Replaces an existing {@link Order}
	 *
	 * @param order
	 * @return
	 * @throws Exception
	 */
	private ResultActions replaceOrder(MockHttpServletResponse order) throws Exception {
		String content = order.getContentAsString();

		Link orderLink = getDiscovererFor(order).findRequiredLinkWithRel(SELF, content);

		byte[] data = "{ \"lineItems\": [], \"location\": \"IN_STORE\" }".getBytes();
		var response = mvc
				.perform(put(orderLink.expand().getHref()).contentType(APPLICATION_JSON).accept(HAL_JSON).content(data)) //
				.andExpect(status().isOk()) //
				.andReturn().getResponse();
		assertThatSelfLinkHasntChanged(order, orderLink, response);
		return readOrder(orderLink);
	}

	private void assertThatSelfLinkHasntChanged(MockHttpServletResponse order, Link orderLink,
			MockHttpServletResponse response) throws UnsupportedEncodingException {
		assertThat(getDiscovererFor(order).findRequiredLinkWithRel(SELF, response.getContentAsString()))
				.isEqualTo(orderLink);
	}

	private ResultActions readOrder(Link orderLink) throws Exception {
		return mvc.perform(get(orderLink.expand().getHref())).andExpect(status().isOk());
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
