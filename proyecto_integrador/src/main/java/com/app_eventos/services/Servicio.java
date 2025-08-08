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
import com.app_eventos.model.interfaces.IEventoConInscripcion;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Servicio {
    
    private static Servicio instance;
    
    public static Servicio getInstance() {
        if (instance == null) {
            instance = new Servicio();
        }
        return instance;
    }
    
    private Servicio() {
        cargarPersonasDePrueba();
    }

    private final List<Evento> eventos = new ArrayList<>();
    private final ObservableList<RolEvento> participantesInscritos = FXCollections.observableArrayList();
    private static final ObservableList<Persona> personas = FXCollections.observableArrayList();

    // ====== MÉTODOS PRIVADOS AUXILIARES ======
    
    private LocalDateTime crearDateTime(LocalDate fecha, LocalTime hora) {
        return LocalDateTime.of(fecha, hora);
    }
    
    private void agregarEvento(Evento evento, EstadoEvento estado) {
        evento.setEstado(estado);
        eventos.add(evento);
    }
    
    private void procesarPeliculas(CicloCine ciclo, String peliculasTexto) {
        if (peliculasTexto == null || peliculasTexto.isBlank()) {
            return;
        }
        
        String[] titulos = peliculasTexto.split("\\r?\\n");
        for (String titulo : titulos) {
            if (!titulo.trim().isEmpty()) {
                ciclo.agregarPelicula(new Pelicula(titulo.trim(), 90));
            }
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
                              boolean postCharla, int cupoMaximo, String peliculasTexto) {
        
        LocalDateTime inicio = crearDateTime(fechaInicio, horaInicio);
        LocalDateTime fin = crearDateTime(fechaFin, horaFin);
        CicloCine ciclo = new CicloCine(nombre, inicio, fin, postCharla, cupoMaximo);
        
        procesarPeliculas(ciclo, peliculasTexto);
        agregarEvento(ciclo, estado);
    }

    public List<Evento> listarEventos() {
        return new ArrayList<>(eventos);
    }

    // ====== MÉTODOS PARA PARTICIPANTES ======
    
    public ObservableList<RolEvento> obtenerParticipantesInscritos() {
        return participantesInscritos;
    }
    
    public void inscribirParticipante(Evento evento, Persona persona) {
        RolEvento inscripcion = new RolEvento(evento, persona, TipoRol.PARTICIPANTE);
        participantesInscritos.add(inscripcion);
        
        if (evento instanceof IEventoConInscripcion eventoConInscripcion) {
            eventoConInscripcion.inscribirParticipante(persona);
        }
    }
    
    public void eliminarParticipante(Evento evento, Persona persona) {
        participantesInscritos.removeIf(rol -> 
            rol.getEvento().equals(evento) && 
            rol.getPersona().equals(persona) && 
            rol.getRol() == TipoRol.PARTICIPANTE);
        
        if (evento instanceof IEventoConInscripcion eventoConInscripcion) {
            eventoConInscripcion.desinscribirParticipante(persona);
        }
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
        if (personas.isEmpty()) {
            personas.add(new Persona("Ana", "González", "12345678", "12345678", "ana@mail.com"));
            personas.add(new Persona("Luis", "Pérez", "87654321", "56781234", "luis@mail.com"));
        }
    }
}
