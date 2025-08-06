package com.app_eventos.services;

import com.app_eventos.model.Persona;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.stream.Collectors;

public class Servicio {

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
