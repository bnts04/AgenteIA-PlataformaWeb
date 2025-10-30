package com.viajafacil.backend.services;

import com.viajafacil.backend.model.ConsultaChat;
import com.viajafacil.backend.repository.ConsultaChatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatInteractivoService {

    @Autowired
    private OpenAIService openAIService;

    @Autowired
    private ConsultaChatRepository chatRepository;

    public ConsultaChat procesarMensaje(Long usuarioId, String mensaje) {
        // Llamar a la IA
        String respuesta = openAIService.generarPlanViaje(mensaje);

        // Guardar en base de datos
        ConsultaChat consulta = new ConsultaChat();
        consulta.setUsuarioId(usuarioId);
        consulta.setMensajeUsuario(mensaje);
        consulta.setRespuestaIA(respuesta);

        return chatRepository.save(consulta);
    }

    public List<ConsultaChat> obtenerHistorial(Long usuarioId) {
        return chatRepository.findByUsuarioIdOrderByFechaAsc(usuarioId);
    }

    public ConsultaChat modificarItinerario(Long usuarioId, String instruccion) {
        // Obtener la última conversación guardada
        List<ConsultaChat> historial = chatRepository.findByUsuarioIdOrderByFechaAsc(usuarioId);
        if (historial.isEmpty()) {
            ConsultaChat nueva = new ConsultaChat();
            nueva.setUsuarioId(usuarioId);
            nueva.setMensajeUsuario(instruccion);
            nueva.setRespuestaIA("No tienes un itinerario previo para modificar. Por favor genera uno primero.");
            return chatRepository.save(nueva);
        }

        ConsultaChat ultima = historial.get(historial.size() - 1);

        // Instrucción que se envía a OpenAI: modificar el plan previo
        String promptModificado = "Modifica este itinerario según la instrucción del usuario. " +
                "Itinerario actual: " + ultima.getRespuestaIA() +
                " | Instrucción del usuario: " + instruccion;

        String nuevaRespuesta = openAIService.generarPlanViaje(promptModificado);

        ConsultaChat modificada = new ConsultaChat();
        modificada.setUsuarioId(usuarioId);
        modificada.setMensajeUsuario(instruccion);
        modificada.setRespuestaIA(nuevaRespuesta);
        return chatRepository.save(modificada);
    }


}