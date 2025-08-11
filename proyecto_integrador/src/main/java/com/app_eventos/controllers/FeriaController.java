package com.app_eventos.controllers;

import com.app_eventos.model.enums.TipoAmbiente;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

public class FeriaController {

    @FXML
    private Spinner<Integer> spinnerCantidadStands;

    @FXML
    private ComboBox<TipoAmbiente> comboAmbiente;

    @FXML
    public void initialize() {
        spinnerCantidadStands.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 500, 1));
        comboAmbiente.getItems().setAll(TipoAmbiente.values());
    }

    public int getCantidadStands() { return spinnerCantidadStands.getValue(); }

    public TipoAmbiente getAmbienteSeleccionado() { return comboAmbiente.getValue(); }
    
    public void setValores(int cant, TipoAmbiente amb) {
        spinnerCantidadStands.getValueFactory().setValue(cant);
        comboAmbiente.getSelectionModel().select(amb);
    }
}