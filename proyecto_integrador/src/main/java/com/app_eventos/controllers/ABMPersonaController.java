package com.app_eventos.controllers;

import com.app_eventos.model.Persona;
import com.app_eventos.services.PersonaService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

import java.util.Optional;

public class ABMPersonaController {

    // Filtros de búsqueda
    @FXML private TextField txtNombreFiltro;
    @FXML private TextField txtDNIFiltro;

    // Tabla y columnas
    @FXML private TableView<Persona> tablaPersonas;
    @FXML private TableColumn<Persona, String> colNombre;
    @FXML private TableColumn<Persona, String> colApellido;
    @FXML private TableColumn<Persona, String> colDNI;
    @FXML private TableColumn<Persona, String> colTelefono;
    @FXML private TableColumn<Persona, String> colEmail;
    @FXML private TableColumn<Persona, String> colRol; // Aunque no lo usemos por ahora

    // Modal
    @FXML private StackPane modalOverlay;
    @FXML private TextField txtDNI;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtEmail;

    private final PersonaService personaService = new PersonaService();
    private Persona personaSeleccionada = null;
    private boolean modoEdicion = false;

    @FXML
    public void initialize() {
        // Configurar columnas
        colNombre.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getNombre()));
        colApellido.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getApellido()));
        colDNI.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDni()));
        colTelefono.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getTelefono()));
        colEmail.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getEmail()));

        // Enlazar datos
        tablaPersonas.setItems(personaService.obtenerPersonasFiltradas());

        // Listeners para filtros
        txtNombreFiltro.setOnKeyReleased(this::filtrar);
        txtDNIFiltro.setOnKeyReleased(this::filtrar);

        // Selección de tabla
        tablaPersonas.setOnMouseClicked(this::onSeleccionarFila);

        modalOverlay.setVisible(false);
        tablaPersonas.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnMousePressed(event -> {
                    // Si el clic fue fuera de la tabla
                    if (!tablaPersonas.isHover()) {
                        tablaPersonas.getSelectionModel().clearSelection();
                        personaSeleccionada = null;
                    }
                });
            }
        });

    }

    @FXML
    public void mostrarModal(ActionEvent event) {
        modoEdicion = false;
        limpiarFormulario();
        modalOverlay.setVisible(true);
    }

    @FXML
    public void modificarPersona(ActionEvent event) {
        if (personaSeleccionada == null) {
            mostrarAlerta("Debe seleccionar una persona para modificar.");
            return;
        }

        modoEdicion = true;

        txtNombre.setText(personaSeleccionada.getNombre());
        txtApellido.setText(personaSeleccionada.getApellido());
        txtDNI.setText(personaSeleccionada.getDni());
        txtTelefono.setText(personaSeleccionada.getTelefono());
        txtEmail.setText(personaSeleccionada.getEmail());

        modalOverlay.setVisible(true);
    }

    @FXML
    public void eliminarPersona(ActionEvent event) {
        if (personaSeleccionada == null) {
            mostrarAlerta("Debe seleccionar una persona para eliminar.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmación");
        confirmacion.setHeaderText("¿Está seguro que desea eliminar a " + personaSeleccionada + "?");

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            personaService.eliminarPersona(personaSeleccionada);
            personaSeleccionada = null;
        }
    }

    @FXML
    public void altaPersona(ActionEvent event) {
        try {
            Persona nueva = new Persona(
                txtNombre.getText(),
                txtApellido.getText(),
                txtDNI.getText(),
                txtTelefono.getText(),
                txtEmail.getText()
            );

            if (modoEdicion) {
                personaService.actualizarPersona(personaSeleccionada, nueva);
            } else {
                personaService.guardarPersona(nueva);
            }

            modalOverlay.setVisible(false);
            limpiarFormulario();
            personaSeleccionada = null;

        } catch (Exception e) {
            mostrarAlerta("Error: " + e.getMessage());
        }
    }

    @FXML
    public void cerrarModal(ActionEvent event) {
        modalOverlay.setVisible(false);
        limpiarFormulario();
    }

    private void onSeleccionarFila(MouseEvent event) {
        personaSeleccionada = tablaPersonas.getSelectionModel().getSelectedItem();
    }

    private void filtrar(KeyEvent keyEvent) {
        personaService.filtrarPersonas(txtNombreFiltro.getText(), txtDNIFiltro.getText());
    }

    private void limpiarFormulario() {
        txtNombre.clear();
        txtApellido.clear();
        txtDNI.clear();
        txtTelefono.clear();
        txtEmail.clear();
    }

    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
