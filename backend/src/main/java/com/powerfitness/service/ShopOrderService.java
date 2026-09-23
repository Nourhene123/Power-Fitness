package com.powerfitness.service;

import com.powerfitness.dto.AdminOrderDto;
import com.powerfitness.dto.OrderDto;
import com.powerfitness.dto.PlaceOrderRequest;
import java.util.List;

/** Cash-on-delivery shop orders — no payment gateway, the customer pays when the parcel arrives. */
public interface ShopOrderService {

    OrderDto placeOrder(Long userId, PlaceOrderRequest body);

    List<OrderDto> myOrders(Long userId);

    /** Coach/admin order queue, optionally filtered by status (null/blank = all). */
    List<AdminOrderDto> adminList(String status);

    AdminOrderDto updateStatus(Long orderId, String status);
}
