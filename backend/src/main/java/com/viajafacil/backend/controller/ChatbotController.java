package com.viajafacil.backend.controller;

// Importa la anotación @Autowired para inyectar dependencias automáticamente
import org.springframework.beans.factory.annotation.Autowired;

// Importa las anotaciones necesarias para crear controladores REST y definir rutas :(RequestMapping, GetMapping, PostMapping)
import org.springframework.web.bind.annotation.*;

// Importa el servicio que se comunica con la IA (que seria el OpenAI)
import com.viajafacil.backend.services.OpenAIService;

// Importa el modelo que representa una consulta del chat (entidad usada en la BD)
import com.viajafacil.backend.model.ConsultaChat;

// Importa el repositorio que permite guardar y consultar los mensajes en la base de datos
import com.viajafacil.backend.repository.ConsultaChatRepository;

// Importa clases de utilidades de Java para manejar listas y mapas (colecciones de datos)
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// ===== CONTROLADOR =====

@RestController  // Indica que esta clase responderá a solicitudes HTTP y devolverá datos en formato JSON
@RequestMapping("/chatbot")  // Define la ruta base del controlador: todas las rutas empiezan con /chatbot
@CrossOrigin(origins = "*")  // Permite que cualquier frontend (Angular, React, etc.) pueda conectarse al backend
public class ChatbotController {

    @Autowired
    private OpenAIService openAIService;
    // Inyecta el servicio encargado de comunicarse con la API de IA

    @Autowired
    private ConsultaChatRepository consultaChatRepository;
    // Inyecta el repositorio para guardar y consultar los mensajes en la base de datos

    // ===== ENDPOINT PARA HACER UNA PREGUNTA A LA IA =====
    @PostMapping("/preguntar")  // Define la ruta POST: /chatbot/preguntar
    public Map<String, Object> preguntarIA(
            @RequestParam Long usuarioId,   // Recibe el ID del usuario como parámetro
            @RequestParam String mensaje) { // Recibe el mensaje del usuario desde el frontend

        // Llama al servicio de IA y obtiene la respuesta generada
        String respuestaIA = openAIService.generarPlanViaje(mensaje);

        // Crea una nueva instancia del modelo ConsultaChat para guardar la interacción
        ConsultaChat consulta = new ConsultaChat();
        consulta.setUsuarioId(usuarioId);
        consulta.setMensajeUsuario(mensaje);
        consulta.setRespuestaIA(respuestaIA);

        // Guarda la consulta en la base de datos
        consultaChatRepository.save(consulta);

        // Crea un mapa con los datos de respuesta que se enviarán al frontend
        Map<String, Object> response = new HashMap<>();
        response.put("usuarioId", usuarioId);
        response.put("mensajeUsuario", mensaje);
        response.put("respuestaIA", respuestaIA);
        response.put("fecha", consulta.getFecha());  // Añade la fecha registrada en la BD

        // Retorna la respuesta como JSON
        return response;
    }

    // ===== ENDPOINT PARA OBTENER EL HISTORIAL DE UN USUARIO =====
    @GetMapping("/historial/{usuarioId}")  // Define la ruta GET: /chatbot/historial/{usuarioId}
    public List<ConsultaChat> historial(@PathVariable Long usuarioId) {
        // Busca todas las consultas del usuario en la base de datos
        return consultaChatRepository.findByUsuarioId(usuarioId);
    }
}
