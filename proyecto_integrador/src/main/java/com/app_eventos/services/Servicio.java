package com.app_eventos.services;

import com.app_eventos.model.*;
import com.app_eventos.model.enums.*;
import com.app_eventos.repository.Repositorio;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Servicio/Fachada para la UI.
 *
 * - Aplica validaciones de negocio.
 * - Delega la persistencia al Repositorio.
 * - No mantiene listas cacheadas: cada pedido lee de la BD.
 */
public class Servicio {

    // --------- Singleton -------------------------------------------------------
    private static final Servicio INSTANCE = new Servicio();
    public static Servicio getInstance() { return INSTANCE; }
    private Servicio() {}
    private final Repositorio repositorio = new Repositorio();

    // --------- Validaciones de fechas -----------------------------------------
    private static final int MAX_YEARS_ADELANTE = 2;

    private LocalDateTime crearDateTime(LocalDate fecha, LocalTime hora) {
        return LocalDateTime.of(fecha, hora);
    }

    private void validarAlta(LocalDate fIni, LocalTime hIni, LocalDate fFin, LocalTime hFin, EstadoEvento estado) {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime inicio = LocalDateTime.of(fIni, hIni);
        LocalDateTime fin    = LocalDateTime.of(fFin, hFin);
        LocalDateTime limite = LocalDate.now().plusYears(MAX_YEARS_ADELANTE).atTime(23,59,59);

        if (!inicio.isAfter(ahora)) throw new IllegalArgumentException("La fecha/hora de inicio debe ser posterior al momento actual.");
        if (!fin.isAfter(inicio))   throw new IllegalArgumentException("La fecha/hora de fin debe ser posterior al inicio.");
        if (inicio.isAfter(limite) || fin.isAfter(limite))
            throw new IllegalArgumentException("Las fechas no pueden superar 2 años desde hoy.");
    }

    private void validarUpdate(LocalDate fIni, LocalTime hIni, LocalDate fFin, LocalTime hFin) {
        LocalDateTime inicio = LocalDateTime.of(fIni, hIni);
        LocalDateTime fin    = LocalDateTime.of(fFin, hFin);
        LocalDateTime limite = LocalDate.now().plusYears(MAX_YEARS_ADELANTE).atTime(23,59,59);

        if (!fin.isAfter(inicio))   throw new IllegalArgumentException("La fecha/hora de fin debe ser posterior al inicio.");
        if (inicio.isAfter(limite) || fin.isAfter(limite))
            throw new IllegalArgumentException("Las fechas no pueden superar 2 años desde hoy.");
    }

    // --------- ALTAS -----------------------------------------------------------

    public void crearFeria(String nombre, LocalDate fIni, LocalDate fFin, LocalTime hIni, LocalTime hFin,
                           EstadoEvento estado, int cantidadStands, TipoAmbiente ambiente) {
        validarAlta(fIni, hIni, fFin, hFin, estado);
        var feria = new Feria(nombre, crearDateTime(fIni, hIni), crearDateTime(fFin, hFin), cantidadStands, ambiente);
        feria.setEstado(estado);
        repositorio.guardarEvento(feria);
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
                               EstadoEvento estado, boolean postCharla, int cupoMaximo,
                               java.util.List<Pelicula> peliculasSeleccionadas) {
        validarAlta(fIni, hIni, fFin, hFin, estado);
        var cc = new CicloCine(nombre, crearDateTime(fIni, hIni), crearDateTime(fFin, hFin), postCharla, cupoMaximo);
        cc.setEstado(estado);
        if (peliculasSeleccionadas != null) peliculasSeleccionadas.forEach(cc::agregarPelicula);
        repositorio.guardarEvento(cc);
    }

    // --------- UPDATES ---------------------------------------------------------

