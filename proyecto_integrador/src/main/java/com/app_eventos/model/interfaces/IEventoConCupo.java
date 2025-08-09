package com.app_eventos.model.interfaces;

public interface IEventoConCupo extends IEventoConInscripcion {
    boolean hayCupoDisponible();
    boolean tieneCupoDisponible();
    int getCupoMaximo();
    void setCupoMaximo(int cupoMaximo);
    int getCupoDisponible();
}
