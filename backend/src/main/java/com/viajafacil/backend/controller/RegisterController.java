package com.viajafacil.backend.controller;

import com.viajafacil.backend.model.Usuario;              // Entidad Usuario (mapea la tabla en BD)
import com.viajafacil.backend.repository.UsuarioRepository; // Repositorio para acceder a los datos de Usuario
import org.springframework.beans.factory.annotation.Autowired;  // Inyección de dependencias
import org.springframework.http.HttpStatus;                      // Códigos de estado HTTP (200, 400, 409, etc.)
import org.springframework.http.ResponseEntity;                 // Objeto que encapsula respuestas HTTP
import org.springframework.web.bind.annotation.*;               // Anotaciones REST (Controller, Mapping, etc.)

import java.util.HashMap; // Estructura para crear respuestas tipo JSON
import java.util.Map;

@RestController
@RequestMapping("/register") // Marca la clase como un controlador REST (devuelve JSON)
public class RegisterController {// Define la ruta base: /register

    @Autowired
    private UsuarioRepository usuarioRepository;// Inyecta el repositorio que maneja la entidad Usuario

//Este seria el DTO
    public static class RegisterRequest {
        // Clase interna usada para recibir los datos enviados desde el frontend
        private String nombre;
        private String apellido;
        private String correo;
        private String contrasena;
        private String telefono;

        // getters y setters
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getApellido() { return apellido; }
        public void setApellido(String apellido) { this.apellido = apellido; }
        public String getCorreo() { return correo; }
        public void setCorreo(String correo) { this.correo = correo; }
        public String getContrasena() { return contrasena; }
        public void setContrasena(String contrasena) { this.contrasena = contrasena; }
        public String getTelefono() { return telefono; }
        public void setTelefono(String telefono) { this.telefono = telefono; }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegisterRequest req) {
        // Crea un mapa de respuesta para devolver mensajes y datos al frontend
        Map<String, Object> response = new HashMap<>();

        // Validaciones básicas
        if (req.getCorreo() == null || req.getCorreo().trim().isEmpty()) {
            // Verifica que el correo no esté vacío o nulo
            response.put("status", "error");
            response.put("message", "Correo es requerido");
            // Retorna respuesta con código 400 (Bad Request)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        if (req.getContrasena() == null || req.getContrasena().trim().isEmpty()) {
            response.put("status", "error");
            response.put("message", "Contraseña es requerida");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Verificar si correo ya existe
        if (usuarioRepository.existsByCorreo(req.getCorreo())) {// Llama al método del repositorio
            response.put("status", "error");
            response.put("message", "Correo ya registrado");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        // Crear entidad Usuario y guardar
        Usuario usuario = new Usuario();
        usuario.setNombre(req.getNombre());
        usuario.setApellido(req.getApellido());
        usuario.setCorreo(req.getCorreo());


        usuario.setContrasena(req.getContrasena());
        usuario.setTelefono(req.getTelefono());

        // Guarda el nuevo usuario en la base de datos
        Usuario guardado = usuarioRepository.save(usuario);

        response.put("status", "success");
        response.put("message", "Usuario registrado");
        response.put("usuario", guardado);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
