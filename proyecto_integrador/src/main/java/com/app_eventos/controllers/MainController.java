package com.app_eventos.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MainController {

    @FXML private Label fechaLabel;
    @FXML private StackPane contenidoCentral;

    @FXML
    public void initialize() {
        // Mostrar fecha actual
        LocalDate hoy = LocalDate.now();
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy");
        String fechaFormateada = "Hoy: " + hoy.format(formato);
        fechaLabel.setText(fechaFormateada.substring(0, 1).toUpperCase() + fechaFormateada.substring(1));

        mostrarInicio();
    }

    private void cargarContenidoCentral(String rutaFxml) {
        try {
            URL url = getClass().getResource(rutaFxml);
            if (url == null) {
                throw new IllegalStateException("No se encontró el FXML en la ruta: " + rutaFxml);
            }
            FXMLLoader loader = new FXMLLoader(url);
            Parent vista = loader.load();
            contenidoCentral.getChildren().setAll(vista);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- HANDLERS DE BOTONES ---
    @FXML
    private void mostrarInicio() {
        cargarContenidoCentral("/fxml/inicio.fxml");

    }

    @FXML
    private void mostrarEventos() {
        cargarContenidoCentral("/fxml/abm/abmEvento.fxml");
    }

    @FXML
    private void mostrarPersonas() {
        cargarContenidoCentral("/fxml/abm/abmPersona.fxml");
    }

    @FXML
    private void mostrarParticipantes() {
        cargarContenidoCentral("/fxml/abm/abmParticipante.fxml");
    }

    @FXML
    private void mostrarPeliculas() {
        cargarContenidoCentral("/fxml/abm/abmPelicula.fxml");
    }
}
