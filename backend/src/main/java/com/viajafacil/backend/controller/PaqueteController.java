package com.viajafacil.backend.controller;

import com.viajafacil.backend.model.Paquete;
import com.viajafacil.backend.model.Usuario;
import com.viajafacil.backend.repository.PaqueteRepository;
import com.viajafacil.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/paquetes")
public class PaqueteController {

    @Autowired
    private PaqueteRepository paqueteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // GET: listar todos
    @GetMapping
    public List<Paquete> getAllPaquetes() {
        return paqueteRepository.findAll();
    }

    // POST: crear nuevo (con usuario)
    @PostMapping
    public Paquete createPaquete(@RequestBody Paquete paquete) {
        // Normalizar categoría
        if (paquete.getCategoria() != null) {
            paquete.setCategoria(paquete.getCategoria().toUpperCase());
        }

        if (paquete.getUsuario() != null && paquete.getUsuario().getId_usuario() != null) {
            Usuario usuario = usuarioRepository.findById(paquete.getUsuario().getId_usuario())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            paquete.setUsuario(usuario);
        }
        return paqueteRepository.save(paquete);
    }

    // GET: obtener por id
    @GetMapping("/{id}")
    public Paquete getPaqueteById(@PathVariable Long id) {
        return paqueteRepository.findById(id).orElse(null);
    }

    // PUT: actualizar paquete
    @PutMapping("/{id}")
    public Paquete updatePaquete(@PathVariable Long id, @RequestBody Paquete detallesPaquete) {
        Optional<Paquete> paqueteExistente = paqueteRepository.findById(id);

        if (paqueteExistente.isPresent()) {
            Paquete paquete = paqueteExistente.get();
            paquete.setDestino(detallesPaquete.getDestino());
            paquete.setDescripcion(detallesPaquete.getDescripcion());

            // Normalizar categoría
            if (detallesPaquete.getCategoria() != null) {
                paquete.setCategoria(detallesPaquete.getCategoria().toUpperCase());
            }

            paquete.setPrecio(detallesPaquete.getPrecio());

            if (detallesPaquete.getUsuario() != null && detallesPaquete.getUsuario().getId_usuario() != null) {
                Usuario usuario = usuarioRepository.findById(detallesPaquete.getUsuario().getId_usuario())
                        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
                paquete.setUsuario(usuario);
            }

            return paqueteRepository.save(paquete);
        } else {
            return null; // mejor sería devolver ResponseEntity con 404
        }
    }

    // DELETE: eliminar paquete
    @DeleteMapping("/{id}")
    public String deletePaquete(@PathVariable Long id) {
        if (paqueteRepository.existsById(id)) {
            paqueteRepository.deleteById(id);
            return "Paquete con id " + id + " eliminado correctamente.";
        } else {
            return "Paquete con id " + id + " no encontrado.";
        }
    }
}
