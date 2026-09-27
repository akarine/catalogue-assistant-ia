package com.akarine.catalogue.product;

import java.math.BigDecimal;

public record ProductDto(Long id, String reference, String name, String category, String brand,
                         BigDecimal price, int stock, String description) {

    static ProductDto from(Product p) {
        return new ProductDto(p.getId(), p.getReference(), p.getName(), p.getCategory(), p.getBrand(),
                p.getPrice(), p.getStock(), p.getDescription());
    }
}