    public void actualizarFeria(Feria feria, String nombre, LocalDate fIni, LocalDate fFin,
                                LocalTime hIni, LocalTime hFin, EstadoEvento estado,
                                int cantidadStands, TipoAmbiente ambiente) {
        validarUpdate(fIni, hIni, fFin, hFin);
        feria.setNombre(nombre);
        feria.setFechaInicio(fIni.atTime(hIni));
        feria.setFechaFin(fFin.atTime(hFin));
        feria.setEstado(estado);
        feria.setCantidadStands(cantidadStands);
        feria.setAmbiente(ambiente);
        repositorio.actualizarEvento(feria);
    }

    public void actualizarConcierto(Concierto c, String nombre, LocalDate fIni, LocalDate fFin,
                                    LocalTime hIni, LocalTime hFin, EstadoEvento estado,
                                    TipoEntrada tipoEntrada, int cupoMaximo) {
        validarUpdate(fIni, hIni, fFin, hFin);
        c.setNombre(nombre);
        c.setFechaInicio(fIni.atTime(hIni));
        c.setFechaFin(fFin.atTime(hFin));
        c.setEstado(estado);
        c.setTipoEntrada(tipoEntrada);
        c.setCupoMaximo(cupoMaximo);
        repositorio.actualizarEvento(c);
    }

    public void actualizarExposicion(Exposicion x, String nombre, LocalDate fIni, LocalDate fFin,
                                     LocalTime hIni, LocalTime hFin, EstadoEvento estado,
                                     TipoArte tipoArte) {
        validarUpdate(fIni, hIni, fFin, hFin);
        x.setNombre(nombre);
        x.setFechaInicio(fIni.atTime(hIni));
        x.setFechaFin(fFin.atTime(hFin));
        x.setEstado(estado);
        x.setTipoArte(tipoArte);
        repositorio.actualizarEvento(x);
    }

    public void actualizarTaller(Taller t, String nombre, LocalDate fIni, LocalDate fFin,
                                 LocalTime hIni, LocalTime hFin, EstadoEvento estado,
                                 int cupoMaximo, Modalidad modalidad) {
        validarUpdate(fIni, hIni, fFin, hFin);
        t.setNombre(nombre);
        t.setFechaInicio(fIni.atTime(hIni));
        t.setFechaFin(fFin.atTime(hFin));
        t.setEstado(estado);
        t.setCupoMaximo(cupoMaximo);
        t.setModalidad(modalidad);
        repositorio.actualizarEvento(t);
    }

    public void actualizarCicloCine(CicloCine cc, String nombre, LocalDate fIni, LocalDate fFin,
                                    LocalTime hIni, LocalTime hFin, EstadoEvento estado,
                                    boolean postCharla, int cupoMaximo, java.util.List<Pelicula> pelis) {
        validarUpdate(fIni, hIni, fFin, hFin);
        cc.setNombre(nombre);
        cc.setFechaInicio(fIni.atTime(hIni));
        cc.setFechaFin(fFin.atTime(hFin));
        cc.setEstado(estado);
        cc.setPostCharla(postCharla);
        cc.setCupoMaximo(cupoMaximo);
        cc.clearPeliculas();
        if (pelis != null) pelis.forEach(cc::agregarPelicula);
        repositorio.actualizarEvento(cc);
    }

    // --------- Listados / filtros / baja --------------------------------------

    public java.util.List<Evento> listarEventos() {
        return repositorio.listarEventos();
    }

    public java.util.List<Evento> buscarEventos(TipoEvento tipo, EstadoEvento estado,
                                                LocalDate desde, LocalDate hasta) {
        return repositorio.buscarEventos(tipo, estado, desde, hasta);
    }

    public void eliminarEvento(Evento e) {
        if (e == null) throw new IllegalArgumentException("Evento inválido.");
        if (e.getEstado() == EstadoEvento.CONFIRMADO)
            throw new IllegalStateException("No puede eliminarse un evento confirmado.");
        repositorio.eliminarEvento(e);
    }

    /** Eventos que aceptan inscripción según la lógica del agregado. */
    public ObservableList<Evento> obtenerEventosDisponibles() {
        var lista = repositorio.listarEventos();
        var out = FXCollections.<Evento>observableArrayList();
        for (Evento e : lista) if (e.Inscripcion()) out.add(e);
        return out;
    }

