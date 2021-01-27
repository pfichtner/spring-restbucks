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
package org.springsource.restbucks.customercard.web;

import org.springframework.data.repository.support.DomainClassConverter;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springsource.restbucks.customercard.CustomerCard;
import org.springsource.restbucks.customercard.CustomerCardNumber;
import org.springsource.restbucks.customercard.CustomerCardScan;
import org.springsource.restbucks.customercard.CustomerCardService;
import org.springsource.restbucks.order.Order;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;

/**
 * Spring MVC controller to handle customer card scans for an {@link CustomerCardScan}.
 *
 * @author Peter Fichtner
 */
@Controller
@RequestMapping("/orders/{id}")
//@ExposesResourceFor(CustomerCardScan.class)
@RequiredArgsConstructor
class CustomerCardController {

	private final @NonNull CustomerCardService customerCardService;
	private final @NonNull CustomerCardLinks customerCardLinks;

	/**
	 * Accepts a customer card scan for an {@link Order}
	 *
	 * @param order the {@link Order} to process the customer card scan for. Retrieved from the path variable and converted into an
	 *          {@link Order} instance by Spring Data's {@link DomainClassConverter}. Will be {@literal null} in case no
	 *          {@link Order} with the given id could be found.
	 * @param number the {@link CustomerCardNumber} unmarshaled from the request payload.
	 * @return
	 */
	@PutMapping(path = CustomerCardLinks.SCAN_CUSTOMER_CARD)
	ResponseEntity<?> submitScan(@PathVariable("id") Order order, @RequestBody CustomerCardScanForm form) {
		throw new UnsupportedOperationException("not implemented");
	}


	/**
	 * EntityModel implementation for scan results.
	 *
	 * @author Peter Fichtner
	 */
	@Data
	@EqualsAndHashCode(callSuper = true)
	static class ScanModel extends RepresentationModel<ScanModel> {
		private final CustomerCard customerCard;
	}

	@Value
	@RequiredArgsConstructor(onConstructor = @__(@JsonCreator))
	static class CustomerCardScanForm {
		CustomerCardNumber number;
	}
}
