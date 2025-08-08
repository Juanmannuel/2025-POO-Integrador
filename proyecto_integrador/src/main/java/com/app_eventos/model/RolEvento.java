package com.app_eventos.model;
import com.app_eventos.model.enums.TipoRol;
import com.app_eventos.model.interfaces.IEventoConCupo;

import java.time.LocalDateTime;
import java.util.Optional;

public class RolEvento {

    private Long id;
    private Evento evento;
    private Persona persona;
    private TipoRol rol;
    private LocalDateTime fechaAsignacion;
    private boolean baja = false;
    private LocalDateTime fechaBaja;

    // Constructor con validaciones de negocio
    public RolEvento(Evento evento, Persona persona, TipoRol rol) {
        validarAsignacionRol(evento, persona, rol);
        this.evento = evento;
        this.persona = persona;
        this.rol = rol;
        this.fechaAsignacion = LocalDateTime.now();
    }

    public RolEvento() {}

    // Métodos de utilidad
    public boolean esOrganizador() {
        return rol == TipoRol.ORGANIZADOR;
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

    // ⭐ MÉTODOS DE NEGOCIO DEL MODELO RICO

    /**
     * Valida que se pueda asignar el rol según las reglas de negocio
     */
    private void validarAsignacionRol(Evento evento, Persona persona, TipoRol rol) {
        if (evento == null || persona == null || rol == null) {
            throw new IllegalArgumentException("Evento, persona y rol no pueden ser nulos");
        }
        
        // REGLA: Solo eventos confirmados permiten inscripciones
        if (!evento.puedeInscribirParticipantes()) {
            throw new IllegalStateException("No se puede asignar roles a eventos no confirmados o finalizados");
        }
        
        // REGLA: Una persona no puede tener más de un rol en el mismo evento
        if (evento.personaTieneRol(persona)) {
            throw new IllegalStateException("La persona ya tiene un rol asignado en este evento");
        }
        
        // REGLA: Validar cupo si es participante
        if (rol == TipoRol.PARTICIPANTE && evento instanceof IEventoConCupo) {
            IEventoConCupo eventoConCupo = (IEventoConCupo) evento;
            if (!eventoConCupo.tieneCupoDisponible()) {
                throw new IllegalStateException("No hay cupo disponible para este evento");
            }
        }
    }
    
    /**
     * Dar de baja la participación (borrado lógico)
     */
    public void darDeBaja() {
        if (this.baja) {
            throw new IllegalStateException("Esta asignación ya está dada de baja");
        }
        this.baja = true;
        this.fechaBaja = LocalDateTime.now();
    }
    
    /**
     * Reactivar la participación
     */
    public void reactivar() {
        if (!this.baja) {
            throw new IllegalStateException("Esta asignación ya está activa");
        }
        this.baja = false;
        this.fechaBaja = null;
    }
    
    /**
     * Consulta si la participación está activa
     */
    public boolean estaActivo() {
        return !baja;
    }
    
    /**
     * Consulta si se puede modificar la participación
     */
    public boolean puedeModificar() {
        return estaActivo() && evento.puedeInscribirParticipantes();
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

    public LocalDateTime getFechaAsignacion() {
        return fechaAsignacion;
    }

    public boolean isBaja() {
        return baja;
    }

    public LocalDateTime getFechaBaja() {
        return fechaBaja;
    }

    @Override
    public String toString() {
        return persona + " se encuentra como " + rol + " en el evento " + evento.getNombre();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        RolEvento that = (RolEvento) obj;
        return persona.equals(that.persona) && rol == that.rol;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(persona, rol);
    }
}
