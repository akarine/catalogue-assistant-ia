package com.akarine.catalogue.assistant;

/**
 * Erreur du fournisseur d'IA (quota, clé refusée, modèle inconnu…), quel que soit le fournisseur.
 */
public class AiProviderException extends RuntimeException {

    public AiProviderException(Throwable cause) {
        super(rootMessage(cause), cause);
    }

    private static String rootMessage(Throwable e) {
        Throwable root = e;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        return root.getClass().getSimpleName() + " : " + root.getMessage();
    }
}
