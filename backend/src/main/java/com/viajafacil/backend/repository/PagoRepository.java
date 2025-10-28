package com.viajafacil.backend.repository;

import com.viajafacil.backend.model.Pago; // Entidad gestionada por este repositorio
import org.springframework.data.jpa.repository.JpaRepository; // Proporciona las operaciones CRUD
import org.springframework.data.jpa.repository.Query; // Permite definir consultas JPQL personalizadas
import org.springframework.data.repository.query.Param; // Define parámetros nombrados en consultas @Query

import java.time.LocalDateTime; // Tipo de dato para manejar fecha y hora
import java.util.List; // Estructura de lista para resultados múltiples

public interface PagoRepository extends JpaRepository<Pago, Long> {
    // Extiende JpaRepository para heredar todos los métodos CRUD básicos:
    // save(), findAll(), findById(), deleteById(), etc.
    // La entidad gestionada es Pago y su clave primaria es de tipo Long.

    @Query("SELECT p FROM Pago p WHERE p.reserva.usuario.id_usuario = :usuarioId ORDER BY p.fecha_pago DESC")
        // Recupera todos los pagos realizados por un usuario específico (según su id_usuario),
        // ordenados de más reciente a más antiguo.
    List<Pago> findByUsuarioId(@Param("usuarioId") Long usuarioId);

    @Query("""
           SELECT p FROM Pago p
            WHERE (:metodo IS NULL OR p.metodo = :metodo)
                     AND p.fecha_pago >= COALESCE(:desde, p.fecha_pago)
                     AND p.fecha_pago <= COALESCE(:hasta, p.fecha_pago)
                   ORDER BY p.fecha_pago DESC
           """)
        // Esta consulta permite filtrar pagos según:
        // - método de pago (efectivo, Yape, etc.),
        // - rango de fechas (desde / hasta).
        // Si alguno de los parámetros es null, no se aplica ese filtro.
        // Los resultados se ordenan por fecha de pago descendente.
    List<Pago> filter(
            @Param("metodo") String metodo,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta
    );
}
