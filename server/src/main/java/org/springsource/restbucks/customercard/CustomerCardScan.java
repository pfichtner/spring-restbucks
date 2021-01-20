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

import java.time.LocalDateTime;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.springframework.util.Assert;
import org.springsource.restbucks.core.AbstractEntity;
import org.springsource.restbucks.order.Order;

import lombok.Getter;
import lombok.ToString;

/**
 * Scan of a customer card
 * 
 * @author Peter Fichtner
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Getter
@ToString(callSuper = true)
public class CustomerCardScan extends AbstractEntity {

	private final CustomerCard customerCard;
	@JoinColumn(name = "rborder") //
	@OneToOne(cascade = CascadeType.MERGE) //
	private final Order order;
	private final LocalDateTime scanDate;

	protected CustomerCardScan() {

		this.customerCard = null;
		this.order = null;
		this.scanDate = null;
	}

	/**
	 * Creates a new {@link CustomerCardScan} referring to the given {@link Order}.
	 * 
	 * @param order must not be {@literal null}.
	 */
	public CustomerCardScan(CustomerCard customerCardNumber, Order order) {

		Assert.notNull(customerCardNumber, "CustomerCardNumber must not be null!");
		Assert.notNull(order, "Order must not be null!");

		this.customerCard = customerCardNumber;
		this.order = order;
		this.scanDate = LocalDateTime.now();
	}

}
