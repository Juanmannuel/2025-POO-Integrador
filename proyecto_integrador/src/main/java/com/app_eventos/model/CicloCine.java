package com.app_eventos.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.app_eventos.model.enums.EstadoEvento;
import com.app_eventos.model.enums.TipoEvento;
import com.app_eventos.model.enums.TipoRol;
import com.app_eventos.model.interfaces.IEventoConCupo;

public class CicloCine extends Evento implements IEventoConCupo {

    private final List<Pelicula> peliculas = new ArrayList<>();
    private final List<Persona> participantes = new ArrayList<>();
    private boolean postCharla;
    private int cupoMaximo;

    // Constructor
    public CicloCine(String nombre, LocalDateTime fechaInicio, LocalDateTime fechaFin, boolean postCharla, int cupoMaximo) {
        super(nombre, fechaInicio, fechaFin, TipoEvento.CICLO_CINE);
        setPostCharla(postCharla);
        setCupoMaximo(cupoMaximo);
    }

    public CicloCine() {
        super();
        setTipoEvento(TipoEvento.CICLO_CINE);
    }

    // Métodos específicos
    public void agregarPelicula(Pelicula pelicula) {
        if (pelicula == null) {
            throw new IllegalArgumentException("La película no puede ser nula.");
        }
        peliculas.add(pelicula);
    }

    public void quitarPelicula(Pelicula pelicula) {
        peliculas.remove(pelicula);
    }

    // Implementación de IEventoConInscripcion 
    @Override
    public void inscribirParticipante(Persona persona) {
        if (getEstado() != EstadoEvento.CONFIRMADO) {
            throw new IllegalStateException("El evento debe estar confirmado para inscribir participantes.");
        }
        if (participantes.size() >= cupoMaximo) {
            throw new IllegalStateException("Cupo lleno. No se pueden inscribir más participantes.");
        }
        if (participantes.contains(persona)) {
            throw new IllegalArgumentException("El participante ya está inscrito.");
        }
        participantes.add(persona);
    }

    @Override
    public void desinscribirParticipante(Persona persona) {
        participantes.remove(persona);
    }

    @Override
    public List<Persona> getParticipantes() {
        return new ArrayList<>(participantes);
    }

    // Implementación de IEventoConCupo 
    @Override
    public int getCupoMaximo() {
        return cupoMaximo;
    }

    @Override
    public void setCupoMaximo(int cupoMaximo) {
        if (cupoMaximo <= 0) {
            throw new IllegalArgumentException("El cupo máximo debe ser mayor que cero.");
        }
        this.cupoMaximo = cupoMaximo;
    }

    @Override
    public int getCupoDisponible() {
        return cupoMaximo - participantes.size();
    }

    // Lógica de roles permitidos
    @Override
    protected boolean rolPermitido(TipoRol rol) {
        return rol == TipoRol.ORGANIZADOR;
    }

    // Getters y setters 
    public List<Pelicula> getPeliculas() {
        return new ArrayList<>(peliculas);
    }

    public boolean isPostCharla() {
        return postCharla;
    }

    public void setPostCharla(boolean postCharla) {
        this.postCharla = postCharla;
    }
}
