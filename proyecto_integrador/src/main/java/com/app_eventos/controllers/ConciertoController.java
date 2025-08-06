package com.app_eventos.controllers;

import com.app_eventos.model.enums.TipoEntrada;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

public class ConciertoController {

    @FXML private ComboBox<TipoEntrada> comboTipoEntradaConcierto;
    @FXML private TextField txtCupoMaximo;
    @FXML private TextArea textAreaArtistas;

    @FXML
    public void initialize() {
        comboTipoEntradaConcierto.getItems().setAll(TipoEntrada.values());
        comboTipoEntradaConcierto.setPromptText("Seleccione una opción");

        // Configurar TextField para aceptar solo números
        TextFormatter<String> formatter = new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*")) {
                return change;
            }
            return null;
        });
        txtCupoMaximo.setTextFormatter(formatter);
    }

    public TipoEntrada getTipoEntradaSeleccionado() {
        return comboTipoEntradaConcierto.getValue();
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

    // Métodos setter para cargar datos en modo edición
    public void setCupoMaximo(int cupo) {
        txtCupoMaximo.setText(String.valueOf(cupo));
    }

    public void setTipoEntrada(TipoEntrada tipoEntrada) {
        comboTipoEntradaConcierto.setValue(tipoEntrada);
    }

    // Métodos para manejar artistas
    public String getArtistas() {
        return textAreaArtistas.getText();
    }

    public void setArtistas(String artistas) {
        textAreaArtistas.setText(artistas);
    }
}
