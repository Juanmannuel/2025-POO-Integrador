package com.app_eventos.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

public class ABMParticipanteController {

    // Filtros
    @FXML private ComboBox<?> comboEventoFiltro;
    @FXML private TextField txtNombreFiltro;
    @FXML private TextField txtDNIFiltro;

    // Tabla
    @FXML private TableView<?> tablaParticipantes;
    @FXML private TableColumn<?, ?> colEvento;
    @FXML private TableColumn<?, ?> colNombre;
    @FXML private TableColumn<?, ?> colDNI;
    @FXML private TableColumn<?, ?> colTelefono;
    @FXML private TableColumn<?, ?> colEmail;
    @FXML private TableColumn<?, ?> colEstadoEvento;

    // Modal inscripción
    @FXML private StackPane modalOverlay;
    @FXML private ComboBox<?> comboEvento;
    @FXML private ComboBox<?> comboParticipante;

    @FXML private Label lblEstadoEvento;
    @FXML private Label lblTipoEvento;
    @FXML private Label lblCupoDisponible;
    @FXML private Label lblFechaEvento;

    @FXML private Label lblNombreParticipante;
    @FXML private Label lblDniParticipante;
    @FXML private Label lblTelefonoParticipante;
    @FXML private Label lblEmailParticipante;

    // Modal detalles (modificar)
    @FXML private StackPane modalModificarOverlay;

    @FXML
    public void initialize() {
        modalOverlay.setVisible(false);
        modalModificarOverlay.setVisible(false);
    }

    @FXML
    private void mostrarModalAlta() {
        limpiarModalAlta();
        modalOverlay.setVisible(true);
    }

    @FXML
    private void cerrarModal() {
        modalOverlay.setVisible(false);
    }

    @FXML
    private void altaParticipante() {
        cerrarModal();
    }

    @FXML
    private void bajaParticipante() { }

    @FXML
    private void modificarParticipante() {
        modalModificarOverlay.setVisible(true);
    }

    @FXML
    private void cerrarModalModificar() {
        modalModificarOverlay.setVisible(false);
    }

    private void limpiarModalAlta() {
        comboEvento.getSelectionModel().clearSelection();
        comboParticipante.getSelectionModel().clearSelection();
        lblEstadoEvento.setText("Estado: -");
        lblTipoEvento.setText("Tipo: -");
        lblCupoDisponible.setText("Cupo: -");
        lblFechaEvento.setText("Fecha: -");
        lblNombreParticipante.setText("Nombre: -");
        lblDniParticipante.setText("DNI: -");
        lblTelefonoParticipante.setText("Teléfono: -");
        lblEmailParticipante.setText("Email: -");
    }
}