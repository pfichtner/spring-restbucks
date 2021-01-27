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
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springsource.restbucks.order.Order;
import org.springsource.restbucks.order.OrderTestUtils;

/**
 * Unit test for {@link CustomerCardOrderModelProcessorUnitTest}.
 *
 * @author Peter Fichtner
 */
@ExtendWith(MockitoExtension.class)
class CustomerCardOrderModelProcessorUnitTest {

	@Mock(lenient = true) //
	CustomerCardLinks customercardlinks;

	CustomerCardOrderModelProcessor processor;
	Link customerCardLink;

	@BeforeEach
	void setUp() {

		HttpServletRequest request = new MockHttpServletRequest();
		ServletRequestAttributes requestAttributes = new ServletRequestAttributes(request);
		RequestContextHolder.setRequestAttributes(requestAttributes);

		customerCardLink = new Link("customercard", CustomerCardLinks.CUSTOMER_CARD_REL);

		processor = new CustomerCardOrderModelProcessor(customercardlinks);
		when(customercardlinks.getCustomerCardLink(Mockito.any(Order.class))).thenReturn(customerCardLink);
	}

	@Test
	void addsCustomerCardLinkForFreshOrder() {

		Order order = OrderTestUtils.createExistingOrder();

		EntityModel<Order> resource = processor.process(new EntityModel<Order>(order));
		assertThat(resource.getLink(CustomerCardLinks.CUSTOMER_CARD_REL)).hasValue(customerCardLink);
	}

}
