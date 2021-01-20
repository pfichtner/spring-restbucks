/*
 * Copyright 2019 the original author or authors.
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
package org.springsource.restbucks.customercard.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.transaction.annotation.Transactional;
import org.springsource.restbucks.customercard.CustomerCardNumber;
import org.springsource.restbucks.customercard.web.CustomerCardController.CustomerCardScanForm;
import org.springsource.restbucks.order.Order.Status;
import org.springsource.restbucks.order.OrderRepository;

/**
 * Integration test for {@link CustomerCardController}.
 *
 * @author Peter Fichtner
 */
@Transactional
@SpringBootTest
class CustomerCardControllerIntegrationTest {

	@Autowired CustomerCardController controller;
	@Autowired OrderRepository orders;

	@Test
	void processesCustomerCardScan() throws Exception {

		// Given
		var order = orders.findByStatus(Status.PAYMENT_EXPECTED).get(0);

		// When
		var model = new CustomerCardScanForm(new CustomerCardNumber("AAFF123456"));
		var entity = controller.submitScan(order, model);

		// Then
		assertThat(entity.getHeaders().getLocation()).isNotNull();
		assertThat(entity.getBody()).isInstanceOfSatisfying(RepresentationModel.class, it -> {
			assertThat(it.hasLink("order")).isTrue();
		});
	}
}
