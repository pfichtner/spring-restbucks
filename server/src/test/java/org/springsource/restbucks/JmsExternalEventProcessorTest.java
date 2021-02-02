package org.springsource.restbucks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springsource.restbucks.order.OrderTestUtils.createOrder;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springsource.restbucks.JmsExternalEventProcessorTest.EventListenerForTest;
import org.springsource.restbucks.order.Order;
import org.springsource.restbucks.order.OrderRepository;
import org.springsource.restbucks.payment.OrderPaid;

@SpringBootTest
@Import(EventListenerForTest.class)
class JmsExternalEventProcessorTest {

	@Component
	static class EventListenerForTest {

		List<Object> events = new CopyOnWriteArrayList<>();

		@JmsListener(destination = "restbucks")
		// TODO receive OrderPaid, not Object
		public void receive(Object orderPaid) {
			events.add(orderPaid);
		}

	}

	@Autowired
	OrderRepository orders;
	Order order;

	@Autowired
	EventListenerForTest eventListenerForTest;

	@Test
	void externalEventsAreExposedViaJms() throws Exception {
		givenAnOrder();
		whenOrderIsPaid();
		thenEventWasPublishedViaJms(new OrderPaid(order.getId()));
	}

	void givenAnOrder() {
		order = orders.save(createOrder());
	}

	private void whenOrderIsPaid() {
		orders.save(order.markPaid());
	}

	void thenEventWasPublishedViaJms(OrderPaid orderPaid) throws InterruptedException {
		// TODO useAwaitability
		TimeUnit.SECONDS.sleep(5);
		assertThat(eventListenerForTest.events).hasSize(1);
		// TODO verify content
//		assertThat(eventListenerForTest.events).containsExactly(orderPaid);
	}

}
