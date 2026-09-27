package com.akarine.catalogue.assistant;

import com.akarine.catalogue.product.Availability;
import com.akarine.catalogue.product.CatalogService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogToolsTest {

    @Mock
    CatalogService catalogService;

    @InjectMocks
    CatalogTools tools;

    @Test
    void laRechercheDelegueAuServiceCatalogue() {
        tools.searchProducts("Jardin", new BigDecimal("100"), "tuyau");

        verify(catalogService).search("Jardin", new BigDecimal("100"), "tuyau");
    }

    @Test
    void lesCategoriesViennentDuCatalogue() {
        when(catalogService.categories()).thenReturn(List.of("Jardin", "Peinture"));

        assertThat(tools.listCategories()).containsExactly("Jardin", "Peinture");
    }

    @Test
    void laDisponibiliteDelegueAuServiceCatalogue() {
        when(catalogService.availability(13L)).thenReturn(new Availability("JAR-002", "Taille-haie", 0, false));

        assertThat(tools.checkAvailability(13L).available()).isFalse();
    }
}
