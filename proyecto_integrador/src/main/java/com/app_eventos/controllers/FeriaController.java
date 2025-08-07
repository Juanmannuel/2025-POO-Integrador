package com.app_eventos.controllers;

import com.app_eventos.model.enums.TipoAmbiente;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

public class FeriaController {

    @FXML private Spinner<Integer> spinnerCantidadStands;
    @FXML private ComboBox<TipoAmbiente> comboAmbiente;

    @FXML
    public void initialize() {
        // Spinner configuración
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 500, 1);
        spinnerCantidadStands.setValueFactory(valueFactory);
        spinnerCantidadStands.setEditable(false);

        // ComboBox de ambiente
        comboAmbiente.getItems().setAll(TipoAmbiente.values());
        comboAmbiente.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(TipoAmbiente ambiente) {
                return ambiente != null ? ambiente.name().charAt(0) + ambiente.name().substring(1).toLowerCase().replace("_", " ") : "";
            }

            @Override
            public TipoAmbiente fromString(String s) {
                return TipoAmbiente.valueOf(s.toUpperCase().replace(" ", "_"));
            }
        });
    }

    public int getCantidadStands() {
        return spinnerCantidadStands.getValue();
    }

    public TipoAmbiente getAmbienteSeleccionado() {
        return comboAmbiente.getValue();
    }
}
