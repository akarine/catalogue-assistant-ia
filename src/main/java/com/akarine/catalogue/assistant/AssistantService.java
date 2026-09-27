package com.akarine.catalogue.assistant;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * Agent conversationnel du magasin : le modèle d'IA répond en s'appuyant
 * uniquement sur les outils du catalogue.
 */
@Service
public class AssistantService {

    static final String SYSTEM_PROMPT = """
            Tu es l'assistant d'un magasin de bricolage. Tu aides les clients à trouver des produits.
            Règles :
            - Réponds en français, de façon concise et aimable.
            - Utilise toujours les outils pour consulter le catalogue et le stock :
              n'invente jamais de produit, de prix ni de disponibilité.
            - Indique le prix en euros et précise si le produit est en stock.
            - Si aucun produit ne correspond, dis-le et propose une alternative proche du catalogue.
            - Si la question ne concerne pas le magasin, explique poliment que tu ne peux pas y répondre.
            """;

    private final ChatClient chatClient;

    public AssistantService(ObjectProvider<ChatModel> chatModel, ObjectProvider<ChatClient.Builder> builder,
                            CatalogTools tools) {
        // Pas de modèle configuré (spring.ai.model.chat=none) : l'API catalogue reste utilisable sans l'agent.
        this.chatClient = chatModel.getIfAvailable() == null ? null
                : builder.getObject().defaultSystem(SYSTEM_PROMPT).defaultTools(tools).build();
    }

    public String ask(String question) {
        if (chatClient == null) {
            throw new AssistantUnavailableException();
        }
        try {
            return chatClient.prompt()
                    .user(question)
                    .call()
                    .content();
        } catch (RuntimeException e) {
            throw new AiProviderException(e);
        }
    }
}
