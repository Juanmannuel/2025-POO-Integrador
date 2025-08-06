package com.app_eventos.utils;

import com.app_eventos.controllers.*;
import com.app_eventos.model.*;
import com.app_eventos.model.enums.TipoEvento;

public class DatosEspecificosHelper {
    
    public static void capturarDatosEspecificos(Evento evento, Object controllerFragmento) {
        if (controllerFragmento == null) return;
        
        TipoEvento tipo = evento.getTipoEvento();
        
        try {
            switch (tipo) {
                case CONCIERTO -> {
                    ConciertoController controller = (ConciertoController) controllerFragmento;
                    Concierto concierto = (Concierto) evento;
                    concierto.setCupoMaximo(controller.getCupoMaximo());
                    concierto.setTipoEntrada(controller.getTipoEntradaSeleccionado());
                    concierto.setArtistasTexto(controller.getArtistas());
                }
                case TALLER -> {
                    TallerController controller = (TallerController) controllerFragmento;
                    Taller taller = (Taller) evento;
                    taller.setCupoMaximo(controller.getCupoMaximo());
                }
                case CICLO_CINE -> {
                    CicloCineController controller = (CicloCineController) controllerFragmento;
                    CicloCine cicloCine = (CicloCine) evento;
                    cicloCine.setCupoMaximo(controller.getCupoMaximo());
                    cicloCine.setPostCharla(controller.tieneCharlasPosterior());
                    // TODO: Capturar películas cuando el modelo lo soporte
                    // cicloCine.setPeliculas(controller.getPeliculas());
                }
                case EXPOSICION -> {
                    ExposicionController controller = (ExposicionController) controllerFragmento;
                    Exposicion exposicion = (Exposicion) evento;
                    exposicion.setTipoArte(controller.getTipoArteSeleccionado());
                }
                case FERIA -> {
                    FeriaController controller = (FeriaController) controllerFragmento;
                    Feria feria = (Feria) evento;
                    feria.setCantidadStands(controller.getCantidadStands());
                    feria.setTipoAmbiente(controller.esAireLibre() ? 
                        com.app_eventos.model.enums.TipoAmbiente.AIRE_LIBRE : 
                        com.app_eventos.model.enums.TipoAmbiente.TECHADO);
                }
            }
        } catch (ClassCastException e) {
            System.err.println("Error al capturar datos específicos: " + e.getMessage());
        }
    }
    
    public static void cargarDatosEspecificos(Evento evento, Object controllerFragmento) {
        if (controllerFragmento == null) return;
        
        try {
            switch (evento.getTipoEvento()) {
                case CONCIERTO -> {
                    ConciertoController controller = (ConciertoController) controllerFragmento;
                    Concierto concierto = (Concierto) evento;
                    
                    if (concierto.getCupoMaximo() > 0) {
                        controller.setCupoMaximo(concierto.getCupoMaximo());
                    }
                    if (concierto.getTipoEntrada() != null) {
                        controller.setTipoEntrada(concierto.getTipoEntrada());
                    }
                    // Cargar artistas
                    if (concierto.getArtistasTexto() != null) {
                        controller.setArtistas(concierto.getArtistasTexto());
                    }
                }
                case TALLER -> {
                    TallerController controller = (TallerController) controllerFragmento;
                    Taller taller = (Taller) evento;
                    
                    if (taller.getCupoMaximo() > 0) {
                        controller.setCupoMaximo(taller.getCupoMaximo());
                    }
                }
                case CICLO_CINE -> {
                    CicloCineController controller = (CicloCineController) controllerFragmento;
                    CicloCine cicloCine = (CicloCine) evento;
                    
                    if (cicloCine.getCupoMaximo() > 0) {
                        controller.setCupoMaximo(cicloCine.getCupoMaximo());
                    }
                    controller.setCharlasPosterior(cicloCine.isPostCharla());
                    // TODO: Cargar películas cuando el modelo lo soporte
                    // if (cicloCine.getPeliculas() != null) {
                    //     controller.setPeliculas(cicloCine.getPeliculas());
                    // }
                }
                case EXPOSICION -> {
                    ExposicionController controller = (ExposicionController) controllerFragmento;
                    Exposicion exposicion = (Exposicion) evento;
                    
                    if (exposicion.getTipoArte() != null) {
                        controller.setTipoArte(exposicion.getTipoArte());
                    }
                }
                case FERIA -> {
                    FeriaController controller = (FeriaController) controllerFragmento;
                    Feria feria = (Feria) evento;
                    
                    if (feria.getCantidadStands() > 0) {
                        controller.setCantidadStands(feria.getCantidadStands());
                    }
                    if (feria.getTipoAmbiente() != null) {
                        controller.setAireLibre(feria.getTipoAmbiente() == 
                            com.app_eventos.model.enums.TipoAmbiente.AIRE_LIBRE);
                    }
                }
            }
        } catch (ClassCastException e) {
            System.err.println("Error al cargar datos específicos: " + e.getMessage());
        }
    }
}