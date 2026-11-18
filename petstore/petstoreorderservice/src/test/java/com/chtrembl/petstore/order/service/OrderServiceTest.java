package com.chtrembl.petstore.order.service;

import com.chtrembl.petstore.order.model.Order;
import com.chtrembl.petstore.order.model.Product;
import com.chtrembl.petstore.order.repository.OrderRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    private static final String ORDER_ID =
            "68FAE9B1D86B794F0AE0ADD35A437428";

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrderShouldInitializeNewOrder() {

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.createOrder(ORDER_ID);

        assertEquals(ORDER_ID, result.getId());
        assertEquals(Order.Status.PLACED, result.getStatus());
        assertFalse(result.getComplete());
        assertTrue(result.getProducts().isEmpty());

        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void updateOrderShouldAddNewProduct() {

        Product availableProduct = Product.builder()
                .id(1L)
                .name("Ball")
                .photoURL("https://example.com/ball.jpg")
                .build();

        when(productService.getAvailableProducts())
                .thenReturn(List.of(availableProduct));

        when(orderRepository.findById(ORDER_ID))
                .thenReturn(Optional.empty());

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Product requestedProduct = Product.builder()
                .id(1L)
                .quantity(2)
                .build();

        Order request = Order.builder()
                .id(ORDER_ID)
                .email("customer@example.com")
                .products(List.of(requestedProduct))
                .build();

        Order result = orderService.updateOrder(request);

        assertEquals(1, result.getProducts().size());
        assertEquals(1L, result.getProducts().get(0).getId());
        assertEquals(2, result.getProducts().get(0).getQuantity());
        assertEquals("customer@example.com", result.getEmail());

        verify(orderRepository, times(2)).save(any(Order.class));
    }

    @Test
    void updateOrderShouldIncrementQuantityAndCapAtTen() {

        Product existingProduct = Product.builder()
                .id(1L)
                .quantity(8)
                .name("Ball")
                .build();

        Order existingOrder = Order.builder()
                .id(ORDER_ID)
                .products(new ArrayList<>(List.of(existingProduct)))
                .build();

        when(productService.getAvailableProducts())
                .thenReturn(List.of(
                        Product.builder().id(1L).name("Ball").build()
                ));

        when(orderRepository.findById(ORDER_ID))
                .thenReturn(Optional.of(existingOrder));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Product requestedProduct = Product.builder()
                .id(1L)
                .quantity(5)
                .build();

        Order request = Order.builder()
                .id(ORDER_ID)
                .products(List.of(requestedProduct))
                .build();

        Order result = orderService.updateOrder(request);

        assertEquals(10, result.getProducts().get(0).getQuantity());

        verify(orderRepository).save(existingOrder);
    }

    @Test
    void updateOrderShouldRejectUnavailableProduct() {

        when(productService.getAvailableProducts())
                .thenReturn(List.of(
                        Product.builder().id(1L).name("Ball").build()
                ));

        Product unavailableProduct = Product.builder()
                .id(999L)
                .quantity(1)
                .build();

        Order request = Order.builder()
                .id(ORDER_ID)
                .products(List.of(unavailableProduct))
                .build();

        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.updateOrder(request)
        );

        verifyNoInteractions(orderRepository);
    }

    @Test
    void completingOrderShouldClearProducts() {

        Product existingProduct = Product.builder()
                .id(1L)
                .quantity(2)
                .name("Ball")
                .build();

        Order existingOrder = Order.builder()
                .id(ORDER_ID)
                .products(new ArrayList<>(List.of(existingProduct)))
                .complete(false)
                .build();

        when(orderRepository.findById(ORDER_ID))
                .thenReturn(Optional.of(existingOrder));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Order request = Order.builder()
                .id(ORDER_ID)
                .complete(true)
                .build();

        Order result = orderService.updateOrder(request);

        assertTrue(result.getComplete());
        assertTrue(result.getProducts().isEmpty());

        verify(orderRepository).save(existingOrder);
    }
}
