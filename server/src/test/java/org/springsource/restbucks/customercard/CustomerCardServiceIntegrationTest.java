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
package org.springsource.restbucks.customercard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springsource.restbucks.order.OrderTestUtils.createOrder;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springsource.restbucks.AbstractIntegrationTest;
import org.springsource.restbucks.order.Order;
import org.springsource.restbucks.order.OrderRepository;

/**
 * Integration tests for {@link CustomerCardServiceImpl}.
 * 
 * @author Peter Fichtner
 */
class CustomerCardServiceIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	CustomerCardService customerCardService;
	@Autowired
	OrderRepository orders;

	@Test
	void scanCustomerCard() {

		CustomerCardNumber number = new CustomerCardNumber("AAFF123456");
		Order order = orders.save(createOrder());
		var scan = customerCardService.scan(order, number);
		assertThat(customerCardService.getScanFor(order)).hasValue(scan);
	}

}
