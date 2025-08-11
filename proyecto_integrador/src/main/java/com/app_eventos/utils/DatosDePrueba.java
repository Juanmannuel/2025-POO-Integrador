package com.app_eventos.utils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.app_eventos.model.Concierto;
import com.app_eventos.model.Evento;
import com.app_eventos.model.Persona;
import com.app_eventos.model.Taller;
import com.app_eventos.model.enums.EstadoEvento;
import com.app_eventos.model.enums.TipoEvento;

public class DatosDePrueba {

    public static List<Evento> getEventosDePrueba() {
        List<Evento> eventos = new ArrayList<>();

        Taller taller = new Taller();
        taller.setNombre("Taller de Fotografía");
        taller.setEstado(EstadoEvento.CONFIRMADO);
        taller.setFechaInicio(LocalDateTime.of(2025, 8, 12, 15, 0));
        taller.setTipoEvento(TipoEvento.TALLER);
        eventos.add(taller);

        Concierto concierto = new Concierto();
        concierto.setNombre("Concierto de Rock");
        concierto.setEstado(EstadoEvento.CONFIRMADO); // Usamos un estado que sí existe
        concierto.setFechaInicio(LocalDateTime.of(2025, 8, 22, 21, 0));
        concierto.setTipoEvento(TipoEvento.CONCIERTO);
        eventos.add(concierto);

        return eventos;
    }

    // MÉTODO AÑADIDO que faltaba
    public static List<Persona> getPersonasDePrueba() {
        List<Persona> personas = new ArrayList<>();
        personas.add(new Persona("11222333", "Juan", "Perez", "3756-111111", "juan@mail.com"));
        personas.add(new Persona("22333444", "Maria", "Gomez", "3756-222222", "maria@mail.com"));
        return personas;
    }
}