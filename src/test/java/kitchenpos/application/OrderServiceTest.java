package kitchenpos.application;

import static org.mockito.BDDMockito.*;
import static org.junit.jupiter.api.Assertions.*;

import kitchenpos.dao.MenuRepository;
import kitchenpos.dao.OrderRepository;
import kitchenpos.dao.OrderTableRepository;
import kitchenpos.domain.Order;
import kitchenpos.domain.OrderStatus;
import kitchenpos.domain.OrderTable;
import kitchenpos.ui.dto.OrderCreateRequest;
import kitchenpos.ui.dto.OrderLineItemRequest;
import kitchenpos.ui.dto.OrderResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @InjectMocks
    private OrderService orderService;
    @Mock
    private MenuRepository menuRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderTableRepository orderTableRepository;

    @Test
    void testCreateOrder() {
        // given
        OrderCreateRequest request = new OrderCreateRequest(1L, Arrays.asList(
                new OrderLineItemRequest(1L, 1),
                new OrderLineItemRequest(2L, 2)
        ));
        OrderTable orderTable = mock(OrderTable.class);

        given(orderTableRepository.findById(anyLong())).willReturn(Optional.of(orderTable));
        given(menuRepository.countByIdIn(anyList())).willReturn(2L);
        given(orderRepository.save(any(Order.class))).willReturn(new Order());

        // when
        Long result = orderService.create(request);

        // then
        verify(orderTableRepository).findById(1L);
        verify(menuRepository).countByIdIn(anyList());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void testFindAllOrders() {
        // given
        Order order1 = new Order();
        Order order2 = new Order();
        given(orderRepository.findAll()).willReturn(Arrays.asList(order1, order2));

        // when
        List<OrderResponse> results = orderService.findAll();

        // then
        assertEquals(2, results.size());
        verify(orderRepository).findAll();
    }

    @Test
    void testChangeOrderStatus() {
        // given
        Long orderId = 1L;
        String orderStatusName = "COOKING";
        Order savedOrder = mock(Order.class);

        given(orderRepository.findById(anyLong())).willReturn(Optional.of(savedOrder));

        // when
        OrderResponse result = orderService.changeOrderStatus(orderId, orderStatusName);

        // then
        assertNotNull(result);
        verify(savedOrder).updateOrderStatus(OrderStatus.from(orderStatusName));
        verify(orderRepository).findById(orderId);
    }
}