    // --------- Personas --------------------------------------------------------

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

    // --------- Participaciones (roles) ----------------------------------------

    public RolEvento crearParticipacion(Evento evento, Persona persona, TipoRol rol) {
        return new RolEvento(evento, persona, rol);
    }

    public void guardarParticipacion(RolEvento rolEvento) {
        if (!rolEvento.getEvento().Inscripcion())
            throw new IllegalStateException("Inscripción no permitida para este evento.");
        repositorio.guardarRolEvento(rolEvento);
        // Sincroniza el agregado en memoria por si la misma instancia se sigue mostrando
        rolEvento.getEvento().agregarRol(rolEvento);
    }

    public void eliminarParticipacion(RolEvento rolEvento) {
        rolEvento.darDeBaja();
        repositorio.actualizarRolEvento(rolEvento);
    }

    public void reactivarParticipacion(RolEvento rolEvento) {
        rolEvento.reactivar();
        repositorio.actualizarRolEvento(rolEvento);
    }

    public ObservableList<RolEvento> obtenerParticipacionesActivas() {
        return repositorio.obtenerRolesActivos();
    }

    public ObservableList<RolEvento> obtenerSoloParticipantes() {
        verificarEstadosEventos();
        return repositorio.obtenerSoloParticipantes();
    }

    public ObservableList<RolEvento> filtrarParticipaciones(String nombreEvento, String nombrePersona, String dni) {
        return repositorio.filtrarRoles(nombreEvento, nombrePersona, dni);
    }

    public ObservableList<RolEvento> filtrarSoloParticipantes(String nombreEvento, String nombrePersona, String dni) {
        verificarEstadosEventos();
        return repositorio.filtrarSoloParticipantes(nombreEvento, nombrePersona, dni);
    }

    // --------- Películas -------------------------------------------------------

    public ObservableList<Pelicula> obtenerPeliculas() { return repositorio.listarPeliculas(); }

    public void guardarPelicula(Pelicula pelicula) { repositorio.guardarPelicula(pelicula); }

    public void eliminarPelicula(Pelicula pelicula) { repositorio.eliminarPelicula(pelicula); }

    public void actualizarPelicula(Pelicula original, Pelicula actualizada) {
        original.setTitulo(actualizada.getTitulo());
        original.setDuracionMinutos(actualizada.getDuracionMinutos());
        original.setTipo(actualizada.getTipo());
        repositorio.actualizarPelicula(original);
    }

    /** Filtro local (título contains + id numérico si corresponde). */
    public ObservableList<Pelicula> filtrarPeliculas(String titulo, String idTexto) {
        var base = repositorio.listarPeliculas();
        String t = (titulo == null) ? "" : titulo.trim().toLowerCase();
        Long id = null;
        if (idTexto != null && !idTexto.isBlank()) {
            try { id = Long.parseLong(idTexto.trim()); } catch (NumberFormatException ignored) { }
        }
        var out = FXCollections.<Pelicula>observableArrayList();
        for (Pelicula p : base) {
            boolean ok = true;
            if (!t.isBlank()) ok &= p.getTitulo() != null && p.getTitulo().toLowerCase().contains(t);
            if (id != null)   ok &= p.getIdPelicula() != null && p.getIdPelicula().equals(id);
            if (ok) out.add(p);
        }
        return out;
    }

    // --------- Estados automáticos --------------------------------------------

    /** Verifica y persiste cambios de estado por reglas temporales. */
    public void verificarEstadosEventos() {
        for (Evento e : repositorio.listarEventos()) {
            EstadoEvento antes = e.getEstado();
            e.verificarEstadoAutomatico();
            if (antes != e.getEstado()) {
                repositorio.actualizarEvento(e);
            }
        }
    }

    /** Devuelve eventos asegurando estados al día. */
    public ObservableList<Evento> obtenerEventosConEstadosActualizados() {
        verificarEstadosEventos();
        return FXCollections.observableArrayList(repositorio.listarEventos());
    }
}
