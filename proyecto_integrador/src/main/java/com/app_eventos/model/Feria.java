package com.app_eventos.model;

import java.time.LocalDateTime;

import com.app_eventos.model.enums.TipoAmbiente;
import com.app_eventos.model.enums.TipoEvento;
import com.app_eventos.model.enums.EstadoEvento;
import com.app_eventos.model.interfaces.IEventoConInscripcion;

public class Feria extends Evento implements IEventoConInscripcion {

    private int cantidadStands;
    private TipoAmbiente tipoAmbiente;
    private int inscriptos;

    // Constructor
    public Feria(String nombre,
                 LocalDateTime fechaInicio,
                 LocalDateTime fechaFin,
                 int cantidadStands,
                 TipoAmbiente tipoAmbiente) {
        super(nombre, fechaInicio, fechaFin, TipoEvento.FERIA);
        this.cantidadStands = cantidadStands;
        this.tipoAmbiente = tipoAmbiente;
        this.inscriptos = 0;
    }

    public Feria() {
        super();
        this.setTipoEvento(TipoEvento.FERIA);
        this.inscriptos = 0;
    }

    // Modelo RICO

    @Override
    public void inscribir(Persona participante) {
        if (getEstado() != EstadoEvento.CONFIRMADO) {
            throw new IllegalStateException("La feria debe estar confirmada para inscribir.");
        }
        this.inscriptos++;
    }

    // Getters y Setters

    public int getCantidadStands() {
        return cantidadStands;
    }

    public void setCantidadStands(int cantidadStands) {
        this.cantidadStands = cantidadStands;
    }

    public TipoAmbiente getTipoAmbiente() {
        return tipoAmbiente;
    }

    public void setTipoAmbiente(TipoAmbiente tipoAmbiente) {
        this.tipoAmbiente = tipoAmbiente;
    }

    public int getInscriptos() {
        return inscriptos;
    }

    public void setInscriptos(int inscriptos) {
        this.inscriptos = inscriptos;
    }
}
