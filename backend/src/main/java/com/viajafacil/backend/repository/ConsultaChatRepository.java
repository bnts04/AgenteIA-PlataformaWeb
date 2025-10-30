package com.viajafacil.backend.repository;

import com.viajafacil.backend.model.ConsultaChat; // Entidad que manejará este repositorio
import org.springframework.data.jpa.repository.JpaRepository; // Interfaz base de Spring Data JPA

import java.util.List; // Para manejar listas de resultados

public interface ConsultaChatRepository extends JpaRepository<ConsultaChat, Long> {
    // Hereda de JpaRepository todos los métodos CRUD (findAll, save, deleteById, findById, etc.)

    // Método personalizado: busca todas las consultas según el ID del usuario
    List<ConsultaChat> findByUsuarioIdOrderByFechaAsc(Long usuarioId);
}
