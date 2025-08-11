package com.app_eventos.services;

import com.app_eventos.model.CicloCine;
import com.app_eventos.model.Concierto;
import com.app_eventos.model.Evento;
import com.app_eventos.model.Exposicion;
import com.app_eventos.model.Feria;
import com.app_eventos.model.Pelicula;
import com.app_eventos.model.Persona;
import com.app_eventos.model.RolEvento;
import com.app_eventos.model.Taller;
import com.app_eventos.model.enums.EstadoEvento;
import com.app_eventos.model.enums.Modalidad;
import com.app_eventos.model.enums.TipoAmbiente;
import com.app_eventos.model.enums.TipoArte;
import com.app_eventos.model.enums.TipoEntrada;
import com.app_eventos.model.enums.TipoEvento;
import com.app_eventos.model.enums.TipoRol;
import com.app_eventos.repository.Repositorio;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Servicio {
    
    // Instancia estática compartida para todos los controladores
    private static final Servicio INSTANCE = new Servicio();
    
    private List<Evento> eventos = new ArrayList<>();
    private final Repositorio repositorio = new Repositorio();
    
    // Simulación de base de datos en memoria
    // Opción B (estática con bloque estático)
    private static final ObservableList<Persona> personas;
    static { personas = FXCollections.observableArrayList(); }

    // Reglas de validación temporal (ventana de 2 años desde el día actual)
    private static final int MAX_YEARS_ADELANTE = 2;

    // Método para obtener la instancia compartida
    public static Servicio getInstance() {
        return INSTANCE;
    }

    // MÉTODOS PRIVADOS AUXILIARES

    private LocalDateTime crearDateTime(LocalDate fecha, LocalTime hora) {
        return LocalDateTime.of(fecha, hora);
    }
    
    private void agregarEvento(Evento evento, EstadoEvento estado) {
        evento.setEstado(estado);
        eventos.add(evento);
    }

    // Validación de altas: inicio y fin estrictamente futuros y dentro de los próximos 2 años (a nivel día)
    private void validarAlta(LocalDate fIni, LocalTime hIni,
                             LocalDate fFin, LocalTime hFin,
                             EstadoEvento estado) {
        LocalDate hoy = LocalDate.now();
        LocalDateTime ahora = LocalDateTime.now();

        LocalDateTime inicio = LocalDateTime.of(fIni, hIni);
        LocalDateTime fin    = LocalDateTime.of(fFin,  hFin);

        LocalDateTime limiteSuperior = hoy.plusYears(MAX_YEARS_ADELANTE).atTime(23, 59, 59);

        if (!inicio.isAfter(ahora)) {
            throw new IllegalArgumentException("La fecha/hora de inicio debe ser posterior al momento actual.");
        }
        if (!fin.isAfter(inicio)) {
            throw new IllegalArgumentException("La fecha/hora de fin debe ser posterior al inicio.");
        }
        if (inicio.isAfter(limiteSuperior) || fin.isAfter(limiteSuperior)) {
            throw new IllegalArgumentException("Las fechas no pueden superar 2 años desde el día de hoy.");
        }
        if (estado == EstadoEvento.EJECUCIÓN || estado == EstadoEvento.FINALIZADO) {
            throw new IllegalArgumentException("En alta solo se permite PLANIFICACIÓN o CONFIRMADO.");
        }
    }

    // Validación de updates: respeta ventana de 2 años desde hoy y orden temporal
    private void validarUpdate(LocalDate fIni, LocalTime hIni,
                               LocalDate fFin, LocalTime hFin) {
        LocalDate hoy = LocalDate.now();
        LocalDateTime inicio = LocalDateTime.of(fIni, hIni);
        LocalDateTime fin    = LocalDateTime.of(fFin,  hFin);
        LocalDateTime limiteSuperior = hoy.plusYears(MAX_YEARS_ADELANTE).atTime(23, 59, 59);

        if (!fin.isAfter(inicio)) {
            throw new IllegalArgumentException("La fecha/hora de fin debe ser posterior al inicio.");
        }
        if (inicio.isAfter(limiteSuperior) || fin.isAfter(limiteSuperior)) {
            throw new IllegalArgumentException("Las fechas no pueden superar 2 años desde el día de hoy.");
        }
    }

    // Transición automática de estado según tiempo real
    private void actualizarEstadosSegunTiempo() {
        LocalDateTime now = LocalDateTime.now();
        for (Evento e : eventos) {
            if (now.isAfter(e.getFechaFin())) {
                if (e.getEstado() != EstadoEvento.FINALIZADO) e.setEstado(EstadoEvento.FINALIZADO);
            } else if (!now.isBefore(e.getFechaInicio()) && !now.isAfter(e.getFechaFin())) {
                if (e.getEstado() != EstadoEvento.EJECUCIÓN) e.setEstado(EstadoEvento.EJECUCIÓN);
            }
        }
    }

    // Expuesto para que los controladores puedan forzar la sincronización
    public void sincronizarEstadosTiempoReal() {
        actualizarEstadosSegunTiempo();
    }

    // ====== MÉTODOS PARA EVENTOS ======
    
    public void crearFeria(String nombre, LocalDate fechaInicio, LocalDate fechaFin,
                          LocalTime horaInicio, LocalTime horaFin,
                          EstadoEvento estado, int cantidadStands,
                          TipoAmbiente tipoAmbiente) {
        validarAlta(fechaInicio, horaInicio, fechaFin, horaFin, estado);
        LocalDateTime inicio = crearDateTime(fechaInicio, horaInicio);
        LocalDateTime fin = crearDateTime(fechaFin, horaFin);
        Feria feria = new Feria(nombre, inicio, fin, cantidadStands, tipoAmbiente);
        agregarEvento(feria, estado);
        actualizarEstadosSegunTiempo();
    }

    public void crearConcierto(String nombre, LocalDate fechaInicio, LocalDate fechaFin,
                              LocalTime horaInicio, LocalTime horaFin, EstadoEvento estado,
                              TipoEntrada tipoEntrada, int cupoMaximo) {
        validarAlta(fechaInicio, horaInicio, fechaFin, horaFin, estado);
        LocalDateTime inicio = crearDateTime(fechaInicio, horaInicio);
        LocalDateTime fin = crearDateTime(fechaFin, horaFin);
        Concierto concierto = new Concierto(nombre, inicio, fin, tipoEntrada, cupoMaximo);
        agregarEvento(concierto, estado);
        actualizarEstadosSegunTiempo();
    }

    public void crearExposicion(String nombre, LocalDate fechaInicio, LocalDate fechaFin,
                               LocalTime horaInicio, LocalTime horaFin, EstadoEvento estado,
                               TipoArte tipoArte) {
        validarAlta(fechaInicio, horaInicio, fechaFin, horaFin, estado);
        LocalDateTime inicio = crearDateTime(fechaInicio, horaInicio);
        LocalDateTime fin = crearDateTime(fechaFin, horaFin);
        Exposicion exposicion = new Exposicion(nombre, inicio, fin, tipoArte);
        agregarEvento(exposicion, estado);
        actualizarEstadosSegunTiempo();
    }

    public void crearTaller(String nombre, LocalDate fechaInicio, LocalDate fechaFin,
                           LocalTime horaInicio, LocalTime horaFin, EstadoEvento estado,
                           int cupoMaximo, Modalidad modalidad) {
        validarAlta(fechaInicio, horaInicio, fechaFin, horaFin, estado);
        LocalDateTime inicio = crearDateTime(fechaInicio, horaInicio);
        LocalDateTime fin = crearDateTime(fechaFin, horaFin);
        Taller taller = new Taller(nombre, inicio, fin, cupoMaximo, modalidad);
        agregarEvento(taller, estado);
        actualizarEstadosSegunTiempo();
    }
    
    public void crearCicloCine(String nombre, LocalDate fechaInicio, LocalDate fechaFin,
                            LocalTime horaInicio, LocalTime horaFin, EstadoEvento estado,
                            boolean postCharla, int cupoMaximo,
                            List<Pelicula> peliculasSeleccionadas) {
        validarAlta(fechaInicio, horaInicio, fechaFin, horaFin, estado);
        var inicio = LocalDateTime.of(fechaInicio, horaInicio);
        var fin    = LocalDateTime.of(fechaFin, horaFin);

        var ciclo = new CicloCine(nombre, inicio, fin, postCharla, cupoMaximo);
        ciclo.setEstado(estado);

        if (peliculasSeleccionadas != null) {
            for (Pelicula p : peliculasSeleccionadas) {
                ciclo.agregarPelicula(p);
            }
        }

        eventos.add(ciclo); // en memoria, sin BD
        actualizarEstadosSegunTiempo();
    }

    // ====== ACTUALIZAR DATOS ======

    // FERIA
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
        actualizarEstadosSegunTiempo();
    }

    // CONCIERTO
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
        actualizarEstadosSegunTiempo();
    }

    // EXPOSICION
    public void actualizarExposicion(Exposicion x, String nombre, LocalDate fIni, LocalDate fFin,
                                    LocalTime hIni, LocalTime hFin, EstadoEvento estado,
                                    TipoArte tipoArte) {
        validarUpdate(fIni, hIni, fFin, hFin);
        x.setNombre(nombre);
        x.setFechaInicio(fIni.atTime(hIni));
        x.setFechaFin(fFin.atTime(hFin));
        x.setEstado(estado);
        x.setTipoArte(tipoArte);
        actualizarEstadosSegunTiempo();
    }

    // TALLER
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
        actualizarEstadosSegunTiempo();
    }

    // CICLO CINE
    public void actualizarCicloCine(CicloCine cc, String nombre, LocalDate fIni, LocalDate fFin,
                                    LocalTime hIni, LocalTime hFin, EstadoEvento estado,
                                    boolean postCharla, int cupoMaximo, List<Pelicula> pelis) {
        validarUpdate(fIni, hIni, fFin, hFin);
        cc.setNombre(nombre);
        cc.setFechaInicio(fIni.atTime(hIni));
        cc.setFechaFin(fFin.atTime(hFin));
        cc.setEstado(estado);
        cc.setPostCharla(postCharla);
        cc.setCupoMaximo(cupoMaximo);
        // reemplazar películas
        cc.clearPeliculas();
        if (pelis != null) pelis.forEach(cc::agregarPelicula);
        actualizarEstadosSegunTiempo();
    }

    public List<Evento> listarEventos() {
        actualizarEstadosSegunTiempo();
        return new ArrayList<>(eventos);
    }

    public void eliminarEvento(Evento e) {
        if (e == null) throw new IllegalArgumentException("Evento inválido.");

        // Regla de negocio: no eliminar en estado CONFIRMADO
        if (e.getEstado() == EstadoEvento.CONFIRMADO) {
            throw new IllegalStateException("No puede eliminarse un evento confirmado.");
        }

        eventos.remove(e); // 'eventos' es tu lista en memoria
    }

    // Búsqueda por tipo, estado y rango de fechas con superposición de intervalos
    public List<Evento> buscarEventos(TipoEvento tipo, EstadoEvento estado,
                                      LocalDate desde, LocalDate hasta) {
        actualizarEstadosSegunTiempo();

        LocalDateTime from = (desde == null) ? null : desde.atStartOfDay();
        LocalDateTime to   = (hasta == null) ? null : hasta.atTime(23, 59, 59);

        return eventos.stream()
                .filter(e -> tipo == null   || e.getTipoEvento() == tipo)
                .filter(e -> estado == null || e.getEstado() == estado)
                .filter(e -> {
                    if (from == null && to == null) return true;
                    if (from == null) return !e.getFechaInicio().isAfter(to);
                    if (to == null)   return !e.getFechaFin().isBefore(from);
                    return !(e.getFechaFin().isBefore(from) || e.getFechaInicio().isAfter(to));
                })
                .sorted(Comparator.comparing(Evento::getFechaInicio))
                .toList();
    }

    // ====== MÉTODOS PARA PERSONAS ======
    
    public ObservableList<Persona> obtenerPersonas() {
        return personas;
    }

    public void guardarPersona(Persona persona) {
        personas.add(persona);
    }

    public void eliminarPersona(Persona persona) {
        personas.remove(persona);
    }

    public void actualizarPersona(Persona original, Persona actualizada) {
        original.actualizarCon(actualizada);
    }

    public ObservableList<Persona> filtrarPersonas(String nombre, String dni) {
        List<Persona> filtradas = personas.stream()
            .filter(p -> {
                boolean coincideNombre = nombre == null || nombre.isBlank()
                        || p.getNombre().toLowerCase().contains(nombre.toLowerCase());
                boolean coincideDni = dni == null || dni.isBlank()
                        || p.getDni().contains(dni);
                return coincideNombre && coincideDni;
            })
            .collect(Collectors.toList());

        return FXCollections.observableArrayList(filtradas);
    }

    // ====== MÉTODOS PARA ABM PARTICIPANTE ======

    // La validación ocurre en el constructor del modelo RolEvento
    public RolEvento crearParticipacion(Evento evento, Persona persona, TipoRol rol) {
        try {
            return new RolEvento(evento, persona, rol);
        } catch (Exception e) {
            throw new RuntimeException("Error al crear participación: " + e.getMessage(), e);
        }
    }

    // Guarda una participación (intermediario con repositorio)
    public void guardarParticipacion(RolEvento rolEvento) {
        if (!rolEvento.getEvento().puedeInscribirParticipantes()) {
            throw new IllegalStateException("Inscripción no permitida para este evento.");
        }
        try {
            repositorio.guardarRolEvento(rolEvento);
            // También agregar al evento para mantener consistencia
            rolEvento.getEvento().agregarRol(rolEvento);
        } catch (Exception e) {
            throw new RuntimeException("Error al guardar participación: " + e.getMessage(), e);
        }
    }

    // Elimina una participación (borrado lógico)
    public void eliminarParticipacion(RolEvento rolEvento) {
        try {
            rolEvento.darDeBaja();
            repositorio.actualizarRolEvento(rolEvento);
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar participación: " + e.getMessage(), e);
        }
    }

    // Obtiene todas las participaciones activas (TODOS los roles)
    public ObservableList<RolEvento> obtenerParticipacionesActivas() {
        return repositorio.obtenerRolesActivos();
    }

    // Obtiene SOLO participantes (rol PARTICIPANTE) activos
    public ObservableList<RolEvento> obtenerSoloParticipantes() {
        return repositorio.obtenerSoloParticipantes();
    }

    // Filtra participaciones por criterios (delegado al repositorio)
    public ObservableList<RolEvento> filtrarParticipaciones(String nombreEvento, String nombrePersona, String dni) {
        return repositorio.filtrarRoles(nombreEvento, nombrePersona, dni);
    }

    // Filtra SOLO participantes (rol PARTICIPANTE) por criterios
    public ObservableList<RolEvento> filtrarSoloParticipantes(String nombreEvento, String nombrePersona, String dni) {
        return repositorio.filtrarSoloParticipantes(nombreEvento, nombrePersona, dni);
    }

    // Obtiene eventos disponibles para inscripción
    public ObservableList<Evento> obtenerEventosDisponibles() {
        return eventos.stream()
                .filter(Evento::puedeInscribirParticipantes)
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    // Reactiva una participación dada de baja
    public void reactivarParticipacion(RolEvento rolEvento) {
        try {
            rolEvento.reactivar();
            repositorio.actualizarRolEvento(rolEvento);
        } catch (Exception e) {
            throw new RuntimeException("Error al reactivar participación: " + e.getMessage(), e);
        }
    }
    
    // ====== PELÍCULAS (en memoria, sin persistencia real por ahora) ======

    // Lista observable para enlazar con la TableView.
    private static final javafx.collections.ObservableList<Pelicula> peliculas =
            javafx.collections.FXCollections.observableArrayList();

    /** Devuelve la lista observable para setItems(...) en la UI. */
    public javafx.collections.ObservableList<Pelicula> obtenerPeliculas() {
        return peliculas;
    }

    // Alta de película (el modelo rico valida).
    public void guardarPelicula(Pelicula pelicula) {
        peliculas.add(pelicula);
    }

    // Baja de película.
    public void eliminarPelicula(Pelicula pelicula) {
        peliculas.remove(pelicula);
    }

    // Modificación de película (respetando validaciones del modelo).
    public void actualizarPelicula(Pelicula original, Pelicula actualizada) {
        original.setTitulo(actualizada.getTitulo());
        original.setDuracionMinutos(actualizada.getDuracionMinutos());
        original.setTipo(actualizada.getTipo());
    }

    // Filtro por título (contains, case-insensitive) y por ID numérico exacto.
    public javafx.collections.ObservableList<Pelicula> filtrarPeliculas(String titulo, String idTexto) {
        java.util.stream.Stream<Pelicula> stream = peliculas.stream();

        if (titulo != null && !titulo.isBlank()) {
            String t = titulo.toLowerCase();
            stream = stream.filter(p -> p.getTitulo() != null && p.getTitulo().toLowerCase().contains(t));
        }

        if (idTexto != null && !idTexto.isBlank()) {
            try {
                Long id = Long.parseLong(idTexto);
                stream = stream.filter(p -> p.getIdPelicula() != null && p.getIdPelicula().equals(id));
            } catch (NumberFormatException e) {
                stream = stream.filter(p -> false);
            }
        }

        return stream.collect(java.util.stream.Collectors.toCollection(
                javafx.collections.FXCollections::observableArrayList
        ));
    }
}
