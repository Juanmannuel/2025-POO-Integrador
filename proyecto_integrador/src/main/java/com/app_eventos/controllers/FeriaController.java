package com.app_eventos.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.ToggleGroup;

public class FeriaController {

    @FXML private Spinner<Integer> spinnerCantidadStands;
    @FXML private RadioButton radioAireLibre;
    @FXML private RadioButton radioTechada;

    @FXML
    public void initialize() {
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 500, 1);
        spinnerCantidadStands.setValueFactory(valueFactory);
        spinnerCantidadStands.setEditable(false); // Deshabilita la edición directa

        ToggleGroup grupoAmbiente = new ToggleGroup();
        radioAireLibre.setToggleGroup(grupoAmbiente);
        radioTechada.setToggleGroup(grupoAmbiente);

        // Opcional: seleccionar uno por defecto
        radioAireLibre.setSelected(true);
    }

    // public int getCantidadStands() {
    //     return spinnerCantidadStands.getValue();
    // }

    // public String getAmbienteSeleccionado() {
    //     if (radioAireLibre.isSelected()) return "AIRE_LIBRE";
    //     if (radioTechada.isSelected()) return "TECHADO";
    //     return null;
    // }
}
