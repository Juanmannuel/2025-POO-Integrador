package com.app_eventos.services;

import com.app_eventos.model.Concierto;
import com.app_eventos.model.Evento;
import com.app_eventos.model.Feria;
import com.app_eventos.model.Persona;
import com.app_eventos.model.RolEvento;
import com.app_eventos.model.enums.EstadoEvento;
import com.app_eventos.model.enums.TipoAmbiente;
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
                            EstadoEvento estado, int cantidadStands, TipoAmbiente tipoAmbiente,
                            List<RolEvento> responsables) {

            LocalDateTime inicio = LocalDateTime.of(fechaInicio, horaInicio);
            LocalDateTime fin = LocalDateTime.of(fechaFin, horaFin);

            Feria feria = new Feria(nombre, inicio, fin, cantidadStands, tipoAmbiente);
            feria.setEstado(estado);
            feria.setRoles(responsables);

            for (RolEvento rol : responsables) {
                rol.setEvento(feria); // ¡asignamos el evento a cada rol!
            }

            eventos.add(feria);
        }


        public List<Evento> listarEventos() {
            return eventos;
        }

        public void crearConcierto(String nombre, LocalDate fechaInicio, LocalDate fechaFin,
                            LocalTime horaInicio, LocalTime horaFin,
                            EstadoEvento estado, TipoEntrada tipoEntrada, int cupoMaximo, List<Persona> artistas) {

        LocalDateTime inicio = LocalDateTime.of(fechaInicio, horaInicio);
        LocalDateTime fin = LocalDateTime.of(fechaFin, horaFin);

        Concierto concierto = new Concierto(nombre, inicio, fin, tipoEntrada);
        concierto.setEstado(estado);

        concierto.setCupoMaximo(cupoMaximo); // asumimos que agregás el campo
        for (Persona artista : artistas) {
            concierto.agregarArtista(artista);
        }

        eventos.add(concierto);
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
