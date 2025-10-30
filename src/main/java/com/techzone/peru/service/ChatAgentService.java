package com.techzone.peru.service;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import com.techzone.peru.model.dto.ChatResponse;
import com.techzone.peru.model.dto.ProductSuggestionDto;
import com.techzone.peru.model.entity.Producto;
import com.techzone.peru.model.entity.ProductoVariante;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class ChatAgentService {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private InformacionService informacionService;

    @Autowired
    private ProductService productService;

    @Autowired
    private VertexAI vertexAI;

    @Value("${vertex.model.name:gemini-2.5-pro}")
    private String modelName;

    public String handleMessage(String message) {
        if (message == null || message.isBlank()) {
            return "¿Podrías escribir tu consulta?";
        }

        String text = message.trim();
        String lower = text.toLowerCase(Locale.ROOT);

        // Pedido: buscar número simple en el texto
        if (lower.contains("pedido") || lower.contains("orden")) {
            String numero = extractOrderNumber(text);
            if (numero.isBlank()) {
                return "Por favor, dime el número de tu pedido.";
            }
            String estado = pedidoService.consultarEstadoPedido(numero);
            String context = estado == null || estado.isBlank() ? "No encontré ese pedido." : ("Estado del pedido " + numero + ": " + estado);
            return generateAnswer("El usuario pregunta por el estado de su pedido.", context, message);
        }

        // Métodos de pago
        if (lower.contains("pago") || lower.contains("tarjeta") || lower.contains("efectivo")) {
            String context = informacionService.getMetodosDePago();
            return generateAnswer("El usuario consulta métodos de pago.", context, message);
        }

        // Garantías / devoluciones
        if (lower.contains("garant") || lower.contains("devolu")) {
            String context = informacionService.getPoliticaGarantias();
            return generateAnswer("El usuario consulta garantías y devoluciones.", context, message);
        }

        // Envío
        if (lower.contains("envio") || lower.contains("envío") || lower.contains("enviar")) {
            String ciudad = extractCity(text);
            if (ciudad.isBlank()) {
                return "Para calcular el costo de envío, indícame tu ciudad.";
            }
            String context = informacionService.getCostoEnvio(ciudad);
            return generateAnswer("El usuario consulta costos de envío.", context, message);
        }

        // Búsqueda de productos en BD por nombre (más disparadores: marcas/categorías comunes)
        if (lower.contains("buscar") || lower.contains("producto") || lower.contains("recomienda") || lower.contains("recomendar") || lower.contains("recomendación")
                || lower.contains("iphone") || lower.contains("samsung") || lower.contains("xiaomi") || lower.contains("audifon") || lower.contains("audífon")
                || lower.contains("teclado") || lower.contains("mouse") || lower.contains("laptop") || lower.contains("celular") || lower.contains("smartphone")) {
            String query = extractQuery(text);
            System.out.println("[Agent] Query extraída: " + query);
            Integer budget = extractBudgetSoles(text);
            if (budget != null) {
                System.out.println("[Agent] Presupuesto detectado (S/): " + budget);
            }
            List<Producto> baseResults = productService.searchByKeywordsSmart(query);
            List<Producto> results = baseResults;
            // Filtrar por presupuesto si hay variantes con precio; si queda vacío, mantén baseResults
            if (budget != null && !baseResults.isEmpty()) {
                List<Producto> filtered = baseResults.stream()
                        .filter(p -> p.getVariants() != null && p.getVariants().stream().anyMatch(v -> v.getPrecio() != null && v.getPrecio().doubleValue() <= budget))
                        .toList();
                if (!filtered.isEmpty()) {
                    results = filtered;
                }
            }
            System.out.println("[Agent] Resultados encontrados (tras presupuesto si aplica): " + results.size());

            if (results.isEmpty()) {
                return generateAnswer(
                        "El usuario busca productos.",
                        "No encontré coincidencias. Pide otra palabra (marca/modelo) o categoría.",
                        message
                );
            }

            String listContext = buildProductsContext(results, budget);
            String instruction = "Recomienda solo de la lista proporcionada (no inventes modelos). Si hay presupuesto, prioriza <= presupuesto.";
            return generateAnswer(instruction, listContext, message);
        }

        String context = "Puedo ayudarte con estado de pedidos, métodos de pago, garantías y costos de envío.";
        return generateAnswer("Ayuda general.", context, message);
    }

    public ChatResponse handleMessageWithProducts(String message) {
        if (message == null || message.isBlank()) {
            return new ChatResponse("¿Podrías escribir tu consulta?", null);
        }

        String text = message.trim();
        String lower = text.toLowerCase(Locale.ROOT);

        // Búsqueda de productos con posibles sugerencias
        if (lower.contains("buscar") || lower.contains("producto") || lower.contains("recomienda") || lower.contains("recomendar") || lower.contains("recomendación")
                || lower.contains("iphone") || lower.contains("samsung") || lower.contains("xiaomi") || lower.contains("audifon") || lower.contains("audífon")
                || lower.contains("teclado") || lower.contains("mouse") || lower.contains("laptop") || lower.contains("celular") || lower.contains("smartphone")) {
            String query = extractQuery(text);
            Integer budget = extractBudgetSoles(text);
            List<Producto> baseResults = productService.searchByKeywordsSmart(query);
            List<Producto> results = baseResults;
            if (budget != null && !baseResults.isEmpty()) {
                List<Producto> filtered = baseResults.stream()
                        .filter(p -> p.getVariants() != null && p.getVariants().stream().anyMatch(v -> v.getPrecio() != null && v.getPrecio().doubleValue() <= budget))
                        .toList();
                if (!filtered.isEmpty()) {
                    results = filtered;
                }
            }
            if (!results.isEmpty()) {
                List<ProductSuggestionDto> suggestions = results.stream().limit(3).map(this::toSuggestion).toList();
                String listContext = buildProductsContext(results, budget);
                String instruction = "Recomienda solo de la lista proporcionada (no inventes modelos). Si hay presupuesto, prioriza <= presupuesto.";
                String reply = generateAnswer(instruction, listContext, message);
                return new ChatResponse(reply, suggestions);
            }
            String reply = generateAnswer("El usuario busca productos.", "No encontré coincidencias. Pide otra palabra (marca/modelo) o categoría.", message);
            return new ChatResponse(reply, null);
        }

        // Otros casos: reutiliza handleMessage para el reply
        String reply = handleMessage(message);
        return new ChatResponse(reply, null);
    }

    private String extractOrderNumber(String text) {
        // Usamos Regex (como en extractCity) para buscar el patrón específico que
        // coincide con tus pedidos (ej. "TZ-029DC6DA")

        // (?i) -> Ignora mayúsculas/minúsculas
        // (TZ-[A-Za-z0-9-]+) -> Busca "TZ-" seguido de letras, números o guiones
        var m = java.util.regex.Pattern.compile("(?i)(TZ-[A-Za-z0-9-]+)")
                .matcher(text);

        if (m.find()) {
            // m.group(1) es el texto capturado por los paréntesis
            return m.group(1).toUpperCase(Locale.ROOT);
        }

        return ""; // Devuelve vacío si no encontró el patrón
    }

    private String extractCity(String text) {
        var m = java.util.regex.Pattern.compile("(?i)(?:a|para)\\s+([A-Za-zÁÉÍÓÚáéíóúñÑ\\s]+)").matcher(text);
        return m.find() ? m.group(1).trim() : "";
    }

    private String extractQuery(String text) {
        String cleaned = text.toLowerCase(Locale.ROOT)
                .replaceAll("[¿?]", " ")
                .replaceAll("[\\.,]", " ")
                .replaceAll("\n", " ")
                .trim();

        // Elimina verbos comunes y stopwords
        String[] stop = new String[]{
                "buscar", "busca", "busco", "recomienda", "recomendar", "recomendación", "producto", "productos",
                "un", "una", "unos", "unas", "el", "la", "los", "las", "de", "del", "para", "por", "en", "barato", "barata",
                "quiero", "necesito", "me", "algo", "que", "con"
        };
        for (String s : stop) {
            cleaned = cleaned.replace(" " + s + " ", " ");
            if (cleaned.startsWith(s + " ")) cleaned = cleaned.substring(s.length() + 1);
            if (cleaned.endsWith(" " + s)) cleaned = cleaned.substring(0, cleaned.length() - s.length() - 1);
        }
        cleaned = cleaned.replaceAll("\\s+", " ").trim();
        if (cleaned.isBlank()) {
            // fallback: intenta encontrar última palabra relevante del texto original
            var m = java.util.regex.Pattern.compile("(?i)(?:buscar|producto|recomienda|recomendar)\\s+(.*)").matcher(text);
            return m.find() ? m.group(1).trim() : text;
        }
        return cleaned;
    }

    private Integer extractBudgetSoles(String text) {
        // Detecta patrones como "5000 soles", "S/ 1200", "s/. 2,500", "2500 s"
        var m = java.util.regex.Pattern.compile("(?i)(s/?\\.?\\s*)?([0-9]{3,}(?:[\\.,][0-9]{3})*)(?:\\s*(sol|soles|s))?").matcher(text);
        if (m.find()) {
            String num = m.group(2).replace(".", "").replace(",", "");
            try { return Integer.parseInt(num); } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    private String buildProductsContext(List<Producto> products, Integer budget) {
        StringBuilder sb = new StringBuilder();
        if (budget != null) {
            sb.append("Presupuesto del usuario: S/ ").append(budget).append('\n');
        }
        sb.append("Productos disponibles en inventario (elige solo de aquí):\n");
        int count = 0;
        for (Producto p : products) {
            if (p == null) continue;
            String name = p.getNombre() == null ? "(sin nombre)" : p.getNombre();
            Double minPrice = null;
            if (p.getVariants() != null) {
                minPrice = p.getVariants().stream()
                        .filter(v -> v.getPrecio() != null)
                        .map(v -> v.getPrecio().doubleValue())
                        .min(Double::compareTo)
                        .orElse(null);
            }
            sb.append("- ").append(name);
            if (minPrice != null) sb.append(" | precio desde: S/ ").append(minPrice.intValue());
            sb.append('\n');
            if (++count >= 5) break;
        }
        return sb.toString();
    }

    private String generateAnswer(String instruction, String context, String userMessage) {
        try {
            String prompt = String.format("""
                %s
                Contexto (información de la tienda o resultado de consultas):
                %s

                Responde en una sola respuesta breve y clara, en español, adaptada al usuario.
                Pregunta datos que falten de forma concreta solo si es necesario.

                Mensaje del usuario: "%s"
            """, instruction, context, userMessage);

            GenerativeModel model = new GenerativeModel(modelName, vertexAI);
            GenerateContentResponse response = model.generateContent(prompt);
            String text = ResponseHandler.getText(response);
            return (text == null || text.isBlank()) ? context : text.trim();
        } catch (Exception e) {
            return context; // fallback seguro si falla la IA
        }
    }

    private ProductSuggestionDto toSuggestion(Producto p) {
        Long id = p.getId();
        String name = p.getNombre() == null ? "(sin nombre)" : p.getNombre();
        String image = null;
        String priceStr = null;
        if (p.getVariants() != null && !p.getVariants().isEmpty()) {
            ProductoVariante v = p.getVariants().stream()
                    .filter(x -> x.getPrecio() != null || x.getImagenPrincipalUrl() != null)
                    .findFirst().orElse(p.getVariants().get(0));
            if (v.getImagenPrincipalUrl() != null) {
                image = "/uploads/" + v.getImagenPrincipalUrl();
            }
            if (v.getPrecio() != null) {
                priceStr = "S/ " + v.getPrecio().intValue();
            }
        }
        return new ProductSuggestionDto(id, name, priceStr, image);
    }
}


