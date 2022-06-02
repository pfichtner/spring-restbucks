package org.springsource.restbucks.order.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springsource.restbucks.order.Order.Status.PAYMENT_EXPECTED;
import static org.springsource.restbucks.order.OrderTestUtils.existingOrderWithStatus;
import static org.springsource.restbucks.order.web.CoreOrderResourceProcessor.CANCEL_REL;
import static org.springsource.restbucks.order.web.CoreOrderResourceProcessor.UPDATE_REL;

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
	Link updateLink = Link.of("update", UPDATE_REL);
	Link cancelLink = Link.of("cancel", CANCEL_REL);

	@BeforeEach
	void setUp() {
		sut = new CoreOrderResourceProcessor(entityLinks);

		when(linkBuilder.withRel(UPDATE_REL)).thenReturn(updateLink);
		when(linkBuilder.withRel(CANCEL_REL)).thenReturn(cancelLink);
		when(typedEntityLinks.linkForItemResource(any(Order.class))).thenReturn(linkBuilder);
		when(entityLinks.forType(any())).thenReturn(typedEntityLinks);
	}

	@ParameterizedTest
	@EnumSource(value = Status.class)
	void verifyUpdateAndCancelLink(Status status) {
		EntityModel<Order> resource = sut.process(EntityModel.of(existingOrderWithStatus(status)));
		if (status == PAYMENT_EXPECTED) {
			assertThat(resource.getLink(UPDATE_REL)).hasValue(updateLink);
			assertThat(resource.getLink(CANCEL_REL)).hasValue(cancelLink);
		} else {
			assertThat(resource.getLink(UPDATE_REL)).isEmpty();
			assertThat(resource.getLink(CANCEL_REL)).isEmpty();
		}
	}

}
