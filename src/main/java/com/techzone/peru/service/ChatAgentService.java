package com.techzone.peru.service;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import com.techzone.peru.model.dto.ChatResponse;
import com.techzone.peru.model.dto.ProductSuggestionDto;
import com.techzone.peru.model.entity.ConversacionChatbot; // <-- Importar
import com.techzone.peru.model.entity.Producto;
import com.techzone.peru.model.entity.ProductoVariante;
import com.techzone.peru.repository.ConversacionChatbotRepository; // <-- Importar
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime; // <-- Importar
import java.util.List;
import java.util.Locale;

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
    @Autowired
    private ConversacionChatbotRepository conversacionRepository; // <-- Inyectar repositorio

    @Value("${vertex.model.name:gemini-2.5-pro}")
    private String modelName;

    // Método auxiliar para guardar en BD
    private void guardarConversacion(String sessionId, String mensajeUsuario, String respuestaBot, String intencion) {
        try {
            ConversacionChatbot chat = new ConversacionChatbot();
            chat.setSessionId(sessionId);
            chat.setMensajeUsuario(mensajeUsuario);
            chat.setRespuestaBot(respuestaBot);
            chat.setIntencion(intencion);
            chat.setFecha(OffsetDateTime.now());
            conversacionRepository.save(chat);
        } catch (Exception e) {
            System.err.println("Error al guardar conversación: " + e.getMessage());
        }
    }

    // --- MËTODO ACTUALIZADO ---
    // Ahora recibe sessionId para poder guardar el historial
    public ChatResponse handleMessageWithProducts(String message, String sessionId) {
        if (message == null || message.isBlank()) {
            return new ChatResponse("¿Podrías escribir tu consulta?", null);
        }

        String text = message.trim();
        String lower = text.toLowerCase(Locale.ROOT);
        String reply = "";
        List<ProductSuggestionDto> suggestions = null;
        String intencionDetectada = "general";

        // 1. Detección de Productos
        if (lower.contains("buscar") || lower.contains("producto") || lower.contains("recomienda")
                || lower.contains("precio") || lower.contains("celular") || lower.contains("laptop")
                || lower.contains("iphone") || lower.contains("samsung")) {

            intencionDetectada = "busqueda_producto";
            String query = extractQuery(text);
            Integer budget = extractBudgetSoles(text);
            List<Producto> baseResults = productService.searchByKeywordsSmart(query);

            // Lógica de filtrado
            List<Producto> results = baseResults;
            if (budget != null && !baseResults.isEmpty()) {
                List<Producto> filtered = baseResults.stream()
                        .filter(p -> p.getVariants() != null && p.getVariants().stream().anyMatch(v -> v.getPrecio() != null && v.getPrecio().doubleValue() <= budget))
                        .toList();
                if (!filtered.isEmpty()) results = filtered;
            }

            if (!results.isEmpty()) {
                suggestions = results.stream().limit(3).map(this::toSuggestion).toList();
                String listContext = buildProductsContext(results, budget);
                String instruction = "Recomienda de esta lista. Si hay presupuesto, respétalo.";
                reply = generateAnswer(instruction, listContext, message);
            } else {
                reply = generateAnswer("El usuario busca productos.", "No encontré coincidencias. Pide otra palabra clave.", message);
            }

            // 2. Otras intenciones (Pedido, Pago, Envío)
        } else if (lower.contains("pedido") || lower.contains("orden")) {
            intencionDetectada = "consulta_pedido";
            String numero = extractOrderNumber(text);
            if (numero.isBlank()) {
                reply = "Por favor, dime el número de tu pedido (ej. TZ-12345).";
            } else {
                String estado = pedidoService.consultarEstadoPedido(numero);
                reply = generateAnswer("El usuario consulta pedido.", estado, message);
            }
        } else if (lower.contains("pago") || lower.contains("tarjeta")) {
            intencionDetectada = "metodos_pago";
            reply = generateAnswer("Consulta pagos", informacionService.getMetodosDePago(), message);
        } else if (lower.contains("envio") || lower.contains("costo")) {
            intencionDetectada = "costo_envio";
            String ciudad = extractCity(text);
            String context = ciudad.isBlank() ? "Pide la ciudad para calcular envío." : informacionService.getCostoEnvio(ciudad);
            reply = generateAnswer("Consulta envío", context, message);
        } else {
            // Fallback
            reply = generateAnswer("Ayuda general.", "Soy el asistente virtual de TechZone.", message);
        }

        // --- ¡GUARDAMOS LA CONVERSACIÓN! ---
        guardarConversacion(sessionId, message, reply, intencionDetectada);

        return new ChatResponse(reply, suggestions);
    }

    // --- Métodos Privados (Iguales que antes, pero asegúrate de incluir el Regex corregido) ---

    private String extractOrderNumber(String text) {
        var m = java.util.regex.Pattern.compile("(?i)(TZ-[A-Za-z0-9-]+)").matcher(text);
        return m.find() ? m.group(1).toUpperCase(Locale.ROOT) : "";
    }

    private String extractCity(String text) {
        var m = java.util.regex.Pattern.compile("(?i)(?:a|para)\\s+([A-Za-zÁÉÍÓÚáéíóúñÑ\\s]+)").matcher(text);
        return m.find() ? m.group(1).trim() : "";
    }

    private String extractQuery(String text) {
        String cleaned = text.toLowerCase(Locale.ROOT).replaceAll("[¿?.,]", " ").trim();
        // (Tu lógica de stopwords aquí...)
        return cleaned;
    }

    private Integer extractBudgetSoles(String text) {
        var m = java.util.regex.Pattern.compile("(?i)(s/?\\.?\\s*)?([0-9]{3,}(?:[\\.,][0-9]{3})*)(?:\\s*(sol|soles|s))?").matcher(text);
        if (m.find()) {
            String num = m.group(2).replace(".", "").replace(",", "");
            try { return Integer.parseInt(num); } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    private String buildProductsContext(List<Producto> products, Integer budget) {
        StringBuilder sb = new StringBuilder();
        if (budget != null) sb.append("Presupuesto: S/ ").append(budget).append('\n');
        for (Producto p : products) {
            sb.append("- ").append(p.getNombre()).append('\n');
        }
        return sb.toString();
    }

    private String generateAnswer(String instruction, String context, String userMessage) {
        try {
            String prompt = String.format("""
                %s
                Contexto: %s
                Usuario: "%s"
                Responde brevemente en español.
            """, instruction, context, userMessage);
            GenerativeModel model = new GenerativeModel(modelName, vertexAI);
            GenerateContentResponse response = model.generateContent(prompt);
            return ResponseHandler.getText(response).trim();
        } catch (Exception e) {
            return context;
        }
    }

    private ProductSuggestionDto toSuggestion(Producto p) {
        // Lógica simple para extraer imagen y precio
        String image = null;
        String price = null;
        if(p.getVariants() != null && !p.getVariants().isEmpty()) {
            image = "/uploads/" + p.getVariants().get(0).getImagenPrincipalUrl();
            price = "S/ " + p.getVariants().get(0).getPrecio();
        }
        return new ProductSuggestionDto(p.getId(), p.getNombre(), price, image);
    }
}