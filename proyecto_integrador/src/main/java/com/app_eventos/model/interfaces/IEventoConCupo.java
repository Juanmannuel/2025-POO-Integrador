package com.app_eventos.model.interfaces;

public interface IEventoConCupo {
    boolean hayCupoDisponible();
    int getCupoMaximo();
    int getInscriptos();
    boolean estaCompleto();
}
