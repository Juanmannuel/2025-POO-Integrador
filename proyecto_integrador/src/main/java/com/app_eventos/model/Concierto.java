package com.app_eventos.model;

import jakarta.persistence.*;
import com.app_eventos.model.enums.*;
import com.app_eventos.model.interfaces.IEventoConCupo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "concierto")
public class Concierto extends Evento implements IEventoConCupo {

    @Enumerated(EnumType.STRING)
    @Column(name = "tipoEntrada", nullable = false)
    private TipoEntrada tipoEntrada;

    @Column(name = "cupoMaximo", nullable = false)
    private int cupoMaximo;

    @ManyToMany
    @JoinTable(
        name = "concierto_participante",
        joinColumns = @JoinColumn(name = "evento_id"),
        inverseJoinColumns = @JoinColumn(name = "persona_id")
    )
    private List<Persona> participantes = new ArrayList<>();

    public Concierto() {
        super();
        setTipoEvento(TipoEvento.CONCIERTO);
    }

    public Concierto(String nombre, LocalDateTime fi, LocalDateTime ff, TipoEntrada tipoEntrada, int cupoMaximo) {
        super(nombre, fi, ff, TipoEvento.CONCIERTO);
        setTipoEntrada(tipoEntrada);
        setCupoMaximo(cupoMaximo);
    }

    // Participantes
    @Override
    public void inscribirParticipante(Persona persona) {
        validarPuedeInscribir();

        if (personaTieneRol(persona))
            throw new IllegalStateException("No puede ser participante y responsable a la vez.");

        if (getCupoDisponible() <= 0)
            throw new IllegalStateException("Cupo completo.");

        if (participantes.contains(persona))
            throw new IllegalArgumentException("La persona ya está inscripta.");

        participantes.add(persona);
    }

    @Override public void desinscribirParticipante(Persona persona) { participantes.remove(persona); }
    @Override public List<Persona> getParticipantes() { return participantes; }

    // Cupo 
    @Override public int getCupoMaximo() { return cupoMaximo; }

    @Override
    public void setCupoMaximo(int v) {
        if (v <= 0) throw new IllegalArgumentException("El cupo debe ser mayor a 0.");
        this.cupoMaximo = v;
    }

    @Override
    public int getCupoDisponible() {
        int disp = cupoMaximo - participantes.size();
        return Math.max(0, disp);
    }

    // Roles permitidos 
    @Override
    protected boolean rolPermitido(TipoRol rol) {
        return rol == TipoRol.ORGANIZADOR || rol == TipoRol.ARTISTA;
    }

    // Validación de restricciones de rol
    @Override
    protected void validarRestriccionesRol(TipoRol rol, Persona persona) {
        if (participantes.contains(persona))
            throw new IllegalStateException("Ya es participante; no puede ser responsable.");
    }

    // Props
    public TipoEntrada getTipoEntrada() { return tipoEntrada; }
    public void setTipoEntrada(TipoEntrada t) {
        if (t == null) throw new IllegalArgumentException("Por favor, seleccione un tipo de entrada.");
        this.tipoEntrada = t;
    }
}
