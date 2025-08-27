package com.app_eventos.model;

import jakarta.persistence.*;
import com.app_eventos.model.enums.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Entity
@Table(name = "evento")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Evento {

    // Atributos
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idEvento")
    private Long idEvento;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private LocalDateTime fechaInicio;

    @Column(nullable = false)
    private LocalDateTime fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoEvento estado = EstadoEvento.PLANIFICACIÓN;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipoEvento", nullable = false)
    private TipoEvento tipoEvento;

    @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RolEvento> roles = new ArrayList<>();

    // Constructores
    protected Evento() {
        this.estado = EstadoEvento.PLANIFICACIÓN;
    }

    public Evento(String nombre, LocalDateTime fechaInicio, LocalDateTime fechaFin, TipoEvento tipoEvento) {
        setNombre(nombre);
        asignarFechas(fechaInicio, fechaFin);
        setTipoEvento(tipoEvento);
        setEstado(EstadoEvento.PLANIFICACIÓN);
    }

    public void asignarFechas(LocalDateTime ini, LocalDateTime fin) {
        if (ini == null || fin == null)
            throw new IllegalArgumentException("Las fechas/horas de inicio y fin son obligatorias.");
        LocalDateTime actual = LocalDateTime.now();
        if (ini.isBefore(actual))
            throw new IllegalArgumentException("La fecha/hora de inicio no puede estar en el pasado.");

        if (!fin.isAfter(ini))
            throw new IllegalArgumentException("La fecha/hora de fin debe ser posterior a la de inicio.");
        this.fechaInicio = ini;
        this.fechaFin = fin;
    }

    private boolean rolCumplido() {
        try { validarRol(); return true; }
        catch (RuntimeException ex) { return false; }
    }

    private void validarGestionarRoles() {
        verificarEstadoAutomatico();
        if (estado == EstadoEvento.EJECUCIÓN || estado == EstadoEvento.FINALIZADO)
            throw new IllegalStateException("No se pueden gestionar roles cuando el evento está en EJECUCIÓN o FINALIZADO.");
    }

    // Gestión de fechas
    public void setFechas(LocalDate fIni, LocalTime hIni, LocalDate fFin, LocalTime hFin) {
        asignarFechas(
            (fIni != null && hIni != null) ? LocalDateTime.of(fIni, hIni) : null,
            (fFin != null && hFin != null) ? LocalDateTime.of(fFin, hFin) : null
        );
    }

    // Gestión de estados
    public void cambiarEstado(EstadoEvento nuevoEstado) {
        if (this.estado == nuevoEstado) return;
        if (this.estado != EstadoEvento.PLANIFICACIÓN)
            throw new IllegalStateException("No se permite cambiar manualmente el estado desde " + this.estado + ".");
        if (this.fechaInicio.isBefore(LocalDateTime.now()))
            throw new IllegalStateException("No se puede confirmar con fecha/hora de inicio pasada.");
        validarRol();
        this.estado = nuevoEstado;
    }

    public void setEstado(EstadoEvento nuevo) {
        if (nuevo == null) throw new IllegalArgumentException("El estado del evento es obligatorio.");
        if (this.idEvento == null) {
            this.estado = EstadoEvento.PLANIFICACIÓN;
            return;
        }
        if (this.estado != nuevo) cambiarEstado(nuevo);
    }

    public void verificarEstadoAutomatico() {
        LocalDateTime actual = LocalDateTime.now();
        if (estado == EstadoEvento.CONFIRMADO && !actual.isBefore(fechaInicio) && actual.isBefore(fechaFin)) {
            validarRol();
            this.estado = EstadoEvento.EJECUCIÓN;
            return;
        }
        if ((estado == EstadoEvento.EJECUCIÓN) && !actual.isBefore(fechaFin)) {
            this.estado = EstadoEvento.FINALIZADO;
        }
    }

    // Inscripciones
    public boolean esInscribible() {
        LocalDateTime actual = LocalDateTime.now();
        return this.estado == EstadoEvento.CONFIRMADO && actual.isBefore(getFechaFin()) && rolCumplido();
    }

    protected void validarPuedeInscribir() {
        if (!esInscribible()) throw new IllegalStateException("No se permite inscribir.");
    }

    public void validarPuedeModificar() {
        verificarEstadoAutomatico();
        if (estado == EstadoEvento.EJECUCIÓN || estado == EstadoEvento.FINALIZADO)
            throw new IllegalStateException("El evento se encuentra en ejecución o finalizó, no se puede modificar");
    }

    // Gestión de roles
    protected void validarRestriccionesRol(TipoRol rol, Persona persona) {}

    public void agregarResponsable(Persona persona, TipoRol rol) {
        validarGestionarRoles();
        if (persona == null || rol == null) throw new IllegalArgumentException("Persona y rol requeridos.");
        if (!rolPermitido(rol)) throw new IllegalArgumentException("Rol no permitido para " + tipoEvento);
        if (roles.stream().anyMatch(r -> r.getPersona().equals(persona)))
            throw new IllegalArgumentException("La persona ya tiene un rol asignado en este evento.");
        validarRestriccionesRol(rol, persona);
        roles.add(new RolEvento(this, persona, rol));
    }

    public void borrarResponsable(Persona persona, TipoRol rol) {
        validarGestionarRoles();
        if (persona == null || rol == null || roles.isEmpty()) {
            return;
        }
        roles.removeIf(r -> r.getPersona().equals(persona) && r.getRol() == rol);
    }

    // si la persona ya tiene un rol, devuelve true
    public boolean personaTieneRol(Persona persona) {
        return roles.stream().anyMatch(r -> r.getPersona().equals(persona));
    }

    public int contarRol(TipoRol rol) {
        return (int) roles.stream().filter(r -> r.getRol() == rol).count();
    }

    public void validarRol() {
        if (contarRol(TipoRol.ORGANIZADOR) == 0)
            throw new IllegalStateException("El evento debe tener al menos un ORGANIZADOR.");
    }

    protected abstract boolean rolPermitido(TipoRol rol);

    // Limita las opciones que se muestran al asignar un rol segun el tipo de evento
    public EnumSet<TipoRol> rolesPermitidosAsignacion() {
        EnumSet<TipoRol> set = EnumSet.noneOf(TipoRol.class);
        for (TipoRol r : TipoRol.values()) if (rolPermitido(r)) set.add(r);
        return set;
    }

    public void agregarRol(RolEvento rol) { if (rol != null) roles.add(rol); }

    // Getters y Setters
    public Long getIdEvento() { return idEvento; }
    public void setIdEvento(Long idEvento) { this.idEvento = idEvento; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("Nombre obligatorio.");
        this.nombre = nombre;
    }

    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public LocalDateTime getFechaFin() { return fechaFin; }

    public EstadoEvento getEstado() { return estado; }

    public TipoEvento getTipoEvento() { return tipoEvento; }
    public void setTipoEvento(TipoEvento tipoEvento) {
        if (tipoEvento == null) throw new IllegalArgumentException("El tipo de evento es obligatorio.");
        this.tipoEvento = tipoEvento;
    }

    public List<RolEvento> getRoles() { return new ArrayList<>(roles); }
}