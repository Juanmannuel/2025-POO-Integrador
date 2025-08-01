package com.app_eventos.model;

import com.app_eventos.model.enums.TipoRol;

public class RolEvento {

    private Long id;
    private Evento evento;
    private Persona persona;
    private TipoRol rol;

    // Constructor
    public RolEvento(Evento evento, Persona persona, TipoRol rol) {
        if (evento == null || persona == null || rol == null) {
            throw new IllegalArgumentException("Evento, persona y rol no pueden ser nulos");
        }
        this.evento = evento;
        this.persona = persona;
        this.rol = rol;
    }

    public RolEvento() {}

    // Métodos de utilidad
    public boolean esResponsable() {
        return rol == TipoRol.ORGANIZADOR || rol == TipoRol.ORGANIZADOR;
    }

    public boolean esInstructor() {
        return rol == TipoRol.INSTRUCTOR;
    }

    public boolean esCurador() {
        return rol == TipoRol.CURADOR;
    }

    public boolean esArtista() {
        return rol == TipoRol.ARTISTA;
    }

    public boolean esParticipante() {
        return rol == TipoRol.PARTICIPANTE;
    }

    // Getters y setters

    public Long getId() {
        return id;
    }

    public Evento getEvento() {
        return evento;
    }

    public void setEvento(Evento evento) {
        this.evento = evento;
    }

    public Persona getPersona() {
        return persona;
    }

    public void setPersona(Persona persona) {
        this.persona = persona;
    }

    public TipoRol getRol() {
        return rol;
    }

    public void setRol(TipoRol rol) {
        this.rol = rol;
    }

    @Override
    public String toString() {
        return persona + " como " + rol + " en evento " + evento.getNombre();
    }
}
