package com.viajafacil.backend.repository;

import com.viajafacil.backend.model.Itinerario; // Entidad que se gestionará desde este repositorio
import org.springframework.data.jpa.repository.JpaRepository; // Interfaz base para operaciones CRUD
import org.springframework.data.jpa.repository.Query; // Permite escribir consultas personalizadas (JPQL)
//legunaje consulta orientada a obejetos

import java.util.List; // Para retornar listas de resultados

public interface ItinerarioRepository extends JpaRepository<Itinerario, Long> {
// Hereda todos los métodos CRUD de JpaRepository (findAll, save, findById, deleteById, etc.)
    // Maneja la entidad Itinerario y su clave primaria de tipo Long

    @Query("""
       SELECT i
       FROM Itinerario i
       WHERE i.paquete.id_paquete = :idPaquete
         AND (:dia IS NULL OR i.dia = :dia)
       ORDER BY i.dia ASC,
                i.hora_inicio ASC NULLS LAST,
                COALESCE(i.orden, 0) ASC
       """)
        // Filtra los itinerarios según el id del paquete.
        // Si "dia" es null, muestra todos los días; si no, solo los de ese día.
        // Ordena los resultados por día, hora de inicio (si existe) y orden manual.

    List<Itinerario> listarPorPaqueteYDiaOrdenado(Long idPaquete, Integer dia);

    @Query("""
           SELECT COALESCE(MAX(i.orden), 0)
           FROM Itinerario i
           WHERE i.paquete.id_paquete = :idPaquete AND i.dia = :dia
           """)
        // Esta consulta obtiene el número máximo de "orden" usado en un día específico,
        // dentro de un paquete, para luego poder asignar el siguiente número (auto incremento lógico).
    Integer maxOrdenEnDia(Long idPaquete, Integer dia);
}
