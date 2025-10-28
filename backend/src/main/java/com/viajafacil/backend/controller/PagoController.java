package com.viajafacil.backend.controller;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.viajafacil.backend.model.Pago;
import com.viajafacil.backend.model.Reserva;
import com.viajafacil.backend.repository.PagoRepository;
import com.viajafacil.backend.repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;// Tipo para representar horas (sin fecha)
import java.util.*;

@RestController
@RequestMapping("/pagos")
@CrossOrigin(origins = "*")
public class PagoController {

    @Autowired private PagoRepository pagoRepository;
    @Autowired private ReservaRepository reservaRepository;

    // Entradas del DTO , la transeferencia de datos
    public static class PagoRequest {
        public Long reservaId;
        public Double monto;
        public String metodo;     // Efectivo, Tarjeta, Yape, Plin,Transferencia
        public String estado;     // opcional (si no llega, se mantiene el default del entity)
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm")
        public LocalDateTime fechaPago; // opcional si no llega, @PrePersist
    }

    public static class PagoUpdateRequest {
        public Double monto;      // opcional
        public String metodo;     // opcional
        public String estado;     // opcional
    }

    // ---------- CREATE (confirma la reserva) ----------
    @PostMapping
    @Transactional
    public Map<String, Object> registrarPago(@RequestBody PagoRequest req) {
        Map<String, Object> resp = new LinkedHashMap<>();

        if (req == null || req.reservaId == null || req.monto == null ||
                req.metodo == null || req.metodo.isBlank()) {
            resp.put("status", "error");
            resp.put("message", "reservaId, monto y metodo son obligatorios");
            return resp;
        }

        Optional<Reserva> resOpt = reservaRepository.findById(req.reservaId);
        if (resOpt.isEmpty()) {
            resp.put("status", "error");
            resp.put("message", "Reserva no encontrada");
            return resp;
        }
        Reserva reserva = resOpt.get();

        // 1) Guardar pago
        Pago p = new Pago();
        p.setReserva(reserva);
        p.setMonto(req.monto);
        p.setMetodo(req.metodo.toUpperCase());
        if (req.estado != null && !req.estado.isBlank()) p.setEstado(req.estado);
        if (req.fechaPago != null) p.setFecha_pago(req.fechaPago);

        Pago guardado = pagoRepository.save(p);

        // 2) Confirmar la reserva (si no lo estaba)
        if (!"confirmado".equalsIgnoreCase(reserva.getEstado_reserva())) {
            reserva.setEstado_reserva("confirmado");
            reservaRepository.save(reserva);
        }

        resp.put("status", "success");
        resp.put("message", "Pago registrado y reserva confirmada");
        resp.put("pago", guardado);
        resp.put("reserva", Map.of(
                "id_reserva", reserva.getId_reserva(),
                "estado_reserva", reserva.getEstado_reserva()
        ));
        return resp;
    }

    //  READ por id
    @GetMapping("/{id}")
    public Map<String, Object> obtenerPorId(@PathVariable Long id) {
        Map<String, Object> resp = new LinkedHashMap<>();
        Optional<Pago> opt = pagoRepository.findById(id);
        if (opt.isEmpty()) {
            resp.put("status", "error");
            resp.put("message", "Pago no encontrado");
            return resp;
        }
        resp.put("status", "success");
        resp.put("pago", opt.get());
        return resp;
    }

    //  Historial por usuario
    @GetMapping("/historial/usuario/{usuarioId}")
    public List<Pago> historialPorUsuario(@PathVariable Long usuarioId) {
        return pagoRepository.findByUsuarioId(usuarioId);
    }

    //  Filtro por método y rango de fechas
    @GetMapping
    public List<Pago> filtrar(
            @RequestParam(required = false) String metodo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta
    ) {
        String m = (metodo == null || metodo.isBlank()) ? null : metodo.toUpperCase();
        return pagoRepository.filter(m, desde, hasta);
    }

    //UPDATE
    @PutMapping("/{id}")
    public Map<String, Object> actualizarPago(@PathVariable Long id,
                                              @RequestBody PagoUpdateRequest req) {
        Map<String, Object> resp = new LinkedHashMap<>();

        Optional<Pago> opt = pagoRepository.findById(id);
        if (opt.isEmpty()) {
            resp.put("status", "error");
            resp.put("message", "Pago no encontrado");
            return resp;
        }

        Pago p = opt.get();
        if (req.monto != null) p.setMonto(req.monto);
        if (req.metodo != null && !req.metodo.isBlank()) p.setMetodo(req.metodo.toUpperCase());
        if (req.estado != null && !req.estado.isBlank()) p.setEstado(req.estado);

        Pago actualizado = pagoRepository.save(p);

        resp.put("status", "success");
        resp.put("message", "Pago actualizado");
        resp.put("pago", actualizado);
        return resp;
    }

    //  DELETE
    @DeleteMapping("/{id}")
    public Map<String, Object> eliminarPago(@PathVariable Long id) {
        Map<String, Object> resp = new LinkedHashMap<>();
        Optional<Pago> opt = pagoRepository.findById(id);
        if (opt.isEmpty()) {
            resp.put("status", "error");
            resp.put("message", "Pago no encontrado");
            return resp;
        }

        pagoRepository.delete(opt.get());
        resp.put("status", "success");
        resp.put("message", "Pago eliminado correctamente");
        return resp;
    }
}
