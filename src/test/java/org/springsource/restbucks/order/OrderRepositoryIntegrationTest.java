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
import static org.springsource.restbucks.order.Order.Status.PAID;
import static org.springsource.restbucks.order.Order.Status.PAYMENT_EXPECTED;
import static org.springsource.restbucks.order.OrderTestUtils.createExampleOrder;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springsource.restbucks.TransactionalIntegrationTest;

/**
 * Integration tests for Spring Data based {@link OrderRepository}.
 * 
 * @author Oliver Gierke
 */
@TransactionalIntegrationTest
class OrderRepositoryIntegrationTest {

	@Autowired OrderRepository repository;

	@Test
	void createsNewOrder() {
		Long countBeforeInsert = repository.count();
		var order = repository.save(createExampleOrder());
		var result = repository.findAll();

		assertThat(result).hasSize(countBeforeInsert.intValue() + 1);
		assertThat(result).contains(order);
	}

	@Test
	void findsOrderByStatus() {
		int paidCountBeforeInsert = repository.findByStatus(PAID).size();
		int paymentCountBeforeInsert = repository.findByStatus(PAYMENT_EXPECTED).size();

		var order = repository.save(createExampleOrder());
		assertThat(repository.findByStatus(PAYMENT_EXPECTED)).hasSize(paymentCountBeforeInsert + 1);
		assertThat(repository.findByStatus(PAID)).hasSize(paidCountBeforeInsert);

		order.markPaid();
		order = repository.save(order);

		assertThat(repository.findByStatus(PAYMENT_EXPECTED)).hasSize(paymentCountBeforeInsert);
		assertThat(repository.findByStatus(PAID)).hasSize(paidCountBeforeInsert + 1);
	}
}
