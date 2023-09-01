/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springsource.restbucks;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpMethod.POST;

import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mediatype.Affordances;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;
import org.springsource.restbucks.order.Order;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * {@link RepresentationModelProcessor} to add an {@link Order} creation link to
 * the root resource.
 *
 * @author Peter Fichtner
 */
@Component
@RequiredArgsConstructor
class RootResourceModelProcessor implements RepresentationModelProcessor<RepositoryLinksResource> {

	private final @NonNull EntityLinks entityLinks;

	@Override
	public RepositoryLinksResource process(RepositoryLinksResource model) {
		Link orderCollectionResourceLink = entityLinks.linkToCollectionResource(Order.class);
		return model.mapLink(orderCollectionResourceLink.getRel(), link -> //
		link.andAffordances(Affordances.of(orderCollectionResourceLink) //
				.afford(POST).withInput(Order.class) //
				.build().stream().collect(toList())));
	}

}
