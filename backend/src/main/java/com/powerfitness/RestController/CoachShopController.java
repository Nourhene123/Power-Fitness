package com.powerfitness.RestController;

import com.powerfitness.DTO.AdminOrderDto;
import com.powerfitness.DTO.CreateProductRequest;
import com.powerfitness.DTO.ProductDto;
import com.powerfitness.DTO.UpdateOrderStatusRequest;
import com.powerfitness.DTO.UpdateProductRequest;
import com.powerfitness.Services.Interface.FileStorageService;
import com.powerfitness.Services.Interface.ProductService;
import com.powerfitness.Services.Interface.ShopOrderService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/coach/shop")
@PreAuthorize("hasAnyRole('COACH','ADMIN')")
public class CoachShopController {

    private final ProductService productService;
    private final ShopOrderService orderService;
    private final FileStorageService fileStorageService;

    public CoachShopController(ProductService productService, ShopOrderService orderService,
                               FileStorageService fileStorageService) {
        this.productService = productService;
        this.orderService = orderService;
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/products/image")
    public Map<String, String> uploadProductImage(@RequestParam("file") MultipartFile file) {
        return Map.of("url", fileStorageService.storeProductImage(file));
    }

    @GetMapping("/products")
    public List<ProductDto> products() {
        return productService.adminList();
    }

    @PostMapping("/products")
    public ProductDto createProduct(@Valid @RequestBody CreateProductRequest body) {
        return productService.create(body);
    }

    @PutMapping("/products/{id}")
    public ProductDto updateProduct(@PathVariable Long id, @Valid @RequestBody UpdateProductRequest body) {
        return productService.update(id, body);
    }

    @DeleteMapping("/products/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable Long id) {
        productService.delete(id);
    }

    @GetMapping("/orders")
    public List<AdminOrderDto> orders(@RequestParam(required = false) String status) {
        return orderService.adminList(status);
    }

    @PatchMapping("/orders/{id}/status")
    public AdminOrderDto updateOrderStatus(@PathVariable Long id, @Valid @RequestBody UpdateOrderStatusRequest body) {
        return orderService.updateStatus(id, body.status());
    }
}
