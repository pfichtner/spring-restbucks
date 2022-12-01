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

import static java.util.Arrays.stream;
import static java.util.function.Function.identity;
import static java.util.function.Predicate.not;
import static java.util.stream.Stream.concat;
import static org.springframework.util.ReflectionUtils.doWithFields;
import static org.springsource.restbucks.core.Currencies.EURO;
import static org.springsource.restbucks.order.Location.TAKE_AWAY;
import static org.springsource.restbucks.order.Order.Status.PAID;
import static org.springsource.restbucks.order.Order.Status.PAYMENT_EXPECTED;
import static org.springsource.restbucks.order.Order.Status.PREPARING;
import static org.springsource.restbucks.order.Order.Status.READY;
import static org.springsource.restbucks.order.Order.Status.TAKEN;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.persistence.Id;

import org.javamoney.moneta.Money;
import org.springframework.util.ReflectionUtils;
import org.springsource.restbucks.order.Order.Status;

import lombok.experimental.UtilityClass;

/**
 * Utility methods for testing.
 *
 * @author Oliver Gierke
 * @author Peter Fichtner
 */
@UtilityClass
public class OrderTestUtils {

	private static final Map<Status, Consumer<Order>> actions = Map.of( //
			PAYMENT_EXPECTED, o -> identity(), //
			PAID, Order::markPaid, //
			PREPARING, Order::markInPreparation, //
			READY, Order::markPrepared, //
			TAKEN, Order::markTaken //
	);

	public static Order createExistingOrder() {
		return setId(new Order(), 1L);
	}

	private static Order setId(Order order, long id) {
		doWithFields(Order.class, f -> setFieldValue(f, order, id), f -> f.isAnnotationPresent(Id.class));
		return order;
	}

	private static void setFieldValue(Field field, Order target, Object value) {
		ReflectionUtils.makeAccessible(field);
		ReflectionUtils.setField(field, target, value);
	}

	public static Order createOrder() {
		return new Order(TAKE_AWAY, new LineItem("English breakfast", Money.of(2.70, EURO)));
	}

	public static Order existingOrderWithStatus(Status status) {
		return orderWithStatus(createExistingOrder(), status);
	}

	private static Order orderWithStatus(Order order, Status target) {
		// there is no "takeWhileInclusive" in JDK :-/
		concat(stream(Status.values()).takeWhile(not(target::equals)), Stream.of(target)).map(actions::get)
				.forEach(c -> c.accept(order));
		return order;
	}

}
