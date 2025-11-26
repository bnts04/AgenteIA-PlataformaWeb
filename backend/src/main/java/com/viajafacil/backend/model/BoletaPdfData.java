package com.viajafacil.backend.model;

import java.time.LocalDate;
import java.util.List;

public class BoletaPdfData {

    // Datos de pago / boleta
    private Long idPago;

    // Datos del cliente
    private String nombreCliente;
    private String correoCliente;

    // Datos del paquete / viaje
    private String nombrePaquete;
    private LocalDate fechaSalida;     // opcional, si luego lo agregan a Reserva
    private LocalDate fechaRetorno;
    private Integer numeroPersonas;

    // Datos económicos
    private Double montoTotal;
    private String metodoPago;
    private LocalDate fechaPago;

    // Itinerario en formato de líneas de texto
    private List<String> actividades;



    public BoletaPdfData() {
    }

    public BoletaPdfData(Long idPago,
                         String nombreCliente,
                         String correoCliente,
                         String nombrePaquete,
                         LocalDate fechaSalida,
                         LocalDate fechaRetorno,
                         Integer numeroPersonas,
                         Double montoTotal,
                         String metodoPago,
                         LocalDate fechaPago,
                         List<String> actividades) {
        this.idPago = idPago;
        this.nombreCliente = nombreCliente;
        this.correoCliente = correoCliente;
        this.nombrePaquete = nombrePaquete;
        this.fechaSalida = fechaSalida;
        this.fechaRetorno = fechaRetorno;
        this.numeroPersonas = numeroPersonas;
        this.montoTotal = montoTotal;
        this.metodoPago = metodoPago;
        this.fechaPago = fechaPago;
        this.actividades = actividades;
    }

    public Long getIdPago() {
        return idPago;
    }

    public void setIdPago(Long idPago) {
        this.idPago = idPago;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getCorreoCliente() {
        return correoCliente;
    }

    public void setCorreoCliente(String correoCliente) {
        this.correoCliente = correoCliente;
    }

    public String getNombrePaquete() {
        return nombrePaquete;
    }

    public void setNombrePaquete(String nombrePaquete) {
        this.nombrePaquete = nombrePaquete;
    }

    public LocalDate getFechaSalida() {
        return fechaSalida;
    }

    public void setFechaSalida(LocalDate fechaSalida) {
        this.fechaSalida = fechaSalida;
    }

    public LocalDate getFechaRetorno() {
        return fechaRetorno;
    }

    public void setFechaRetorno(LocalDate fechaRetorno) {
        this.fechaRetorno = fechaRetorno;
    }

    public Integer getNumeroPersonas() {
        return numeroPersonas;
    }

    public void setNumeroPersonas(Integer numeroPersonas) {
        this.numeroPersonas = numeroPersonas;
    }

    public Double getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(Double montoTotal) {
        this.montoTotal = montoTotal;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public LocalDate getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDate fechaPago) {
        this.fechaPago = fechaPago;
    }

    public List<String> getActividades() {
        return actividades;
    }

    public void setActividades(List<String> actividades) {
        this.actividades = actividades;
    }
}
