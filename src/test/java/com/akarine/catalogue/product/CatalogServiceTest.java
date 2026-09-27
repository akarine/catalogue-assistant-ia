package com.akarine.catalogue.product;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogServiceTest {

    @Mock
    ProductRepository repository;

    @InjectMocks
    CatalogService service;

    private static Product perceuse(int stock) {
        return new Product("PER-001", "Perceuse", "Outillage", "Makita", new BigDecimal("129.90"), stock, "18V");
    }

    @Test
    void lesCriteresVidesSontTransmisCommeNull() {
        when(repository.search(null, null, null)).thenReturn(List.of(perceuse(3)));

        var result = service.search("  ", null, "");

        assertThat(result).singleElement().extracting(ProductDto::reference).isEqualTo("PER-001");
        verify(repository).search(null, null, null);
    }

    @Test
    void produitDisponibleSiStockPositif() {
        when(repository.findById(1L)).thenReturn(Optional.of(perceuse(3)));

        assertThat(service.availability(1L).available()).isTrue();
    }

    @Test
    void produitIndisponibleSiStockNul() {
        when(repository.findById(1L)).thenReturn(Optional.of(perceuse(0)));

        assertThat(service.availability(1L).available()).isFalse();
    }

    @Test
    void produitInconnuLeveUneException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void creationEnregistreLeProduit() {
        when(repository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var nouveau = new NewProduct("MAR-002", "Maillet", "Outillage à main", "Stanley",
                new BigDecimal("15.00"), 10, "Maillet caoutchouc");

        var created = service.create(nouveau);

        assertThat(created.reference()).isEqualTo("MAR-002");
        verify(repository).save(any(Product.class));
    }
}
