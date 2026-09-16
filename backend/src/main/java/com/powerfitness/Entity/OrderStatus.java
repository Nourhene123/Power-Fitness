package com.powerfitness.Entity;

/** Fulfillment status of a {@link ShopOrder} — cash-on-delivery, so this tracks packing/shipping, not payment. */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
