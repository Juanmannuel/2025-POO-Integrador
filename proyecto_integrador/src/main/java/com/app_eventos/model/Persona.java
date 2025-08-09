package com.app_eventos.model;

public class Persona {

    private Long idPersona;

    private String nombre;
    private String apellido;
    private String dni;
    private String telefono;
    private String email;

    // Constructor
    public Persona(String nombre, String apellido, String dni, String telefono, String email) {
        setNombre(nombre);
        setApellido(apellido);
        setDni(dni);
        setTelefono(telefono);
        setEmail(email);
    }

    //  Validación y asignación del DNI (solo dígitos, entre 7 y 10 caracteres)
    public void setDni(String dni) {
        if (dni == null || !dni.matches("\\d{7,10}")) {
            throw new IllegalArgumentException("DNI inválido: debe tener entre 7 y 10 dígitos");
        }
        this.dni = dni;
    }

    // Validación de formato de email (permite null)
    public void setEmail(String email) {
        if (email != null && !email.matches("^[\\w-.]+@[\\w-]+\\.[a-zA-Z]{2,}$")) {
            throw new IllegalArgumentException("Formato de email inválido");
        }
        this.email = email;
    }

    // Capitaliza automáticamente el nombre al asignarlo
    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }
        this.nombre = capitalizar(nombre);
    }

    // Capitaliza automáticamente el apellido al asignarlo
    public void setApellido(String apellido) {
        if (apellido == null || apellido.isBlank()) {
            throw new IllegalArgumentException("El apellido no puede estar vacío");
        }
        this.apellido = capitalizar(apellido);
    }

    // Asigna el teléfono sin validaciones estrictas 
    public void setTelefono(String telefono) {
        if (telefono == null || !telefono.matches("\\d{6,15}")) {
            throw new IllegalArgumentException("Teléfono inválido: debe tener entre 6 y 15 dígitos");
        }
        this.telefono = telefono;
    }

    // Método de utilidad para capitalizar texto: "roBERto" -> "Roberto"
    private String capitalizar(String texto) {
        if (texto == null || texto.isBlank()) return texto;
        return texto.substring(0, 1).toUpperCase() + texto.substring(1).toLowerCase();
    }

    // Método que actualiza los campos de esta persona con los de otra 
    public void actualizarCon(Persona otra) {
        setNombre(otra.getNombre());
        setApellido(otra.getApellido());
        setDni(otra.getDni());
        setTelefono(otra.getTelefono());
        setEmail(otra.getEmail());
    }

    // Getters
    public Long getIdPersona() { return idPersona; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getDni() { return dni; }
    public String getTelefono() { return telefono; }
    public String getEmail() { return email; }

    // Representación de la persona para cuadros de diálogo u otros usos
    @Override
    public String toString() {
        return apellido + ", " + nombre;
    }

    // Metodos de la capa de persistencia.
}