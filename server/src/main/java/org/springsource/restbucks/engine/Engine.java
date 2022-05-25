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
package org.springsource.restbucks.engine;

import java.util.concurrent.TimeUnit;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springsource.restbucks.order.Order;
import org.springsource.restbucks.order.OrderRepository;
import org.springsource.restbucks.payment.OrderPaid;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Simple {@link OrderPaid} listener marking the according {@link Order} as in
 * process, sleeping for 5 seconds and marking the order as processed right
 * after that.
 * 
 * @author Oliver Gierke
 * @author Stéphane Nicoll
 */
@Slf4j
@Service
@RequiredArgsConstructor
class Engine {

	private final @NonNull OrderRepository repository;

	@Async
	@TransactionalEventListener
	public void handleOrderPaidEvent(OrderPaid event) throws InterruptedException {
		var order = loadOrder(event.getOrderId());

		LOG.info("Starting to process order {}.", order);
		order.markInPreparation();
		order = repository.save(order);

		TimeUnit.SECONDS.sleep(5);

		order.markPrepared();
		order = repository.save(order);

		LOG.info("Finished processing order {}.", order);
	}


	private Order loadOrder(long id) {
		return repository.findById(id) //
				.orElseThrow(() -> new IllegalStateException(String.format("No order found for id %s!", id)));
	}

}
