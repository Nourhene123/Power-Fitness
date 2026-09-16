package com.powerfitness.Services.Implimentation;

import com.powerfitness.DTO.CreateProductRequest;
import com.powerfitness.DTO.ProductDto;
import com.powerfitness.DTO.UpdateProductRequest;
import com.powerfitness.Entity.Product;
import com.powerfitness.Entity.ProductCategory;
import com.powerfitness.Exception.BusinessRuleException;
import com.powerfitness.Exception.ResourceNotFoundException;
import com.powerfitness.Mapper.ProductMapper;
import com.powerfitness.Repository.ProductRepository;
import com.powerfitness.Services.Interface.ProductService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository products;
    private final ProductMapper mapper;

    public ProductServiceImpl(ProductRepository products, ProductMapper mapper) {
        this.products = products;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> browse(String category) {
        if (category == null || category.isBlank()) {
            return products.findByActiveTrueOrderByCreatedAtDesc().stream().map(mapper::toDto).toList();
        }
        return products.findByActiveTrueAndCategoryOrderByCreatedAtDesc(parseCategory(category)).stream()
                .map(mapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDto get(Long id) {
        Product product = products.findById(id).filter(Product::isActive)
                .orElseThrow(() -> ResourceNotFoundException.of("Product", id));
        return mapper.toDto(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> adminList() {
        return products.findAllByOrderByCreatedAtDesc().stream().map(mapper::toDto).toList();
    }

    @Override
    public ProductDto create(CreateProductRequest body) {
        Product product = Product.builder()
                .name(body.name().trim())
                .category(parseCategory(body.category()))
                .description(body.description())
                .price(body.price())
                .imageUrl(body.imageUrl())
                .stockQty(body.stockQty())
                .active(true)
                .build();
        return mapper.toDto(products.save(product));
    }

    @Override
    public ProductDto update(Long id, UpdateProductRequest body) {
        Product product = products.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Product", id));
        product.setName(body.name().trim());
        product.setCategory(parseCategory(body.category()));
        product.setDescription(body.description());
        product.setPrice(body.price());
        product.setImageUrl(body.imageUrl());
        product.setStockQty(body.stockQty());
        product.setActive(body.active());
        return mapper.toDto(product);
    }

    @Override
    public void delete(Long id) {
        if (!products.existsById(id)) {
            throw ResourceNotFoundException.of("Product", id);
        }
        products.deleteById(id);
    }

    private static ProductCategory parseCategory(String category) {
        try {
            return ProductCategory.valueOf(category.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException("Unknown product category: " + category);
        }
    }
}
