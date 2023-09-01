/*
 * Copyright 2013-2019 the original author or authors.
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

import javax.money.MonetaryAmount;
import javax.persistence.Entity;

import org.springsource.restbucks.core.AbstractEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * A line item.
 * 
 * @author Oliver Gierke
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class LineItem extends AbstractEntity {

	private String name;
	private int quantity;
	private Milk milk;
	private Size size;
	private MonetaryAmount price;

	public LineItem(String name, MonetaryAmount price) {
		this(name, 1, Milk.SEMI, Size.LARGE, price);
	}

}
