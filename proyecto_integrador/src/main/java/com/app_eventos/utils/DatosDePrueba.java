package com.app_eventos.utils;

import com.app_eventos.model.Evento;
import com.app_eventos.model.Persona;
import com.app_eventos.model.Taller;
import com.app_eventos.model.Concierto;
import com.app_eventos.model.enums.EstadoEvento;
import com.app_eventos.model.enums.TipoEvento;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DatosDePrueba {

    public static List<Evento> getEventosDePrueba() {
        List<Evento> eventos = new ArrayList<>();

        // Taller el día 12 de Agosto 2025
        Taller taller = new Taller();
        taller.setNombre("Taller de Fotografía");
        taller.setFechaInicio(LocalDateTime.of(2025, 8, 12, 15, 0));
        taller.setFechaFin(LocalDateTime.of(2025, 8, 12, 17, 0));
        taller.setTipoEvento(TipoEvento.TALLER);
        taller.setEstado(EstadoEvento.CONFIRMADO); // Usamos un estado que sí existe
        eventos.add(taller);

        // Concierto el día 22 de Agosto 2025
        Concierto concierto = new Concierto();
        concierto.setNombre("Concierto de Rock");
        concierto.setFechaInicio(LocalDateTime.of(2025, 8, 22, 21, 0));
        concierto.setFechaFin(LocalDateTime.of(2025, 8, 22, 23, 0));
        concierto.setTipoEvento(TipoEvento.CONCIERTO);
        concierto.setEstado(EstadoEvento.CONFIRMADO); // También usamos CONFIRMADO
        eventos.add(concierto);
        

        return eventos;
    }

    public static List<Persona> getPersonasDePrueba() {
        List<Persona> personas = new ArrayList<>();
        // Usamos datos que pasan las validaciones de la clase Persona
        personas.add(new Persona("Juan", "Perez", "11222333", "3756111111", "juan@mail.com"));
        personas.add(new Persona("Maria", "Gomez", "22333444", "3756222222", "maria@mail.com"));
        return personas;
    }
}

