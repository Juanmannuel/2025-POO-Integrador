package com.app_eventos.model;

import jakarta.persistence.*;
import com.app_eventos.model.enums.TipoRol;
import com.app_eventos.model.interfaces.IEventoConCupo;

/**
 * Tabla puente Evento–Persona con atributo "tipo" (rol).
 * Se respeta tu modelo: baja lógica y helpers de negocio.
 */
@Entity
@Table(name = "rol_evento",
       uniqueConstraints = @UniqueConstraint(columnNames = {"evento_idEvento","persona_idPersona","tipo"}))
public class RolEvento {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "evento_idEvento", referencedColumnName = "idEvento")
    private Evento evento;

    @ManyToOne(optional = false)
    @JoinColumn(name = "persona_idPersona", referencedColumnName = "idPersona")
    private Persona persona;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoRol rol;

    @Column(name = "deletedAt")
    private java.time.LocalDateTime deletedAt; // baja lógica

    public RolEvento() {}
    public RolEvento(Evento evento, Persona persona, TipoRol rol) {
        validarAsignacionRol(evento, persona, rol);
        this.evento = evento; this.persona = persona; this.rol = rol;
    }

    // ===== helpers de negocio existentes =====
    public boolean esOrganizador() { 
        return rol == TipoRol.ORGANIZADOR; 
    }
    public boolean esInstructor() { 
        return rol == TipoRol.INSTRUCTOR; 
    }
    public boolean esCurador(){ 
        return rol == TipoRol.CURADOR; 
    }
    public boolean esArtista() { 
        return rol == TipoRol.ARTISTA; 
    }
    public boolean esParticipante() { 
        return rol == TipoRol.PARTICIPANTE; 
    }

    private void validarAsignacionRol(Evento evento, Persona persona, TipoRol rol) {
        if (evento == null || persona == null || rol == null)
            throw new IllegalArgumentException("Evento, persona y rol no pueden ser nulos");
        if (!evento.Inscripcion())
            throw new IllegalStateException("No se puede asignar roles a eventos no confirmados o finalizados");
        if (rol == TipoRol.PARTICIPANTE && evento instanceof IEventoConCupo conCupo && !conCupo.tieneCupoDisponible())
            throw new IllegalStateException("No hay cupo disponible para este evento");
    }

    public void darDeBaja() { 
        if (estaActivo()) this.deletedAt = java.time.LocalDateTime.now(); 
        else throw new IllegalStateException("Ya dada de baja"); 
    }
    public void reactivar() { 
        if (!estaActivo()) this.deletedAt = null; 
        else throw new IllegalStateException("Ya activa"); 
    }
    public boolean estaActivo() { 
        return deletedAt == null; 
    }
    /** usado por ABMParticipanteController */
    public boolean puedeModificar() { 
        return estaActivo() && evento.Inscripcion(); 
    }
    public boolean isBaja() { 
        return deletedAt != null; 
    }

    // ===== getters/setters usados por Repositorio =====
    public Long getId() { return id; }
    public Evento getEvento() { return evento; }
    public void setEvento(Evento evento) { this.evento = evento; }
    public Persona getPersona() { return persona; }
    public void setPersona(Persona persona) { this.persona = persona; }
    public TipoRol getRol() { return rol; }
    public void setRol(TipoRol rol) { this.rol = rol; }

    @Override public String toString() { return persona + " se encuentra como " + rol + " en el evento " + evento.getNombre(); }
    @Override public boolean equals(Object o){
        if(this==o) return true;
        if(!(o instanceof RolEvento x)) return false;
        return persona.equals(x.persona) && rol == x.rol;
    }
    @Override public int hashCode(){ return java.util.Objects.hash(persona, rol); }
}
