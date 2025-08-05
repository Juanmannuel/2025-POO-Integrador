package com.app_eventos.controllers;

import com.app_eventos.model.enums.TipoEntrada;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

public class ConciertoController {

    @FXML private ComboBox<TipoEntrada> comboTipoEntradaConcierto;
    @FXML private Spinner<Integer> spinnerCupoMaximo;

    @FXML
    public void initialize() {
        comboTipoEntradaConcierto.getItems().setAll(TipoEntrada.values());
        comboTipoEntradaConcierto.setPromptText("Seleccione una opción");

        spinnerCupoMaximo.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 5000, 0)
        );
        spinnerCupoMaximo.setEditable(false);
    }

    public TipoEntrada getTipoEntradaSeleccionado() {
        return comboTipoEntradaConcierto.getValue();
    }

    public int getCupoMaximo() {
        return spinnerCupoMaximo.getValue();
    }
}
