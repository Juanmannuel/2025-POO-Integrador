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
    private static final ObservableList<Persona> personas = FXCollections.observableArrayList();
    
    // Constructor privado para Singleton
    private Servicio() {
        cargarPersonasDePrueba(); // Datos de ejemplo para testeo
    }
    
    // Método para obtener la instancia compartida
    public static Servicio getInstance() {
        return INSTANCE;
    }

    public void crearFeria(String nombre, LocalDate fechaInicio, LocalDate fechaFin,
                       LocalTime horaInicio, LocalTime horaFin,
                       EstadoEvento estado, int cantidadStands,
                       TipoAmbiente tipoAmbiente) {

    LocalDateTime inicio = LocalDateTime.of(fechaInicio, horaInicio);
    LocalDateTime fin = LocalDateTime.of(fechaFin, horaFin);

    Feria feria = new Feria(nombre, inicio, fin, cantidadStands, tipoAmbiente);
    feria.setEstado(estado);

    eventos.add(feria);
    // Sincronizar con el repositorio para que esté disponible en ABM Participante
    repositorio.agregarEvento(feria);
    }

    public List<Evento> listarEventos() {
        return eventos;
    }

    public void crearConcierto(String nombre, LocalDate fechaInicio, LocalDate fechaFin,
                            LocalTime horaInicio, LocalTime horaFin, EstadoEvento estado,
                            TipoEntrada tipoEntrada, int cupoMaximo) {

        LocalDateTime inicio = LocalDateTime.of(fechaInicio, horaInicio);
        LocalDateTime fin = LocalDateTime.of(fechaFin, horaFin);

        Concierto concierto = new Concierto(nombre, inicio, fin, tipoEntrada, cupoMaximo);
        concierto.setEstado(estado); // valida internamente
        eventos.add(concierto);
        // Sincronizar con el repositorio para que esté disponible en ABM Participante
        repositorio.agregarEvento(concierto);
    }

    public void crearExposicion(String nombre, LocalDate fechaInicio, LocalDate fechaFin,
                                LocalTime horaInicio, LocalTime horaFin, EstadoEvento estado,
                                TipoArte tipoArte) {

        LocalDateTime inicio = LocalDateTime.of(fechaInicio, horaInicio);
        LocalDateTime fin = LocalDateTime.of(fechaFin, horaFin);

        Exposicion exposicion = new Exposicion(nombre, inicio, fin, tipoArte);
        exposicion.setEstado(estado); // valida internamente
        eventos.add(exposicion);
    }

    public void crearTaller(String nombre, LocalDate fechaInicio, LocalDate fechaFin,
                            LocalTime horaInicio, LocalTime horaFin, EstadoEvento estado,
                            int cupoMaximo, Modalidad modalidad) {

        LocalDateTime inicio = LocalDateTime.of(fechaInicio, horaInicio);
        LocalDateTime fin = LocalDateTime.of(fechaFin, horaFin);

        Taller taller = new Taller(nombre, inicio, fin, cupoMaximo, modalidad);
        taller.setEstado(estado); // validación dentro de Evento
        eventos.add(taller);
    }
    public void crearCicloCine(String nombre, LocalDate fechaInicio, LocalDate fechaFin,
                            LocalTime horaInicio, LocalTime horaFin, EstadoEvento estado,
                            boolean postCharla, int cupoMaximo, String peliculasTexto) {

        LocalDateTime inicio = LocalDateTime.of(fechaInicio, horaInicio);
        LocalDateTime fin = LocalDateTime.of(fechaFin, horaFin);

        CicloCine ciclo = new CicloCine(nombre, inicio, fin, postCharla, cupoMaximo);
        ciclo.setEstado(estado);

        // Procesar texto de películas y agregarlas (en el futuro serán objetos Pelicula reales)
        if (peliculasTexto != null && !peliculasTexto.isBlank()) {
        String[] titulos = peliculasTexto.split("\\r?\\n");
        for (String titulo : titulos) {
            // Por ahora solo guardamos como texto, sin crear Pelicula real
            // Cuando tengas ABM de Películas, aquí buscarás en la lista de películas reales
            ciclo.agregarPelicula(new Pelicula(titulo, 90)); // 90 como valor temporal
        }
    }
        eventos.add(ciclo);
}

    // Devuelve la lista completa de personas cargadas.
    public ObservableList<Persona> obtenerPersonas() {
        return personas;

        // Persistencia
        // return FXCollections.observableArrayList(repositorioPersona.obtenerTodas());
    }

    // Agrega una nueva persona.
    public void guardarPersona(Persona persona) {
        personas.add(persona);

        // Persistencia
        // repositorioPersona.guardar(persona);
    }

    // Elimina una persona existente.
    public void eliminarPersona(Persona persona) {
        personas.remove(persona);

        // Persistencia
        // repositorioPersona.eliminar(persona);
    }

    // Actualiza los datos de una persona.
    public void actualizarPersona(Persona original, Persona actualizada) {
        original.actualizarCon(actualizada);

        // Persistencia 
        // repositorioPersona.actualizar(original);
    }

    // Filtra personas por nombre y/o DNI.
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

    //Carga inicial de personas para pruebas.
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
}
