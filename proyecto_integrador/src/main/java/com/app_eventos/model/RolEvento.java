package com.app_eventos.model;

import com.app_eventos.model.enums.TipoRol;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rol_evento")
public class RolEvento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evento_idevento", nullable = false)
    private Evento evento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "persona_idpersona", nullable = false)
    private Persona persona;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 32)
    private TipoRol rol;

    @Column(name = "fecha_asignacion", nullable = false)
    private LocalDateTime fechaAsignacion;

    public RolEvento() {}

    public RolEvento(Evento evento, Persona persona, TipoRol rol) {
        this.evento = evento;
        this.persona = persona;
        this.rol = rol;
        this.fechaAsignacion = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        if (fechaAsignacion == null) fechaAsignacion = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Evento getEvento() { return evento; }
    public void setEvento(Evento evento) { this.evento = evento; }
    public Persona getPersona() { return persona; }
    public void setPersona(Persona persona) { this.persona = persona; }
    public TipoRol getRol() { return rol; }
    public void setRol(TipoRol rol) { this.rol = rol; }

    // >>> GETTER/SETTER necesarios para que HQL resuelva la propiedad
    public LocalDateTime getFechaAsignacion() { return fechaAsignacion; }
    public void setFechaAsignacion(LocalDateTime fechaAsignacion) { this.fechaAsignacion = fechaAsignacion; }
}
