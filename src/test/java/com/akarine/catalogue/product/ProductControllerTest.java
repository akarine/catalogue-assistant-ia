package com.akarine.catalogue.product;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    CatalogService catalogService;

    private static final ProductDto PERCEUSE = new ProductDto(1L, "PER-001", "Perceuse-visseuse sans fil 18V",
            "Outillage électroportatif", "Makita", new BigDecimal("129.90"), 12, "18V");

    @Test
    void rechercheAvecFiltres() throws Exception {
        when(catalogService.search("Outillage électroportatif", new BigDecimal("150"), "perceuse"))
                .thenReturn(List.of(PERCEUSE));

        mvc.perform(get("/api/products")
                        .param("category", "Outillage électroportatif")
                        .param("maxPrice", "150")
                        .param("q", "perceuse"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reference").value("PER-001"))
                .andExpect(jsonPath("$[0].price").value(129.90));
    }

    @Test
    void prixMaxNegatifRefuse() throws Exception {
        mvc.perform(get("/api/products").param("maxPrice", "-5"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(catalogService);
    }

    @Test
    void produitInconnuRenvoie404AuFormatProblemDetail() throws Exception {
        when(catalogService.findById(99L)).thenThrow(new ProductNotFoundException(99L));

        mvc.perform(get("/api/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Produit introuvable"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void creationRenvoie201AvecLocation() throws Exception {
        when(catalogService.create(any(NewProduct.class))).thenReturn(PERCEUSE);

        mvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reference":"PER-001","name":"Perceuse-visseuse sans fil 18V",
                                 "category":"Outillage électroportatif","brand":"Makita",
                                 "price":129.90,"stock":12,"description":"18V"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/products/1"));
    }

    @Test
    void creationInvalideRenvoie400() throws Exception {
        mvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reference":"","name":"Sans prix","category":"Outillage"}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(catalogService);
    }
}
