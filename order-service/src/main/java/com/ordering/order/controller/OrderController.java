package com.ordering.order.controller;

import com.ordering.avro.Order;
import com.ordering.order.dto.BatchOrderRequest;
import com.ordering.order.dto.CreateOrderRequest;
import com.ordering.order.dto.OrderResponse;
import com.ordering.order.service.OrderProducerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderProducerService producerService;

    public OrderController(OrderProducerService producerService) {
        this.producerService = producerService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse response = producerService.sendOrder(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/random")
    public ResponseEntity<OrderResponse> createRandomOrder(@RequestParam(defaultValue = "normal") String mode) {
        Order randomOrder = producerService.generateRandomOrder(mode);
        CreateOrderRequest request = new CreateOrderRequest(
                randomOrder.getOrderId(),
                randomOrder.getProduct(),
                randomOrder.getPrice()
        );
        OrderResponse response = producerService.sendOrder(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/batch")
    public ResponseEntity<Map<String, Object>> createBatchOrders(@RequestBody BatchOrderRequest request) {
        producerService.startBatchGeneration(request.getCount(), request.getDelayMs(), request.getMode());
        return ResponseEntity.ok(Map.of(
                "message", "Batch order production triggered in background",
                "count", request.getCount(),
                "delayMs", request.getDelayMs(),
                "mode", request.getMode()
        ));
    }
}
