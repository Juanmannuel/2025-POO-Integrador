package com.app_eventos.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
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

    @FXML
    public void initialize() {
        // Inicialización de componentes
        modalOverlay.setVisible(false);

        // Aquí podrías cargar datos en comboEventoFiltro y tablaParticipantes
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
        // Acá van los datos del participante
        cerrarModal();
    }

    @FXML
    private void modificarParticipante() {
        Object participanteSeleccionado = tablaParticipantes.getSelectionModel().getSelectedItem();

        if (participanteSeleccionado != null) {
        // Acá van los datos del participante seleccionado
        
        } else {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle("Selección requerida");
        alerta.setHeaderText(null);
        alerta.setContentText("Debe seleccionar un participante de la tabla para modificar.");
        alerta.showAndWait();
        }
    }

    @FXML
    private void bajaParticipante() {
    }
}

