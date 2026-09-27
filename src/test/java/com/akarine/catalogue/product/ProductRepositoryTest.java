package com.akarine.catalogue.product;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste la requête de recherche sur la base H2 alimentée par data.sql.
 */
@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    ProductRepository repository;

    @Test
    void sansCritereRetourneToutLeCatalogueTrieParPrix() {
        var products = repository.search(null, null, null);

        assertThat(products).hasSize(15);
        assertThat(products).extracting(Product::getPrice).isSorted();
    }

    @Test
    void filtreParCategorieSansTenirCompteDeLaCasse() {
        var products = repository.search("jardin", null, null);

        assertThat(products).extracting(Product::getReference)
                .containsExactlyInAnyOrder("JAR-001", "JAR-002", "JAR-003");
    }

    @Test
    void combineCategoriePrixMaxEtTexte() {
        var products = repository.search("Outillage électroportatif", new BigDecimal("100"), "perceuse");

        assertThat(products).extracting(Product::getReference).containsExactly("PER-002");
    }

    @Test
    void chercheLeTexteDansLaDescription() {
        var products = repository.search(null, null, "meubles");

        assertThat(products).extracting(Product::getReference).containsExactly("PER-003");
    }
}
