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
import com.app_eventos.model.enums.TipoRol;
import com.app_eventos.repository.Repositorio;
import com.app_eventos.model.enums.TipoPelicula;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
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

    
    // Constructor privado para Singleton
    private Servicio() {
        cargarPersonasDePrueba(); // Datos de ejemplo para testeo
    }
    
    // Método para obtener la instancia compartida
    public static Servicio getInstance() {
        return INSTANCE;
    }

    // ====== MÉTODOS PRIVADOS AUXILIARES ======
    
    private LocalDateTime crearDateTime(LocalDate fecha, LocalTime hora) {
        return LocalDateTime.of(fecha, hora);
    }
    
    private void agregarEvento(Evento evento, EstadoEvento estado) {
        evento.setEstado(estado);
        eventos.add(evento);
        // Sincronizar con el repositorio para que esté disponible en ABM Participante
        repositorio.agregarEvento(evento);
    }
    
    private void procesarPeliculas(CicloCine ciclo, String peliculasTexto, TipoPelicula tipo) {
        if (peliculasTexto == null || peliculasTexto.isBlank()) return;
        String[] titulos = peliculasTexto.split("\\r?\\n");
        for (String titulo : titulos) {
            String t = titulo.trim();
            if (!t.isEmpty()) ciclo.agregarPelicula(new Pelicula(t, 90, tipo));
        }
    }

    // ====== MÉTODOS PARA EVENTOS ======
    
    public void crearFeria(String nombre, LocalDate fechaInicio, LocalDate fechaFin,
                          LocalTime horaInicio, LocalTime horaFin,
                          EstadoEvento estado, int cantidadStands,
                          TipoAmbiente tipoAmbiente) {
        
        LocalDateTime inicio = crearDateTime(fechaInicio, horaInicio);
        LocalDateTime fin = crearDateTime(fechaFin, horaFin);
        Feria feria = new Feria(nombre, inicio, fin, cantidadStands, tipoAmbiente);
        agregarEvento(feria, estado);
    }

    public void crearConcierto(String nombre, LocalDate fechaInicio, LocalDate fechaFin,
                              LocalTime horaInicio, LocalTime horaFin, EstadoEvento estado,
                              TipoEntrada tipoEntrada, int cupoMaximo) {
        
        LocalDateTime inicio = crearDateTime(fechaInicio, horaInicio);
        LocalDateTime fin = crearDateTime(fechaFin, horaFin);
        Concierto concierto = new Concierto(nombre, inicio, fin, tipoEntrada, cupoMaximo);
        agregarEvento(concierto, estado);
    }

    public void crearExposicion(String nombre, LocalDate fechaInicio, LocalDate fechaFin,
                               LocalTime horaInicio, LocalTime horaFin, EstadoEvento estado,
                               TipoArte tipoArte) {
        
        LocalDateTime inicio = crearDateTime(fechaInicio, horaInicio);
        LocalDateTime fin = crearDateTime(fechaFin, horaFin);
        Exposicion exposicion = new Exposicion(nombre, inicio, fin, tipoArte);
        agregarEvento(exposicion, estado);
    }

    public void crearTaller(String nombre, LocalDate fechaInicio, LocalDate fechaFin,
                           LocalTime horaInicio, LocalTime horaFin, EstadoEvento estado,
                           int cupoMaximo, Modalidad modalidad) {
        
        LocalDateTime inicio = crearDateTime(fechaInicio, horaInicio);
        LocalDateTime fin = crearDateTime(fechaFin, horaFin);
        Taller taller = new Taller(nombre, inicio, fin, cupoMaximo, modalidad);
        agregarEvento(taller, estado);
    }
    
    public void crearCicloCine(String nombre, LocalDate fechaInicio, LocalDate fechaFin,
                           LocalTime horaInicio, LocalTime horaFin, EstadoEvento estado,
                           boolean postCharla, int cupoMaximo, String peliculasTexto,
                           TipoPelicula tipo) {

        LocalDateTime inicio = crearDateTime(fechaInicio, horaInicio);
        LocalDateTime fin = crearDateTime(fechaFin, horaFin);
        CicloCine ciclo = new CicloCine(nombre, inicio, fin, postCharla, cupoMaximo);
        procesarPeliculas(ciclo, peliculasTexto, tipo);
        agregarEvento(ciclo, estado);
    }

    public List<Evento> listarEventos() {
        return new ArrayList<>(eventos);
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

    private void cargarPersonasDePrueba() {
        try {
            if (personas.isEmpty()) {
                personas.add(new Persona("Ana", "González", "12345678", "12345678", "ana@mail.com"));
                personas.add(new Persona("Luis", "Pérez", "87654321", "56781234", "luis@mail.com"));
            }
        } catch (Exception e) {
            // Si hay error al cargar datos de prueba, no hacer nada
            // Los datos se pueden cargar manualmente desde la UI
            System.err.println("Error al cargar datos de prueba: " + e.getMessage());
        }
    }

    // ⭐ NUEVOS MÉTODOS PARA ABM PARTICIPANTE - MODELO RICO

    /**
     * Factory method: Crea una nueva participación validando reglas de negocio
     * La validación ocurre en el constructor del modelo RolEvento
     */
    public RolEvento crearParticipacion(Evento evento, Persona persona, TipoRol rol) {
        try {
            // El modelo rico valida todo en el constructor
            return new RolEvento(evento, persona, rol);
        } catch (Exception e) {
            throw new RuntimeException("Error al crear participación: " + e.getMessage(), e);
        }
    }

    /**
     * Guarda una participación (intermediario con repositorio)
     */
    public void guardarParticipacion(RolEvento rolEvento) {
        try {
            repositorio.guardarRolEvento(rolEvento);
            // También agregar al evento para mantener consistencia
            rolEvento.getEvento().agregarRol(rolEvento);
        } catch (Exception e) {
            throw new RuntimeException("Error al guardar participación: " + e.getMessage(), e);
        }
    }

    /**
     * Elimina una participación (borrado lógico)
     */
    public void eliminarParticipacion(RolEvento rolEvento) {
        try {
            // La lógica de borrado está en el modelo
            rolEvento.darDeBaja();
            repositorio.actualizarRolEvento(rolEvento);
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar participación: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene todas las participaciones activas (TODOS los roles)
     */
    public ObservableList<RolEvento> obtenerParticipacionesActivas() {
        return repositorio.obtenerRolesActivos();
    }

    /**
     * Obtiene SOLO participantes (rol PARTICIPANTE) activos
     */
    public ObservableList<RolEvento> obtenerSoloParticipantes() {
        return repositorio.obtenerSoloParticipantes();
    }

    /**
     * Filtra participaciones por criterios (delegado al repositorio)
     */
    public ObservableList<RolEvento> filtrarParticipaciones(String nombreEvento, String nombrePersona, String dni) {
        return repositorio.filtrarRoles(nombreEvento, nombrePersona, dni);
    }

    /**
     * Filtra SOLO participantes (rol PARTICIPANTE) por criterios
     */
    public ObservableList<RolEvento> filtrarSoloParticipantes(String nombreEvento, String nombrePersona, String dni) {
        return repositorio.filtrarSoloParticipantes(nombreEvento, nombrePersona, dni);
    }

    /**
     * Obtiene eventos disponibles para inscripción
     */
    public ObservableList<Evento> obtenerEventosDisponibles() {
        // Usar la lista de eventos del Servicio (donde se guardan los eventos creados)
        return eventos.stream()
                .filter(evento -> evento.puedeInscribirParticipantes())
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    /**
     * Reactiva una participación dada de baja
     */
    public void reactivarParticipacion(RolEvento rolEvento) {
        try {
            // La lógica de reactivación está en el modelo
            rolEvento.reactivar();
            repositorio.actualizarRolEvento(rolEvento);
        } catch (Exception e) {
            throw new RuntimeException("Error al reactivar participación: " + e.getMessage(), e);
        }
    }
    
    // ====== PELÍCULAS (en memoria; sin persistencia real de momento) ======

    /** Lista observable para enlazar con la TableView. */
    private static final javafx.collections.ObservableList<Pelicula> peliculas =
            javafx.collections.FXCollections.observableArrayList();

    /** Devuelve la lista observable para setItems(...) en la UI. */
    public javafx.collections.ObservableList<Pelicula> obtenerPeliculas() {
        return peliculas;

        // Persistencia real:
        // return FXCollections.observableArrayList(repositorioPelicula.obtenerTodas());
    }

    /** Alta de película (el modelo rico valida). */
    public void guardarPelicula(Pelicula pelicula) {
        peliculas.add(pelicula);

        // Persistencia real:
        // repositorioPelicula.guardar(pelicula);
    }

    /** Baja de película. */
    public void eliminarPelicula(Pelicula pelicula) {
        peliculas.remove(pelicula);

        // Persistencia real:
        // repositorioPelicula.eliminar(pelicula);
    }

    /** Modificación de película (respetando validaciones del modelo). */
    public void actualizarPelicula(Pelicula original, Pelicula actualizada) {
        original.setTitulo(actualizada.getTitulo());
        original.setDuracionMinutos(actualizada.getDuracionMinutos());
        original.setTipo(actualizada.getTipo());

        // Persistencia real:
        // repositorioPelicula.actualizar(original);
    }

    /** Filtro por título (contains, case-insensitive) y por ID numérico exacto. */
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
                // Si el ID no es un número válido, el resultado es vacío:
                stream = stream.filter(p -> false);
            }
        }

        return stream.collect(java.util.stream.Collectors.toCollection(
                javafx.collections.FXCollections::observableArrayList
        ));
    }

    /** (Opcional) Datos de prueba para la UI. Llamalo desde el constructor si querés. */
    private void cargarPeliculasDePrueba() {
        try {
            if (peliculas.isEmpty()) {
                peliculas.add(new Pelicula("Inception", 148, TipoPelicula.DOS_D));
                peliculas.add(new Pelicula("Interstellar", 169, TipoPelicula.DOS_D));
                peliculas.add(new Pelicula("El secreto de sus ojos", 129, TipoPelicula.DOS_D));
            }
        } catch (Exception e) {
            System.err.println("Error al cargar películas de prueba: " + e.getMessage());
        }
    }

}
