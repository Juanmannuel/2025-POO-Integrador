package com.app_eventos.services;

import com.app_eventos.model.*;
import com.app_eventos.model.enums.*;
import com.app_eventos.repository.Repositorio;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.*;

public class Servicio {

    private static final Servicio INSTANCE = new Servicio();
    public static Servicio getInstance() { return INSTANCE; }
    private Servicio() {}

    private final Repositorio repositorio = new Repositorio();
    private static final int MAX_YEARS_ADELANTE = 2;

    private LocalDateTime crearDateTime(LocalDate fecha, LocalTime hora) {
        return LocalDateTime.of(fecha, hora);
    }

    private void validarAlta(LocalDate fIni, LocalTime hIni, LocalDate fFin, LocalTime hFin, EstadoEvento estado) {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDate hoy = LocalDate.now();
        LocalDateTime inicio = LocalDateTime.of(fIni, hIni);
        LocalDateTime fin    = LocalDateTime.of(fFin, hFin);
        LocalDateTime limite = hoy.plusYears(MAX_YEARS_ADELANTE).atTime(23,59,59);
        if (!inicio.isAfter(ahora)) throw new IllegalArgumentException("La fecha/hora de inicio debe ser posterior al momento actual.");
        if (!fin.isAfter(inicio))   throw new IllegalArgumentException("La fecha/hora de fin debe ser posterior al inicio.");
        if (inicio.isAfter(limite) || fin.isAfter(limite))
            throw new IllegalArgumentException("Las fechas no pueden superar 2 años desde hoy.");
    }

    private void validarUpdate(LocalDate fIni, LocalTime hIni, LocalDate fFin, LocalTime hFin) {
        LocalDate hoy = LocalDate.now();
        LocalDateTime inicio = LocalDateTime.of(fIni, hIni);
        LocalDateTime fin    = LocalDateTime.of(fFin, hFin);
        LocalDateTime limite = hoy.plusYears(MAX_YEARS_ADELANTE).atTime(23,59,59);
        if (!fin.isAfter(inicio))   throw new IllegalArgumentException("La fecha/hora de fin debe ser posterior al inicio.");
        if (inicio.isAfter(limite) || fin.isAfter(limite))
            throw new IllegalArgumentException("Las fechas no pueden superar 2 años desde hoy.");
    }

    // ===== ALTAS =====
    public void crearFeria(String nombre, LocalDate fIni, LocalDate fFin, LocalTime hIni, LocalTime hFin,
                           EstadoEvento estado, int cantidadStands, TipoAmbiente ambiente) {
        validarAlta(fIni, hIni, fFin, hFin, estado);
        var f = new Feria(nombre, crearDateTime(fIni, hIni), crearDateTime(fFin, hFin), cantidadStands, ambiente);
        f.setEstado(estado);
        repositorio.guardarEvento(f);
    }

    public void crearConcierto(String nombre, LocalDate fIni, LocalDate fFin, LocalTime hIni, LocalTime hFin,
                               EstadoEvento estado, TipoEntrada tipoEntrada, int cupoMaximo) {
        validarAlta(fIni, hIni, fFin, hFin, estado);
        var c = new Concierto(nombre, crearDateTime(fIni, hIni), crearDateTime(fFin, hFin), tipoEntrada, cupoMaximo);
        c.setEstado(estado);
        repositorio.guardarEvento(c);
    }

    public void crearExposicion(String nombre, LocalDate fIni, LocalDate fFin, LocalTime hIni, LocalTime hFin,
                                EstadoEvento estado, TipoArte tipoArte) {
        validarAlta(fIni, hIni, fFin, hFin, estado);
        var x = new Exposicion(nombre, crearDateTime(fIni, hIni), crearDateTime(fFin, hFin), tipoArte);
        x.setEstado(estado);
        repositorio.guardarEvento(x);
    }

    public void crearTaller(String nombre, LocalDate fIni, LocalDate fFin, LocalTime hIni, LocalTime hFin,
                            EstadoEvento estado, int cupoMaximo, Modalidad modalidad) {
        validarAlta(fIni, hIni, fFin, hFin, estado);
        var t = new Taller(nombre, crearDateTime(fIni, hIni), crearDateTime(fFin, hFin), cupoMaximo, modalidad);
        t.setEstado(estado);
        repositorio.guardarEvento(t);
    }

