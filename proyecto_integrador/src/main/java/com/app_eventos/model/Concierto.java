package com.app_eventos.model;

import jakarta.persistence.*;
import com.app_eventos.model.enums.*;
import com.app_eventos.model.interfaces.IEventoConCupo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Concierto con cupo. Lista de participantes solo para pruebas en memoria. */
@Entity @Table(name = "concierto")
public class Concierto extends Evento implements IEventoConCupo {

    @Enumerated(EnumType.STRING) @Column(name = "tipoEntrada")
    private TipoEntrada tipoEntrada;

    @Column(name = "cupoMaximo")
    private int cupoMaximo;

    @Transient
    private final List<Persona> participantes = new ArrayList<>();

    public Concierto() { 
        super(); setTipoEvento(TipoEvento.CONCIERTO); 
    }

    public Concierto(String nombre, LocalDateTime fi, LocalDateTime ff, TipoEntrada tipoEntrada, int cupoMaximo) {
        super(nombre, fi, ff, TipoEvento.CONCIERTO);
        setTipoEntrada(tipoEntrada); setCupoMaximo(cupoMaximo);
    }

    // ===== inscripción en memoria (persistencia real la hace Servicio+Repositorio) =====
    @Override public void inscribirParticipante(Persona persona) {
        validarPuedeInscribir();
        if (participantes.size() >= cupoMaximo) throw new IllegalStateException("Cupo completo.");
        if (participantes.contains(persona)) throw new IllegalArgumentException("Ya inscrito.");
        participantes.add(persona);
    }
    @Override public void desinscribirParticipante(Persona persona) { participantes.remove(persona); }
    @Override public List<Persona> getParticipantes() { return new ArrayList<>(participantes); }

    // ===== cupo =====
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
    public void setCupoMaximo(int v) {
        if (v <= 0) throw new IllegalArgumentException("Cupo > 0");
        this.cupoMaximo = v;
    }

    @Override 
    public int getCupoDisponible() { 
        return cupoMaximo - participantes.size(); 
    }

    @Override 
    protected boolean rolPermitido(TipoRol rol) { 
        return rol == TipoRol.ORGANIZADOR || rol == TipoRol.ARTISTA; 
    }

    public TipoEntrada getTipoEntrada() { 
        return tipoEntrada; 
    }
    public void setTipoEntrada(TipoEntrada t) { 
        if (t == null) throw new IllegalArgumentException("Tipo entrada"); 
        this.tipoEntrada = t; 
    }
    // nuevo helper que suelen usar para poblar listas
    public java.util.List<Persona> getArtistas() {
        return obtenerResponsables(TipoRol.ARTISTA);
    }
}
