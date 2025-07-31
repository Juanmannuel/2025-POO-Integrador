package com.app_eventos.model;

import java.time.Duration;
import java.time.LocalDateTime;

import com.app_eventos.model.enums.EstadoEvento;
import com.app_eventos.model.enums.TipoAmbiente;
import com.app_eventos.model.enums.TipoEvento;

public class Feria extends Evento {
    private int cantidadStands;
    private TipoAmbiente tipoAmbiente;

    // Constructor
    public Feria(Long idEvento, String nombre, String descripcion,
                 LocalDateTime fechaInicio, LocalDateTime fechaFin,
                 Duration duracionEstimada, EstadoEvento estado,
                 int cantidadStands, TipoAmbiente tipoAmbiente) {

        super(idEvento, TipoEvento.FERIA, nombre, descripcion,
              fechaInicio, fechaFin, duracionEstimada, estado);

        this.cantidadStands = cantidadStands;
        this.tipoAmbiente = tipoAmbiente;
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