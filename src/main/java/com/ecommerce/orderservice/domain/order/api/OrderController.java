package com.ecommerce.orderservice.domain.order.api;

import com.ecommerce.orderservice.domain.order.service.OrderCreationService;
import com.ecommerce.orderservice.domain.order.service.OrderService;
import com.ecommerce.orderservice.domain.order.dto.OrderDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final OrderCreationService orderCreationService;
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Validated OrderDto orderDto) {
        OrderDto response = orderCreationService.create(orderDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping(value = {"/{userId}/{cursorOrderId}", "/{userId}"})
    public ResponseEntity<?> getOrders(@PathVariable Long userId, @PathVariable(required = false) Long cursorOrderId) {
        List<OrderDto> response = orderService.findOrderByUserIdAndOrderId(userId, cursorOrderId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
