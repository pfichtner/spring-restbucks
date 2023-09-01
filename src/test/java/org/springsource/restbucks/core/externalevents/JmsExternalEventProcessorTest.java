package org.springsource.restbucks.core.externalevents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springsource.restbucks.order.OrderTestUtils.createExistingOrder;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.jms.JMSException;
import javax.jms.TextMessage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.test.annotation.DirtiesContext;
import org.springsource.restbucks.core.externalevents.JmsExternalEventProcessorTest.EventListenerForTest;
import org.springsource.restbucks.order.Order;
import org.springsource.restbucks.order.OrderRepository;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@Import(EventListenerForTest.class)
@DirtiesContext // #1
class JmsExternalEventProcessorTest {

	// we should consume this event because in another domain we don't want to share
	// classes, so deserialzation target should be another class
	public static class MyOrderPaidForTest {
		public long orderId;
	}

	static class EventListenerForTest {

		@Autowired
		private ObjectMapper objectMapper;

		private final List<MyOrderPaidForTest> events = new CopyOnWriteArrayList<>();

		@JmsListener(destination = "restbucks", selector = "_type = 'org.springsource.restbucks.payment.OrderPaid'")
		public void receiveObject(TextMessage message) throws JMSException {
			events.add(deserialize(message.getText(), MyOrderPaidForTest.class));
		}

		private <T> T deserialize(String json, Class<T> clasz) {
			try {
				return objectMapper.readValue(json, clasz);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

	}

	@Autowired
	OrderRepository orders;
	Order order;

	@Autowired
	EventListenerForTest eventListenerForTest;

	@Autowired // #1
	ApplicationEventMulticaster applicationEventMulticaster; // #1

	@BeforeEach
	void setup() {
		// how to add/remove the Listener for this test only?
		eventListenerForTest.events.clear();
		removeEngine(applicationEventMulticaster); // #1
	}

	private static void removeEngine(ApplicationEventMulticaster applicationEventMulticaster) {
		// remove the engine as listener since the engine complains because the thread
		// get's interrupted while engine is preparing the order (all statements marked
		// #1 where introduced for that, too)
		applicationEventMulticaster.removeApplicationListeners(
				l -> ("public" + " " + "void" + " org.springsource.restbucks.engine.Engine" + "."
						+ "handleOrderPaidEvent(" + org.springsource.restbucks.order.OrderPaid.class.getName() + ")")
								.equals(l.toString()));
	}

	@Test
	void externalEventsAreExposedViaJms() {
		givenAnOrder();
		whenOrderIsPaid();
		thenEventWasPublishedViaJms(anOrderPaidEventForOrder(order));
	}

	private Long anOrderPaidEventForOrder(Order order) {
		return order.getId();
	}

	void givenAnOrder() {
		order = orders.save(createExistingOrder());
	}

	private void whenOrderIsPaid() {
		order.markPaid();
		orders.save(order);
	}

	void thenEventWasPublishedViaJms(Long oid) {
		await().untilAsserted(
				() -> assertThat(eventListenerForTest.events).singleElement().matches(o -> oid.equals(o.orderId)));
	}

}
