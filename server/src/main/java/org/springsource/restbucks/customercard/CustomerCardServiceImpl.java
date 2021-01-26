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

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springsource.restbucks.order.Order;
import org.springsource.restbucks.order.OrderRepository;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Implementation of {@link CustomerCardService} delegating persistence operations to {@link CustomerCardScanRepository}. 
 * 
 * @author Peter Fichtner
 */
@Service
@Transactional
@RequiredArgsConstructor
class CustomerCardServiceImpl implements CustomerCardService {

	private final @NonNull CustomerCardScanRepository customerCardScanRepository;
	private final @NonNull OrderRepository orderRepository;


	@Override
	public CustomerCardScan scan(Order order, CustomerCardNumber customerCardNumber) {
		return customerCardScanRepository.save(new CustomerCardScan(new CustomerCard(customerCardNumber), order));
	}

	@Override
	public Optional<CustomerCardScan> getScanFor(Order order) {
		return customerCardScanRepository.findByOrder(order);
	}
}
