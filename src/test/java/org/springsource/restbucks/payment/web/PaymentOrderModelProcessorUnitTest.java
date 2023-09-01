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
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springsource.restbucks.order.Order.Status.PAYMENT_EXPECTED;
import static org.springsource.restbucks.order.Order.Status.READY;
import static org.springsource.restbucks.order.OrderTestUtils.createExistingOrder;
import static org.springsource.restbucks.order.OrderTestUtils.existingOrderWithStatus;
import static org.springsource.restbucks.payment.web.PaymentLinks.PAYMENT_REL;
import static org.springsource.restbucks.payment.web.PaymentLinks.RECEIPT_REL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springsource.restbucks.order.Order;
import org.springsource.restbucks.order.Order.Status;

/**
 * Unit test for {@link PaymentOrderModelProcessor}.
 *
 * @author Oliver Gierke
 */
@ExtendWith(MockitoExtension.class)
class PaymentOrderModelProcessorUnitTest {

	@Mock(lenient = true)
	PaymentLinks paymentLinks;

	PaymentOrderModelProcessor sut;
	Link paymentLink = Link.of("payment", PAYMENT_REL);
	Link receiptLink = Link.of("receipt", RECEIPT_REL);

	@BeforeEach
	void setUp() {
		sut = new PaymentOrderModelProcessor(paymentLinks);
		when(paymentLinks.getPaymentLink(any(Order.class))).thenReturn(paymentLink);
		when(paymentLinks.getReceiptLink(any(Order.class))).thenReturn(receiptLink);
	}

	@ParameterizedTest
	@EnumSource(value = Status.class)
	void doesNotAddLinksForNeitherFreshNorUnfinishedOrders(Status status) {
		if (status != READY && status != PAYMENT_EXPECTED) {
			EntityModel<Order> resource = sut.process(EntityModel.of(existingOrderWithStatus(status)));
			assertThat(resource.getLink(PAYMENT_REL)).isEmpty();
			assertThat(resource.getLink(RECEIPT_REL)).isEmpty();
		}
	}

	@Test
	void addsPaymentLinkForFreshOrder() {
		Order order = createExistingOrder();
		EntityModel<Order> resource = sut.process(EntityModel.of(order));
		assertThat(resource.getLink(PAYMENT_REL)).hasValue(paymentLink);
	}

	@Test
	void addsReceiptLinkForPreparedOrder() {
		EntityModel<Order> resource = sut.process(EntityModel.of(existingOrderWithStatus(READY)));
		assertThat(resource.getLink(RECEIPT_REL)).hasValue(receiptLink);
	}

}
