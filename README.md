# Catalogue Assistant IA

Assistant conversationnel pour un magasin de bricolage : un **agent IA** répond aux questions des clients
en interrogeant lui-même une **API REST catalogue** (recherche de produits, stock, catégories).

*English version below.*

![Java 21](https://img.shields.io/badge/Java-21-orange) ![Spring Boot 4](https://img.shields.io/badge/Spring%20Boot-4.1-6db33f) ![Spring AI 2](https://img.shields.io/badge/Spring%20AI-2.0-6db33f) ![Tests](https://img.shields.io/badge/tests-21%20JUnit%205-blue)

## Exemple

```bash
curl -s -X POST localhost:8080/api/assistant/chat \
  -H "Content-Type: application/json" \
  -d '{"question":"Avez-vous un taille-haie disponible ?"}'
```

```json
{"answer": "Nous proposons bien un taille-haie électrique 500W (Bosch) au prix de 99,90 € (réf. JAR-002), mais il est actuellement en rupture de stock. […]"}
```

Pour répondre, l'agent a **choisi seul** d'appeler deux outils Java : la recherche dans le catalogue, puis la vérification du stock.
Une question hors sujet (« Quelle est la capitale du Japon ? ») est poliment refusée.

## Fonctionnalités

**API REST catalogue**

| Méthode | URL | Description |
|---|---|---|
| `GET` | `/api/products?category=&maxPrice=&q=` | Recherche multicritère (catégorie, prix max, texte), triée par prix |
| `GET` | `/api/products/{id}` | Fiche produit (404 au format Problem Details si inconnu) |
| `GET` | `/api/products/{id}/availability` | Stock et disponibilité |
| `POST` | `/api/products` | Création d'un produit (validation, 201 + en-tête `Location`) |

**Agent IA**

| Méthode | URL | Description |
|---|---|---|
| `POST` | `/api/assistant/chat` | Question en langage naturel → réponse fondée sur les données du catalogue |

## Architecture

```mermaid
flowchart LR
    C[Client] -->|POST /api/assistant/chat| AC[AssistantController]
    AC --> AS[AssistantService<br/>ChatClient + prompt système]
    AS <-->|appel d'outils| LLM[(Modèle d'IA<br/>Gemini ou Mistral)]
    AS --> T[CatalogTools<br/>@Tool]
    T --> S[CatalogService]
    C -->|/api/products| PC[ProductController] --> S
    S --> R[ProductRepository<br/>Spring Data JPA] --> DB[(H2)]
```

- **Appel d'outils (tool calling)** : les méthodes annotées `@Tool` de `CatalogTools` (`listCategories`, `searchProducts`,
  `checkAvailability`) sont décrites au modèle, qui décide quand les appeler et avec quels paramètres.
- **Pas d'hallucination sur les données** : le prompt système impose de s'appuyer uniquement sur les outils
  (jamais de produit, prix ou stock inventé) et de refuser les questions hors sujet.
- **Fournisseur d'IA interchangeable** : Gemini ou Mistral, choisi par configuration (`AI_CHAT_MODEL`), sans changer le code.
- **Dégradation propre** : sans modèle configuré, l'API catalogue fonctionne et le chat répond `503` ;
  les erreurs du fournisseur (quota, clé, modèle) sont renvoyées en `502` au format
  [RFC 9457 Problem Details](https://www.rfc-editor.org/rfc/rfc9457).

## Stack technique

Java 21 · Spring Boot 4.1 (Spring MVC, Spring Data JPA, Bean Validation) · Spring AI 2.0 (Google Gemini, Mistral AI) ·
H2 · Maven · JUnit 5 · Mockito · AssertJ · MockMvc

## Lancer le projet

Prérequis : **Java 21**. Maven est fourni (`mvnw`).

```bash
# 1. Configurer l'IA (optionnel : sans clé, seule l'API catalogue est active)
cp .env.example .env
# puis renseigner dans .env :
#   AI_CHAT_MODEL=google-genai
#   GEMINI_API_KEY=<votre clé, gratuite sur https://aistudio.google.com>

# 2. Démarrer
./mvnw spring-boot:run

# 3. Essayer
curl "localhost:8080/api/products?q=perceuse&maxPrice=100"
```

Le fichier `.env` est exclu de Git : les clés d'API ne sont jamais versionnées.
Console de la base : `http://localhost:8080/h2-console` (URL JDBC `jdbc:h2:mem:catalogue`, utilisateur `sa`).

## Tests

```bash
./mvnw test
```

21 tests : requêtes JPA sur base H2 (`@DataJpaTest`), logique métier et outils de l'agent (Mockito),
contrôleurs REST (`@WebMvcTest` : 200, 201, 400, 404, 503).
Les tests n'appellent aucun fournisseur d'IA.

## Pistes d'évolution

- Nouvelles tentatives avec délai en cas de quota dépassé (`429`)
- Mémoire de conversation entre plusieurs questions
- Publication d'événements de stock via Kafka
- Front Angular, conteneurisation Docker / Kubernetes

---

## English

**Catalogue Assistant IA** is a conversational assistant for a DIY store. An **AI agent** answers customer questions
by calling a **product catalogue REST API** by itself (product search, stock availability, categories).

- **Tool calling** with Spring AI: `@Tool` methods are exposed to the model, which decides which ones to call and with which arguments.
- **Grounded answers**: the system prompt requires the model to rely only on catalogue data and to decline off-topic questions.
- **Pluggable AI provider**: Google Gemini or Mistral AI, selected through configuration only.
- **Graceful degradation**: the catalogue API works without any AI provider; provider errors are returned as RFC 9457 Problem Details.
- **Stack**: Java 21, Spring Boot 4.1, Spring AI 2.0, Spring Data JPA, H2, Maven, JUnit 5, Mockito (21 tests).

Run: `cp .env.example .env`, set `AI_CHAT_MODEL=google-genai` and `GEMINI_API_KEY`, then `./mvnw spring-boot:run`.

---

Auteure : **Karine Avakova** – ingénieure Java / Spring Boot · [github.com/akarine](https://github.com/akarine)
