package com.app_eventos.services;

import com.app_eventos.model.CicloCine;
import com.app_eventos.model.Concierto;
import com.app_eventos.model.Evento;
import com.app_eventos.model.Exposicion;
import com.app_eventos.model.Feria;
import com.app_eventos.model.Pelicula;
import com.app_eventos.model.Persona;
import com.app_eventos.model.Taller;
import com.app_eventos.model.enums.EstadoEvento;
import com.app_eventos.model.enums.Modalidad;
import com.app_eventos.model.enums.TipoAmbiente;
import com.app_eventos.model.enums.TipoArte;
import com.app_eventos.model.enums.TipoEntrada;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Servicio {

    private List<Evento> eventos = new ArrayList<>();

    public void crearFeria(String nombre, LocalDate fechaInicio, LocalDate fechaFin,
                       LocalTime horaInicio, LocalTime horaFin,
                       EstadoEvento estado, int cantidadStands,
                       TipoAmbiente tipoAmbiente) {

    LocalDateTime inicio = LocalDateTime.of(fechaInicio, horaInicio);
    LocalDateTime fin = LocalDateTime.of(fechaFin, horaFin);

    Feria feria = new Feria(nombre, inicio, fin, cantidadStands, tipoAmbiente);
    feria.setEstado(estado);

    eventos.add(feria);
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

    
    // Simulación de base de datos en memoria
    private static final ObservableList<Persona> personas = FXCollections.observableArrayList();

    public Servicio() {
        cargarPersonasDePrueba(); // Datos de ejemplo para testeo
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
        if (personas.isEmpty()) {
            personas.add(new Persona("Ana", "González", "12345678", "12345678", "ana@mail.com"));
            personas.add(new Persona("Luis", "Pérez", "87654321", "56781234", "luis@mail.com"));
        }
    }
}
