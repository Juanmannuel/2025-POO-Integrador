package com.app_eventos.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

public class ABMPersonaController {

    // Referencias mínimas a los elementos del FXML
    @FXML private TableView<?> tablaPersonas;
    @FXML private ComboBox<?> comboTipoRolFiltro;
    @FXML private TextField txtNombreFiltro;
    @FXML private TextField txtDNIFiltro;
    @FXML private Pagination paginador;

    @FXML private StackPane modalOverlay;
    @FXML private TextField txtDNI;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtEmail;
    @FXML private ComboBox<?> comboRol;

    // Métodos vacíos que no hagan nada aún
    @FXML
    public void initialize() {
        // Inicialización vacía por ahora
    }
    
    @FXML
    private void mostrarModal() {
        modalOverlay.setVisible(true);
    }

    @FXML
    private void cerrarModal() {
        modalOverlay.setVisible(false);
    }

    @FXML
    private void guardarPersona() {
        // Lógica se implementará más adelante
    }


}