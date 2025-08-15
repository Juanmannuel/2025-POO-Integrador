package com.app_eventos.services;

import com.app_eventos.model.*;
import com.app_eventos.model.enums.*;
import com.app_eventos.repository.Repositorio;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.*;
import java.util.Comparator;
import java.util.List;

public class Servicio {

    private static final Servicio INSTANCE = new Servicio();
    public static Servicio getInstance() { return INSTANCE; }
    private Servicio() {}

    private final Repositorio repositorio = new Repositorio();

    // Guards de nulidad
    private void validarAlta(LocalDate fIni, LocalTime hIni, LocalDate fFin, LocalTime hFin, EstadoEvento estado) {
        if (fIni == null)  throw new IllegalArgumentException("La Fecha de inicio es obligatoria.");
        if (hIni == null)  throw new IllegalArgumentException("La Hora de inicio es obligatoria.");
        if (fFin == null)  throw new IllegalArgumentException("La Fecha de fin es obligatoria.");
        if (hFin == null)  throw new IllegalArgumentException("La Hora de fin es obligatoria.");
        if (estado == null) throw new IllegalArgumentException("El Estado es obligatorio.");
    }

    private void validarUpdate(LocalDate fIni, LocalTime hIni, LocalDate fFin, LocalTime hFin) {
        if (fIni == null)  throw new IllegalArgumentException("La Fecha de inicio es obligatoria.");
        if (hIni == null)  throw new IllegalArgumentException("La Hora de inicio es obligatoria.");
        if (fFin == null)  throw new IllegalArgumentException("La Fecha de fin es obligatoria.");
        if (hFin == null)  throw new IllegalArgumentException("La Hora de fin es obligatoria.");
    }

    // ALTAS
    public void crearFeria(String nombre, LocalDate fIni, LocalDate fFin, LocalTime hIni, LocalTime hFin,
                           EstadoEvento estado, int cantidadStands, TipoAmbiente ambiente) {
        validarAlta(fIni, hIni, fFin, hFin, estado);
        var f = new Feria();
        f.setNombre(nombre);
        f.setFechas(fIni, hIni, fFin, hFin); // valida negocio en dominio
        f.setCantidadStands(cantidadStands);
        f.setAmbiente(ambiente);
        // Estado inicial
        if (estado == EstadoEvento.PLANIFICACIÓN) f.setEstado(estado); else f.cambiarEstado(estado);
        repositorio.guardarEvento(f);
    }

    public void crearConcierto(String nombre, LocalDate fIni, LocalDate fFin, LocalTime hIni, LocalTime hFin,
                               EstadoEvento estado, TipoEntrada tipoEntrada, int cupoMaximo) {
        validarAlta(fIni, hIni, fFin, hFin, estado);
        var c = new Concierto();
        c.setNombre(nombre);
        c.setFechas(fIni, hIni, fFin, hFin);
        c.setTipoEntrada(tipoEntrada);
        c.setCupoMaximo(cupoMaximo);
        if (estado == EstadoEvento.PLANIFICACIÓN) c.setEstado(estado); else c.cambiarEstado(estado);
        repositorio.guardarEvento(c);
    }

    public void crearExposicion(String nombre, LocalDate fIni, LocalDate fFin, LocalTime hIni, LocalTime hFin,
                                EstadoEvento estado, TipoArte tipoArte) {
        validarAlta(fIni, hIni, fFin, hFin, estado);
        var x = new Exposicion();
        x.setNombre(nombre);
        x.setFechas(fIni, hIni, fFin, hFin);
        x.setTipoArte(tipoArte);
        if (estado == EstadoEvento.PLANIFICACIÓN) x.setEstado(estado); else x.cambiarEstado(estado);
        repositorio.guardarEvento(x);
    }

    public void crearTaller(String nombre, LocalDate fIni, LocalDate fFin, LocalTime hIni, LocalTime hFin,
                            EstadoEvento estado, int cupoMaximo, Modalidad modalidad) {
        validarAlta(fIni, hIni, fFin, hFin, estado);
        var t = new Taller();
        t.setNombre(nombre);
        t.setFechas(fIni, hIni, fFin, hFin);
        t.setCupoMaximo(cupoMaximo);
        t.setModalidad(modalidad);
        if (estado == EstadoEvento.PLANIFICACIÓN) t.setEstado(estado); else t.cambiarEstado(estado);
        repositorio.guardarEvento(t);
    }

    public void crearCicloCine(String nombre, LocalDate fIni, LocalDate fFin, LocalTime hIni, LocalTime hFin,
                               EstadoEvento estado, boolean postCharla, int cupoMaximo, List<Pelicula> pelis) {
        validarAlta(fIni, hIni, fFin, hFin, estado);
        var cc = new CicloCine();
        cc.setNombre(nombre);
        cc.setFechas(fIni, hIni, fFin, hFin);
        cc.setPostCharla(postCharla);
        cc.setCupoMaximo(cupoMaximo);
        if (pelis != null) pelis.forEach(cc::agregarPelicula);
        if (estado == EstadoEvento.PLANIFICACIÓN) cc.setEstado(estado); else cc.cambiarEstado(estado);
        repositorio.guardarEvento(cc);
    }

