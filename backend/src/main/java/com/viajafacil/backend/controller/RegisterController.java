package com.viajafacil.backend.controller;

import com.viajafacil.backend.model.Usuario;              // Entidad Usuario (mapea la tabla en BD)
import com.viajafacil.backend.repository.UsuarioRepository; // Repositorio para acceder a los datos de Usuario
import org.springframework.beans.factory.annotation.Autowired;  // Inyección de dependencias
import org.springframework.http.HttpStatus;                      // Códigos de estado HTTP (200, 400, 409, etc.)
import org.springframework.http.ResponseEntity;                 // Objeto que encapsula respuestas HTTP
import org.springframework.web.bind.annotation.*;               // Anotaciones REST (Controller, Mapping, etc.)
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap; // Estructura para crear respuestas tipo JSON
import java.util.Map;

@RestController
@RequestMapping("/register") // Marca la clase como un controlador REST (devuelve JSON)
public class RegisterController { // Define la ruta base: /register

    @Autowired
    private UsuarioRepository usuarioRepository; // Inyecta el repositorio que maneja la entidad Usuario

    @Autowired
    private PasswordEncoder passwordEncoder; //este seria el bycript

    // Regex simple para validar formato de correo
    private static final String EMAIL_REGEX = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$";

    //Este seria el DTO
    public static class RegisterRequest {
        // Clase interna usada para recibir los datos enviados desde el frontend
        private String nombre;
        private String apellido;
        private String documentoIdentidad;
        private String nacionalidad;
        private String correo;
        private String contrasena;
        private String telefono;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate fechaNacimiento;

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

        public String getDocumentoIdentidad() { return documentoIdentidad; }
        public void setDocumentoIdentidad(String documentoIdentidad) { this.documentoIdentidad = documentoIdentidad; }

        public String getNacionalidad() { return nacionalidad; }
        public void setNacionalidad(String nacionalidad) { this.nacionalidad = nacionalidad; }

        public LocalDate getFechaNacimiento() { return fechaNacimiento; }
        public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegisterRequest req) {

        Map<String, Object> response = new HashMap<>();

        //  Validaciones básicas
        if (req.getNombre() == null || req.getNombre().trim().isEmpty()) {
            response.put("status", "error");
            response.put("message", "Nombre es requerido");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (req.getApellido() == null || req.getApellido().trim().isEmpty()) {
            response.put("status", "error");
            response.put("message", "Apellido es requerido");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (req.getDocumentoIdentidad() == null || req.getDocumentoIdentidad().trim().isEmpty()) {
            response.put("status", "error");
            response.put("message", "Documento de identidad es requerido");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (req.getNacionalidad() == null || req.getNacionalidad().trim().isEmpty()) {
            response.put("status", "error");
            response.put("message", "Nacionalidad es requerida");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (req.getFechaNacimiento() == null) {
            response.put("status", "error");
            response.put("message", "Fecha de nacimiento es requerida");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (req.getCorreo() == null || req.getCorreo().trim().isEmpty()) {
            response.put("status", "error");
            response.put("message", "Correo es requerido");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (req.getContrasena() == null || req.getContrasena().trim().isEmpty()) {
            response.put("status", "error");
            response.put("message", "Contraseña es requerida");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        String correoLimpio = req.getCorreo().trim();
        String contrasenaLimpia = req.getContrasena().trim();

        //  Validar formato de correo
        if (!correoLimpio.matches(EMAIL_REGEX)) {
            response.put("status", "error");
            response.put("message", "Formato de correo no válido");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Regla mínima de seguridad para contraseña
        if (contrasenaLimpia.length() < 8) {
            response.put("status", "error");
            response.put("message", "La contraseña debe tener al menos 8 caracteres");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Verificar si correo ya existe
        if (usuarioRepository.existsByCorreo(correoLimpio)) {
            response.put("status", "error");
            response.put("message", "Correo ya registrado");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        // Verificar si documento de identidad ya existe
        if (usuarioRepository.existsByDocumentoIdentidad(req.getDocumentoIdentidad().trim())) {
            response.put("status", "error");
            response.put("message", "Documento de identidad ya registrado");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        //  Crear entidad Usuario y guardar en BD
        Usuario usuario = new Usuario();
        usuario.setNombre(req.getNombre().trim());
        usuario.setApellido(req.getApellido().trim());
        usuario.setDocumentoIdentidad(req.getDocumentoIdentidad().trim());
        usuario.setNacionalidad(req.getNacionalidad().trim());
        usuario.setCorreo(correoLimpio);
        usuario.setTelefono(req.getTelefono());
        usuario.setFechaNacimiento(req.getFechaNacimiento());

        //Aqui encripto la contraseña
        String hashedPassword = passwordEncoder.encode(contrasenaLimpia);
        usuario.setContrasena(hashedPassword);

        Usuario guardado = usuarioRepository.save(usuario);

        //  Respuesta al frontend
        // No devolvemos la contraseña
        guardado.setContrasena(null);

        response.put("status", "success");
        response.put("message", "Usuario registrado");
        response.put("usuario", guardado);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
