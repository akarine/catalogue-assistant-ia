package com.akarine.catalogue.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Recherche multicritère : chaque critère null est ignoré.
     */
    @Query("""
            select p from Product p
            where (:category is null or lower(p.category) = lower(cast(:category as string)))
              and (:maxPrice is null or p.price <= :maxPrice)
              and (:text is null
                   or lower(p.name) like lower(concat('%', cast(:text as string), '%'))
                   or lower(p.description) like lower(concat('%', cast(:text as string), '%')))
            order by p.price
            """)
    List<Product> search(@Param("category") String category,
                         @Param("maxPrice") BigDecimal maxPrice,
                         @Param("text") String text);

    @Query("select distinct p.category from Product p order by p.category")
    List<String> findCategories();
}
