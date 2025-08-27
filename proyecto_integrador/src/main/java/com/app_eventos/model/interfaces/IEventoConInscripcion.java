package com.app_eventos.model.interfaces;

import java.util.List;
import com.app_eventos.model.Persona;
public interface IEventoConInscripcion {
    void inscribirParticipante(Persona persona);
    void desinscribirParticipante(Persona persona);
    List<Persona> getParticipantes();
    int getCupoMaximo();
    void setCupoMaximo(int cupoMax);
    int getCupoDisponible();
}
