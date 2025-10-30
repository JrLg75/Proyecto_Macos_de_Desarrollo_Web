package com.techzone.peru.model.dto;

// Este DTO representa la estructura de nivel superior de la solicitud de Dialogflow.
public record DialogflowWebhookRequest(String responseId, QueryResult queryResult, String session) {
}