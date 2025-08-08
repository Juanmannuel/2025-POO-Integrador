package com.app_eventos.controllers;

import com.app_eventos.model.Evento;
import com.app_eventos.model.Persona;
import com.app_eventos.model.RolEvento;
import com.app_eventos.model.enums.TipoRol;
import com.app_eventos.services.Servicio;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;

public class AsigRolEventoController {

    @FXML private ComboBox<Persona> comboPersona;
    @FXML private ComboBox<TipoRol> comboTipoRol;
    @FXML private TableView<RolEvento> tablaRoles;
    @FXML private TableColumn<RolEvento, String> colPersona;
    @FXML private TableColumn<RolEvento, TipoRol> colRol;
    @FXML private TableColumn<RolEvento, String> colDni;
    @FXML private TableColumn<RolEvento, String> colNombre;

    private final ObservableList<RolEvento> rolesEvento = FXCollections.observableArrayList();
    private final Servicio servicio = new Servicio();
    private Evento evento;

    @FXML
    public void initialize() {
        comboPersona.setItems(servicio.obtenerPersonas());
        comboTipoRol.getItems().setAll(TipoRol.values());

        colDni.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getPersona().getDni()));
        colNombre.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getPersona().getNombre() + " " +
                                    data.getValue().getPersona().getApellido()));
        colRol.setCellValueFactory(new PropertyValueFactory<>("rol"));

        tablaRoles.setItems(rolesEvento);
    }

    @FXML
    public void agregarRol() {
        Persona persona = comboPersona.getValue();
        TipoRol rol = comboTipoRol.getValue();

        if (persona == null || rol == null) {
            mostrarAlerta("Error", "Debe seleccionar persona y rol.");
            return;
        }

        RolEvento nuevo = new RolEvento(evento, persona, rol); // ✅ USAMOS el evento recibido
        if (!rolesEvento.contains(nuevo)) {
            rolesEvento.add(nuevo);
        }

        comboPersona.getSelectionModel().clearSelection();
        comboTipoRol.getSelectionModel().clearSelection();
    }

    @FXML
    public void eliminarRolSeleccionado() {
        RolEvento seleccionado = tablaRoles.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            rolesEvento.remove(seleccionado);
        } else {
            mostrarAlerta("Atención", "Debe seleccionar la persona con su rol para eliminarla.");
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    public ObservableList<RolEvento> getRolesAsignados() {
        return rolesEvento;
    }

        public void setEvento(Evento evento) {
        this.evento = evento;
    }

    
}