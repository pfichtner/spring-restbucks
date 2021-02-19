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

import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.mediatype.hal.HalLinkRelation;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.hateoas.server.TypedEntityLinks;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springsource.restbucks.Restbucks;
import org.springsource.restbucks.customercard.CustomerCardScan;
import org.springsource.restbucks.order.Order;

import lombok.Getter;

/**
 * Helper component to create links to the {@link CustomerCardScan}. 
 *
 * @author Peter Fichtner
 */
@Component
class CustomerCardLinks {

	static final String CUSTOMER_CARD = "/customercard";
	static final LinkRelation CUSTOMER_CARD_REL = HalLinkRelation.curied(Restbucks.CURIE_NAMESPACE,
			"customercard");

	private final @Getter TypedEntityLinks<Order> orderLinks;

	/**
	 * Creates a new {@link CustomerCardLinks} for the given {@link EntityLinks}.
	 * 
	 * @param entityLinks must not be {@literal null}.
	 */
	CustomerCardLinks(EntityLinks entityLinks) {

		Assert.notNull(entityLinks, "EntityLinks must not be null!");

		this.orderLinks = entityLinks.forType(Order::getId);
	}

	Link getCustomerCardLink(Order order) {
		return orderLinks.linkForItemResource(order).slash(CUSTOMER_CARD).withRel(CUSTOMER_CARD_REL);
	}

}
