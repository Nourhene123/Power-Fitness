package com.powerfitness.Services.Interface;

import com.powerfitness.DTO.AdminOrderDto;
import com.powerfitness.DTO.OrderDto;
import com.powerfitness.DTO.PlaceOrderRequest;
import java.util.List;

/** Cash-on-delivery shop orders — no payment gateway, the customer pays when the parcel arrives. */
public interface ShopOrderService {

    OrderDto placeOrder(Long userId, PlaceOrderRequest body);

    List<OrderDto> myOrders(Long userId);

    /** Coach/admin order queue, optionally filtered by status (null/blank = all). */
    List<AdminOrderDto> adminList(String status);

    AdminOrderDto updateStatus(Long orderId, String status);
}
