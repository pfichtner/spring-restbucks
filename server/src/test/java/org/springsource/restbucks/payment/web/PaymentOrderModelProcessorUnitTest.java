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
package org.springsource.restbucks.payment.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springsource.restbucks.order.Order.Status.PAYMENT_EXPECTED;
import static org.springsource.restbucks.order.Order.Status.READY;
import static org.springsource.restbucks.order.OrderTestUtils.createExistingOrder;
import static org.springsource.restbucks.order.OrderTestUtils.existingOrderWithStatus;

import java.util.EnumSet;

import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springsource.restbucks.order.Order;
import org.springsource.restbucks.order.Order.Status;

/**
 * Unit test for {@link PaymentOrderModelProcessorUnitTest}.
 *
 * @author Oliver Gierke
 */
@ExtendWith(MockitoExtension.class)
class PaymentOrderModelProcessorUnitTest {

	@Mock(lenient = true) //
	PaymentLinks paymentLinks;

	PaymentOrderModelProcessor processor;
	Link paymentLink = Link.of("payment", PaymentLinks.PAYMENT_REL);
	Link receiptLink = Link.of("receipt", PaymentLinks.RECEIPT_REL);

	@BeforeEach
	void setUp() {
		HttpServletRequest request = new MockHttpServletRequest();
		ServletRequestAttributes requestAttributes = new ServletRequestAttributes(request);
		RequestContextHolder.setRequestAttributes(requestAttributes);

		processor = new PaymentOrderModelProcessor(paymentLinks);
		when(paymentLinks.getPaymentLink(any(Order.class))).thenReturn(paymentLink);
		when(paymentLinks.getReceiptLink(any(Order.class))).thenReturn(receiptLink);
	}

	@ParameterizedTest
	@EnumSource(value = Status.class)
	void doesNotAddLinksForNeitherFreshNorUnfinishedOrders(Status status) {
		if (EnumSet.of(READY, PAYMENT_EXPECTED).contains(status)) {
			return;
		}
		EntityModel<Order> resource = processor.process(EntityModel.of(existingOrderWithStatus(status)));
		assertThat(resource.hasLinks()).isFalse();
	}

	@Test
	void addsPaymentLinkForFreshOrder() {
		EntityModel<Order> resource = processor.process(EntityModel.of(createExistingOrder()));
		assertThat(resource.getLink(PaymentLinks.PAYMENT_REL)).hasValue(paymentLink);
	}

	@Test
	void addsReceiptLinkForPaidOrder() {
		Order order = createExistingOrder();
		order.markPaid();
		order.markInPreparation();
		order.markPrepared();

		EntityModel<Order> resource = processor.process(EntityModel.of(order));
		assertThat(resource.getLink(PaymentLinks.RECEIPT_REL)).hasValue(receiptLink);
	}

}
