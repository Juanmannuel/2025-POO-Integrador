package com.app_eventos.controllers;

import com.app_eventos.model.enums.TipoEntrada;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

public class ConciertoController {

    @FXML
    private Spinner<Integer> spinnerCupoMaximo;
    
    @FXML
    private ComboBox<TipoEntrada> comboTipoEntradaConcierto;

    @FXML
    public void initialize() {
        // Cupo máximo entre 0 y 100000
        spinnerCupoMaximo.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 5000, 0));
        spinnerCupoMaximo.setEditable(true);
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