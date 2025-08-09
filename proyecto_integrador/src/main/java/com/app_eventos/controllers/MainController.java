package com.app_eventos.controllers;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class MainController {

    // Contenedor central donde se cargan las vistas
    @FXML private StackPane contenidoCentral;

    // Botones (asegurate de conectarlos en el FXML si tienen onAction)
    @FXML private Button btnInicio;
    @FXML private Button btnEventos;
    @FXML private Button btnPersonas;
    @FXML private Button btnParticipantes;
    @FXML private Button btnPeliculas; // opcional si usás películas

    // Label para mostrar la fecha (debe existir en el FXML: fx:id="fechaLabel")
    @FXML private Label fechaLabel;

    @FXML
    public void initialize() {
        System.out.println("DEBUG: MainController.initialize() - INICIANDO.");

        // Fecha en español (Argentina)
        if (fechaLabel != null) {
            LocalDate hoy = LocalDate.now();
            DateTimeFormatter formato = DateTimeFormatter.ofPattern(
                    "EEEE, d 'de' MMMM 'de' yyyy",
                    new Locale("es", "AR")
            );
            fechaLabel.setText("Hoy: " + hoy.format(formato));
        } else {
            System.out.println("WARN: 'fechaLabel' es nulo. Verifica fx:id en el FXML.");
        }

        // Cargar la vista de inicio por defecto
        mostrarInicio();
    }

    // ======== Acciones de navegación ========

    @FXML
    private void mostrarInicio() {
        cargarEnCentro("/fxml/inicio.fxml");
    }

    @FXML
    private void mostrarEventos() {
        cargarEnCentro("/fxml/abm/abmEvento.fxml");
    }

    @FXML
    private void mostrarPersonas() {
        cargarEnCentro("/fxml/abm/abmPersona.fxml");
    }

    @FXML
    private void mostrarParticipantes() {
        cargarEnCentro("/fxml/abm/abmParticipante.fxml");
    }

    @FXML
    private void mostrarPeliculas() {
        // Usalo solo si tenés esta vista
        cargarEnCentro("/fxml/abm/abmPelicula.fxml");
    }

    // ======== Utilidad para cargar vistas ========

    private void cargarEnCentro(String ruta) {
        if (contenidoCentral == null) {
            System.out.println("ERROR: 'contenidoCentral' es nulo. Verifica fx:id en el FXML principal.");
            return;
        }
        try {
            System.out.println("DEBUG: Cargando FXML: " + ruta);
            URL fxmlUrl = getClass().getResource(ruta);
            if (fxmlUrl == null) {
                System.out.println("ERROR: No se encontró el FXML en " + ruta);
                return;
            }
            Parent vista = FXMLLoader.load(fxmlUrl);
            contenidoCentral.getChildren().setAll(vista);
            System.out.println("DEBUG: Vista cargada correctamente en el panel central.");
        } catch (IOException e) {
            System.out.println("ERROR: Falló la carga del FXML: " + ruta);
            e.printStackTrace();
        }
    }
}
