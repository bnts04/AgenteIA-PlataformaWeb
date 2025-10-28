package com.viajafacil.backend.model;

import com.fasterxml.jackson.annotation.JsonFormat;   // Controla el formato de fecha/hora al devolver JSON
import com.fasterxml.jackson.annotation.JsonIgnore;   // Evita que un atributo se incluya en el JSON
import com.fasterxml.jackson.annotation.JsonProperty; // Permite renombrar un campo al convertir a JSON
import jakarta.persistence.*;                         // Anotaciones JPA: @Entity, @Id, @Column, etc.
import org.springframework.format.annotation.DateTimeFormat; // Da formato a fechas/horas en el backend (para entrada)


import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity // Indica que esta clase representa una tabla en la base de datos
@Table(name = "itinerario") // Define el nombre exacto de la tabla
public class Itinerario {

    @Id // Clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID autoincremental
    private Long id_itinerario; // Identificador único del itinerario

    @ManyToOne(fetch = FetchType.LAZY) // Relación muchos a uno con la entidad Paquete
    @JoinColumn(name = "id_paquete", nullable = false)// Clave foránea hacia la tabla paquete
    @JsonIgnore // Evita que se cargue el objeto completo de Paquete en el JSON
    private Paquete paquete; // Relación: cada itinerario pertenece a un paquete

    @Column(nullable = false)// Campo obligatorio
    private Integer dia; // Día del itinerario

    @Column(nullable = false, length = 200) // Campo obligatorio con máximo 200 caracteres
    private String actividad; // Nombre o título de la actividad

    @Column(name = "descripcion_itinerario", columnDefinition = "TEXT", nullable = true)
    private String descripcion;// Descripción detallada de la actividad (texto largo)

    @Column(length = 150)// Campo opcional, máximo 150 caracteres
    private String lugar; // Lugar donde se realizará la actividad

    @DateTimeFormat(pattern = "HH:mm")// Formato para recibir hora en el backend
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")// Formato para mostrar hora en JSON
    private LocalTime hora_inicio; // Hora de inicio de la actividad

    @DateTimeFormat(pattern = "HH:mm")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime hora_fin; // Hora de finalización de la actividad

    //orden dentro del día (por si no se usan horas)
    private Integer orden;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm")
    @Column(nullable = false)
    private LocalDateTime fecha_creacion;

    @PrePersist // Método que se ejecuta automáticamente antes de guardar
    public void prePersist() {
        if (fecha_creacion == null) {
            fecha_creacion = LocalDateTime.now();
        }
    }

    // Getters / Setters
    public Long getId_itinerario() { return id_itinerario; }
    public void setId_itinerario(Long id_itinerario) { this.id_itinerario = id_itinerario; }

    public Paquete getPaquete() { return paquete; }
    public void setPaquete(Paquete paquete) { this.paquete = paquete; }

    public Integer getDia() { return dia; }
    public void setDia(Integer dia) { this.dia = dia; }

    public String getLugar() { return lugar; }
    public void setLugar(String lugar) { this.lugar = lugar; }

    public Integer getOrden() {return orden;}
    public void setOrden(Integer orden) {this.orden = orden;}

    public String getActividad() { return actividad; }
    public void setActividad(String actividad) { this.actividad = actividad; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public LocalTime getHora_inicio() { return hora_inicio; }
    public void setHora_inicio(LocalTime hora_inicio) { this.hora_inicio = hora_inicio; }

    public LocalTime getHora_fin() { return hora_fin; }
    public void setHora_fin(LocalTime hora_fin) { this.hora_fin = hora_fin; }

    public LocalDateTime getFecha_creacion() { return fecha_creacion; }
    public void setFecha_creacion(LocalDateTime fecha_creacion) { this.fecha_creacion = fecha_creacion; }

    // Exponer id_paquete en el JSON sin cargar el objeto paquete
    @Transient
    @JsonProperty("id_paquete")
    public Long getIdPaquete() {
        return (paquete != null) ? paquete.getId_paquete() : null;
    }
}

