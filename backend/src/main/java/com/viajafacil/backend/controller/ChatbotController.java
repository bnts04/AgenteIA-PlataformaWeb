package com.viajafacil.backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.viajafacil.backend.services.OpenAIService;
import com.viajafacil.backend.model.ConsultaChat;
import com.viajafacil.backend.repository.ConsultaChatRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chatbot")
@CrossOrigin(origins = "*")
public class ChatbotController {

    @Autowired
    private OpenAIService openAIService;

    @Autowired
    private ConsultaChatRepository consultaChatRepository;

    @PostMapping("/preguntar")
    public Map<String, Object> preguntarIA(
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) String mensaje,
            @RequestBody(required = false) JsonNode body) {

        // Si el cuerpo viene en formato JSON (caso modificación)
        if (body != null && body.has("mensaje")) {
            mensaje = body.get("mensaje").asText();
        }

        if (mensaje == null || mensaje.isEmpty()) {
            throw new IllegalArgumentException("El mensaje no puede estar vacío.");
        }

        if (usuarioId == null) {
            usuarioId = 1L;
        }

        String respuestaIA;

        // CASO 1: Modificación de itinerario existente
        if (body != null && body.has("itinerarioPrevio")) {
            JsonNode itinerarioPrevio = body.get("itinerarioPrevio");
            String instrucciones = body.has("instrucciones") ? body.get("instrucciones").asText() : "";

            // Extraer el destino del itinerario previo
            String destinoActual = itinerarioPrevio.has("destino")
                    ? itinerarioPrevio.get("destino").asText()
                    : "desconocido";

            //  PROMPT MEJORADO
            String prompt = String.format("""
                    ⚠️ INSTRUCCIÓN CRÍTICA: ESTO ES UNA MODIFICACIÓN DE UN ITINERARIO EXISTENTE.
                    
                    DESTINO ACTUAL QUE DEBES MANTENER: %s
                    NO CAMBIES EL DESTINO BAJO NINGUNA CIRCUNSTANCIA.
                    
                    ITINERARIO ACTUAL (JSON):
                    %s
                    
                    SOLICITUD DE MODIFICACIÓN DEL USUARIO:
                    "%s"
                    
                    INSTRUCCIONES OBLIGATORIAS:
                    1. El destino DEBE permanecer como: %s
                    2. Mantén la misma cantidad de días que tiene el itinerario actual
                    3. Mantén la misma estructura JSON (planes, días, etc.)
                    4. SOLO modifica el aspecto específico mencionado por el usuario:
                       - Si menciona "comida vegetariana" → cambia SOLO el campo "comida" en cada día
                       - Si menciona "hotel económico" → cambia SOLO el campo "alojamiento"
                       - Si menciona "actividades culturales" → cambia SOLO el campo "actividades"
                    5. Actualiza los costos si es necesario según los cambios
                    6. Mantén las conversiones de moneda (monedaDestino, USD, PEN)
                    
                    %s
                    
                    RESPONDE ÚNICAMENTE CON EL JSON DEL ITINERARIO MODIFICADO.
                    EL DESTINO DEBE SER: %s
                    """,
                    destinoActual,
                    itinerarioPrevio.toPrettyString(),
                    mensaje,
                    destinoActual,
                    instrucciones,
                    destinoActual
            );

            System.out.println("=== PROMPT DE MODIFICACIÓN ===");
            System.out.println("Destino a mantener: " + destinoActual);
            System.out.println("Solicitud del usuario: " + mensaje);

            respuestaIA = openAIService.generarPlanViaje(prompt);
        }
        //  CASO 2: Nueva consulta (crear itinerario desde cero)
        else {
            respuestaIA = openAIService.generarPlanViaje(mensaje);
        }

        // Guardar en la base de datos
        ConsultaChat consulta = new ConsultaChat();
        consulta.setUsuarioId(usuarioId);
        consulta.setMensajeUsuario(mensaje);
        consulta.setRespuestaIA(respuestaIA);
        consultaChatRepository.save(consulta);

        // Armar respuesta al frontend
        Map<String, Object> response = new HashMap<>();
        response.put("usuarioId", usuarioId);
        response.put("mensajeUsuario", mensaje);
        response.put("respuestaIA", respuestaIA);
        response.put("fecha", consulta.getFecha());

        return response;
    }

    // Ver historial de consultas de un usuario
    @GetMapping("/historial/{usuarioId}")
    public List<ConsultaChat> historial(@PathVariable Long usuarioId) {
        return consultaChatRepository.findByUsuarioIdOrderByFechaAsc(usuarioId);
    }

}
