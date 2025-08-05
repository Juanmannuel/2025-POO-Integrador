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

    // Modal detalles
    @FXML private StackPane modalDetallesOverlay;
    @FXML private Label lblDetalleEvento;
    @FXML private Label lblDetalleParticipante;
    @FXML private Label lblDetalleEstado;
    @FXML private Label lblDetalleTipo;
    @FXML private Label lblDetalleFecha;
    @FXML private TextArea txtDetalleInfo;

    @FXML
    public void initialize() {
        // Inicialización de componentes
        modalOverlay.setVisible(false);
        modalDetallesOverlay.setVisible(false);

        // Aquí podrías cargar datos en comboEventoFiltro y tablaParticipantes
    }

    @FXML
    private void actualizarLista() {
        System.out.println("Actualizando lista de participantes...");
        // Acá iría la lógica para aplicar los filtros y refrescar la tabla
    }

    @FXML
    private void mostrarModalInscribir() {
        limpiarModalInscripcion();
        modalOverlay.setVisible(true);
    }

    @FXML
    private void cerrarModal() {
        modalOverlay.setVisible(false);
    }

    @FXML
    private void inscribirParticipante() {
        System.out.println("Inscribiendo participante...");
        // Lógica para registrar la participación
        cerrarModal();
    }

    @FXML
    private void desinscribirParticipante() {
        System.out.println("Desinscribiendo participante...");
        // Lógica para eliminar la inscripción
    }

    @FXML
    private void verDetallesParticipante() {
        System.out.println("Mostrando detalles de participación...");
        // Aquí podrías cargar los datos del participante seleccionado
        modalDetallesOverlay.setVisible(true);
    }

    @FXML
    private void cerrarModalDetalles() {
        modalDetallesOverlay.setVisible(false);
    }

    private void limpiarModalInscripcion() {
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

