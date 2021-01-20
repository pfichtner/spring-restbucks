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

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springsource.restbucks.order.OrderRepository;

/**
 * Unit tests for {@link CustomerCardImpl}.
 * 
 * @author Peter Fichtner
 */
@ExtendWith(MockitoExtension.class)
class CustomerCardServiceImplUnitTest {

	static final CustomerCardNumber NUMBER = new CustomerCardNumber("AAFF123456");

	CustomerCardService customerCardService;

	@Mock
	CustomerCardScanRepository customerCardScanRepository;
	@Mock
	OrderRepository orderRepository;

	@BeforeEach
	void setUp() {
		this.customerCardService = new CustomerCardImpl(customerCardScanRepository, orderRepository);
	}

	@Test
	void rejectsNullPaymentRepository() {

		assertThatExceptionOfType(IllegalArgumentException.class) //
				.isThrownBy(() -> new CustomerCardImpl(customerCardScanRepository, null));
	}

	@Test
	void rejectsNullCreditCardRepository() {

		assertThatExceptionOfType(IllegalArgumentException.class) //
				.isThrownBy(() -> new CustomerCardImpl(null, orderRepository));
	}

}
