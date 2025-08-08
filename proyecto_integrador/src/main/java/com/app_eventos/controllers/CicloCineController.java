package com.app_eventos.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;

public class CicloCineController {

    @FXML
    private TextArea txtPeliculas;

    @FXML
    private RadioButton radioSi;

    @FXML
    private RadioButton radioNo;

    @FXML
    private Spinner<Integer> spinnerCupoMaximo;

    private ToggleGroup charlaGroup;

    @FXML
    public void initialize() {
        // Configurar el Spinner de cupo máximo (editable solo numérico)
        IntegerSpinnerValueFactory valueFactory = new IntegerSpinnerValueFactory(0, 1000, 0);
        spinnerCupoMaximo.setValueFactory(valueFactory);
        spinnerCupoMaximo.setEditable(true);

        // Restringir entrada a solo números en modo editable
        spinnerCupoMaximo.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                spinnerCupoMaximo.getEditor().setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        // Agrupar los RadioButton
        charlaGroup = new ToggleGroup();
        radioSi.setToggleGroup(charlaGroup);
        radioNo.setToggleGroup(charlaGroup);

        // Seleccionar "No" por defecto (opcional)
        radioNo.setSelected(true);
    }
    public int getCupoMaximo() {
        return spinnerCupoMaximo.getValue();
    }

    public boolean isPostCharla() {
        return radioSi.isSelected();
    }

    // Por ahora, el TextArea devuelve solo texto separado por saltos de línea
    public String getPeliculasTexto() {
        return txtPeliculas.getText();
    }
}
