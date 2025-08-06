package com.app_eventos.utils;

import com.app_eventos.controllers.*;
import com.app_eventos.model.enums.TipoEvento;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

import java.time.LocalDate;

public class ValidacionEventoHelper {
    
    public static String validarCamposBasicos(TextField txtNombre, ComboBox<TipoEvento> comboTipo,
                                            DatePicker dateInicio, DatePicker dateFin, 
                                            ComboBox<?> comboEstado) {
        if (txtNombre.getText().isBlank()) {
            return "El nombre del evento es obligatorio.";
        }
        if (comboTipo.getValue() == null) {
            return "Debe seleccionar un tipo de evento.";
        }
        if (dateInicio.getValue() == null) {
            return "La fecha de inicio es obligatoria.";
        }
        if (dateFin.getValue() == null) {
            return "La fecha de fin es obligatoria.";
        }
        if (comboEstado.getValue() == null) {
            return "Debe seleccionar un estado.";
        }
        return null;
    }
    
    public static String validarLogicaFechas(DatePicker dateInicio, DatePicker dateFin, boolean esNuevo) {
        if (dateFin.getValue().isBefore(dateInicio.getValue())) {
            return "La fecha de fin no puede ser anterior a la fecha de inicio.";
        }
        
        if (esNuevo && dateInicio.getValue().isBefore(LocalDate.now())) {
            return "La fecha de inicio no puede ser anterior a hoy para eventos nuevos.";
        }
        
        return null;
    }
    
    public static String validarDatosEspecificos(Object controllerFragmento, TipoEvento tipo) {
        if (controllerFragmento == null || tipo == null) {
            return null;
        }

        try {
            switch (tipo) {
                case CONCIERTO -> {
                    ConciertoController controller = (ConciertoController) controllerFragmento;
                    if (controller.getCupoMaximo() <= 0) {
                        return "Debe ingresar un cupo máximo mayor a 0 para el concierto.";
                    }
                    if (controller.getTipoEntradaSeleccionado() == null) {
                        return "Debe seleccionar el tipo de entrada (gratuita o paga) para el concierto.";
                    }
                }
                case TALLER -> {
                    TallerController controller = (TallerController) controllerFragmento;
                    if (controller.getCupoMaximo() <= 0) {
                        return "Debe ingresar un cupo máximo mayor a 0 para el taller.";
                    }
                }
                case CICLO_CINE -> {
                    CicloCineController controller = (CicloCineController) controllerFragmento;
                    if (controller.getCupoMaximo() <= 0) {
                        return "Debe ingresar un cupo máximo mayor a 0 para el ciclo de cine.";
                    }
                }
                case EXPOSICION -> {
                    ExposicionController controller = (ExposicionController) controllerFragmento;
                    if (controller.getTipoArteSeleccionado() == null) {
                        return "Debe seleccionar el tipo de arte para la exposición.";
                    }
                }
                case FERIA -> {
                    FeriaController controller = (FeriaController) controllerFragmento;
                    if (controller.getCantidadStands() <= 0) {
                        return "Debe ingresar una cantidad de stands mayor a 0 para la feria.";
                    }
                }
            }
        } catch (ClassCastException e) {
            return "Error al validar datos específicos del evento.";
        }

        return null;
    }
}