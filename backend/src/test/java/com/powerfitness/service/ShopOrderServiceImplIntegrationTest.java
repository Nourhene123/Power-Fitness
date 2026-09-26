package com.powerfitness.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.powerfitness.dto.OrderItemLineRequest;
import com.powerfitness.dto.PlaceOrderRequest;
import com.powerfitness.entity.Product;
import com.powerfitness.entity.ProductCategory;
import com.powerfitness.entity.Role;
import com.powerfitness.entity.User;
import com.powerfitness.exception.BusinessRuleException;
import com.powerfitness.repository.ProductRepository;
import com.powerfitness.repository.UserRepository;
import com.powerfitness.support.AbstractIntegrationTest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Stock handling in {@link ShopOrderServiceImpl} against a real PostgreSQL instance.
 *
 * <p>Deliberately not {@code @Transactional}: the concurrency test needs each order to commit in
 * its own transaction, exactly as two real HTTP requests would. Every test uses its own users and
 * products (random emails/names), so nothing leaks between tests.
 */
class ShopOrderServiceImplIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ShopOrderService shopOrderService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    private User newCustomer() {
        return userRepository.save(User.builder()
                .name("Shop Customer")
                .email("customer-" + UUID.randomUUID() + "@example.com")
                .password("irrelevant-for-this-test")
                .role(Role.USER)
                .build());
    }

    private Product newProduct(ProductCategory category, int stock) {
        return productRepository.save(Product.builder()
                .name("Test product " + UUID.randomUUID())
                .category(category)
                .price(new BigDecimal("50.00"))
                .stockQty(stock)
                .build());
    }

    private static PlaceOrderRequest order(OrderItemLineRequest... lines) {
        return new PlaceOrderRequest(List.of(lines), "Test Customer", "+21600000000", "1 Test Street", "Tunis", null);
    }

    private int stockOf(Product product) {
        return productRepository.findById(product.getId()).orElseThrow().getStockQty();
    }

    @Test
    void theSameProductInTwoSizesIsCheckedAgainstTheCombinedQuantity() {
        User customer = newCustomer();
        Product tee = newProduct(ProductCategory.APPAREL, 3);

        // 2 x M + 2 x L = 4, but only 3 in stock: must be refused as a business rule, not a 500.
        assertThatThrownBy(() -> shopOrderService.placeOrder(customer.getId(), order(
                new OrderItemLineRequest(tee.getId(), 2, "M"),
                new OrderItemLineRequest(tee.getId(), 2, "L"))))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Not enough stock");

        assertThat(stockOf(tee)).isEqualTo(3);
    }

    @Test
    void theSameProductInTwoSizesWithinStockDecrementsTheCombinedQuantity() {
        User customer = newCustomer();
        Product tee = newProduct(ProductCategory.APPAREL, 5);

        shopOrderService.placeOrder(customer.getId(), order(
                new OrderItemLineRequest(tee.getId(), 2, "M"),
                new OrderItemLineRequest(tee.getId(), 1, "L")));

        assertThat(stockOf(tee)).isEqualTo(2);
    }

    @Test
    void concurrentOrdersCannotSellTheSameStockTwice() throws Exception {
        Product creatine = newProduct(ProductCategory.SUPPLEMENTS, 3);
        int buyers = 6;
        List<User> customers = new ArrayList<>();
        for (int i = 0; i < buyers; i++) {
            customers.add(newCustomer());
        }

        // Six customers each try to buy 2 of the 3 units at the same moment.
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(buyers);
        List<Future<Boolean>> results = new ArrayList<>();
        for (User customer : customers) {
            Callable<Boolean> attempt = () -> {
                start.await();
                try {
                    shopOrderService.placeOrder(customer.getId(), order(new OrderItemLineRequest(creatine.getId(), 2, null)));
                    return true;
                } catch (BusinessRuleException outOfStock) {
                    return false;
                }
            };
            results.add(pool.submit(attempt));
        }
        start.countDown();

        int succeeded = 0;
        for (Future<Boolean> result : results) {
            if (result.get()) {
                succeeded++;
            }
        }
        pool.shutdown();

        // Only one order of 2 fits in a stock of 3; the database must agree with what was sold.
        assertThat(succeeded).isEqualTo(1);
        assertThat(stockOf(creatine)).isEqualTo(1);
    }
}
