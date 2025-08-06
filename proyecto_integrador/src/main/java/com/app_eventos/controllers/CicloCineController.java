package com.app_eventos.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class CicloCineController {

    @FXML
    private TextArea txtPeliculas;

    @FXML
    private RadioButton radioSi;

    @FXML
    private RadioButton radioNo;

    @FXML
    private TextField txtCupoMaximo;

    private ToggleGroup charlaGroup;

    @FXML
    public void initialize() {
        // Configurar TextField para aceptar solo números
        TextFormatter<String> formatter = new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*")) {
                return change;
            }
            return null;
        });
        txtCupoMaximo.setTextFormatter(formatter);

        // Agrupar los RadioButton
        charlaGroup = new ToggleGroup();
        radioSi.setToggleGroup(charlaGroup);
        radioNo.setToggleGroup(charlaGroup);

        // Seleccionar "No" por defecto (opcional)
        radioNo.setSelected(true);
    }

    public int getCupoMaximo() {
        String texto = txtCupoMaximo.getText();
        if (texto == null || texto.trim().isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // Métodos getter para obtener datos
    public boolean tieneCharlasPosterior() {
        return radioSi.isSelected();
    }

    public String getPeliculas() {
        return txtPeliculas.getText();
    }

    // Métodos setter para cargar datos en modo edición
    public void setCupoMaximo(int cupo) {
        txtCupoMaximo.setText(String.valueOf(cupo));
    }

    public void setCharlasPosterior(boolean tieneCharlas) {
        if (tieneCharlas) {
            radioSi.setSelected(true);
        } else {
            radioNo.setSelected(true);
        }
    }

    public void setPeliculas(String peliculas) {
        txtPeliculas.setText(peliculas);
    }
}
