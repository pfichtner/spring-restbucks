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

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;
import org.springsource.restbucks.order.Order;

/**
 * {@link ResourceProcessor} to enrich {@link Order} {@link Resource}s with links to the {@link CustomerCardController}.
 * 
 * @author Peter Fichtner
 */
@Component
@RequiredArgsConstructor
class CustomerCardOrderModelProcessor implements RepresentationModelProcessor<EntityModel<Order>> {

	private final @NonNull CustomerCardLinks customerCardLinks;

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.server.RepresentationModelProcessor#process(org.springframework.hateoas.RepresentationModel)
	 */
	@Override
	public EntityModel<Order> process(EntityModel<Order> resource) {

		var order = resource.getContent();

		if (!order.isPaid()) {
			resource.add(customerCardLinks.getScanCustomerCardLink(order));
		}

		return resource;
	}
}
