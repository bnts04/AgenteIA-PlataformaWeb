package com.viajafacil.backend.repository;

import com.viajafacil.backend.model.Reserva; // Entidad que maneja este repositorio
import org.springframework.data.jpa.repository.JpaRepository; // Interfaz de Spring que provee las operaciones CRUD
import org.springframework.data.jpa.repository.Query; // Permite definir consultas personalizadas con JPQL
import org.springframework.data.repository.query.Param; // Asocia parámetros a las variables de la consulta
import java.util.List; // Estructura de datos para manejar listas de resultados

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    // Hereda todos los métodos CRUD
    @Query("SELECT r FROM Reserva r WHERE r.usuario.id_usuario = :idUsuario")
    // Esta consulta obtiene todas las reservas que pertenecen a un usuario específico.
    // Usa la relación entre Reserva y Usuario: (reserva.usuario.id_usuario).
    List<Reserva> findByUsuarioId(@Param("idUsuario") Long idUsuario);
    // Método que devuelve una lista de reservas asociadas al usuario indicado.
}
