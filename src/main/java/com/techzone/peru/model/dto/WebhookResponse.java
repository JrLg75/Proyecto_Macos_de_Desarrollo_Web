package com.techzone.peru.model.dto;

// DTO para la respuesta que Dialogflow espera.
public record WebhookResponse(String fulfillmentText) {
}