package com.app_eventos.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.app_eventos.model.enums.TipoEntrada;
import com.app_eventos.model.enums.TipoEvento;
import com.app_eventos.model.enums.TipoRol;
import com.app_eventos.model.enums.EstadoEvento;
import com.app_eventos.model.interfaces.IEventoConCupo;

public class Concierto extends Evento implements IEventoConCupo {

    private TipoEntrada tipoEntrada;
    private int cupoMaximo;
    private final List<Persona> participantes = new ArrayList<>();

    public Concierto(String nombre,
                     LocalDateTime fechaInicio,
                     LocalDateTime fechaFin,
                     TipoEntrada tipoEntrada,
                     int cupoMaximo) {
        super(nombre, fechaInicio, fechaFin, TipoEvento.CONCIERTO);
        setTipoEntrada(tipoEntrada);
        setCupoMaximo(cupoMaximo);
    }

    public Concierto() {
        super();
        setTipoEvento(TipoEvento.CONCIERTO);
    }

    // Implementación de IEventoConInscripcion 
    @Override
    public void inscribirParticipante(Persona persona) {
        if (getEstado() != EstadoEvento.CONFIRMADO) {
            throw new IllegalStateException("El concierto debe estar confirmado para inscribir participantes.");
        }
        if (participantes.size() >= cupoMaximo) {
            throw new IllegalStateException("No se pueden inscribir más participantes, cupo completo.");
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
    public boolean hayCupoDisponible() {
        return participantes.size() < cupoMaximo;
    }

    @Override
    public boolean tieneCupoDisponible() {
        return hayCupoDisponible();
    }

    @Override
    public int getCupoMaximo() {
        return cupoMaximo;
    }

    @Override
    public void setCupoMaximo(int cupoMaximo) {
        if (cupoMaximo <= 0) {
            throw new IllegalArgumentException("El cupo máximo debe ser mayor a cero.");
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
        return rol == TipoRol.ARTISTA  || rol == TipoRol.ORGANIZADOR;
    }

    // Getters y setters propios
    public TipoEntrada getTipoEntrada() {
        return tipoEntrada;
    }

    public void setTipoEntrada(TipoEntrada tipoEntrada) {
        if (tipoEntrada == null) {
            throw new IllegalArgumentException("El tipo de entrada no puede ser nulo.");
        }
        this.tipoEntrada = tipoEntrada;
    }
}
