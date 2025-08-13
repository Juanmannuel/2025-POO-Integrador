package com.app_eventos.controllers;

import com.app_eventos.model.Persona;
import com.app_eventos.model.enums.Modalidad;
import com.app_eventos.model.enums.TipoRol;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextFormatter;

public class TallerController {

    @FXML
    private Spinner<Integer> spinnerCupoMaximo;

    @FXML
    private ComboBox<Modalidad> comboModalidad;

    @FXML
    public void initialize() {
        // Configuración del spinner de cupo máximo
        spinnerCupoMaximo.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1000, 0));
        spinnerCupoMaximo.setEditable(true); // permitir edición

        // Filtrar solo números
        TextFormatter<Integer> formatter = new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*")) {
                return change; // acepta solo dígitos
            }
            return null; // ignora letras
        });

        spinnerCupoMaximo.getEditor().setTextFormatter(formatter);

        // Modalidad enum
        comboModalidad.getItems().setAll(Modalidad.values());
    }
    
    public int getCupoMaximo() { return spinnerCupoMaximo.getValue(); }

    public Modalidad getModalidadSeleccionada() { return comboModalidad.getValue(); }

    public void setValores(int cupo, Modalidad mod) {
        spinnerCupoMaximo.getValueFactory().setValue(cupo);
        comboModalidad.getSelectionModel().select(mod);
    }
}