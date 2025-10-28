package com.viajafacil.backend.controller;

import com.viajafacil.backend.model.Paquete;
import com.viajafacil.backend.repository.PaqueteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/paquetes")
@CrossOrigin(origins = "http://localhost:4200") // si tu frontend usa otro puerto, cámbialo
public class PaqueteController {

    @Autowired
    private PaqueteRepository paqueteRepository;

    //  GET: listar todos los paquetes
    @GetMapping
    public List<Paquete> getAllPaquetes() {
        return paqueteRepository.findAll();
    }

    //  POST: crear nuevo paquete
    @PostMapping
    public Paquete createPaquete(@RequestBody Paquete paquete) {
        // Normalizar categoría
        if (paquete.getCategoria() != null) {
            paquete.setCategoria(paquete.getCategoria().toLowerCase());
        }
        return paqueteRepository.save(paquete);
    }

    //  GET: obtener paquete por ID
    @GetMapping("/{id}")
    public Paquete getPaqueteById(@PathVariable Long id) {
        return paqueteRepository.findById(id).orElse(null);
    }

    //  PUT: actualizar paquete
    @PutMapping("/{id}")
    public Paquete updatePaquete(@PathVariable Long id,
                                 @RequestBody Paquete detallesPaquete) {// Recibe el cuerpo JSON con los nuevos datos

        Optional<Paquete> paqueteExistente = paqueteRepository.findById(id);// Busca si el paquete existe


        if (paqueteExistente.isPresent()) {// Si se encontró el paquete
            Paquete paquete = paqueteExistente.get();// Obtiene la entidad existente
            // Actualiza los campos editables con los valores recibidos
            paquete.setDescripcion(detallesPaquete.getDescripcion());
            paquete.setCategoria(detallesPaquete.getCategoria());
            paquete.setPrecio_base(detallesPaquete.getPrecio_base());
            // Guarda los cambios en la base de datos y retorna el paquete actualizado
            return paqueteRepository.save(paquete);
        } else {
            return null; // podrías reemplazar por ResponseEntity con 404
        }
    }

    //  DELETE: eliminar paquete
    @DeleteMapping("/{id}")// Define el endpoint DELETE /paquetes/{id}
    public String deletePaquete(@PathVariable Long id) {
        // Verifica si existe el paquete con el ID recibido
        if (paqueteRepository.existsById(id)) {
            // Si existe, lo elimina de la base de datos
            paqueteRepository.deleteById(id);
            // Devuelve mensaje de confirmación
            return "Paquete con id " + id + " eliminado correctamente.";
        } else {
            return "Paquete con id " + id + " no encontrado.";
        }
    }
}
