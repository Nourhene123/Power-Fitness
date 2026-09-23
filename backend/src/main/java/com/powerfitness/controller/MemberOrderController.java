package com.powerfitness.controller;

import com.powerfitness.dto.OrderDto;
import com.powerfitness.dto.PlaceOrderRequest;
import com.powerfitness.security.AppUserPrincipal;
import com.powerfitness.service.ShopOrderService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me/orders")
@PreAuthorize("hasRole('USER')")
public class MemberOrderController {

    private final ShopOrderService orderService;

    public MemberOrderController(ShopOrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public OrderDto placeOrder(@AuthenticationPrincipal AppUserPrincipal principal,
                               @Valid @RequestBody PlaceOrderRequest body) {
        return orderService.placeOrder(principal.id(), body);
    }

    @GetMapping
    public List<OrderDto> myOrders(@AuthenticationPrincipal AppUserPrincipal principal) {
        return orderService.myOrders(principal.id());
    }
}
