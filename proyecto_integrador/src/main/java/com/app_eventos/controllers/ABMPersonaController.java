package com.app_eventos.controllers;

import com.app_eventos.model.enums.TipoRol;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ABMPersonaController {

    // Filtros
    @FXML private ComboBox<TipoRol> comboTipoRolFiltro;
    @FXML private TextField txtNombreFiltro;
    @FXML private TextField txtDNIFiltro;

    // Tabla
    @FXML private TableView<?> tablaPersonas;
    @FXML private TableColumn<?, ?> colNombre;
    @FXML private TableColumn<?, ?> colDNI;
    @FXML private TableColumn<?, ?> colTelefono;
    @FXML private TableColumn<?, ?> colEmail;
    @FXML private TableColumn<?, ?> colRol;

    // Modal
    @FXML private StackPane modalOverlay;
    @FXML private TextField txtDNI;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtEmail;
    @FXML private ComboBox<TipoRol> comboRol;

    @FXML
    public void initialize() {
        // Inicializar combos
        comboTipoRolFiltro.setItems(FXCollections.observableArrayList(TipoRol.values()));
        comboRol.setItems(FXCollections.observableArrayList(TipoRol.values()));

        // Ocultar modal por defecto
        modalOverlay.setVisible(false);
    }

    @FXML
    private void mostrarModal() {
        limpiarCampos();
        modalOverlay.setVisible(true);
    }

    @FXML
    private void cerrarModal() {
        modalOverlay.setVisible(false);
    }

    @FXML
    private void guardarEvento() {
        // Acá podrías validar y guardar los datos
        System.out.println("Guardando persona:");
        System.out.println("Nombre: " + txtNombre.getText());
        System.out.println("DNI: " + txtDNI.getText());
        System.out.println("Rol: " + comboRol.getValue());

        cerrarModal(); // cerrar modal luego de guardar
    }

    private void limpiarCampos() {
        txtDNI.clear();
        txtNombre.clear();
        txtApellido.clear();
        txtTelefono.clear();
        txtEmail.clear();
        comboRol.getSelectionModel().clearSelection();
    }
}