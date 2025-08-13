package com.app_eventos.model;

import com.app_eventos.model.enums.TipoRol;
import jakarta.persistence.*;

/** Rol asignado a una persona dentro de un evento. */
@Entity
@Table(name = "rol_evento")
public class RolEvento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK: rol_evento.evento_idevento → evento.idEvento */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evento_idevento", nullable = false)
    private Evento evento;

    /** FK: rol_evento.persona_idpersona → persona.idPersona */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "persona_idpersona", nullable = false)
    private Persona persona;

    /** Columna válida en tu BD: "tipo". */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 32)
    private TipoRol rol;

    public RolEvento() {}
    public RolEvento(Evento evento, Persona persona, TipoRol rol) {
        this.evento = evento; this.persona = persona; this.rol = rol;
    }

    public Long getId() { return id; }
    public Evento getEvento() { return evento; }
    public void setEvento(Evento evento) { this.evento = evento; }
    public Persona getPersona() { return persona; }
    public void setPersona(Persona persona) { this.persona = persona; }
    public TipoRol getRol() { return rol; }
    public void setRol(TipoRol rol) { this.rol = rol; }
}
