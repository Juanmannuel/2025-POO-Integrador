package com.app_eventos.controllers;

import com.app_eventos.model.enums.Modalidad;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

public class TallerController {

    @FXML
    private TextField txtCupoMaximo;

    @FXML
    private ComboBox<String> comboInstructor;

    @FXML
    private ComboBox<Modalidad> comboModalidad;

    @FXML
    public void initialize() {
        // Configuración del TextField de cupo máximo - solo números
        TextFormatter<String> formatter = new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*")) {
                return change; // acepta solo dígitos
            }
            return null; // ignora letras
        });
        txtCupoMaximo.setTextFormatter(formatter);

        // Modalidad enum
        comboModalidad.getItems().setAll(Modalidad.values());

        // Combo instructor (vacío por ahora)
        comboInstructor.getItems().clear();
    }
    
    // Método para obtener el cupo máximo ingresado
    public int getCupoMaximo() {
        try {
            String texto = txtCupoMaximo.getText();
            return texto.isBlank() ? 0 : Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // Métodos setter para cargar datos en modo edición
    public void setCupoMaximo(int cupo) {
        txtCupoMaximo.setText(String.valueOf(cupo));
    }

    public void setInstructor(String instructor) {
        comboInstructor.setValue(instructor);
    }

    public void setModalidad(Modalidad modalidad) {
        comboModalidad.setValue(modalidad);
    }
}