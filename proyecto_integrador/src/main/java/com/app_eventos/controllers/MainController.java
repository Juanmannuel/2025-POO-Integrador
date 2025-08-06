package com.app_eventos.controllers;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class MainController {

    @FXML
    private StackPane contenidoCentral;
    @FXML
    private Label fechaLabel;
    @FXML
    private Button btnInicio;
    @FXML
    private Button btnEventos;
    @FXML
    private Button btnParticipantes;
    @FXML
    private Button btnPersonas;

    @FXML
    public void initialize() {
        System.out.println("DEBUG: MainController.initialize() - INICIANDO.");
        
        LocalDate hoy = LocalDate.now();
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy");
        fechaLabel.setText("Hoy: " + hoy.format(formato));

        System.out.println("DEBUG: MainController.initialize() - Llamando a mostrarInicio().");
        mostrarInicio();
    }

    @FXML
    private void mostrarInicio() {
        System.out.println("DEBUG: Método mostrarInicio() EJECUTADO.");

        if (contenidoCentral == null) {
            System.out.println("ERROR FATAL: El panel 'contenidoCentral' es NULO.");
            return;
        }
        try {
            String ruta = "/fxml/inicio.fxml";
            System.out.println("DEBUG: Intentando encontrar el archivo en la ruta: " + ruta);
            URL fxmlUrl = getClass().getResource(ruta);

            if (fxmlUrl == null) {
                System.out.println("************************************************************");
                System.out.println("ERROR GRAVE: No se encontró el archivo inicio.fxml.");
                System.out.println("************************************************************");
                return;
            }
            System.out.println("DEBUG: ¡Archivo encontrado!");

            Parent vista = FXMLLoader.load(fxmlUrl);
            System.out.println("DEBUG: El archivo FXML fue cargado por FXMLLoader.");

            contenidoCentral.getChildren().setAll(vista);
            System.out.println("DEBUG: ¡La vista fue añadida al panel central!");

        } catch (Exception e) {
            System.out.println("************************************************************");
            System.out.println("ERROR FATAL: Ocurrió una excepción al cargar el FXML de inicio.");
            e.printStackTrace();
            System.out.println("************************************************************");
        }
    }
    
    // --- MÉTODOS RESTAURADOS ---
    @FXML
    private void mostrarEventos() {
        try {
            Parent vista = FXMLLoader.load(getClass().getResource("/fxml/abm/abmEvento.fxml"));
            contenidoCentral.getChildren().setAll(vista);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void mostrarPersonas() {
        try {
            Parent vista = FXMLLoader.load(getClass().getResource("/fxml/abm/abmPersona.fxml"));
            contenidoCentral.getChildren().setAll(vista);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void mostrarParticipantes() {
        try {
            Parent vista = FXMLLoader.load(getClass().getResource("/fxml/abm/abmParticipante.fxml"));
            contenidoCentral.getChildren().setAll(vista);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}