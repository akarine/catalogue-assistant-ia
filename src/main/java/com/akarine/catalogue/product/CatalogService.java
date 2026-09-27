package com.akarine.catalogue.product;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CatalogService {

    private final ProductRepository repository;

    public CatalogService(ProductRepository repository) {
        this.repository = repository;
    }

    public List<ProductDto> search(String category, BigDecimal maxPrice, String text) {
        return repository.search(blankToNull(category), maxPrice, blankToNull(text)).stream()
                .map(ProductDto::from)
                .toList();
    }

    public List<String> categories() {
        return repository.findCategories();
    }

    public ProductDto findById(Long id) {
        return ProductDto.from(getProduct(id));
    }

    public Availability availability(Long id) {
        Product p = getProduct(id);
        return new Availability(p.getReference(), p.getName(), p.getStock(), p.getStock() > 0);
    }

    @Transactional
    public ProductDto create(NewProduct newProduct) {
        return ProductDto.from(repository.save(newProduct.toEntity()));
    }

    private Product getProduct(Long id) {
        return repository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
    }

    private static String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
