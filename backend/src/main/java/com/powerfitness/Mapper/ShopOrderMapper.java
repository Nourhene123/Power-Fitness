package com.powerfitness.Mapper;

import com.powerfitness.DTO.AdminOrderDto;
import com.powerfitness.DTO.OrderDto;
import com.powerfitness.DTO.OrderItemDto;
import com.powerfitness.Entity.ShopOrder;
import com.powerfitness.Entity.ShopOrderItem;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper
public interface ShopOrderMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "lineTotal", expression = "java(item.getUnitPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity())))")
    OrderItemDto toItemDto(ShopOrderItem item);

    List<OrderItemDto> toItemDtos(List<ShopOrderItem> items);

    OrderDto toOrderDto(ShopOrder order, List<OrderItemDto> items);

    @Mapping(target = "buyerName", source = "order.user.name")
    @Mapping(target = "buyerEmail", source = "order.user.email")
    AdminOrderDto toAdminDto(ShopOrder order, List<OrderItemDto> items);
}
