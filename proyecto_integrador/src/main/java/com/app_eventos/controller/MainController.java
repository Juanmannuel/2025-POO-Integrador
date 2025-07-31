package com.app_eventos.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MainController {

    @FXML
    private Label fechaLabel;

    @FXML
    private StackPane contenidoCentral;

    @FXML
    private Button btnEventos;

    @FXML
    public void initialize() {
        // Mostrar fecha actual
        LocalDate hoy = LocalDate.now();
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy");
        String fechaFormateada = "Hoy: " + hoy.format(formato);
        fechaLabel.setText(fechaFormateada.substring(0, 1).toUpperCase() + fechaFormateada.substring(1));
    }

    @FXML
    private void mostrarEventos() {
        try {
            Node vistaEventos = FXMLLoader.load(getClass().getResource("/fxml/abmEvento.fxml"));
            contenidoCentral.getChildren().setAll(vistaEventos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
