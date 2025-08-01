package com.app_eventos.model;

import java.time.LocalDateTime;

import com.app_eventos.model.enums.TipoArte;
import com.app_eventos.model.enums.TipoEvento;
import com.app_eventos.model.enums.EstadoEvento;
import com.app_eventos.model.interfaces.IEventoConInscripcion;

public class Exposicion extends Evento implements IEventoConInscripcion {

    private TipoArte tipoArte;
    private Persona curador;
    private int inscriptos;

    // Constructor
    public Exposicion(String nombre,
                      LocalDateTime fechaInicio,
                      LocalDateTime fechaFin,
                      TipoArte tipoArte) {
        super(nombre, fechaInicio, fechaFin, TipoEvento.EXPOSICION);
        this.tipoArte = tipoArte;
        this.inscriptos = 0;
    }

    public Exposicion() {
        super();
        this.setTipoEvento(TipoEvento.EXPOSICION);
        this.inscriptos = 0;
    }

    // Modelo RICO

    @Override
    public void inscribir(Persona participante) {
        if (getEstado() != EstadoEvento.CONFIRMADO) {
            throw new IllegalStateException("La exposición debe estar confirmada para inscribir.");
        }
        this.inscriptos++;
    }

    public void asignarCurador(Persona persona) {
        if (this.curador != null) {
            throw new IllegalStateException("Ya hay un curador asignado.");
        }
        this.curador = persona;
    }

    public void quitarCurador() {
        this.curador = null;
    }

    // Getters y setters

    public TipoArte getTipoArte() {
        return tipoArte;
    }

    public void setTipoArte(TipoArte tipoArte) {
        this.tipoArte = tipoArte;
    }

    public Persona getCurador() {
        return curador;
    }

    public void setCurador(Persona curador) {
        this.curador = curador;
    }

    public int getInscriptos() {
        return inscriptos;
    }

    public void setInscriptos(int inscriptos) {
        this.inscriptos = inscriptos;
    }
}
