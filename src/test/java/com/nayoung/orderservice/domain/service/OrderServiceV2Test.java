package com.nayoung.orderservice.domain.service;

import com.nayoung.orderservice.domain.Order;
import com.nayoung.orderservice.domain.OrderItemStatus;
import com.nayoung.orderservice.domain.repository.OrderRepository;
import com.nayoung.orderservice.kafka.producer.KafkaProducerService;
import com.nayoung.orderservice.kafka.streams.KStreamKTableJoinConfig;
import com.nayoung.orderservice.web.dto.OrderDto;
import com.nayoung.orderservice.web.dto.OrderItemDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
class OrderServiceV2Test {

    @Autowired
    OrderServiceV2 orderServiceV2;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    KafkaProducerService kafkaProducerService;

    private final Long CUSTOMER_ACCOUNT_ID = 2L;

    @AfterEach
    void afterEach() {
        orderRepository.deleteAll();
    }

    @Test
    void 최종_주문_생성 () throws InterruptedException {
        OrderDto requestedOrder = getRequestedOrder();
        OrderDto savedOrderDto = orderServiceV2.create(requestedOrder);

        log.error(savedOrderDto.getEventId());
        OrderDto orderProcessingResult = getOrderProcessingResult(savedOrderDto.getEventId(), OrderItemStatus.FAILED);
        kafkaProducerService.send(KStreamKTableJoinConfig.ORDER_PROCESSING_RESULT_TOPIC,
                savedOrderDto.getEventId(), orderProcessingResult);

        Thread.sleep(3000);
        Order order = orderRepository.findByEventId(savedOrderDto.getEventId()).orElse(null);
        assert order != null;

        Assertions.assertEquals(OrderItemStatus.FAILED, order.getOrderStatus());
        Assertions.assertTrue(order.getOrderItems()
                .stream()
                .allMatch(orderItem -> Objects.equals(OrderItemStatus.FAILED, orderItem.getOrderItemStatus())));
    }

    private OrderDto getRequestedOrder() {
        List<OrderItemDto> orderItemDtos = Stream
                .iterate(1, i -> i < 3, i -> i + 1)
                .map(i -> OrderItemDto.builder()
                        .itemId((long) i)
                        .quantity(3L)
                        .build())
                .toList();

        return OrderDto.builder()
                .customerAccountId(CUSTOMER_ACCOUNT_ID)
                .orderItemDtos(orderItemDtos)
                .build();
    }

    private OrderDto getOrderProcessingResult(String eventId, OrderItemStatus orderItemStatus) {
        List<OrderItemDto> orderItemDtos = Stream
                .iterate(1, i -> i < 3, i -> i + 1)
                .map(i -> OrderItemDto.builder()
                        .itemId((long) i)
                        .orderItemStatus(orderItemStatus)
                        .build())
                .toList();

        return OrderDto.builder()
                .customerAccountId(CUSTOMER_ACCOUNT_ID)
                .eventId(eventId)
                .orderStatus(orderItemStatus)
                .orderItemDtos(orderItemDtos)
                .build();
    }
}