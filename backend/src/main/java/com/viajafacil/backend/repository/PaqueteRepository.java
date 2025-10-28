package com.viajafacil.backend.repository;

import com.viajafacil.backend.model.Paquete; // Entidad que maneja este repositorio
import org.springframework.data.jpa.repository.JpaRepository; // Proporciona métodos CRUD (save, findAll, deleteById, etc.)
import org.springframework.stereotype.Repository; // Marca explícitamente esta interfaz como repositorio Spring

import java.util.List; // Para manejar listas de resultados

@Repository
public interface PaqueteRepository extends JpaRepository<Paquete, Long> {
    //  hereda todos los métodos básicos de CRUD (save, findAll, findById, deleteById, etc.)
    List<Paquete> findByCategoria(String categoria);
    // Este método busca todos los paquetes que coincidan con una categoría específica.
    // Ejemplo: findByCategoria("ALTO") devolverá todos los paquetes de categoría ALTO.
}
