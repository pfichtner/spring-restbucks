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
package org.springsource.restbucks.payment;

import static java.time.Month.DECEMBER;
import static java.time.Year.now;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springsource.restbucks.TransactionalIntegrationTest;

/**
 * Integration tests for {@link CreditCardRepository}.
 *
 * @author Oliver Gierke
 */
@TransactionalIntegrationTest
class CreditCardRepositoryIntegrationTest {

	static final CreditCard CREDIT_CARD = new CreditCard(new CreditCardNumber("4321432143214321"), "Oliver Gierke",
			DECEMBER, now().plusYears(1));

	@Autowired
	CreditCardRepository repository;

	@Test
	void returnsEmptyOptionalIfCreditCardIsNotFound() {
		assertThat(findByNumber(CREDIT_CARD.getNumber())).isEmpty();
	}

	@Test
	void createsCreditCard() {
		repository.save(creditCard());
		assertThat(findByNumber(CREDIT_CARD.getNumber())).hasValueSatisfying(c -> {
			assertThat(c.getNumber()).isEqualTo(CREDIT_CARD.getNumber());
			assertThat(c.getCardHolderName()).isEqualTo(CREDIT_CARD.getCardHolderName());
			assertThat(c.getExpirationDate()).isEqualTo(CREDIT_CARD.getExpirationDate());
		});
	}

	Optional<CreditCard> findByNumber(CreditCardNumber creditCardNumber) {
		return repository.findByNumber(creditCardNumber);
	}

	public static CreditCard creditCard() {
		return CREDIT_CARD;
	}

}
