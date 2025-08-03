package com.app_eventos.controllers;

import com.app_eventos.model.Persona;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class PersonaController {

    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtApellido;
    @FXML
    private TextField txtDni;
    @FXML
    private TextField txtTelefono;
    @FXML
    private TextField txtEmail;

    @FXML
    private TableView<Persona> tablaPersonas;
    @FXML
    private TableColumn<Persona, String> colNombre;
    @FXML
    private TableColumn<Persona, String> colApellido;
    @FXML
    private TableColumn<Persona, String> colDni;

    private ObservableList<Persona> listaPersonas = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Configuramos columnas
        colNombre.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNombre()));
        colApellido.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getApellido()));
        colDni.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDni()));

        // Seteamos lista vacía a la tabla
        tablaPersonas.setItems(listaPersonas);
    }

    @FXML
    private void agregarPersona() {
    try {
        // Creamos una nueva Persona con los datos ingresados (sin ID)
        Persona persona = new Persona(
            txtNombre.getText(),
            txtApellido.getText(),
            txtDni.getText(),
            txtTelefono.getText(),
            txtEmail.getText()
        );

        listaPersonas.add(persona);  // Agregamos a la lista (y por ende a la tabla)
        limpiarCampos();

    } catch (IllegalArgumentException e) {
        mostrarAlerta("Error de validación", e.getMessage());
    }
}

    @FXML
    private void eliminarPersona() {
        Persona seleccionada = tablaPersonas.getSelectionModel().getSelectedItem();
        if (seleccionada != null) {
            listaPersonas.remove(seleccionada);
        }
    }

    @FXML
    private void limpiarCampos() {
        txtNombre.clear();
        txtApellido.clear();
        txtDni.clear();
        txtTelefono.clear();
        txtEmail.clear();
    }

    @FXML
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
    @FXML
    private void modificarPersona() {
        Persona seleccionada = tablaPersonas.getSelectionModel().getSelectedItem();
        if (seleccionada != null) {
            seleccionada.setNombre(txtNombre.getText());
            seleccionada.setApellido(txtApellido.getText());
            seleccionada.setDni(txtDni.getText());
            seleccionada.setTelefono(txtTelefono.getText());
            seleccionada.setEmail(txtEmail.getText());

            // Refrescar la tabla
            tablaPersonas.refresh();
            limpiarCampos();
        } else {
            mostrarAlerta("Sin selección", "Seleccioná una persona de la tabla para modificar.");
        }
    }
}
