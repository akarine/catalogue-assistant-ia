package com.akarine.catalogue.web;

import com.akarine.catalogue.assistant.AiProviderException;
import com.akarine.catalogue.assistant.AssistantUnavailableException;
import com.akarine.catalogue.product.ProductNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Erreurs renvoyées au format standard RFC 9457 (Problem Details).
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(ProductNotFoundException.class)
    ProblemDetail productNotFound(ProductNotFoundException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        problem.setTitle("Produit introuvable");
        return problem;
    }

    @ExceptionHandler(AssistantUnavailableException.class)
    ProblemDetail assistantUnavailable(AssistantUnavailableException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
        problem.setTitle("Assistant indisponible");
        return problem;
    }

    /**
     * Erreur renvoyée par le fournisseur d'IA (quota dépassé, clé refusée…).
     */
    @ExceptionHandler(AiProviderException.class)
    ProblemDetail aiProviderError(AiProviderException e) {
        log.warn("Erreur du fournisseur d'IA", e);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, e.getMessage());
        problem.setTitle("Erreur du fournisseur d'IA");
        return problem;
    }
}
