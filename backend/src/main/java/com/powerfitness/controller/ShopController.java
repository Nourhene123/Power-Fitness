package com.powerfitness.controller;

import com.powerfitness.dto.ProductDto;
import com.powerfitness.service.ProductService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shop")
public class ShopController {

    private final ProductService productService;

    public ShopController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/products")
    public List<ProductDto> products(@RequestParam(required = false) String category) {
        return productService.browse(category);
    }

    @GetMapping("/products/{id}")
    public ProductDto product(@PathVariable Long id) {
        return productService.get(id);
    }
}