    public void crearCicloCine(String nombre, LocalDate fIni, LocalDate fFin, LocalTime hIni, LocalTime hFin,
                               EstadoEvento estado, boolean postCharla, int cupoMaximo, java.util.List<Pelicula> pelis) {
        validarAlta(fIni, hIni, fFin, hFin, estado);
        var cc = new CicloCine(nombre, crearDateTime(fIni, hIni), crearDateTime(fFin, hFin), postCharla, cupoMaximo);
        cc.setEstado(estado);
        if (pelis != null) pelis.forEach(cc::agregarPelicula);
        repositorio.guardarEvento(cc);
    }

    // ===== UPDATES =====
    public void actualizarFeria(Feria f, String nombre, LocalDate fIni, LocalDate fFin,
                                LocalTime hIni, LocalTime hFin, EstadoEvento estado,
                                int cantidadStands, TipoAmbiente ambiente) {
        validarUpdate(fIni, hIni, fFin, hFin);
        f.setNombre(nombre); f.setFechaInicio(fIni.atTime(hIni)); f.setFechaFin(fFin.atTime(hFin));
        f.setEstado(estado); f.setCantidadStands(cantidadStands); f.setAmbiente(ambiente);
        repositorio.actualizarEvento(f);
    }

    public void actualizarConcierto(Concierto c, String nombre, LocalDate fIni, LocalDate fFin,
                                    LocalTime hIni, LocalTime hFin, EstadoEvento estado,
                                    TipoEntrada tipoEntrada, int cupoMaximo) {
        validarUpdate(fIni, hIni, fFin, hFin);
        c.setNombre(nombre); c.setFechaInicio(fIni.atTime(hIni)); c.setFechaFin(fFin.atTime(hFin));
        c.setEstado(estado); c.setTipoEntrada(tipoEntrada); c.setCupoMaximo(cupoMaximo);
        repositorio.actualizarEvento(c);
    }

    public void actualizarExposicion(Exposicion x, String nombre, LocalDate fIni, LocalDate fFin,
                                     LocalTime hIni, LocalTime hFin, EstadoEvento estado, TipoArte tipoArte) {
        validarUpdate(fIni, hIni, fFin, hFin);
        x.setNombre(nombre); x.setFechaInicio(fIni.atTime(hIni)); x.setFechaFin(fFin.atTime(hFin));
        x.setEstado(estado); x.setTipoArte(tipoArte);
        repositorio.actualizarEvento(x);
    }

    public void actualizarTaller(Taller t, String nombre, LocalDate fIni, LocalDate fFin,
                                 LocalTime hIni, LocalTime hFin, EstadoEvento estado,
                                 int cupoMaximo, Modalidad modalidad) {
        validarUpdate(fIni, hIni, fFin, hFin);
        t.setNombre(nombre); t.setFechaInicio(fIni.atTime(hIni)); t.setFechaFin(fFin.atTime(hFin));
        t.setEstado(estado); t.setCupoMaximo(cupoMaximo); t.setModalidad(modalidad);
        repositorio.actualizarEvento(t);
    }

    public void actualizarCicloCine(CicloCine cc, String nombre, LocalDate fIni, LocalDate fFin,
                                    LocalTime hIni, LocalTime hFin, EstadoEvento estado,
                                    boolean postCharla, int cupoMaximo, java.util.List<Pelicula> pelis) {
        validarUpdate(fIni, hIni, fFin, hFin);
        cc.setNombre(nombre); cc.setFechaInicio(fIni.atTime(hIni)); cc.setFechaFin(fFin.atTime(hFin));
        cc.setEstado(estado); cc.setPostCharla(postCharla); cc.setCupoMaximo(cupoMaximo);
        cc.clearPeliculas(); if (pelis != null) pelis.forEach(cc::agregarPelicula);
        repositorio.actualizarEvento(cc);
    }

    // ===== Listados / Búsquedas / Eliminación =====
    public java.util.List<Evento> listarEventos() { return repositorio.listarEventos(); }
    public java.util.List<Evento> buscarEventos(TipoEvento tipo, EstadoEvento estado, LocalDate desde, LocalDate hasta) {
        return repositorio.buscarEventos(tipo, estado, desde, hasta);
    }
    public void eliminarEvento(Evento e) {
        if (e == null) throw new IllegalArgumentException("Evento inválido.");
        if (e.getEstado() == EstadoEvento.CONFIRMADO)
            throw new IllegalStateException("No puede eliminarse un evento confirmado.");
        repositorio.eliminarEvento(e);
    }
    public ObservableList<Evento> obtenerEventosConEstadosActualizados() {
        verificarEstadosEventos();
        return FXCollections.observableArrayList(repositorio.listarEventos());
    }

