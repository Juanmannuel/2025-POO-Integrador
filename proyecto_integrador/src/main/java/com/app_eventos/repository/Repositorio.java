package com.app_eventos.repository;

import com.app_eventos.model.Evento;
import com.app_eventos.model.Persona;
import com.app_eventos.model.RolEvento;
import java.util.ArrayList;
import java.util.List;

public class Repositorio {
    
    // Almacenamiento en memoria (simulando base de datos)
    private List<Evento> eventos = new ArrayList<>();
    private List<Persona> personas = new ArrayList<>();
    private List<RolEvento> rolesEvento = new ArrayList<>();
    
    private Long siguienteIdEvento = 1L;
    private Long siguienteIdPersona = 1L;
    private Long siguienteIdRol = 1L;
    
    // ===== MÉTODOS PARA PERSONA =====
    
    public Persona guardarPersona(Persona persona) {
        if (persona.getIdPersona() == null) {
            persona.setIdPersona(siguienteIdPersona++);
            personas.add(persona);
        } else {
            // Actualizar persona existente
            int index = personas.indexOf(persona);
            if (index >= 0) {
                personas.set(index, persona);
            }
        }
        return persona;
    }
    
    public List<Persona> obtenerTodasLasPersonas() {
        return new ArrayList<>(personas);
    }
    
    public Persona buscarPersonaPorId(Long id) {
        return personas.stream()
                      .filter(p -> p.getIdPersona().equals(id))
                      .findFirst()
                      .orElse(null);
    }
    
    public Persona buscarPersonaPorDni(String dni) {
        return personas.stream()
                      .filter(p -> p.getDni().equals(dni))
                      .findFirst()
                      .orElse(null);
    }
    
    public boolean existePersonaConDni(String dni) {
        return personas.stream()
                      .anyMatch(p -> p.getDni().equals(dni));
    }
    
    public void eliminarPersona(Long id) {
        personas.removeIf(p -> p.getIdPersona().equals(id));
        // También eliminar roles asociados
        rolesEvento.removeIf(r -> r.getPersona().getIdPersona().equals(id));
    }
    
    // ===== MÉTODOS PARA EVENTO =====
    
    public Evento guardarEvento(Evento evento) {
        if (evento.getIdEvento() == null) {
            evento.setIdEvento(siguienteIdEvento++);
            eventos.add(evento);
        } else {
            // Actualizar evento existente
            int index = eventos.indexOf(evento);
            if (index >= 0) {
                eventos.set(index, evento);
            }
        }
        return evento;
    }
    
    public List<Evento> obtenerTodosLosEventos() {
        return new ArrayList<>(eventos);
    }
    
    public Evento buscarEventoPorId(Long id) {
        return eventos.stream()
                     .filter(e -> e.getIdEvento().equals(id))
                     .findFirst()
                     .orElse(null);
    }
    
    public void eliminarEvento(Long id) {
        eventos.removeIf(e -> e.getIdEvento().equals(id));
        // También eliminar roles asociados
        rolesEvento.removeIf(r -> r.getEvento().getIdEvento().equals(id));
    }
    
    // ===== MÉTODOS PARA ROL EVENTO (INSCRIPCIONES) =====
    
    public RolEvento guardarRolEvento(RolEvento rolEvento) {
        if (rolEvento.getId() == null) {
            rolEvento.setId(siguienteIdRol++);
            rolesEvento.add(rolEvento);
        }
        return rolEvento;
    }
    
    public List<RolEvento> obtenerTodosLosRoles() {
        return new ArrayList<>(rolesEvento);
    }
    
    public List<RolEvento> obtenerRolesPorEvento(Long idEvento) {
        return rolesEvento.stream()
                         .filter(r -> r.getEvento().getIdEvento().equals(idEvento))
                         .toList();
    }
    
    public List<RolEvento> obtenerRolesPorPersona(Long idPersona) {
        return rolesEvento.stream()
                         .filter(r -> r.getPersona().getIdPersona().equals(idPersona))
                         .toList();
    }
    
    public boolean estaInscripto(Long idPersona, Long idEvento) {
        return rolesEvento.stream()
                         .anyMatch(r -> r.getPersona().getIdPersona().equals(idPersona) &&
                                      r.getEvento().getIdEvento().equals(idEvento));
    }
    
    public void eliminarRolEvento(Long id) {
        rolesEvento.removeIf(r -> r.getId().equals(id));
    }
    
    // ===== MÉTODOS DE UTILIDAD =====
    
    public int contarParticipantesPorEvento(Long idEvento) {
        return (int) rolesEvento.stream()
                               .filter(r -> r.getEvento().getIdEvento().equals(idEvento))
                               .filter(r -> r.esParticipante())
                               .count();
    }
}
