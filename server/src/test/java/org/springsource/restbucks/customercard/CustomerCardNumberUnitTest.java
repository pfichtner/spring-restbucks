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

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link CustomerCardNumber}.
 * 
 * @author Peter Fichtner
 */
class CustomerCardNumberUnitTest {

	@Test
	void rejectsInvalidLength() {
		
		assertThatExceptionOfType(IllegalArgumentException.class) //
			.isThrownBy(() -> new CustomerCardNumber("AAFF12345"));
		assertThatExceptionOfType(IllegalArgumentException.class) //
		.isThrownBy(() -> new CustomerCardNumber("AAFF1234567"));
	}

	@Test
	void rejectsLettersInNumericPart() {
		
		assertThatExceptionOfType(IllegalArgumentException.class) //
			.isThrownBy(() -> new CustomerCardNumber("AAFF12345A"));
	}

	@Test
	void createsValidCreditCardNumber() {
		new CustomerCardNumber("AAFF123456");
	}
}
