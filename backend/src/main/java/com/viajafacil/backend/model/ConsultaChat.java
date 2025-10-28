package com.viajafacil.backend.model;

import jakarta.persistence.*;        // Contiene las anotaciones de JPA (@Entity, @Id, @Column, etc.)
import java.time.LocalDateTime;      // Permite manejar fecha y hora del sistema (sin zona horaria)
import jakarta.persistence.Entity;   // Marca la clase como entidad JPA
import jakarta.persistence.GeneratedValue; // Controla la generación automática del ID
import jakarta.persistence.Table;    // Permite definir el nombre de la tabla en la base de datos

@Entity // Marca esta clase como entidad de base de datos
@Table(name = "consultas_chat")// Define el nombre de la tabla en la BD
public class ConsultaChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;// Identificador único de cada registro

    private Long usuarioId; // Guarda el ID del usuario que hizo la consulta
    private String mensajeUsuario; // Mensaje enviado por el usuario

    @Column(columnDefinition = "TEXT") // Permite guardar textos largos (respuesta de IA)
    private String respuestaIA; // Respuesta generada por la inteligencia artificial


    private LocalDateTime fecha = LocalDateTime.now();// Fecha y hora automáticas al crear la consulta

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public String getMensajeUsuario() { return mensajeUsuario; }
    public void setMensajeUsuario(String mensajeUsuario) { this.mensajeUsuario = mensajeUsuario; }

    public String getRespuestaIA() { return respuestaIA; }
    public void setRespuestaIA(String respuestaIA) { this.respuestaIA = respuestaIA; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}
