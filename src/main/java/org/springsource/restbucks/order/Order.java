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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.money.MonetaryAmount;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

import org.javamoney.moneta.Money;
import org.springframework.util.Assert;
import org.springsource.restbucks.core.AbstractAggregateRoot;
import org.springsource.restbucks.payment.OrderPaid;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.ToString;

/**
 * An order.
 *
 * @author Oliver Gierke
 */
@Entity
@Getter
@ToString(exclude = "lineItems")
@Table(name = "RBOrder")
public class Order extends AbstractAggregateRoot {

	private Location location;
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private final LocalDateTime orderedDate;
	private Status status;

	@OrderColumn
	@Column(unique = true)
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	private List<LineItem> lineItems = new ArrayList<>();

	/**
	 * Creates a new {@link Order} for the given {@link LineItem}s and
	 * {@link Location}.
	 * 
	 * @param lineItems must not be {@literal null}.
	 * @param location
	 */
	public Order(Collection<LineItem> lineItems, Location location) {
		setLocation(location);
		this.status = Status.PAYMENT_EXPECTED;
		setLineItems(lineItems);
		this.orderedDate = LocalDateTime.now();
	}

	public void setLineItems(Collection<LineItem> lineItems) {
		this.lineItems.clear();
		this.lineItems.addAll(lineItems);
	}

	public void setLocation(Location location) {
		Assert.notNull(location, "Location must not be null!");
		this.location = location;
	}

	/**
	 * Creates a new {@link Order} containing the given {@link LineItem}s.
	 * 
	 * @param location must not be {@literal null}.
	 * @param items    must not be {@literal null}.
	 */
	public Order(Location location, LineItem... items) {
		this(List.of(items), location);
	}

	Order() {
		this(Location.TAKE_AWAY);
	}

	/**
	 * Returns the price of the {@link Order} calculated from the contained items.
	 * 
	 * @return will never be {@literal null}.
	 */
	public MonetaryAmount getPrice() {
		return lineItems.stream() //
				.map(LineItem::getPrice) //
				.reduce(MonetaryAmount::add) //
				.orElse(Money.of(0.0, "EUR"));
	}

	/**
	 * Marks the {@link Order} as payed.
	 */
	public void markPaid() {
		if (isPaid()) {
			throw new IllegalStateException("Already paid order cannot be paid again!");
		}
		this.status = Status.PAID;
		registerEvent(new OrderPaid(getId()));
	}

	/**
	 * Marks the {@link Order} as in preparation.
	 */
	public void markInPreparation() {
		if (this.status != Status.PAID) {
			throw new IllegalStateException(String
					.format("Order must be in state payed to start preparation! Current status: %s", this.status));
		}
		this.status = Status.PREPARING;
	}

	/**
	 * Marks the {@link Order} as prepared.
	 */
	public void markPrepared() {
		if (this.status != Status.PREPARING) {
			throw new IllegalStateException(String.format(
					"Cannot mark Order prepared that is currently not preparing! Current status: %s.", this.status));
		}
		this.status = Status.READY;
	}

	public void markTaken() {
		if (this.status != Status.READY) {
			throw new IllegalStateException(String
					.format("Cannot mark Order taken that is currently not paid! Current status: %s.", this.status));
		}
		this.status = Status.TAKEN;
	}

	/**
	 * Returns whether the {@link Order} has been paid already.
	 * 
	 * @return
	 */
	public boolean isPaid() {
		return !this.status.equals(Status.PAYMENT_EXPECTED);
	}

	/**
	 * Returns if the {@link Order} is ready to be taken.
	 * 
	 * @return
	 */
	public boolean isReady() {
		return this.status.equals(Status.READY);
	}

	public boolean isTaken() {
		return this.status.equals(Status.TAKEN);
	}

	/**
	 * Enumeration for all the statuses an {@link Order} can be in.
	 * 
	 * @author Oliver Gierke
	 */
	public static enum Status {

		/**
		 * Placed, but not payed yet. Still changeable.
		 */
		PAYMENT_EXPECTED,

		/**
		 * {@link Order} was payed. No changes allowed to it anymore.
		 */
		PAID,

		/**
		 * The {@link Order} is currently processed.
		 */
		PREPARING,

		/**
		 * The {@link Order} is ready to be picked up by the customer.
		 */
		READY,

		/**
		 * The {@link Order} was completed.
		 */
		TAKEN;
	}
}
