package com.akarine.catalogue.product;

import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final CatalogService catalogService;

    public ProductController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    public List<ProductDto> search(@RequestParam(required = false) String category,
                                   @RequestParam(required = false) @PositiveOrZero BigDecimal maxPrice,
                                   @RequestParam(required = false) String q) {
        return catalogService.search(category, maxPrice, q);
    }

    @GetMapping("/{id}")
    public ProductDto findById(@PathVariable Long id) {
        return catalogService.findById(id);
    }

    @GetMapping("/{id}/availability")
    public Availability availability(@PathVariable Long id) {
        return catalogService.availability(id);
    }

    @PostMapping
    public ResponseEntity<ProductDto> create(@Valid @RequestBody NewProduct newProduct) {
        ProductDto created = catalogService.create(newProduct);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }
}
