package com.viajafacil.backend.repository;

import com.viajafacil.backend.model.Presupuesto; // Entidad que maneja este repositorio
import org.springframework.data.jpa.repository.JpaRepository; // Interfaz que provee operaciones CRUD
import org.springframework.data.jpa.repository.Query; // Permite crear consultas JPQL personalizadas
import org.springframework.data.repository.query.Param; // Define parámetros para las consultas con @Query
import java.util.List; // Para devolver múltiples resultados (colecciones)

public interface PresupuestoRepository extends JpaRepository<Presupuesto, Long> {
    // Extiende JpaRepository para heredar métodos CRUD:
    @Query("SELECT p FROM Presupuesto p WHERE p.usuario.id_usuario = :idUsuario")
    // Esta consulta obtiene todos los presupuestos asociados a un usuario específico.
    // Usa la relación entre Presupuesto y Usuario (presupuesto.usuario.id_usuario).
    // :idUsuario es un parámetro nombrado que se enlaza mediante @Param.
    List<Presupuesto> findByUsuarioId(@Param("idUsuario") Long idUsuario);
    // Devuelve una lista de presupuestos pertenecientes al usuario indicado.
}

