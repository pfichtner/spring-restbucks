package org.springsource.restbucks.order.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static org.junit.jupiter.params.provider.EnumSource.Mode.INCLUDE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springsource.restbucks.order.OrderTestUtils.existingOrderWithStatus;
import static org.springsource.restbucks.order.web.CoreOrderResourceProcessor.CANCEL_REL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.hateoas.server.LinkBuilder;
import org.springframework.hateoas.server.TypedEntityLinks;
import org.springsource.restbucks.order.Order;
import org.springsource.restbucks.order.Order.Status;

/**
 * Unit test for {@link CoreOrderResourceProcessor}.
 *
 * @author Peter Fichtner
 */
@ExtendWith(MockitoExtension.class)
class CoreOrderResourceProcessorTest {

	@Mock(lenient = true)
	EntityLinks entityLinks;
	@Mock(lenient = true)
	TypedEntityLinks<Object> typedEntityLinks;
	@Mock(lenient = true)
	LinkBuilder linkBuilder;

	CoreOrderResourceProcessor sut;
	Link cancelLink = Link.of("cancel", CANCEL_REL);

	@BeforeEach
	void setUp() {
		sut = new CoreOrderResourceProcessor(entityLinks);

		when(linkBuilder.withRel(CANCEL_REL)).thenReturn(cancelLink);
		when(typedEntityLinks.linkForItemResource(any(Order.class))).thenReturn(linkBuilder);
		when(entityLinks.forType(any())).thenReturn(typedEntityLinks);
	}

	@ParameterizedTest
	@EnumSource(value = Status.class, mode = EXCLUDE, names = "PAYMENT_EXPECTED")
	void whenStateIsNotPaymentExcpected_cancelLinkIsAbsent(Status status) {
		EntityModel<Order> resource = orderEntityModel(status);
		assertThat(resource.getLink(CANCEL_REL)).isEmpty();
	}

	@ParameterizedTest
	@EnumSource(value = Status.class, mode = INCLUDE, names = "PAYMENT_EXPECTED")
	void whenStateIsPaymentExcpected_cancelLinkIsPresent(Status status) {
		EntityModel<Order> resource = orderEntityModel(status);
		assertThat(resource.getLink(CANCEL_REL)).hasValue(cancelLink);
	}

	private EntityModel<Order> orderEntityModel(Status status) {
		return sut.process(EntityModel.of(existingOrderWithStatus(status)));
	}

}
