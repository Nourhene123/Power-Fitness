package com.powerfitness.mapper;

import com.powerfitness.dto.ProductDto;
import com.powerfitness.entity.Product;
import org.mapstruct.Mapper;

@Mapper
public interface ProductMapper {

    ProductDto toDto(Product product);
}
