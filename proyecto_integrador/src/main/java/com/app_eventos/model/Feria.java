package com.app_eventos.model;

import java.time.LocalDateTime;

import com.app_eventos.model.enums.TipoAmbiente;
import com.app_eventos.model.enums.TipoEvento;

public class Feria extends Evento {

    private int cantidadStands;
    private TipoAmbiente tipoAmbiente;

    // Constructor
    public Feria(String nombre,
                 LocalDateTime fechaInicio,
                 LocalDateTime fechaFin,
                 int cantidadStands,
                 TipoAmbiente tipoAmbiente) {
        super(nombre, fechaInicio, fechaFin, TipoEvento.FERIA);
        this.cantidadStands = cantidadStands;
        this.tipoAmbiente = tipoAmbiente;
    }

    public Feria() {
        super();
        this.setTipoEvento(TipoEvento.FERIA);
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
}
