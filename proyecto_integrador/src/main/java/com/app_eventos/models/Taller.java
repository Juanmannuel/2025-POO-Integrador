package com.app_eventos.models;

import com.app_eventos.models.enums.EstadoEvento;
import com.app_eventos.models.enums.Modalidad;
import com.app_eventos.models.enums.TipoInscripcion;
import com.app_eventos.models.enums.TipoEvento;

import java.time.Duration;
import java.time.LocalDateTime;

public class Taller extends Evento {
    private TipoInscripcion tipoInscripcion;
    private Modalidad modalidad;
    private int cupo;

    // Constructor
    public Taller(Long idEvento, String nombre, String descripcion,
                  LocalDateTime fechaInicio, LocalDateTime fechaFin,
                  Duration duracionEstimada, EstadoEvento estado,
                  TipoInscripcion tipoInscripcion, Modalidad modalidad, int cupo) {

        super(idEvento, TipoEvento.TALLER, nombre, descripcion,
              fechaInicio, fechaFin, duracionEstimada, estado);

        this.tipoInscripcion = tipoInscripcion;
        this.modalidad = modalidad;
        this.cupo = cupo;
    }

    // Getters y Setters
    public TipoInscripcion getTipoInscripcion() {
        return tipoInscripcion;
    }

    public void setTipoInscripcion(TipoInscripcion tipoInscripcion) {
        this.tipoInscripcion = tipoInscripcion;
    }

    public Modalidad getModalidad() {
        return modalidad;
    }

    public void setModalidad(Modalidad modalidad) {
        this.modalidad = modalidad;
    }

    public int getCupo() {
        return cupo;
    }

    public void setCupo(int cupo) {
        this.cupo = cupo;
    }
}
