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
package org.springsource.restbucks.order.web;

import static org.springframework.hateoas.MediaTypes.HAL_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.UnsupportedEncodingException;

import org.junit.jupiter.api.Test;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.mediatype.hal.HalLinkRelation;
import org.springframework.hateoas.mediatype.hal.HalLinkRelation.HalLinkRelationBuilder;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springsource.restbucks.AbstractWebIntegrationTest;
import org.springsource.restbucks.Restbucks;

/**
 * Integration test for REST resources exposed by Spring Data REST.
 * 
 * @author Oliver Gierke
 * @author Peter Fichtner
 */
class OrderResourceIntegrationTest extends AbstractWebIntegrationTest {

	private static final HalLinkRelationBuilder BUILDER = HalLinkRelation.curieBuilder(Restbucks.CURIE_NAMESPACE);

	private static final LinkRelation ORDERS_REL = BUILDER.relation("orders");

	@Test
	void exposesOrdersResourceViaRootResource() throws Exception {
		assertHasOrdersLink(getRoot());
	}

	private MockHttpServletResponse getRoot() throws Exception {
		return mvc.perform(get("/")) //
				.andDo(print()) //
				.andExpect(status().isOk()) //
				.andExpect(content().contentTypeCompatibleWith(HAL_JSON)) //
				.andReturn().getResponse();
	}

	private void assertHasOrdersLink(MockHttpServletResponse response) throws UnsupportedEncodingException {
		getDiscovererFor(response).findLinksWithRel(ORDERS_REL, response.getContentAsString());
	}

}
