package com.app_eventos.model;

import jakarta.persistence.*;
import com.app_eventos.model.enums.*;
import java.time.LocalDateTime;

/** Feria con cantidad de stands y ambiente. */
@Entity @Table(name = "feria")
public class Feria extends Evento {

    @Column(name = "cantidadStands") private int cantidadStands;
    @Enumerated(EnumType.STRING) @Column(name = "ambiente") private TipoAmbiente ambiente;

    public Feria() { super(); setTipoEvento(TipoEvento.FERIA); }
    public Feria(String n, LocalDateTime fi, LocalDateTime ff, int stands, TipoAmbiente amb) {
        super(n, fi, ff, TipoEvento.FERIA); setCantidadStands(stands); setAmbiente(amb);
    }

    @Override protected boolean rolPermitido(TipoRol rol) { return rol == TipoRol.ORGANIZADOR; }

    public int getCantidadStands() { return cantidadStands; }
    public void setCantidadStands(int v) { if (v <= 0) throw new IllegalArgumentException("Stands > 0"); this.cantidadStands = v; }
    public TipoAmbiente getAmbiente() { return ambiente; }
    public void setAmbiente(TipoAmbiente a) { if (a == null) throw new IllegalArgumentException("Ambiente nulo"); this.ambiente = a; }
}
