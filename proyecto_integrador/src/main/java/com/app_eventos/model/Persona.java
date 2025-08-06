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
        this.telefono = telefono;
        setEmail(email);
    }

    public Persona() {}

    // Validaciones modelo rico
    public void setDni(String dni) {
        if (dni == null || dni.length() < 7 || dni.length() > 10) {
            throw new IllegalArgumentException("DNI inválido");
        }
        this.dni = dni;
    }

    public void setEmail(String email) {
        if (email != null && !email.matches("^[\\w-.]+@[\\w-]+\\.[a-zA-Z]{2,}$")) {
            throw new IllegalArgumentException("Formato de email inválido");
        }
        this.email = email;
    }

    public void setNombre(String nombre) {
        this.nombre = capitalizar(nombre);
    }

    public void setApellido(String apellido) {
        this.apellido = capitalizar(apellido);
    }

    private String capitalizar(String texto) {
        if (texto == null || texto.isBlank()) return texto;
        return texto.substring(0, 1).toUpperCase() + texto.substring(1).toLowerCase();
    }

    // Getters

    public Long getIdPersona() {
        return idPersona;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getDni() {
        return dni;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    // FACTORY METHOD - Lógica de negocio para crear persona
    public static Persona crearPersona(String nombre, String apellido, String dni, 
                                      String telefono, String email) {
        Persona persona = new Persona(nombre, apellido, dni, telefono, email);
        persona.aplicarReglas();
        return persona;
    }
    
    // Lógica de negocio para modificar datos
    public void modificarDatos(String nombre, String apellido, String telefono, String email) {
        setNombre(nombre);
        setApellido(apellido);
        this.telefono = telefono;
        setEmail(email);
        aplicarReglas();
    }
    
    // Lógica de negocio: verificar si puede ser eliminada
    public boolean puedeSerEliminada() {
        // TODO: Verificar si tiene eventos asociados
        return true; // Por ahora permite eliminar
    }
    
    // Métodos de utilidad
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }
    
    // Aplicar reglas de negocio (capitalización, etc.)
    private void aplicarReglas() {
        // Las reglas ya se aplican en los setters
    }
    
    public void setIdPersona(Long idPersona) {
        this.idPersona = idPersona;
    }

    @Override
    public String toString() {
        return apellido + ", " + nombre;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Persona persona = (Persona) obj;
        return dni != null && dni.equals(persona.dni);
    }
    
    @Override
    public int hashCode() {
        return dni != null ? dni.hashCode() : 0;
    }
}
