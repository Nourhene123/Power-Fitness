package com.powerfitness.mapper;

import com.powerfitness.dto.AdminOrderDto;
import com.powerfitness.dto.OrderDto;
import com.powerfitness.dto.OrderItemDto;
import com.powerfitness.entity.ShopOrder;
import com.powerfitness.entity.ShopOrderItem;
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
