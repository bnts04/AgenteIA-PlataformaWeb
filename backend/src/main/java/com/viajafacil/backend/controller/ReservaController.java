package com.viajafacil.backend.controller;

import com.viajafacil.backend.model.Reserva;          // Entidad Reserva (mapea a tabla de reservas)
import com.viajafacil.backend.model.Usuario;          // Entidad Usuario (relación con Reserva)
import com.viajafacil.backend.model.Paquete;          // Entidad Paquete (relación con Reserva)
import com.viajafacil.backend.repository.ReservaRepository; // DAO para acceder a la tabla de reservas
import com.viajafacil.backend.repository.UsuarioRepository; // DAO de usuarios
import com.viajafacil.backend.repository.PaqueteRepository; // DAO de paquetes
import org.springframework.beans.factory.annotation.Autowired; // Inyección de dependencias
import org.springframework.web.bind.annotation.*; // Anotaciones REST (Controller, Mapping, etc.)

import java.time.LocalDate; // Tipo de dato para manejar fechas
import java.util.*;

@RestController                         // Marca la clase como un controlador REST
@RequestMapping("/reservas")             // Ruta base para todos los endpoints: /reservas
@CrossOrigin(origins = "*")              // Permite peticiones desde cualquier origen (CORS)
public class ReservaController {

    @Autowired
    private ReservaRepository reservaRepository; // Repositorio que maneja la tabla Reserva

    @Autowired
    private UsuarioRepository usuarioRepository; // Repositorio de usuarios

    @Autowired
    private PaqueteRepository paqueteRepository; // Repositorio de paquetes

    //  Crear nueva reserva (acepta JSON o parámetros en URL)
    @PostMapping
    public Map<String, Object> crearReserva(
            @RequestParam(required = false) Long idUsuario,
            @RequestParam(required = false) Long idPaquete,
            @RequestBody(required = false) Map<String, Object> body) {

        Map<String, Object> response = new HashMap<>();// Respuesta que se devolverá al frontend

        // Permitir recibir datos desde JSON o desde la URL
        if (body != null) {
            if (idUsuario == null && body.get("idUsuario") != null)
                idUsuario = Long.valueOf(body.get("idUsuario").toString());
            if (idPaquete == null && body.get("idPaquete") != null)
                idPaquete = Long.valueOf(body.get("idPaquete").toString());
        }

// Verifica que ambos IDs sean enviados
        if (idUsuario == null || idPaquete == null) {
            response.put("status", "error");
            response.put("message", "Debe enviar idUsuario y idPaquete.");
            return response;
        }

        // Busca si existen el usuario y el paquete
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(idUsuario);
        Optional<Paquete> paqueteOpt = paqueteRepository.findById(idPaquete);

        if (usuarioOpt.isPresent() && paqueteOpt.isPresent()) {
            // Crea una nueva reserva con los datos encontrados
            Reserva reserva = new Reserva();
            reserva.setUsuario(usuarioOpt.get());                // Asocia el usuario
            reserva.setPaquete(paqueteOpt.get());                // Asocia el paquete
            reserva.setFecha_reserva(LocalDate.now());           // Fecha actual del sistema
            reserva.setEstado_reserva("pendiente");              // Estado inicial por defecto

            // Guarda la nueva reserva en la base de datos
            Reserva nuevaReserva = reservaRepository.save(reserva);

            //  Información personalizada para mostrar en el frontend
            Map<String, Object> reservaInfo = new LinkedHashMap<>();
            reservaInfo.put("id_reserva", nuevaReserva.getId_reserva());
            reservaInfo.put("nombre_usuario", nuevaReserva.getUsuario().getNombre());
            reservaInfo.put("nombre_paquete", nuevaReserva.getPaquete().getDescripcion());
            reservaInfo.put("fecha_reserva", nuevaReserva.getFecha_reserva());
            reservaInfo.put("estado_reserva", nuevaReserva.getEstado_reserva());

            // // Arme una respuesta de éxito
            response.put("status", "success");
            response.put("message", " Reserva creada exitosamente.");
            response.put("reserva", reservaInfo);
        } else {
            // Si no se encuentra usuario o paquete
            response.put("status", "error");
            response.put("message", "Usuario o paquete no encontrado.");
        }

        return response;
    }

    //  Listar todas las reservas (solo para admin)
    @GetMapping
    public List<Reserva> listarReservas() {
        return reservaRepository.findAll();
    }
    // Devuelve todas las reservas registradas

    //  Listar reservas de un usuario (para su cuenta personal)
    @GetMapping("/usuario/{idUsuario}")
    public List<Map<String, Object>> listarReservasPorUsuario(@PathVariable Long idUsuario) {
        // Busca todas las reservas asociadas al usuario
        List<Reserva> reservas = reservaRepository.findByUsuarioId(idUsuario);
        List<Map<String, Object>> lista = new ArrayList<>();

        // Convierte cada reserva en un mapa personalizado para el frontend
        for (Reserva r : reservas) {
            Map<String, Object> reservaMap = new LinkedHashMap<>();
            reservaMap.put("id_reserva", r.getId_reserva());
            reservaMap.put("descripcion_paquete", r.getPaquete().getDescripcion());
            reservaMap.put("categoria", r.getPaquete().getCategoria());
            reservaMap.put("precio_base", r.getPaquete().getPrecio_base());
            reservaMap.put("fecha_reserva", r.getFecha_reserva());
            reservaMap.put("estado_reserva", r.getEstado_reserva());
            lista.add(reservaMap);
        }
        return lista;// Devuelve la lista de reservas con datos personalizados

    }


    //  Actualizar estado de reserva (confirmar / cancelar)
    @PutMapping("/{id}")
    public Map<String, Object> actualizarEstadoReserva(
            @PathVariable Long id,
            @RequestParam String nuevoEstado) {

        Map<String, Object> response = new HashMap<>();
        Optional<Reserva> reservaOpt = reservaRepository.findById(id);// Busca la reserva

        if (reservaOpt.isPresent()) {
            Reserva reserva = reservaOpt.get();
            reserva.setEstado_reserva(nuevoEstado.toLowerCase()); // Actualiza el estado en minúsculas
            reservaRepository.save(reserva);                      // Guarda los cambios

            response.put("status", "success");
            response.put("message", "Estado de reserva actualizado correctamente.");
        } else {
            response.put("status", "error");
            response.put("message", "Reserva no encontrada.");
        }

        return response;
    }

    //  Eliminar reserva
    @DeleteMapping("/{id}")
    public String eliminarReserva(@PathVariable Long id) {
        if (reservaRepository.existsById(id)) {
            reservaRepository.deleteById(id);// Elimina la reserva de la BD
            return "Reserva con id " + id + " eliminada correctamente.";
        } else {
            return "Reserva con id " + id + " no encontrada.";
        }
    }
}
