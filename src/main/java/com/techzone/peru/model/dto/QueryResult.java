package com.techzone.peru.model.dto;

import java.util.Map;

// Contiene los detalles de la consulta, incluyendo la intención y los parámetros.
public record QueryResult(
        String queryText,
        Map<String, Object> parameters,
        boolean allRequiredParamsPresent,
        IntentInfo intent,
        String languageCode) {
}