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
            contenidoCentral.getChildren().setAll(vistaEventos);
            
            // Refrescar datos en la ventana de eventos
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
            
            // Refrescar datos en la ventana de personas
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
            
            // Refrescar datos en la ventana de participantes
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
            e.printStackTrace(); // mira la consola si algo truena al cargar el FXML
        }
    }

}
