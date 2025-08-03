package com.app_eventos.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
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
    private Button btnPersonas;

    @FXML
    private Button btnParticipantes;

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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/abm/abmEvento.fxml"));
            Parent vistaEventos = loader.load();

            // Aplicar el CSS específico de la vista de eventos
            vistaEventos.getStylesheets().add(getClass().getResource("/styles/abmEvento.css").toExternalForm());

            contenidoCentral.getChildren().setAll(vistaEventos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void mostrarPersonas() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/abm/abmPersona.fxml"));
            Parent vistaPersonas = loader.load();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void mostrarParticipantes() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/abm/abmParticipante.fxml"));
            Parent vistaParticipantes = loader.load();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