    // ===== Personas =====
    public ObservableList<Persona> obtenerPersonas() { return repositorio.listarPersonas(); }
    public void guardarPersona(Persona persona) { repositorio.guardarPersona(persona); }
    public void eliminarPersona(Persona persona) { repositorio.eliminarPersona(persona); }
    public void actualizarPersona(Persona original, Persona actualizada) {
        original.actualizarCon(actualizada);
        repositorio.actualizarPersona(original);
    }
    public ObservableList<Persona> filtrarPersonas(String nombre, String dni) {
        var base = repositorio.listarPersonas();
        String n = nombre == null ? "" : nombre.trim().toLowerCase();
        String d = dni == null ? "" : dni.trim();
        var out = FXCollections.<Persona>observableArrayList();
        for (Persona p : base) {
            boolean ok = true;
            if (!n.isBlank()) ok &= (p.getNombre()+" "+p.getApellido()).toLowerCase().contains(n);
            if (!d.isBlank()) ok &= p.getDni() != null && p.getDni().contains(d);
            if (ok) out.add(p);
        }
        return out;
    }

    // ===== Participantes =====
    public void inscribirParticipante(Evento evento, Persona persona) {
        repositorio.agregarParticipante(evento, persona);
    }
    public void desinscribirParticipante(Evento evento, Persona persona) {
        repositorio.quitarParticipante(evento, persona);
    }
    public ObservableList<Persona> obtenerParticipantes(Evento evento) {
        return repositorio.obtenerParticipantes(evento);
    }

    // ===== Roles =====
    public RolEvento asignarRol(Evento evento, Persona persona, TipoRol rol) {
        // 1) Persisto (o recupero si ya existe)
        RolEvento rolPersistido = repositorio.asignarRol(evento, persona, rol);

        // 2) Sincronizo el objeto en memoria para la UI SIN usar setRoles(...)
        boolean yaEsta = evento.getRoles() != null &&
                evento.getRoles().stream().anyMatch(r -> r.getPersona().equals(persona) && r.getRol() == rol);
        if (!yaEsta) {
            // usar la lógica del dominio que ya agrega a la lista interna
            evento.agregarResponsable(persona, rol);
        }
        return rolPersistido;
    }

    public void eliminarRol(Evento evento, Persona persona, TipoRol rol) {
        repositorio.eliminarRol(evento, persona, rol);
        if (evento.getRoles() != null) {
            evento.getRoles().removeIf(r -> r.getPersona().equals(persona) && r.getRol() == rol);
        }
    }
    public ObservableList<RolEvento> obtenerRolesDeEvento(Evento evento) { return repositorio.obtenerRolesDeEvento(evento); }
    public ObservableList<RolEvento> obtenerRolesActivos() { return repositorio.obtenerRolesActivos(); }
    public ObservableList<RolEvento> filtrarRoles(String nombreEvento, String nombrePersona, String dni) {
        return repositorio.filtrarRoles(nombreEvento, nombrePersona, dni);
    }

    // ===== Películas =====
    public ObservableList<Pelicula> obtenerPeliculas() { return repositorio.listarPeliculas(); }
    public void guardarPelicula(Pelicula pelicula) { repositorio.guardarPelicula(pelicula); }
    public void eliminarPelicula(Pelicula pelicula) { repositorio.eliminarPelicula(pelicula); }
    public void actualizarPelicula(Pelicula original, Pelicula actualizada) {
        original.setTitulo(actualizada.getTitulo());
        original.setDuracionMinutos(actualizada.getDuracionMinutos());
        original.setTipo(actualizada.getTipo());
        repositorio.actualizarPelicula(original);
    }

    // ===== Estados automáticos =====
    public void verificarEstadosEventos() {
        for (Evento e : repositorio.listarEventos()) {
            EstadoEvento antes = e.getEstado();
            e.verificarEstadoAutomatico();
            if (antes != e.getEstado()) repositorio.actualizarEvento(e);
        }
    }
}
