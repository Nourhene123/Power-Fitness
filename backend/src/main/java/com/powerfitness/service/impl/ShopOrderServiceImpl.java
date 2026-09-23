package com.powerfitness.service.impl;

import com.powerfitness.dto.AdminOrderDto;
import com.powerfitness.dto.OrderDto;
import com.powerfitness.dto.OrderItemDto;
import com.powerfitness.dto.OrderItemLineRequest;
import com.powerfitness.dto.PlaceOrderRequest;
import com.powerfitness.entity.OrderStatus;
import com.powerfitness.entity.Product;
import com.powerfitness.entity.ShopOrder;
import com.powerfitness.entity.ShopOrderItem;
import com.powerfitness.entity.User;
import com.powerfitness.exception.BusinessRuleException;
import com.powerfitness.exception.ResourceNotFoundException;
import com.powerfitness.mapper.ShopOrderMapper;
import com.powerfitness.repository.ProductRepository;
import com.powerfitness.repository.ShopOrderItemRepository;
import com.powerfitness.repository.ShopOrderRepository;
import com.powerfitness.repository.UserRepository;
import com.powerfitness.service.NotificationService;
import com.powerfitness.service.ShopOrderService;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ShopOrderServiceImpl implements ShopOrderService {

    private static final BigDecimal FREE_DELIVERY_THRESHOLD = new BigDecimal("150.000");
    private static final BigDecimal DELIVERY_FEE = new BigDecimal("7.000");

    private final UserRepository users;
    private final ProductRepository products;
    private final ShopOrderRepository orders;
    private final ShopOrderItemRepository orderItems;
    private final NotificationService notificationService;
    private final ShopOrderMapper mapper;

    public ShopOrderServiceImpl(UserRepository users, ProductRepository products, ShopOrderRepository orders,
                                ShopOrderItemRepository orderItems, NotificationService notificationService,
                                ShopOrderMapper mapper) {
        this.users = users;
        this.products = products;
        this.orders = orders;
        this.orderItems = orderItems;
        this.notificationService = notificationService;
        this.mapper = mapper;
    }

    @Override
    public OrderDto placeOrder(Long userId, PlaceOrderRequest body) {
        User user = users.findById(userId).orElseThrow(() -> ResourceNotFoundException.of("User", userId));

        List<ResolvedLine> lines = body.items().stream().map(this::resolveLine).toList();

        BigDecimal subtotal = lines.stream()
                .map(l -> l.product().getPrice().multiply(BigDecimal.valueOf(l.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (subtotal.signum() <= 0) {
            throw new BusinessRuleException("Your cart is empty.");
        }
        BigDecimal deliveryFee = subtotal.compareTo(FREE_DELIVERY_THRESHOLD) < 0 ? DELIVERY_FEE : BigDecimal.ZERO;

        ShopOrder order = orders.save(ShopOrder.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .subtotal(subtotal)
                .deliveryFee(deliveryFee)
                .total(subtotal.add(deliveryFee))
                .recipientName(body.recipientName().trim())
                .phone(body.phone().trim())
                .addressLine(body.addressLine().trim())
                .city(body.city().trim())
                .note(blankToNull(body.note()))
                .build());

        for (ResolvedLine line : lines) {
            orderItems.save(ShopOrderItem.builder()
                    .order(order)
                    .product(line.product())
                    .productName(line.product().getName())
                    .unitPrice(line.product().getPrice())
                    .quantity(line.quantity())
                    .size(line.size())
                    .build());
            line.product().setStockQty(line.product().getStockQty() - line.quantity());
            products.save(line.product());
        }

        notificationService.coachUser().ifPresent(coach -> notificationService.notify(coach, "shop_order",
                "New shop order",
                user.getName() + " placed an order for " + order.getTotal() + " TND (cash on delivery).",
                "/coach/shop/orders"));

        return toOrderDto(order);
    }

    private record ResolvedLine(Product product, int quantity, String size) {}

    private ResolvedLine resolveLine(OrderItemLineRequest line) {
        Product product = products.findById(line.productId())
                .orElseThrow(() -> ResourceNotFoundException.of("Product", line.productId()));
        if (!product.isActive()) {
            throw new BusinessRuleException(product.getName() + " is no longer available.");
        }
        if (product.getStockQty() < line.quantity()) {
            throw new BusinessRuleException("Not enough stock for " + product.getName() + ".");
        }
        return new ResolvedLine(product, line.quantity(), blankToNull(line.size()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> myOrders(Long userId) {
        User user = users.findById(userId).orElseThrow(() -> ResourceNotFoundException.of("User", userId));
        return orders.findByUserOrderByCreatedAtDesc(user).stream().map(this::toOrderDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminOrderDto> adminList(String status) {
        List<ShopOrder> list = (status == null || status.isBlank())
                ? orders.findAllByOrderByCreatedAtDesc()
                : orders.findByStatusOrderByCreatedAtDesc(parseStatus(status));
        return list.stream().map(this::toAdminDto).toList();
    }

    @Override
    public AdminOrderDto updateStatus(Long orderId, String status) {
        ShopOrder order = orders.findById(orderId).orElseThrow(() -> ResourceNotFoundException.of("Order", orderId));
        order.setStatus(parseStatus(status));

        notificationService.notify(order.getUser(), "shop_order_status",
                "Your order status changed",
                "Order #" + order.getId() + " is now " + order.getStatus().name().toLowerCase() + ".",
                "/shop/orders");

        return toAdminDto(order);
    }

    private static OrderStatus parseStatus(String status) {
        try {
            return OrderStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException("Unknown order status: " + status);
        }
    }

    private static String blankToNull(String s) {
        return (s != null && !s.isBlank()) ? s.trim() : null;
    }

    private OrderDto toOrderDto(ShopOrder order) {
        return mapper.toOrderDto(order, itemsOf(order));
    }

    private AdminOrderDto toAdminDto(ShopOrder order) {
        return mapper.toAdminDto(order, itemsOf(order));
    }

    private List<OrderItemDto> itemsOf(ShopOrder order) {
        return mapper.toItemDtos(orderItems.findByOrderOrderByIdAsc(order));
    }
}
