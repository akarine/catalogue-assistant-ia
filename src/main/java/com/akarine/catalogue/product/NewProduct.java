package com.akarine.catalogue.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record NewProduct(
        @NotBlank String reference,
        @NotBlank String name,
        @NotBlank String category,
        String brand,
        @NotNull @Positive BigDecimal price,
        @NotNull @PositiveOrZero Integer stock,
        @Size(max = 1000) String description) {

    Product toEntity() {
        return new Product(reference, name, category, brand, price, stock, description);
    }
}
