package com.app_eventos.repository;

import com.app_eventos.model.Evento;
import com.app_eventos.model.RolEvento;
import com.app_eventos.model.enums.TipoRol;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.stream.Collectors;

public class Repositorio {
    
    // Simulación de base de datos en memoria
    private static final ObservableList<RolEvento> rolesEvento = FXCollections.observableArrayList();
    private static final ObservableList<Evento> eventos = FXCollections.observableArrayList();

    // ⭐ CRUD BÁSICO PARA ROLEVENTO

    public void guardarRolEvento(RolEvento rolEvento) {
        rolesEvento.add(rolEvento);
    }

    public void actualizarRolEvento(RolEvento rolEvento) {
        // En una BD real, esto sería un UPDATE
        // Como trabajamos en memoria, el objeto ya está actualizado
    }

    // ⭐ CONSULTAS ESPECÍFICAS PARA PARTICIPACIONES

    /**
     * Obtiene todos los roles activos (no dados de baja)
     */
    public ObservableList<RolEvento> obtenerRolesActivos() {
        return rolesEvento.stream()
                .filter(RolEvento::estaActivo)
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    /**
     * Obtiene SOLO participantes (rol PARTICIPANTE) activos
     */
    public ObservableList<RolEvento> obtenerSoloParticipantes() {
        return rolesEvento.stream()
                .filter(RolEvento::estaActivo)
                .filter(rol -> rol.getRol() == TipoRol.PARTICIPANTE)
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    /**
     * Filtra roles por criterios múltiples
     */
    public ObservableList<RolEvento> filtrarRoles(String nombreEvento, String nombrePersona, String dni) {
        return rolesEvento.stream()
                .filter(RolEvento::estaActivo)
                .filter(rol -> filtrarPorEvento(rol, nombreEvento))
                .filter(rol -> filtrarPorPersona(rol, nombrePersona, dni))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    /**
     * Filtra SOLO participantes (rol PARTICIPANTE) por criterios múltiples
     */
    public ObservableList<RolEvento> filtrarSoloParticipantes(String nombreEvento, String nombrePersona, String dni) {
        return rolesEvento.stream()
                .filter(RolEvento::estaActivo)
                .filter(rol -> rol.getRol() == TipoRol.PARTICIPANTE)
                .filter(rol -> filtrarPorEvento(rol, nombreEvento))
                .filter(rol -> filtrarPorPersona(rol, nombrePersona, dni))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    /**
     * Obtiene eventos confirmados (disponibles para inscripción)
     */
    public ObservableList<Evento> obtenerEventosConfirmados() {
        return eventos.stream()
                .filter(evento -> evento.puedeInscribirParticipantes())
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    // ⭐ MÉTODOS AUXILIARES DE FILTRADO

    private boolean filtrarPorEvento(RolEvento rol, String nombreEvento) {
        if (nombreEvento == null || nombreEvento.isBlank()) {
            return true;
        }
        return rol.getEvento().getNombre().toLowerCase().contains(nombreEvento.toLowerCase());
    }

    private boolean filtrarPorPersona(RolEvento rol, String nombrePersona, String dni) {
        boolean coincideNombre = nombrePersona == null || nombrePersona.isBlank()
                || rol.getPersona().getNombre().toLowerCase().contains(nombrePersona.toLowerCase())
                || rol.getPersona().getApellido().toLowerCase().contains(nombrePersona.toLowerCase());

        boolean coincideDni = dni == null || dni.isBlank()
                || rol.getPersona().getDni().contains(dni);

        return coincideNombre && coincideDni;
    }

    // ⭐ MÉTODOS PARA AGREGAR DATOS DE PRUEBA

    public void agregarEvento(Evento evento) {
        eventos.add(evento);
    }

    public ObservableList<Evento> obtenerTodosLosEventos() {
        return eventos;
    }
}
