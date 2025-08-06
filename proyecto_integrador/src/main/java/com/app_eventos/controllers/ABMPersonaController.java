package com.app_eventos.controllers;

import com.app_eventos.model.enums.TipoRol;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.collections.FXCollections;

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

    // Modal modificación
    @FXML private StackPane modalModificarOverlay;

    @FXML
    public void initialize() {
        // Inicializar combos
        comboTipoRolFiltro.setItems(FXCollections.observableArrayList(TipoRol.values()));
        comboRol.setItems(FXCollections.observableArrayList(TipoRol.values()));
        modalOverlay.setVisible(false);  // Ocultar modal por defecto
    }

    private void limpiarCampos() {
        txtDNI.clear();
        txtNombre.clear();
        txtApellido.clear();
        txtTelefono.clear();
        txtEmail.clear();
        comboRol.getSelectionModel().clearSelection();
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
    private void altaPersona() {
        // Acá se puede validar y guardar los datos
        cerrarModal(); // cerrar modal después de guardar
    }

    @FXML
    private void modificarPersona() {
        Object personaSeleccionada = tablaPersonas.getSelectionModel().getSelectedItem();

        if (personaSeleccionada != null) {
            // Acá se cargan los datos de la persona seleccionada
        } else {
        // mostrar alerta si no hay selección
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle("Selección requerida");
        alerta.setHeaderText(null);
        alerta.setContentText("Debe seleccionar una persona en la tabla para modificar.");
        alerta.showAndWait();
        }
    }

    @FXML
    private void bajaPersona() {

    }
}