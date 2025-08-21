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
        asignarFechas(fechaInicio, fechaFin); // único punto de verdad
        setTipoEvento(tipoEvento);
        this.estado = EstadoEvento.PLANIFICACIÓN;
    }

    // Fechas

    /** Único método que valida y asigna ambas fechas. */
    private void asignarFechas(LocalDateTime ini, LocalDateTime fin) {
        Objects.requireNonNull(ini, "La fecha y hora de inicio es obligatoria.");
        Objects.requireNonNull(fin, "La fecha y hora de fin es obligatoria.");
        if (!fin.isAfter(ini))
            throw new IllegalArgumentException("La fecha/hora de fin debe ser posterior al inicio.");
        this.fechaInicio = ini;
        this.fechaFin = fin;
    }

    // Setters para fechas con LocalDate y LocalTime
    public void setFechas(LocalDate fIni, LocalTime hIni, LocalDate fFin, LocalTime hFin) {
        asignarFechas(LocalDateTime.of(fIni, hIni), LocalDateTime.of(fFin, hFin));
    }

    // Setters individuales delegan para mantener invariantes.
    public void setFechaInicio(LocalDateTime nuevaInicio) { asignarFechas(nuevaInicio, this.fechaFin); }
    public void setFechaFin(LocalDateTime nuevaFin)       { asignarFechas(this.fechaInicio, nuevaFin); }

    // Reglas de estado
    // Cambia el estado del evento, validando la transición.
    public void cambiarEstado(EstadoEvento nuevoEstado) {
        Objects.requireNonNull(nuevoEstado, "Estado obligatorio");
        if (this.estado == nuevoEstado) return;

        // Solo se permite PLANIFICACIÓN -> CONFIRMADO
        if (this.estado != EstadoEvento.PLANIFICACIÓN)
            throw new IllegalStateException("No se permite cambiar manualmente el estado desde " + this.estado + ".");

        if (this.fechaInicio.isBefore(LocalDateTime.now()))
            throw new IllegalStateException("No se puede confirmar con fecha/hora de inicio pasada.");

        // Reglas de dominio (Evento + overrides de subtipos, p.ej. Taller)
        validarRolesObligatorios();

        this.estado = EstadoEvento.CONFIRMADO;
    }

    // Permite inicializar un evento nuevo en CONFIRMADO sin roles.
        public void setEstado(EstadoEvento nuevo) {
        if (nuevo == null) throw new IllegalArgumentException("El estado del evento es obligatorio.");

        // Entidad NUEVA (sin id): forzar PLANIFICACIÓN siempre
        if (this.idEvento == null) {
            this.estado = EstadoEvento.PLANIFICACIÓN;
            return;
        }

        // Entidad ya persistida -> respetar máquina de estados
        if (this.estado == nuevo) return;
        cambiarEstado(nuevo);
    }

    // Avance automático según fechas.
    public void verificarEstadoAutomatico() {
        LocalDateTime now = LocalDateTime.now();

        // CONFIRMADO -> EJECUCIÓN cuando llega inicio (y antes de fin)
        if (estado == EstadoEvento.CONFIRMADO && !now.isBefore(fechaInicio) && now.isBefore(fechaFin)) {
            // Antes de ejecutar, ahora sí exigimos invariantes (roles listos).
            validarRolesObligatorios();
            this.estado = EstadoEvento.EJECUCIÓN;
            return;
        }

        // CONFIRMADO/EJECUCIÓN -> FINALIZADO cuando pasó la hora de fin
        if ((estado == EstadoEvento.CONFIRMADO || estado == EstadoEvento.EJECUCIÓN) && !now.isBefore(fechaFin)) {
            this.estado = EstadoEvento.FINALIZADO;
        }
    }

    // Inscripción

    public boolean esInscribible() {
        LocalDateTime now = LocalDateTime.now();
        // Solo inscribible si está confirmado, no vencido, y cumple invariantes (roles listos)
        return this.estado == EstadoEvento.CONFIRMADO
                && now.isBefore(getFechaFin())
                && invariantesCumplidasDeFormaSegura();
    }

    public boolean Inscripcion() { return esInscribible(); }

    // Lanza si no se puede inscribir
    protected void validarPuedeInscribir() {
        if (!esInscribible()) throw new IllegalStateException("No se permite inscribir.");
    }

    // Evalúa invariantes sin lanzar (para esInscribible)
    private boolean invariantesCumplidasDeFormaSegura() {
        try {
            validarInvariantes();
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    // Roles

    // Helper para congelar gestión de roles en EJECUCIÓN/FINALIZADO.
    private void validarPuedeGestionarRoles() {
        // verificar estado antes de permitir asignar o borrar roles
        verificarEstadoAutomatico();

        if (estado == EstadoEvento.EJECUCIÓN || estado == EstadoEvento.FINALIZADO) {
            throw new IllegalStateException(
                "No se pueden gestionar roles cuando el evento está en EJECUCIÓN o FINALIZADO."
            );
        }
    }

    // Gancho para validaciones específicas de subtipos cuando asignan roles.
    protected void validarRestriccionesRol(TipoRol rol, Persona persona) {}

    // Wrapper para mantener semántica previa.
    protected void validarRolesObligatorios() { validarInvariantes(); }

    public void agregarResponsable(Persona persona, TipoRol rol) {
        validarPuedeGestionarRoles(); // congelamos altas en ejecución/finalizado
        if (persona == null || rol == null) throw new IllegalArgumentException("Persona y rol requeridos.");
        if (!rolPermitido(rol)) throw new IllegalArgumentException("Rol no permitido para " + tipoEvento);

        boolean yaTieneRol = roles.stream().anyMatch(r -> r.getPersona().equals(persona));
        if (yaTieneRol) throw new IllegalArgumentException("La persona ya tiene un rol asignado en este evento.");

        validarRestriccionesRol(rol, persona);
        this.roles.add(new RolEvento(this, persona, rol));
    }

    public void borrarResponsable(Persona persona, TipoRol rol) {
        validarPuedeGestionarRoles(); // congelamos bajas también
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

    // Invariante por defecto al menos un ORGANIZADOR
    public void validarInvariantes() {
        if (contarPorRol(TipoRol.ORGANIZADOR) == 0)
            throw new IllegalStateException("El evento debe tener al menos un ORGANIZADOR.");
    }

    protected abstract boolean rolPermitido(TipoRol rol);

    public EnumSet<TipoRol> rolesPermitidosParaAsignacion() {
        EnumSet<TipoRol> set = EnumSet.noneOf(TipoRol.class);
        for (TipoRol r : TipoRol.values()) if (rolPermitido(r)) set.add(r);
        return set;
    }

    public void agregarRol(RolEvento rol) { if (rol != null) roles.add(rol); }

    // Getters/Setters

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

    // Copia defensiva para no exponer la colección interna.
    public List<RolEvento> getRoles() { return new ArrayList<>(roles); }

    /*  Valida que el evento no pueda modificarse si está en ejecución o finalizado. */
    public void validarPuedeModificar() {
        verificarEstadoAutomatico();
        if (estado == EstadoEvento.EJECUCIÓN || estado == EstadoEvento.FINALIZADO) {
            throw new IllegalStateException("El evento está en ejecución o finalizó, no se puede modificar");
        }
    }

    /* Valida que las fechas de alta sean a partir de hoy (sin pasado). */
    public static void validarFechasAlta(LocalDate fIni, LocalDate fFin) {
        if (fIni == null || fFin == null) {
            throw new IllegalArgumentException("Las fechas del evento son obligatorias");
        }
        LocalDate hoy = LocalDate.now();
        if (fIni.isBefore(hoy) || fFin.isBefore(hoy)) {
            throw new IllegalArgumentException("Las fechas del evento deben ser desde hoy en adelante");
        }
    }
}