    // MODIFICACIONES
    public void actualizarFeria(Feria f, String nombre, LocalDate fIni, LocalDate fFin,
                                LocalTime hIni, LocalTime hFin, EstadoEvento estado,
                                int cantidadStands, TipoAmbiente ambiente) {
        validarUpdate(fIni, hIni, fFin, hFin);
        f.setNombre(nombre);
        f.setFechas(fIni, hIni, fFin, hFin);         // valida negocio en dominio
        f.setCantidadStands(cantidadStands);
        f.setAmbiente(ambiente);
        if (estado != null && estado != f.getEstado()) f.cambiarEstado(estado);
        repositorio.actualizarEvento(f);
    }

    public void actualizarConcierto(Concierto c, String nombre, LocalDate fIni, LocalDate fFin,
                                    LocalTime hIni, LocalTime hFin, EstadoEvento estado,
                                    TipoEntrada tipoEntrada, int cupoMaximo) {
        validarUpdate(fIni, hIni, fFin, hFin);
        c.setNombre(nombre);
        c.setFechas(fIni, hIni, fFin, hFin);
        c.setTipoEntrada(tipoEntrada);
        c.setCupoMaximo(cupoMaximo);
        if (estado != null && estado != c.getEstado()) c.cambiarEstado(estado);
        repositorio.actualizarEvento(c);
    }

    public void actualizarExposicion(Exposicion x, String nombre, LocalDate fIni, LocalDate fFin,
                                     LocalTime hIni, LocalTime hFin, EstadoEvento estado, TipoArte tipoArte) {
        validarUpdate(fIni, hIni, fFin, hFin);
        x.setNombre(nombre);
        x.setFechas(fIni, hIni, fFin, hFin);
        x.setTipoArte(tipoArte);
        if (estado != null && estado != x.getEstado()) x.cambiarEstado(estado);
        repositorio.actualizarEvento(x);
    }

    public void actualizarTaller(Taller t, String nombre, LocalDate fIni, LocalDate fFin,
                                 LocalTime hIni, LocalTime hFin, EstadoEvento estado,
                                 int cupoMaximo, Modalidad modalidad) {
        validarUpdate(fIni, hIni, fFin, hFin);
        t.setNombre(nombre);
        t.setFechas(fIni, hIni, fFin, hFin);
        t.setCupoMaximo(cupoMaximo);
        t.setModalidad(modalidad);
        if (estado != null && estado != t.getEstado()) t.cambiarEstado(estado);
        repositorio.actualizarEvento(t);
    }

    public void actualizarCicloCine(CicloCine cc, String nombre, LocalDate fIni, LocalDate fFin,
                                    LocalTime hIni, LocalTime hFin, EstadoEvento estado,
                                    boolean postCharla, int cupoMaximo, List<Pelicula> pelis) {
        validarUpdate(fIni, hIni, fFin, hFin);
        cc.setNombre(nombre);
        cc.setFechas(fIni, hIni, fFin, hFin);
        cc.setPostCharla(postCharla);
        cc.setCupoMaximo(cupoMaximo);
        cc.clearPeliculas();
        if (pelis != null) pelis.forEach(cc::agregarPelicula);
        if (estado != null && estado != cc.getEstado()) cc.cambiarEstado(estado);
        repositorio.actualizarEvento(cc);
    }

    public void inscribirParticipante(Evento evento, Persona persona) {
        if (evento == null || persona == null)
            throw new IllegalArgumentException("Evento y persona son obligatorios.");

        // 1) No puede estar ya con rol en este evento
        if (evento.personaTieneRol(persona)) {
            throw new IllegalStateException("No se puede inscribir: la persona ya tiene un rol asignado en este evento.");
        }

        boolean yaInscripta = repositorio.obtenerParticipantes(evento)
                .stream().anyMatch(p -> p.equals(persona));
        if (yaInscripta) {
            throw new IllegalStateException("La persona ya está inscripta en este evento.");
        }

        repositorio.agregarParticipante(evento, persona);
    }

    public void desinscribirParticipante(Evento evento, Persona persona) {
        repositorio.quitarParticipante(evento, persona);
    }

    public ObservableList<Persona> obtenerParticipantes(Evento evento) {
        return repositorio.obtenerParticipantes(evento);
    }

