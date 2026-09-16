package com.powerfitness.Mapper;

import com.powerfitness.DTO.ProductDto;
import com.powerfitness.Entity.Product;
import org.mapstruct.Mapper;

@Mapper
public interface ProductMapper {

    ProductDto toDto(Product product);
}
