package kitchenpos.application;

import kitchenpos.repository.MenuRepository;
import kitchenpos.repository.OrderRepository;
import kitchenpos.repository.OrderTableRepository;
import kitchenpos.domain.Order;
import kitchenpos.domain.OrderStatus;
import kitchenpos.domain.OrderTable;
import kitchenpos.ui.dto.OrderCreateRequest;
import kitchenpos.ui.dto.OrderResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static kitchenpos.domain.OrderStatus.COOKING;

@Service
@Transactional
public class OrderService {
    private final MenuRepository menuRepository;
    private final OrderRepository orderRepository;
    private final OrderTableRepository orderTableRepository;

    public OrderService(
            final MenuRepository menuRepository,
            final OrderRepository orderRepository,
            final OrderTableRepository orderTableRepository
    ) {
        this.menuRepository = menuRepository;
        this.orderRepository = orderRepository;
        this.orderTableRepository = orderTableRepository;
    }

    public Long create(final OrderCreateRequest request) {
        validateEqualsMenuCount(request);
        final OrderTable orderTable = orderTableRepository.getById(request.getOrderTableId());
        orderTable.validateIsEmpty();
        final Order order = Order.of(orderTable, COOKING, LocalDateTime.now());
        return orderRepository.save(order).getId();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findAll() {
        final List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    public OrderResponse changeOrderStatus(final Long orderId, final String orderStatusName) {
        final Order savedOrder = orderRepository.getById(orderId);
        final OrderStatus orderStatus = OrderStatus.from(orderStatusName);
        savedOrder.updateOrderStatus(orderStatus);
        return OrderResponse.from(savedOrder);
    }

    private void validateEqualsMenuCount(final OrderCreateRequest request) {
        if (request.getOrderLineItems().size() != menuRepository.countByIdIn(request.extractMenuIds())) {
            throw new IllegalArgumentException();
        }
    }
}
