package com.viajafacil.backend.controller;

import com.fasterxml.jackson.annotation.JsonFormat; // Formatea LocalTime en JSON (HH:mm)
import com.viajafacil.backend.model.Itinerario;   // Entidad Itinerario (BD)
import com.viajafacil.backend.model.Paquete;      // Entidad Paquete (BD)
import com.viajafacil.backend.repository.ItinerarioRepository; // DAO Itinerario
import com.viajafacil.backend.repository.PaqueteRepository;    // DAO Paquete
import org.springframework.beans.factory.annotation.Autowired;  // Inyección de dependencias
import org.springframework.http.HttpStatus;                      // Códigos HTTP
import org.springframework.http.ResponseEntity;                 // Respuestas HTTP
import org.springframework.web.bind.annotation.*;               // Anotaciones REST

import java.time.LocalTime;
import java.util.*;

@RestController                     // Marca la clase como controlador REST (retorna JSON)
@RequestMapping("/itinerarios")     // Prefijo/base para todos los endpoints de esta clase
@CrossOrigin(origins = "*")         // Habilita CORS para cualquier origen (útil para frontend)
public class ItinerarioController {

    @Autowired private ItinerarioRepository itinerarioRepository; // Inyecta el DAO osea el repository de Itinerario
    @Autowired private PaqueteRepository paqueteRepository;       // Inyecta el DAO de Paquete

    // ---------- DTOs de entrada ----------
    public static class CrearItinerarioReq {
        public Long paqueteId;             // requerido: ID del paquete dueño del ítem
        public Integer dia;                // requerido: día del itinerario (1, 2, ...)
        public String actividad;           // requerido: nombre/título de la actividad
        public String descripcion;         // opcional: detalle de la actividad

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
        public LocalTime horaInicio;       // opcional , lo pusimos por si pone la hora de inicio

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
        public LocalTime horaFin;          // opcional

        public Integer orden; // opcional: posición manual (si no llega, se calcula el orden de llegada)
    }

    public static class ActualizarItinerarioReq {
        public Long paqueteId;             // opcional: mover a otro paquete
        public Integer dia;                // opcional: cambiar de día
        public String actividad;           // opcional: renombrar actividad
        public String descripcion;         // opcional: cambiar descripción

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
        public LocalTime horaInicio;       // opcional

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
        public LocalTime horaFin;          // opcional

        public Integer orden; // esto lo puse para cambiarlo
    }

    @GetMapping // GET /itinerarios
    public List<Itinerario> listarTodos() {
        return itinerarioRepository.findAll(); // Retorna todos los itinerarios
    }

    // ---------- CREATE ----------
    @PostMapping // POST /itinerarios
    public Map<String, Object> crear(@RequestBody CrearItinerarioReq req) { // Recibe JSON con datos
        Map<String, Object> resp = new LinkedHashMap<>(); // Mapa de respuesta (mantiene orden de inserción)

        // Valida requeridos: paqueteId, dia, actividad
        if (req == null || req.paqueteId == null || req.dia == null ||
                req.actividad == null || req.actividad.isBlank()) {
            resp.put("status", "error");
            resp.put("message", "paqueteId, dia y actividad son obligatorios");
            return resp; // Corta ejecución si faltan datos clave
        }

        // Verifica que el paquete exista
        var paqueteOpt = paqueteRepository.findById(req.paqueteId);
        if (paqueteOpt.isEmpty()) {
            resp.put("status", "error");
            resp.put("message", "Paquete no encontrado");
            return resp;// Corta si el paquete no existe
        }

        var paquete = paqueteOpt.get();

        Itinerario it = new Itinerario();
        it.setPaquete(paquete);                     // relación con Paquete
        it.setDia(req.dia);                         // día del itinerario
        it.setActividad(req.actividad.trim());      // actividad (limpia espacios extremos)
        it.setDescripcion(req.descripcion);         // descripción (puede ser null)
        it.setHora_inicio(req.horaInicio);          // hora inicio (puede ser null)
        it.setHora_fin(req.horaFin);                // hora fin (puede ser null)

        // Si no envían orden y tampoco hay hora, automaticamente lo  ordena
        if (req.orden != null) {
            it.setOrden(req.orden);// usa el valor proporcionado
        } else if (req.horaInicio == null) {
            Integer max = itinerarioRepository.maxOrdenEnDia(req.paqueteId, req.dia);// último orden usado
            it.setOrden(max + 1); // coloca al final
        }
        // Si hay horaInicio y no hay orden, se deja orden como esté (podrás ordenar por hora en consulta/frontend)

        Itinerario guardado = itinerarioRepository.save(it);// Persiste el nuevo registro
        // Construye respuesta estándar        resp.put("status", "success");
        resp.put("message", "Actividad registrada en el itinerario");
        resp.put("itinerario", guardado);
        return resp; // Devuelve JSON al cliente
    }

