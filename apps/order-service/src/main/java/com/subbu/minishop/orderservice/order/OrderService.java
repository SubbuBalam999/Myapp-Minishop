package com.subbu.minishop.orderservice.order;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository
    ) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    public List<OrderResponse> getOrders() {
        return toResponses(orderRepository.findAllByOrderByCreatedAtDesc());
    }

    public OrderResponse getOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Order not found"
                ));
        return OrderResponse.from(
                order,
                orderItemRepository.findByOrderIdOrderByIdAsc(id)
        );
    }

    public List<OrderResponse> getOrdersForUser(Long userId) {
        return toResponses(orderRepository.findByUserIdOrderByCreatedAtDesc(userId));
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        BigDecimal totalAmount = request.items().stream()
                .map(item -> item.price().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        Order order = orderRepository.save(
                new Order(request.userId(), totalAmount, OrderStatus.CREATED)
        );

        List<OrderItem> items = request.items().stream()
                .map(item -> new OrderItem(
                        order.getId(),
                        item.productId(),
                        item.productName(),
                        item.quantity(),
                        item.price().setScale(2, RoundingMode.HALF_UP)
                ))
                .toList();

        return OrderResponse.from(order, orderItemRepository.saveAll(items));
    }

    private List<OrderResponse> toResponses(List<Order> orders) {
        if (orders.isEmpty()) {
            return List.of();
        }

        List<Long> orderIds = orders.stream().map(Order::getId).toList();
        Map<Long, List<OrderItem>> itemsByOrder = orderItemRepository
                .findByOrderIdInOrderByOrderIdAscIdAsc(orderIds)
                .stream()
                .collect(Collectors.groupingBy(
                        OrderItem::getOrderId,
                        Collectors.toCollection(ArrayList::new)
                ));

        return orders.stream()
                .map(order -> OrderResponse.from(
                        order,
                        itemsByOrder.getOrDefault(order.getId(), List.of())
                ))
                .toList();
    }
}
