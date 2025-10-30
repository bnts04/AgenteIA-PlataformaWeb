package com.viajafacil.backend.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class OpenAIService {

    @Value("${groq.api.key}")
    private String groqApiKey;

    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    //  método interactivo para el chatbot
    public String procesarConversacion(List<Map<String, String>> historial) {
        System.out.println("DEBUG: Iniciando conversación Groq (modelo 8B-instant). Clave configurada: " +
                (groqApiKey != null && !groqApiKey.trim().isEmpty() ? "SÍ" : "NO"));

        if (groqApiKey == null || groqApiKey.trim().isEmpty()) {
            return "{\"error\": \"Clave de API de Groq no configurada en application.properties.\"}";
        }

        // headers (encabezados)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(groqApiKey.trim());

        // === Mensaje de sistema===
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content",
                "Eres un experto en planificación de viajes para familias multigeneracionales, incluyendo niños, adultos y adultos mayores (abuelos). " +
                        "Responde EXCLUSIVAMENTE con un JSON válido y completo en español, sin texto extra ni markdown. La estructura debe ser:\n" +
                        "{\n" +
                        "  \"destino\": \"nombre del destino\",\n" +
                        "  \"duracion\": \"X días\",\n" +
                        "  \"presupuesto\": \"bajo/medio/alto\",\n" +
                        "  \"planes\": [\n" +
                        "    {\n" +
                        "      \"dia\": 1,\n" +
                        "      \"actividades\": [\"Actividades inclusivas y adecuadas para niños, adultos y adultos mayores\"],\n" +
                        "      \"alojamiento\": \"alojamiento cómodo y accesible\",\n" +
                        "      \"comida\": [\"platos típicos del destino\"],\n" +
                        "      \"transporte\": \"medios cómodos para todas las edades\",\n" +
                        "      \"costoEstimado\": {\n" +
                        "        \"monedaDestino\": \"100 EUR\",\n" +
                        "        \"USD\": \"110 USD\",\n" +
                        "        \"PEN\": \"420 PEN\"\n" +
                        "      }\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"totalCosto\": {\n" +
                        "    \"monedaDestino\": \"suma aproximada en moneda local\",\n" +
                        "    \"USD\": \"suma aproximada en USD\",\n" +
                        "    \"PEN\": \"suma aproximada en PEN\"\n" +
                        "  }\n" +
                        "}\n" +
                        "Usa tipos de cambio aproximados:\n" +
                        "- 1 EUR = 1.1 USD\n" +
                        "- 1 EUR = 4.2 PEN\n" +
                        "- 1 USD = 4.2 PEN\n" +
                        "Si la moneda local no es EUR ni USD, conviértela primero a USD y luego a PEN.\n" +
                        "Varía el alojamiento y la comida según el destino, siempre usando opciones locales y familiares."
        );

        // === Combinar historial + systemMessage ===
        List<Map<String, String>> mensajes = new ArrayList<>();
        mensajes.add(systemMessage);
        mensajes.addAll(historial); // El historial lo envía el frontend/chat

        // === Request Body ===
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "llama-3.1-8b-instant");
        requestBody.put("messages", mensajes);
        requestBody.put("max_tokens", 1500);
        requestBody.put("temperature", 0.7);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(GROQ_URL, entity, String.class);
            System.out.println("DEBUG: Status Code de Groq: " + response.getStatusCode());

            String fullResponse = response.getBody();
            if (fullResponse == null) return "{\"error\": \"Respuesta vacía de Groq.\"}";

            JsonNode root = objectMapper.readTree(fullResponse);
            JsonNode choices = root.path("choices");

            if (choices.isArray() && !choices.isEmpty()) {
                JsonNode contentNode = choices.get(0).path("message").path("content");
                if (!contentNode.isMissingNode()) {
                    return contentNode.asText().trim();
                }
            }

            return "{\"error\": \"Respuesta inválida de Groq.\"}";

        } catch (Exception e) {
            e.printStackTrace();
            try {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Error de conexión o parsing: " + e.getMessage());
                return objectMapper.writeValueAsString(error);
            } catch (Exception inner) {
                return "{\"error\": \"Error interno al generar respuesta.\"}";
            }
        }
    }

    //  método actual para consultas individuales
    public String generarPlanViaje(String consultaUsuario) {
        System.out.println("DEBUG: Consulta individual -> " + consultaUsuario);
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", consultaUsuario);

        List<Map<String, String>> historial = new ArrayList<>();
        historial.add(userMessage);
        return procesarConversacion(historial);
    }

    // método original para extraer preferencias del texto
    private Map<String, String> extraerPreferenciasUsuario(String mensaje) {
        Map<String, String> preferencias = new LinkedHashMap<>();
        mensaje = mensaje.toLowerCase(Locale.ROOT);

        Pattern pRestaurante = Pattern.compile("cenar en ([a-zA-ZÀ-ÿ0-9\\s']+)", Pattern.CASE_INSENSITIVE);
        Matcher mRestaurante = pRestaurante.matcher(mensaje);
        if (mRestaurante.find()) {
            preferencias.put("Restaurante preferido", mRestaurante.group(1).trim());
        }

        if (mensaje.contains("hotel")) {
            preferencias.put("Tipo de alojamiento", "hotel");
        } else if (mensaje.contains("airbnb")) {
            preferencias.put("Tipo de alojamiento", "Airbnb");
        } else if (mensaje.contains("resort")) {
            preferencias.put("Tipo de alojamiento", "resort");
        }

        Pattern pComida = Pattern.compile("comer (?:platos|comida|en) ([a-zA-ZÀ-ÿ\\s']+)", Pattern.CASE_INSENSITIVE);
        Matcher mComida = pComida.matcher(mensaje);
        if (mComida.find()) {
            preferencias.put("Tipo de comida", mComida.group(1).trim());
        }

        if (mensaje.contains("auto")) {
            preferencias.put("Transporte preferido", "auto");
        } else if (mensaje.contains("bus")) {
            preferencias.put("Transporte preferido", "bus");
        } else if (mensaje.contains("tren")) {
            preferencias.put("Transporte preferido", "tren");
        }

        return preferencias;
    }
}
