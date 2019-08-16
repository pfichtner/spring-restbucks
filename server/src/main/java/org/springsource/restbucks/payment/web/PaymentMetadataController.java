package org.springsource.restbucks.payment.web;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PaymentMetadataController {

	@GetMapping(value = "/docs/payment", produces = {
			MediaTypes.HAL_FORMS_JSON_VALUE,
			"!text/html"
	})
	ResponseEntity<RepresentationModel<?>> getPaymentMetadata() {

		Link selfLink = linkTo(methodOn(PaymentMetadataController.class).getPaymentMetadata()).withRel(IanaLinkRelations.SELF) //
				.andAffordance(afford(methodOn(PaymentController.class).submitPayment(null, null)));

		return ResponseEntity.ok(new RepresentationModel<>(selfLink));
	}
}
