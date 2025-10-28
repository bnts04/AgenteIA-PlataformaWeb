package com.viajafacil.backend.controller;

import com.viajafacil.backend.model.Presupuesto;
import com.viajafacil.backend.model.Usuario;
import com.viajafacil.backend.repository.PresupuestoRepository;
import com.viajafacil.backend.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/presupuestos")
@CrossOrigin(origins = "*")
public class PresupuestoController {

    @Autowired
    private PresupuestoRepository presupuestoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Registrar nuevo presupuesto estimado
    @PostMapping
    public Map<String, Object> registrarPresupuesto(
            @RequestParam Long idUsuario,
            @RequestParam String destino,
            @RequestParam String fecha_inicio,
            @RequestParam String fecha_fin,
            @RequestParam Double presupuesto_estimado
    ) {
        Map<String, Object> response = new HashMap<>();

        Optional<Usuario> usuarioOpt = usuarioRepository.findById(idUsuario);
        if (usuarioOpt.isEmpty()) {
            response.put("status", "error");
            response.put("message", "Usuario no encontrado.");
            return response;
        }

        try {
            Presupuesto nuevo = new Presupuesto();
            nuevo.setUsuario(usuarioOpt.get());
            nuevo.setDestino(destino);
            nuevo.setFecha_inicio(LocalDate.parse(fecha_inicio));
            nuevo.setFecha_fin(LocalDate.parse(fecha_fin));
            nuevo.setPresupuesto_estimado(presupuesto_estimado);

            presupuestoRepository.save(nuevo);

            response.put("status", "success");
            response.put("message", "Presupuesto registrado correctamente.");
            response.put("presupuesto", nuevo);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error al registrar el presupuesto: " + e.getMessage());
        }

        return response;
    }

    //  Consultar presupuestos de un usuario
    @GetMapping("/usuario/{idUsuario}")
    public List<Presupuesto> obtenerPresupuestosPorUsuario(@PathVariable Long idUsuario) {
        return presupuestoRepository.findByUsuarioId(idUsuario);
    }

    //  Actualizar un presupuesto existente
    @PutMapping("/{idPresupuesto}")
    public Map<String, Object> actualizarPresupuesto(
            @PathVariable Long idPresupuesto,
            @RequestBody Map<String, Object> body
    ) {
        Map<String, Object> response = new HashMap<>();
        var presupuestoOpt = presupuestoRepository.findById(idPresupuesto);

        if (presupuestoOpt.isEmpty()) {
            response.put("status", "error");
            response.put("message", "Presupuesto no encontrado.");
            return response;
        }

        Presupuesto p = presupuestoOpt.get();

        // Leer del JSON si vienen presentes
        if (body.containsKey("destino")) {
            p.setDestino(Objects.toString(body.get("destino"), null));
        }
        if (body.containsKey("fecha_inicio")) {
            String fi = Objects.toString(body.get("fecha_inicio"), null);
            if (fi != null) p.setFecha_inicio(LocalDate.parse(fi));
        }
        if (body.containsKey("fecha_fin")) {
            String ff = Objects.toString(body.get("fecha_fin"), null);
            if (ff != null) p.setFecha_fin(LocalDate.parse(ff));
        }
        if (body.containsKey("presupuesto_estimado")) {
            Double pe = (body.get("presupuesto_estimado") == null) ? null :
                    Double.valueOf(body.get("presupuesto_estimado").toString());
            if (pe != null) p.setPresupuesto_estimado(pe);
        }

        presupuestoRepository.save(p);

        response.put("status", "success");
        response.put("message", "Presupuesto actualizado correctamente.");
        response.put("presupuesto", p);
        return response;
    }

    //  Eliminar presupuesto
    @DeleteMapping("/{idPresupuesto}")
    public Map<String, Object> eliminarPresupuesto(@PathVariable Long idPresupuesto) {
        Map<String, Object> response = new HashMap<>();

        if (!presupuestoRepository.existsById(idPresupuesto)) {
            response.put("status", "error");
            response.put("message", "Presupuesto no encontrado.");
            return response;
        }

        presupuestoRepository.deleteById(idPresupuesto);
        response.put("status", "success");
        response.put("message", "Presupuesto eliminado correctamente.");

        return response;
    }
}
