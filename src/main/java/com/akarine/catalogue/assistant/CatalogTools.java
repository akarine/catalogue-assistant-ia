package com.akarine.catalogue.assistant;

import com.akarine.catalogue.product.Availability;
import com.akarine.catalogue.product.CatalogService;
import com.akarine.catalogue.product.ProductDto;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Outils mis à disposition du modèle d'IA : c'est lui qui décide quand les appeler
 * et avec quels paramètres (tool calling).
 */
@Component
public class CatalogTools {

    private final CatalogService catalogService;

    public CatalogTools(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @Tool(description = "Liste les catégories de produits existant dans le catalogue du magasin.")
    public List<String> listCategories() {
        return catalogService.categories();
    }

    @Tool(description = """
            Recherche des produits dans le catalogue, triés par prix croissant.
            Tous les critères sont optionnels et se combinent.""")
    public List<ProductDto> searchProducts(
            @ToolParam(required = false, description = "Catégorie exacte, telle que renvoyée par listCategories") String category,
            @ToolParam(required = false, description = "Prix maximum en euros") BigDecimal maxPrice,
            @ToolParam(required = false, description = "Mot-clé cherché dans le nom ou la description, au singulier (ex. perceuse)") String keyword) {
        return catalogService.search(category, maxPrice, keyword);
    }

    @Tool(description = "Donne le stock et la disponibilité d'un produit à partir de son identifiant (champ id).")
    public Availability checkAvailability(@ToolParam(description = "Identifiant du produit") Long productId) {
        return catalogService.availability(productId);
    }
}
