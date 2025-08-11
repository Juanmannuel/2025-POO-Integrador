package com.app_eventos.controllers;

import com.app_eventos.model.enums.TipoEntrada;
import com.app_eventos.services.Servicio;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

public class ConciertoController {

    @FXML
    private Spinner<Integer> spinnerCupoMaximo;

    private final Servicio servicio = Servicio.getInstance();
    
    @FXML
    private ComboBox<TipoEntrada> comboTipoEntradaConcierto;

    @FXML
    public void initialize() {
        // Cupo máximo entre 1 y 100000, valor inicial 1
        spinnerCupoMaximo.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 5000, 1));

        // Cargar enum TipoEntrada
        comboTipoEntradaConcierto.getItems().setAll(TipoEntrada.values());
    }

    public int getCupoMaximo() { return spinnerCupoMaximo.getValue(); }

    public TipoEntrada getTipoEntradaSeleccionada() { return comboTipoEntradaConcierto.getValue(); }
    
    public void setValores(TipoEntrada tipo, int cupo) {
        comboTipoEntradaConcierto.getSelectionModel().select(tipo);
        spinnerCupoMaximo.getValueFactory().setValue(cupo);
    }
}