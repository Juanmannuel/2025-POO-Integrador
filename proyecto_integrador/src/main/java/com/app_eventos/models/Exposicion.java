package com.app_eventos.models;

import com.app_eventos.models.enums.TipoArte;

import java.time.LocalDate;
import java.util.List;

public class Exposicion extends Evento {
    private String tema;
    private TipoArte tipoArte;
    private List<Persona> invitados;

    // Constructor
    public Exposicion(int codigo, String nombre, String descripcion, LocalDate fechaInicio, LocalDate fechaFin,
                      String tema, TipoArte tipoArte, List<Persona> invitados) {
        super(codigo, nombre, descripcion, fechaInicio, fechaFin);
        this.tema = tema;
        this.tipoArte = tipoArte;
        this.invitados = invitados;
    }

    // Getters y Setters
    public String getTema() {
        return tema;
    }

    public void setTema(String tema) {
        this.tema = tema;
    }

    public TipoArte getTipoArte() {
        return tipoArte;
    }

    public void setTipoArte(TipoArte tipoArte) {
        this.tipoArte = tipoArte;
    }

    public List<Persona> getInvitados() {
        return invitados;
    }

    public void setInvitados(List<Persona> invitados) {
        this.invitados = invitados;
    }
}
