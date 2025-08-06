package com.app_eventos.model;

import com.app_eventos.model.enums.EstadoEvento;
import com.app_eventos.model.enums.TipoRol;
import com.app_eventos.model.interfaces.IEventoConCupo;

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
    
    // FACTORY METHOD - Lógica de negocio para inscribir participante
    public static RolEvento inscribirParticipante(Evento evento, Persona persona) {
        // Validar estado del evento
        evento.validarInscripcion(persona);
        
        // Validar cupo si el evento lo requiere
        if (evento instanceof IEventoConCupo eventoConCupo) {
            if (eventoConCupo.estaCompleto()) {
                throw new IllegalStateException("El evento ha alcanzado su cupo máximo");
            }
        }
        
        return new RolEvento(evento, persona, TipoRol.PARTICIPANTE);
    }
    
    // FACTORY METHOD - Lógica de negocio para asignar responsable
    public static RolEvento asignarResponsable(Evento evento, Persona persona) {
        return new RolEvento(evento, persona, TipoRol.ORGANIZADOR);
    }
    
    // FACTORY METHOD - Lógica de negocio para asignar instructor
    public static RolEvento asignarInstructor(Evento evento, Persona persona) {
        if (!(evento instanceof Taller)) {
            throw new IllegalStateException("Solo se pueden asignar instructores a talleres");
        }
        return new RolEvento(evento, persona, TipoRol.INSTRUCTOR);
    }
    
    // FACTORY METHOD - Lógica de negocio para asignar curador
    public static RolEvento asignarCurador(Evento evento, Persona persona) {
        if (!(evento instanceof Exposicion)) {
            throw new IllegalStateException("Solo se pueden asignar curadores a exposiciones");
        }
        return new RolEvento(evento, persona, TipoRol.CURADOR);
    }
    
    // FACTORY METHOD - Lógica de negocio para asignar artista
    public static RolEvento asignarArtista(Evento evento, Persona persona) {
        if (!(evento instanceof Concierto)) {
            throw new IllegalStateException("Solo se pueden asignar artistas a conciertos");
        }
        return new RolEvento(evento, persona, TipoRol.ARTISTA);
    }
    
    // Lógica de negocio para cancelar inscripción
    public boolean puedeSerCancelada() {
        return evento.getEstado() != EstadoEvento.FINALIZADO;
    }
    
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return persona + " como " + rol + " en evento " + evento.getNombre();
    }
}
