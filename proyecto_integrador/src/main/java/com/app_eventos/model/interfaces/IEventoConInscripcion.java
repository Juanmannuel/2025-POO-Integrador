package com.app_eventos.model.interfaces;

import com.app_eventos.model.Persona;
import java.util.List;

public interface IEventoConInscripcion {
    void inscribirParticipante(Persona persona);
    void desinscribirParticipante(Persona persona);
    List<Persona> getParticipantes();
}
