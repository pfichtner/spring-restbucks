package org.springsource.restbucks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springsource.restbucks.order.OrderTestUtils.createOrder;

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
import org.springframework.jms.annotation.JmsListener;
import org.springsource.restbucks.JmsExternalEventProcessorTest.EventListenerForTest;
import org.springsource.restbucks.order.Order;
import org.springsource.restbucks.order.OrderRepository;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@Import(EventListenerForTest.class)
class JmsExternalEventProcessorTest {

	// we should consume this event because in another domain we don't want to share
	// classes, so deserialzation target should be another class
	public static class MyOrderPaidForTest {
		public long orderId;
	}

	static class EventListenerForTest {

		@Autowired
		private ObjectMapper objectMapper;

		private List<MyOrderPaidForTest> events = new CopyOnWriteArrayList<>();

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

	@BeforeEach
	void setup() {
		// how to add/remove the Listener for this test only?
		eventListenerForTest.events.clear();
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
		order = orders.save(createOrder());
	}

	private void whenOrderIsPaid() {
		orders.save(order.markPaid());
	}

	void thenEventWasPublishedViaJms(Long oid) {
		await().untilAsserted(
				() -> assertThat(eventListenerForTest.events).singleElement().matches(o -> oid.equals(o.orderId)));
	}

}
