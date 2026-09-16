package com.powerfitness.Services.Interface;

import com.powerfitness.DTO.CreateProductRequest;
import com.powerfitness.DTO.ProductDto;
import com.powerfitness.DTO.UpdateProductRequest;
import java.util.List;

/** The shop catalog — public browsing plus coach/admin management. */
public interface ProductService {

    /** Active products, optionally filtered by category (null/blank = all). Public. */
    List<ProductDto> browse(String category);

    /** A single active product. 404 if missing or inactive. Public. */
    ProductDto get(Long id);

    /** Every product, active or not. Coach/admin catalog view. */
    List<ProductDto> adminList();

    ProductDto create(CreateProductRequest body);

    ProductDto update(Long id, UpdateProductRequest body);

    void delete(Long id);
}
