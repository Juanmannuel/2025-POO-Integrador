package com.app_eventos.controllers;

import java.io.IOException;
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
    private Label fechaLabel;

    @FXML
    private StackPane contenidoCentral;

    // --- VARIABLES DE BOTONES AÑADIDAS/CORREGIDAS ---
    @FXML
    private Button btnInicio; // Necesario para que el FXML se conecte
    @FXML
    private Button btnEventos;
    @FXML
    private Button btnPersonas;
    @FXML
    private Button btnParticipantes;
    @FXML
    private Button btnPeliculas; // Añadido para el botón "Películas"

    @FXML
    public void initialize() {
        System.out.println("DEBUG: MainController.initialize() - INICIANDO.");
        
        LocalDate hoy = LocalDate.now();
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy");
        fechaLabel.setText("Hoy: " + hoy.format(formato));

        System.out.println("DEBUG: MainController.initialize() - Llamando a mostrarInicio().");
        mostrarInicio();
    }


    /**
     * MÉTODO NUEVO AÑADIDO
     * Carga la vista inicio.fxml en el panel central. Es llamado por el botón "Inicio".
     */
    @FXML
    private void mostrarInicio() {
        try {
            Parent vista = FXMLLoader.load(getClass().getResource("/fxml/inicio.fxml"));
            contenidoCentral.getChildren().setAll(vista);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- MÉTODOS EXISTENTES (SIN CAMBIOS) ---
    @FXML
    private void mostrarEventos() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/abm/abmEvento.fxml"));
            Parent vistaEventos = loader.load();
            contenidoCentral.getChildren().setAll(vistaEventos);
            
            ABMEventoController eventoController = loader.getController();
            if (eventoController != null) {
                eventoController.refrescarDatos();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void mostrarPersonas() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/abm/abmPersona.fxml"));
            Parent vistaPersonas = loader.load();
            contenidoCentral.getChildren().setAll(vistaPersonas);
            
            ABMPersonaController personaController = loader.getController();
            if (personaController != null) {
                personaController.refrescarDatos();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void mostrarParticipantes() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/abm/abmParticipante.fxml"));
            Parent vistaParticipantes = loader.load();
            contenidoCentral.getChildren().setAll(vistaParticipantes);
            
            ABMParticipanteController participanteController = loader.getController();
            if (participanteController != null) {
                participanteController.refrescarDatos();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void mostrarPeliculas() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/abm/abmPelicula.fxml"));
            Parent vista = loader.load();
            contenidoCentral.getChildren().setAll(vista);

            com.app_eventos.controllers.ABMPeliculaController c = loader.getController();
            if (c != null) c.refrescarDatos();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}