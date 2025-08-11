package com.app_eventos.model;

import java.time.LocalDateTime;

import com.app_eventos.model.enums.TipoAmbiente;
import com.app_eventos.model.enums.TipoEvento;
import com.app_eventos.model.enums.TipoRol;

public class Feria extends Evento {

    private int cantidadStands;
    private TipoAmbiente ambiente;
    

    public Feria(String nombre, LocalDateTime fechaInicio, LocalDateTime fechaFin, int cantidadStands, TipoAmbiente tipoAmbiente) {
        super(nombre, fechaInicio, fechaFin, TipoEvento.FERIA);
        setCantidadStands(cantidadStands);
        setTipoAmbiente(tipoAmbiente);
    }

    public Feria() {
        super();
        setTipoEvento(TipoEvento.FERIA);
    }

    @Override
    protected boolean rolPermitido(TipoRol rol) {
        return rol == TipoRol.ORGANIZADOR;
    }


    public int getCantidadStands() { return cantidadStands; }
    public void setCantidadStands(int cantStand) {
        if (cantStand <= 0) 
        throw new IllegalArgumentException("La cantidad de stands debe ser mayor a cero.");
        this.cantidadStands = cantStand;
    }

    public TipoAmbiente getAmbiente() { return ambiente; }
    public void setAmbiente(TipoAmbiente amb) {
        if (amb == null) throw new IllegalArgumentException("Ambiente obligatorio");
        this.ambiente = amb;
    }

    public void setTipoAmbiente(TipoAmbiente tipoAmbiente) {
        if (tipoAmbiente == null) {
            throw new IllegalArgumentException("El tipo de ambiente no puede ser nulo.");
        }
        this.ambiente = tipoAmbiente;
    }
}
