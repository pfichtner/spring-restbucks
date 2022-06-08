/*
 * Copyright 2013-2019 the original author or authors.
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
package org.springsource.restbucks;

import static java.util.Locale.US;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.nio.file.Files;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.client.LinkDiscoverer;
import org.springframework.hateoas.client.LinkDiscoverers;
import org.springframework.hateoas.mediatype.hal.HalLinkRelation;
import org.springframework.hateoas.mediatype.hal.HalLinkRelation.HalLinkRelationBuilder;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.web.context.WebApplicationContext;
import org.springsource.restbucks.order.Order;

import lombok.RequiredArgsConstructor;

/**
 * Base class to derive concrete web test classes from.
 *
 * @author Oliver Gierke
 */
@SpringBootTest
public abstract class AbstractWebIntegrationTest {
	
	protected static final HalLinkRelationBuilder BUILDER = HalLinkRelation.curieBuilder(Restbucks.CURIE_NAMESPACE);
	protected static final LinkRelation ORDERS_REL = BUILDER.relation("orders");

	@Autowired
	protected WebApplicationContext context;
	@Autowired
	protected LinkDiscoverers links;

	protected MockMvc mvc;

	@BeforeEach
	void setUp() {
		mvc = webAppContextSetup(context).defaultRequest(get("/").locale(US)).build();
	}

	/**
	 * Creates a {@link ResultMatcher} that checks for the presence of a link with
	 * the given rel.
	 *
	 * @param rel
	 * @return
	 */
	protected ResultMatcher linkWithRelIsPresent(LinkRelation rel) {
		return new LinkWithRelMatcher(rel, true);
	}

	/**
	 * Creates a {@link ResultMatcher} that checks for the non-presence of a link
	 * with the given rel.
	 *
	 * @param rel
	 * @return
	 */
	protected ResultMatcher linkWithRelIsNotPresent(LinkRelation rel) {
		return new LinkWithRelMatcher(rel, false);
	}

	protected LinkDiscoverer getDiscovererFor(MockHttpServletResponse response) {
		return links.getRequiredLinkDiscovererFor(response.getContentType());
	}


	/**
	 * Access the root resource by referencing the well-known URI.
	 *
	 * @return the response for the orders resource
	 * @throws Exception
	 */
	protected MockHttpServletResponse accessRootResource() throws Exception {
		return mvc.perform(get("/")).andExpect(status().isOk()).andReturn().getResponse();
	}

	/**
	 * Creates a new {@link Order} by looking up the orders link from the source and
	 * posting the content of {@code orders.json} to it. Verifies we receive a
	 * {@code 201 Created} and a {@code Location} header. Follows the location
	 * header to retrieve and verify the {@link Order} just created.
	 *
	 * @param root
	 * @param jsonFilename TODO
	 * @return
	 * @throws Exception
	 */
	protected MockHttpServletResponse createNewOrder(MockHttpServletResponse root) throws Exception {
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

	@RequiredArgsConstructor
	private class LinkWithRelMatcher implements ResultMatcher {

		private final LinkRelation rel;
		private final boolean present;

		@Override
		public void match(MvcResult result) throws Exception {
			MockHttpServletResponse response = result.getResponse();
			String content = response.getContentAsString();
			LinkDiscoverer discoverer = links.getRequiredLinkDiscovererFor(response.getContentType());
			assertThat(discoverer.findLinkWithRel(rel, content)).matches(l -> l.isPresent() == present);
		}
	}

}
