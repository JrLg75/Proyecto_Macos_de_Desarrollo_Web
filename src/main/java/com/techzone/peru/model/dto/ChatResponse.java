package com.techzone.peru.model.dto;

import java.util.List;

public record ChatResponse(String reply, List<ProductSuggestionDto> products) {}


