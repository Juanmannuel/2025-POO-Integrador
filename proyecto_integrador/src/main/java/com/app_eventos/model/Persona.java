package com.app_eventos.model;

import jakarta.persistence.*;

/** Entidad Persona. Solo anotaciones JPA. */
@Entity @Table(name = "persona")
public class Persona {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idPersona")
    private Long idPersona;

    @Column(nullable = false) private String nombre;
    @Column(nullable = false) private String apellido;
    @Column(nullable = false) private String dni;
    private String telefono;
    private String email;

    public Persona() { 
        // JPA requiere un constructor sin parámetros
    }
    public Persona(String nombre, String apellido, String dni, String telefono, String email) {
        setNombre(nombre); setApellido(apellido); setDni(dni); setTelefono(telefono); setEmail(email);
    }

    // ===== validaciones ya existentes =====
    public void setDni(String dni){
        if (dni == null || !dni.matches("\\d{7,10}"))
            throw new IllegalArgumentException("DNI inválido");
        this.dni = dni;
    }
    public void setEmail(String email){
        if (email != null && !email.matches("^[\\w-.]+@[\\w-]+\\.[a-zA-Z]{2,}$"))
            throw new IllegalArgumentException("Email inválido");
        this.email = email;
    }
    public void setNombre(String nombre){
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("Nombre vacío");
        this.nombre = cap(nombre);
    }
    public void setApellido(String apellido){
        if (apellido == null || apellido.isBlank()) throw new IllegalArgumentException("Apellido vacío");
        this.apellido = cap(apellido);
    }
    public void setTelefono(String telefono){
        if (telefono == null || !telefono.matches("\\d{6,15}"))
            throw new IllegalArgumentException("Teléfono inválido");
        this.telefono = telefono;
    }

    private String cap(String t){ 
        return t.substring(0,1).toUpperCase()+t.substring(1).toLowerCase(); 
    }

    public void actualizarCon(Persona o){
        setNombre(o.getNombre()); setApellido(o.getApellido());
        setDni(o.getDni()); setTelefono(o.getTelefono()); setEmail(o.getEmail());
    }

    // ===== getters =====
    public Long getIdPersona(){ 
        return idPersona; 
    }

    public String getNombre(){ 
        return nombre; 
    }
    public String getApellido(){ 
        return apellido; 
    }
    
    public String getDni(){ 
        return dni; 
    }

    public String getTelefono(){ 
        return telefono; 
    }

    public String getEmail(){ 
        return email; 
    }

    @Override 
    public String toString(){ 
        return apellido + ", " + nombre; 
    }
}
