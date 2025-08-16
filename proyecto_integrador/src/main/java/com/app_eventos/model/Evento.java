package com.app_eventos.model;

import jakarta.persistence.*;
import com.app_eventos.model.enums.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/** Entidad base de eventos con invariantes en el dominio. */
@Entity
@Table(name = "evento")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Evento {

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

    // ========= Constructores =========

    protected Evento() {
        this.estado = EstadoEvento.PLANIFICACIÓN;
    }

    public Evento(String nombre, LocalDateTime fechaInicio, LocalDateTime fechaFin, TipoEvento tipoEvento) {
        setNombre(nombre);
        asignarFechas(fechaInicio, fechaFin); // único punto de verdad
        setTipoEvento(tipoEvento);
        this.estado = EstadoEvento.PLANIFICACIÓN;
    }

    /** Único método que valida y asigna ambas fechas. */
    private void asignarFechas(LocalDateTime ini, LocalDateTime fin) {
        Objects.requireNonNull(ini, "La fecha y hora de inicio es obligatoria.");
        Objects.requireNonNull(fin, "La fecha y hora de fin es obligatoria.");

        LocalDateTime limite = LocalDate.now()
                .atTime(23, 59, 59);

        if (!fin.isAfter(ini))
            throw new IllegalArgumentException("La fecha/hora de fin debe ser posterior al inicio.");
        if (ini.isAfter(limite))
            throw new IllegalArgumentException("La fecha/hora de inicio no puede superar 2 años desde hoy.");
        if (fin.isAfter(limite))
            throw new IllegalArgumentException("La fecha/hora de fin no puede superar 2 años desde hoy.");

        this.fechaInicio = ini;
        this.fechaFin = fin;
    }

    /** API cómoda desde UI/Servicio: setea y valida ambas fechas de una vez. */
    public void setFechas(LocalDate fIni, LocalTime hIni, LocalDate fFin, LocalTime hFin) {
        asignarFechas(LocalDateTime.of(fIni, hIni), LocalDateTime.of(fFin, hFin));
    }

    /** Setters individuales delegan para mantener invariantes. */
    public void setFechaInicio(LocalDateTime nuevaInicio) { asignarFechas(nuevaInicio, this.fechaFin); }
    public void setFechaFin(LocalDateTime nuevaFin)       { asignarFechas(this.fechaInicio, nuevaFin); }

    // ========= Reglas de estado =========
    public void cambiarEstado(EstadoEvento nuevoEstado) {
        Objects.requireNonNull(nuevoEstado, "Estado obligatorio");
        if (this.estado == nuevoEstado) return;

        LocalDateTime now = LocalDateTime.now();

        switch (this.estado) {
            case PLANIFICACIÓN -> {
                if (nuevoEstado != EstadoEvento.CONFIRMADO)
                    throw new IllegalStateException("Desde PLANIFICACIÓN solo puede pasar a CONFIRMADO.");
                if (this.fechaInicio.isBefore(now))
                    throw new IllegalStateException("No se puede confirmar con fecha/hora de inicio pasada.");
                validarRolesObligatorios(); // exige al menos ORGANIZADOR
                this.estado = EstadoEvento.CONFIRMADO;
            }
            case CONFIRMADO, EJECUCIÓN, FINALIZADO -> {
                throw new IllegalStateException("No se permite cambiar manualmente el estado desde " + this.estado + ".");
            }
        }
    }

    /** Setter “inteligente”: respeta la máquina de estados. */
    public void setEstado(EstadoEvento nuevo) {
        if (nuevo == null) throw new IllegalArgumentException("El estado del evento es obligatorio.");
        if (this.estado == null || this.estado == nuevo) {
            this.estado = nuevo; // inicialización o no-op
        } else {
            cambiarEstado(nuevo); // valida transiciones
        }
    }

    // ========= Estados automáticos =========
    public void verificarEstadoAutomatico() {
        LocalDateTime now = LocalDateTime.now();

        // CONFIRMADO -> EJECUCIÓN cuando llega la hora de inicio y todavía no pasó la de fin
        if (estado == EstadoEvento.CONFIRMADO && !now.isBefore(fechaInicio) && now.isBefore(fechaFin)) {
            this.estado = EstadoEvento.EJECUCIÓN;
            return;
        }

        // CONFIRMADO/EJECUCIÓN -> FINALIZADO cuando pasó la hora de fin
        if ((estado == EstadoEvento.CONFIRMADO || estado == EstadoEvento.EJECUCIÓN) && !now.isBefore(fechaFin)) {
            this.estado = EstadoEvento.FINALIZADO;
        }
    }

    public boolean esInscribible() {
        LocalDateTime now = LocalDateTime.now();
        return this.estado == EstadoEvento.CONFIRMADO && now.isBefore(getFechaFin());
    }

    public boolean Inscripcion() { return esInscribible(); }

    protected void validarPuedeInscribir() {
        if (!esInscribible()) throw new IllegalStateException("No se permite inscribir.");
    }

    // ========= Roles =========

    /** Helper para congelar gestión de roles en EJECUCIÓN/FINALIZADO. */
    private void validarPuedeGestionarRoles() {
        // Por si el objeto quedó en memoria y el tiempo avanzó
        verificarEstadoAutomatico();

        if (estado == EstadoEvento.EJECUCIÓN || estado == EstadoEvento.FINALIZADO) {
            throw new IllegalStateException(
                "No se pueden gestionar roles cuando el evento está en EJECUCIÓN o FINALIZADO."
            );
        }
        // Si querés permitir solo en PLANIFICACIÓN (no en CONFIRMADO), reemplazá por:
        // if (estado != EstadoEvento.PLANIFICACIÓN) { throw new IllegalStateException(...); }
    }

    /** Gancho para validaciones específicas de subtipos cuando asignan roles. */
    protected void validarRestriccionesRol(TipoRol rol, Persona persona) { /* por defecto nada */ }

    protected void validarRolesObligatorios() { validarInvariantes(); }

    public void agregarResponsable(Persona persona, TipoRol rol) {
        validarPuedeGestionarRoles(); // <--- NUEVO

        if (persona == null || rol == null) throw new IllegalArgumentException("Persona y rol requeridos.");
        if (!rolPermitido(rol)) throw new IllegalArgumentException("Rol no permitido para " + tipoEvento);

        boolean yaTieneRol = roles.stream().anyMatch(r -> r.getPersona().equals(persona));
        if (yaTieneRol) throw new IllegalArgumentException("La persona ya tiene un rol asignado en este evento.");

        validarRestriccionesRol(rol, persona);
        this.roles.add(new RolEvento(this, persona, rol));
    }

    public void borrarResponsable(Persona persona, TipoRol rol) {
        validarPuedeGestionarRoles(); // <--- también congelamos bajas

        if (persona == null || rol == null) return;
        roles.removeIf(r -> r.getPersona().equals(persona) && r.getRol() == rol);
    }

    public List<Persona> obtenerResponsables(TipoRol rol) {
        return roles.stream().filter(r -> r.getRol() == rol).map(RolEvento::getPersona).toList();
    }

    public boolean personaTieneRol(Persona persona) {
        return roles.stream().anyMatch(r -> r.getPersona().equals(persona));
    }

    public long contarPorRol(TipoRol rol) {
        return roles.stream().filter(r -> r.getRol() == rol).count();
    }

    /** Invariante por defecto: al menos un ORGANIZADOR. Subclases pueden reforzar. */
    public void validarInvariantes() {
        if (contarPorRol(TipoRol.ORGANIZADOR) == 0)
            throw new IllegalStateException("Todo evento debe tener al menos un ORGANIZADOR.");
    }

    protected abstract boolean rolPermitido(TipoRol rol);

    public EnumSet<TipoRol> rolesPermitidosParaAsignacion() {
        EnumSet<TipoRol> set = EnumSet.noneOf(TipoRol.class);
        for (TipoRol r : TipoRol.values()) if (rolPermitido(r)) set.add(r);
        return set;
    }

    public void agregarRol(RolEvento rol) { if (rol != null) roles.add(rol); }

    // ========= Getters/Setters =========

    public Long getIdEvento() { return idEvento; }
    public void setIdEvento(Long idEvento) { this.idEvento = idEvento; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("Nombre obligatorio.");
        this.nombre = nombre;
    }

    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public LocalDateTime getFechaFin()    { return fechaFin; }

    public EstadoEvento getEstado()       { return estado; }
    public TipoEvento getTipoEvento()     { return tipoEvento; }
    public void setTipoEvento(TipoEvento tipoEvento) {
        if (tipoEvento == null) throw new IllegalArgumentException("El tipo de evento es obligatorio.");
        this.tipoEvento = tipoEvento;
    }

    public List<RolEvento> getRoles() { return new ArrayList<>(roles); }
}
