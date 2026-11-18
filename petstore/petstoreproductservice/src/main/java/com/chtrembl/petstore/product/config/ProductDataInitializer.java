package com.chtrembl.petstore.product.config;

import com.chtrembl.petstore.product.model.DataPreload;
import com.chtrembl.petstore.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ProductDataInitializer {

    private final DataPreload dataPreload;
    private final ProductRepository productRepository;

    @Bean
    public ApplicationRunner initializeProducts() {
        return args -> dataPreload.getProducts()
                .forEach(productRepository::save);
    }
}