    // ROLES
    public RolEvento asignarRol(Evento evento, Persona persona, TipoRol rol) {
        if (evento == null || persona == null || rol == null)
            throw new IllegalArgumentException("Evento, persona y rol son obligatorios.");

        boolean esParticipante = repositorio.obtenerParticipantes(evento)
                .stream().anyMatch(p -> p.equals(persona));
        if (esParticipante) {
            throw new IllegalStateException("No se puede asignar un rol: la persona está inscripta como participante en este evento.");
        }

        boolean yaTieneRol = evento.getRoles() != null &&
                evento.getRoles().stream().anyMatch(r -> r.getPersona().equals(persona));
        if (yaTieneRol) {
            throw new IllegalStateException("La persona ya tiene un rol asignado en este evento.");
        }

        RolEvento rolPersistido = repositorio.asignarRol(evento, persona, rol);
        boolean yaEstaEnMemoria = evento.getRoles() != null &&
                evento.getRoles().stream().anyMatch(r -> r.getPersona().equals(persona));
        if (!yaEstaEnMemoria) {
            evento.agregarRol(rolPersistido);
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
    public ObservableList<RolEvento> filtrarRoles(String nombreEvento, String nombrePersona, String dni) {
        return repositorio.filtrarRoles(nombreEvento, nombrePersona, dni);
    }

    // Listados, Búsquedas y Eliminación
    public List<Evento> listarEventos() { return repositorio.listarEventos(); }

    public List<Evento> buscarEventos(TipoEvento tipo, EstadoEvento estado, LocalDate desde, LocalDate hasta) {
        return repositorio.buscarEventos(tipo, estado, desde, hasta);
    }

    public void eliminarEvento(Evento e) {
        if (e == null) throw new IllegalArgumentException("Evento inválido.");
        repositorio.eliminarEvento(e);
    }

    public ObservableList<Evento> obtenerEventosConEstadosActualizados() {
        verificarEstadosEventos();
        return FXCollections.observableArrayList(repositorio.listarEventos());
    }

    //  Personas
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

    //  Películas
    public ObservableList<Pelicula> obtenerPeliculas() { return repositorio.listarPeliculas(); }
    public void guardarPelicula(Pelicula pelicula) { repositorio.guardarPelicula(pelicula); }
    public void eliminarPelicula(Pelicula pelicula) { repositorio.eliminarPelicula(pelicula); }
    public void actualizarPelicula(Pelicula original, Pelicula actualizada) {
        original.setTitulo(actualizada.getTitulo());
        original.setDuracionMinutos(actualizada.getDuracionMinutos());
        original.setTipo(actualizada.getTipo());
        repositorio.actualizarPelicula(original);
    }

    // Estados automáticos
    public void verificarEstadosEventos() {
        for (Evento e : repositorio.listarEventos()) {
            EstadoEvento antes = e.getEstado();
            e.verificarEstadoAutomatico();
            if (antes != e.getEstado()) repositorio.actualizarEvento(e);
        }
    }

    // Métricas / utilitarios
    public List<Evento> listarEventosPorRango(LocalDateTime desde, LocalDateTime hasta) {
        var base = repositorio.buscarEventos(null, null, desde.toLocalDate(), hasta.toLocalDate());
        return base.stream()
                .filter(e -> {
                    LocalDateTime ini = e.getFechaInicio();
                    return (ini.isEqual(desde) || ini.isAfter(desde))
                        && (ini.isBefore(hasta) || ini.isEqual(hasta));
                })
                .sorted(Comparator.comparing(Evento::getFechaInicio))
                .toList();
    }

    public long contarEventos() { return repositorio.listarEventos().size(); }

    public long contarEventosActivos() {
        LocalDateTime ahora = LocalDateTime.now();
        return repositorio.listarEventos().stream()
                .filter(e -> e.getEstado() == EstadoEvento.CONFIRMADO)
                .filter(e -> e.getFechaFin() != null && e.getFechaFin().isAfter(ahora))
                .count();
    }

    public long contarPersonas() { return repositorio.listarPersonas().size(); }

    public long contarInscripciones() {
        long total = 0L;
        for (Evento e : repositorio.listarEventos()) {
            total += repositorio.obtenerParticipantes(e).size();
        }
        return total;
    }

    // Inscripción utilitarios
    public List<Evento> listarEventosQueAdmitenInscripcion() {
        LocalDateTime ahora = LocalDateTime.now();
        return repositorio.listarEventos().stream()
                .filter(e -> e.getEstado() == EstadoEvento.CONFIRMADO)
                .filter(e -> e.getFechaFin() != null && e.getFechaFin().isAfter(ahora))
                .filter(e -> (e instanceof Concierto) || (e instanceof Taller) || (e instanceof CicloCine))
                .toList();
    }

    public ObservableList<Evento> obtenerEventosParaInscripcion() {
        return FXCollections.observableArrayList(
            repositorio.listarEventos().stream()
                .filter(e -> e instanceof com.app_eventos.model.interfaces.IEventoConCupo)
                .filter(e -> e.getEstado() == EstadoEvento.CONFIRMADO)
                .filter(e -> e.getFechaFin() != null && e.getFechaFin().isAfter(LocalDateTime.now()))
                .toList()
        );
    }

    public ObservableList<Persona> obtenerPersonasElegiblesParaEvento(Evento e) {
        return FXCollections.observableArrayList(repositorio.personasElegiblesParaInscripcion(e));
    }
}
