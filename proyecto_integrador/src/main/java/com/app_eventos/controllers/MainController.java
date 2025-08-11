package com.app_eventos.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class MainController {

    @FXML private Label fechaLabel;
    @FXML private StackPane contenidoCentral;

    @FXML private Button btnInicio;
    @FXML private Button btnEventos;
    @FXML private Button btnPersona;
    @FXML private Button btnParticipantes;
    @FXML private Button btnPeliculas;

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

    private void setActivo(Button activo) {
        List<Button> todos = Arrays.asList(btnInicio, btnEventos, btnPersona, btnParticipantes, btnPeliculas);
        for (Button b : todos) {
            b.getStyleClass().remove("selected");
        }
        if (!activo.getStyleClass().contains("selected")) {
            activo.getStyleClass().add("selected");
        }
    }

    // --- HANDLERS DE BOTONES ---
    @FXML
    private void mostrarInicio() {
        cargarContenidoCentral("/fxml/inicio.fxml");
        setActivo(btnInicio);
    }

    @FXML
    private void mostrarEventos() {
        cargarContenidoCentral("/fxml/abm/abmEvento.fxml");
        setActivo(btnEventos);
    }

    @FXML
    private void mostrarPersonas() {
        cargarContenidoCentral("/fxml/abm/abmPersona.fxml");
        setActivo(btnPersona);
    }

    @FXML
    private void mostrarParticipantes() {
        cargarContenidoCentral("/fxml/abm/abmParticipante.fxml");
        setActivo(btnParticipantes);
    }

    @FXML
    private void mostrarPeliculas() {
        cargarContenidoCentral("/fxml/abm/abmPelicula.fxml");
        setActivo(btnPeliculas);
    }
}
