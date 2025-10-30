package com.techzone.peru.service;

import org.springframework.stereotype.Service;

@Service
public class InformacionService {

    /**
     * Devuelve un texto estático con los métodos de pago aceptados.
     * @return String con la información de pago.
     */
    public String getMetodosDePago() {
        // En el futuro, esta información podría venir de una base de datos.
        // Por ahora, es un texto fijo.
        return "¡Claro! Aceptamos varios métodos de pago: Tarjetas de Crédito/Débito (Visa, Mastercard), Yape y Plin. ¡Paga como prefieras!";
    }

    /**
     * Devuelve un texto estático con la política de garantías y devoluciones.
     * @return String con la información de la política.
     */
    public String getPoliticaGarantias() {
        return "Nuestra política de garantía cubre defectos de fábrica por 12 meses. Para devoluciones por insatisfacción, tienes 7 días desde la recepción de tu compra, siempre que el producto y su empaque estén en perfecto estado. Puedes iniciar el proceso desde tu cuenta en nuestra web.";
    }

    /**
     * Simula el cálculo del costo de envío para una ciudad dada.
     * @param ciudad El nombre de la ciudad.
     * @return String con el costo de envío o un mensaje si no hay cobertura.
     */
    public String getCostoEnvio(String ciudad) {
        if (ciudad == null || ciudad.isBlank()) {
            return "Por favor, dime a qué ciudad es el envío para poder calcular el costo.";
        }

        // Simulamos una tabla de costos de envío
        java.util.Map<String, Double> costosPorCiudad = new java.util.HashMap<>();
        costosPorCiudad.put("LIMA", 10.0);
        costosPorCiudad.put("AREQUIPA", 20.0);
        costosPorCiudad.put("ICA", 15.0);
        costosPorCiudad.put("CHICLAYO", 25.0);

        // Normalizamos el input para que coincida con nuestras claves (mayúsculas)
        String ciudadNormalizada = ciudad.toUpperCase();

        if (costosPorCiudad.containsKey(ciudadNormalizada)) {
            Double costo = costosPorCiudad.get(ciudadNormalizada);
            return String.format("El costo de envío para %s es de S/ %.2f.", ciudad, costo);
        } else {
            return String.format("Lo siento, actualmente no tenemos cobertura de envío para %s.", ciudad);
        }
    }

}
