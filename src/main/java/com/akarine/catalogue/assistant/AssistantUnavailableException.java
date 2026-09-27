package com.akarine.catalogue.assistant;

public class AssistantUnavailableException extends RuntimeException {

    public AssistantUnavailableException() {
        super("Aucun modèle d'IA n'est configuré (spring.ai.model.chat).");
    }
}
