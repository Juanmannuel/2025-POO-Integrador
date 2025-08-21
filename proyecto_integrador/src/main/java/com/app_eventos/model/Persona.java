package com.app_eventos.model;

import jakarta.persistence.*;

@Entity
@Table(name = "persona")
public class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idPersona")
    private Long idPersona;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(nullable = false, length = 80)
    private String apellido;

    @Column(nullable = false, unique = true, length = 10) // 7–10 dígitos
    private String dni;

    @Column(nullable = false, length = 15) // 6–15 dígitos
    private String telefono;

    @Column(length = 254) // email puede ser null
    private String email;

    public Persona() {}

    public Persona(String nombre, String apellido, String dni, String telefono, String email) {
        setNombre(nombre);
        setApellido(apellido);
        setDni(dni);
        setTelefono(telefono);
        setEmail(email);
    }

    // Normalización automática antes de persistir o actualizar
    @PrePersist @PreUpdate
    private void normalize() {
        if (nombre != null)   nombre   = titleCase(nombre.trim());
        if (apellido != null) apellido = titleCase(apellido.trim());
        if (dni != null)      dni      = onlyDigits(dni);
        if (telefono != null) telefono = onlyDigits(telefono);
        if (email != null && !email.isBlank()) email = email.trim().toLowerCase();
        else email = null; // guardar null si viene vacío
    }

    // Setters con validación
    public void setDni(String dni){
        if (dni == null) throw new IllegalArgumentException("DNI requerido");
        String clean = onlyDigits(dni);
        if (!clean.matches("\\d{7,10}")) throw new IllegalArgumentException("DNI inválido (7 a 10 dígitos, sin puntos)");
        this.dni = clean;
    }

    // Email con validación y normalización
    public void setEmail(String email){
        if (email == null || email.isBlank()) { this.email = null; return; }
        String v = email.trim().toLowerCase();
        if (!v.matches("^[\\w.!#$%&'*+/=?^_`{|}~-]+@[\\w-]+(?:\\.[A-Za-z]{2,})+$"))
            throw new IllegalArgumentException("Email inválido");
        if (v.length() > 254) throw new IllegalArgumentException("Email demasiado largo");
        this.email = v;
    }

    public void setNombre(String nombre){
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("Nombre requerido");
        this.nombre = titleCase(nombre.trim());
    }

    public void setApellido(String apellido){
        if (apellido == null || apellido.isBlank()) throw new IllegalArgumentException("Apellido requerido");
        this.apellido = titleCase(apellido.trim());
    }

    public void setTelefono(String telefono){
        if (telefono == null) throw new IllegalArgumentException("Teléfono requerido");
        String clean = onlyDigits(telefono);
        if (!clean.matches("\\d{6,15}")) throw new IllegalArgumentException("Teléfono inválido (6 a 15 dígitos)");
        this.telefono = clean;
    }

    public void actualizarCon(Persona o){
        if (o == null) throw new IllegalArgumentException("Persona origen nula");
        setNombre(o.getNombre());
        setApellido(o.getApellido());
        setDni(o.getDni());
        setTelefono(o.getTelefono());
        setEmail(o.getEmail());
    }


    // Utiles de normalización
    private static String onlyDigits(String s) { return s.replaceAll("\\D+", ""); }

    // Separa cada palabra y subpalabra separada por espacio o guion
    private static String titleCase(String text) {
        String[] parts = text.toLowerCase().split("\\s+");
        StringBuilder out = new StringBuilder();
        for (int i=0;i<parts.length;i++) {
            if (i>0) out.append(' ');
            out.append(capEachHyphen(parts[i]));
        }
        return out.toString();
    }
    private static String capEachHyphen(String token){
        String[] sub = token.split("-");
        for (int j=0;j<sub.length;j++){
            if (sub[j].length()>0)
                sub[j] = sub[j].substring(0,1).toUpperCase() + (sub[j].length()>1? sub[j].substring(1):"");
        }
        return String.join("-", sub);
    }

    // Getters
    public Long getIdPersona(){ return idPersona; }
    public String getNombre(){ return nombre; }
    public String getApellido(){ return apellido; }
    public String getDni(){ return dni; }
    public String getTelefono(){ return telefono; }
    public String getEmail(){ return email; }

    @Override public String toString(){ return apellido + ", " + nombre; }
}
