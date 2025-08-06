package com.app_eventos.model;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

import com.app_eventos.model.enums.TipoEvento;
import com.app_eventos.model.enums.TipoEntrada;
import com.app_eventos.model.enums.EstadoEvento;
import com.app_eventos.model.interfaces.IEventoConInscripcion;

public class Concierto extends Evento implements IEventoConInscripcion {

    private List<Persona> artistas = new ArrayList<>();
    private String artistasTexto; // Campo para el texto de artistas/bandas desde la UI
    private TipoEntrada tipoEntrada;
    private int inscriptos;
    private int cupoMaximo;

    // Constructor
    public Concierto(String nombre,
                     LocalDateTime fechaInicio,
                     LocalDateTime fechaFin,
                     TipoEntrada tipoEntrada) {
        super(nombre, fechaInicio, fechaFin, TipoEvento.CONCIERTO);
        this.tipoEntrada = tipoEntrada;
        this.inscriptos = 0;
    }

    public Concierto() {
        super();
        this.setTipoEvento(TipoEvento.CONCIERTO);
        this.inscriptos = 0;
    }

    // Modelo RICO
    @Override
    public void inscribir(Persona participante) {
        if (getEstado() != EstadoEvento.CONFIRMADO) {
            throw new IllegalStateException("El concierto debe estar confirmado para inscribir.");
        }
        this.inscriptos++;
    }

    public void agregarArtista(Persona artista) {
        if (!artistas.contains(artista)) {
            artistas.add(artista);
        }
    }

    public void quitarArtista(Persona artista) {
        artistas.remove(artista);
    }

    // Getters y Setters

    public List<Persona> getArtistas() {
        return artistas;
    }

    public void setArtistas(List<Persona> artistas) {
        this.artistas = artistas;
    }

    public TipoEntrada getTipoEntrada() {
        return tipoEntrada;
    }

    public void setTipoEntrada(TipoEntrada tipoEntrada) {
        this.tipoEntrada = tipoEntrada;
    }

    public int getInscriptos() {
        return inscriptos;
    }

    public void setInscriptos(int inscriptos) {
        this.inscriptos = inscriptos;
    }

    public int getCupoMaximo() {
        return cupoMaximo;
    }

    public void setCupoMaximo(int cupoMaximo) {
        this.cupoMaximo = cupoMaximo;
    }

    public String getArtistasTexto() {
        return artistasTexto;
    }

    public void setArtistasTexto(String artistasTexto) {
        this.artistasTexto = artistasTexto;
    }

    // Validaciones específicas del concierto
    public void validarDatos() {
        if (cupoMaximo <= 0) {
            throw new IllegalStateException("El cupo máximo debe ser mayor a 0");
        }
        if (tipoEntrada == null) {
            throw new IllegalStateException("Debe especificar el tipo de entrada (gratuita o paga)");
        }
    }
}
