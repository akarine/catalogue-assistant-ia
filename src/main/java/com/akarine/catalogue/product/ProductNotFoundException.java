package com.akarine.catalogue.product;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(Long id) {
        super("Produit introuvable : " + id);
    }
}