    // -------- LISTAR por paquete (y opcional por día) --------
    @GetMapping("/paquete/{idPaquete}")
    public List<Itinerario> listarPorPaquete(
            @PathVariable Long idPaquete, //ID del paquete
            @RequestParam(required = false) Integer dia // filtro por día, esto es opcional
    ) {
        return itinerarioRepository.listarPorPaqueteYDiaOrdenado(idPaquete, dia);
    }

    // -------- GET por id --------
    @GetMapping("/{id}")
    public Map<String, Object> obtenerPorId(@PathVariable Long id) {
        Map<String, Object> resp = new LinkedHashMap<>();
        var opt = itinerarioRepository.findById(id); //esto lo busca por ID
        if (opt.isEmpty()) {
            resp.put("status", "error");
            resp.put("message", "Itinerario no encontrado");
            return resp;
        }
        resp.put("status", "success");
        resp.put("itinerario", opt.get());
        return resp;
    }

    // -------- UPDATE --------
    @PutMapping("/{id}")
    public Map<String, Object> actualizar(@PathVariable Long id,
                                          @RequestBody ActualizarItinerarioReq req) {
        Map<String, Object> resp = new LinkedHashMap<>();
        var opt = itinerarioRepository.findById(id); //aqui verifica la existencia
        if (opt.isEmpty()) {
            resp.put("status", "error");
            resp.put("message", "Itinerario no encontrado");
            return resp;
        }

        Itinerario it = opt.get();// Entidad a actualizar

        // Si se solicita cambiar de paquete, valida nuevo paquete
        if (req.paqueteId != null && !req.paqueteId.equals(
                it.getPaquete() != null ? it.getPaquete().getId_paquete() : null)) {
            var p = paqueteRepository.findById(req.paqueteId);
            if (p.isEmpty()) {
                resp.put("status", "error");
                resp.put("message", "Paquete no encontrado");
                return resp;
            }
            it.setPaquete(p.get()); //aplica al nuevo paquete
        }

        // Aplica cambios solo si vienen informados
        if (req.actividad != null && !req.actividad.isBlank()) it.setActividad(req.actividad.trim());
        if (req.descripcion != null) it.setDescripcion(req.descripcion);
        if (req.horaInicio != null) it.setHora_inicio(req.horaInicio);
        if (req.horaFin != null) it.setHora_fin(req.horaFin);
        if (req.orden != null) it.setOrden(req.orden);

        Itinerario actualizado = itinerarioRepository.save(it);
        // Respuesta estándar
        resp.put("status", "success");
        resp.put("message", "Itinerario actualizado");
        resp.put("itinerario", actualizado);
        return resp;
    }

    // -------- DELETE --------
    @DeleteMapping("/{id}")
    public Map<String, Object> eliminar(@PathVariable Long id) {
        Map<String, Object> resp = new LinkedHashMap<>();
        var opt = itinerarioRepository.findById(id);// Verifica existencia
        if (opt.isEmpty()) {
            resp.put("status", "error");
            resp.put("message", "Itinerario no encontrado");
            return resp;// No existe el recurso
        }
        itinerarioRepository.delete(opt.get());
        resp.put("status", "success");
        resp.put("message", "Itinerario eliminado correctamente");
        return resp;
    }

    @PutMapping("/{id}/lugar")
    public ResponseEntity<?> actualizarLugar(
            @PathVariable Long id,// ID del itinerario
            @RequestBody Map<String, String> body) {

        String lugar = (body != null) ? body.get("lugar") : null;
        if (lugar == null || lugar.isBlank()) {
            // Valida que 'lugar' venga y tenga contenido
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "El campo 'lugar' es obligatorio"
            ));
        }

// Busca el itinerario y actualiza solo el campo 'lugar'
        return itinerarioRepository.findById(id)
                .map(it -> {
                    it.setLugar(lugar);// Aplica cambio
                    itinerarioRepository.save(it);
                    return ResponseEntity.ok(Map.of(
                            "status", "success",
                            "message", "Lugar actualizado",
                            "itinerario", it
                    ));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "status", "error",
                        "message", "Itinerario no encontrado"
                )));
    }
}
