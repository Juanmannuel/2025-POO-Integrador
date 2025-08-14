package com.app_eventos.model.interfaces;

public interface IEventoConCupo extends IEventoConInscripcion {
    int getCupoMaximo();
    void setCupoMaximo(int cupoMax);
    int getCupoDisponible();
}
