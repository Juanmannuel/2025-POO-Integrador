package com.app_eventos.models;

import com.app_eventos.models.enums.TipoAmbiente;
import com.app_eventos.models.enums.TipoEntrada;

import java.time.LocalDate;

public class Feria extends Evento {
    private TipoAmbiente tipoAmbiente;
    private TipoEntrada tipoEntrada;

    // Constructor
    public Feria(int codigo, String nombre, String descripcion, LocalDate fechaInicio, LocalDate fechaFin,
                 TipoAmbiente tipoAmbiente, TipoEntrada tipoEntrada) {
        super(codigo, nombre, descripcion, fechaInicio, fechaFin);
        this.tipoAmbiente = tipoAmbiente;
        this.tipoEntrada = tipoEntrada;
    }

    // Getters y setters
    public TipoAmbiente getTipoAmbiente() {
        return tipoAmbiente;
    }

    public void setTipoAmbiente(TipoAmbiente tipoAmbiente) {
        this.tipoAmbiente = tipoAmbiente;
    }

    public TipoEntrada getTipoEntrada() {
        return tipoEntrada;
    }

    public void setTipoEntrada(TipoEntrada tipoEntrada) {
        this.tipoEntrada = tipoEntrada;
    }
}