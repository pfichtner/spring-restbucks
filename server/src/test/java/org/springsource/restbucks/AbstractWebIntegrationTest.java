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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.client.LinkDiscoverer;
import org.springframework.hateoas.client.LinkDiscoverers;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.web.context.WebApplicationContext;

import lombok.RequiredArgsConstructor;

/**
 * Base class to derive concrete web test classes from.
 *
 * @author Oliver Gierke
 */
@SpringBootTest
public abstract class AbstractWebIntegrationTest {

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
