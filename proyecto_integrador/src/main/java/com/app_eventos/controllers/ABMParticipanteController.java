package com.app_eventos.controllers;

import com.app_eventos.model.Evento;
import com.app_eventos.model.Persona;
import com.app_eventos.model.RolEvento;
import com.app_eventos.model.enums.EstadoEvento;
import com.app_eventos.model.interfaces.IEventoConCupo;
import com.app_eventos.repository.Repositorio;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

public class ABMParticipanteController {

    private Repositorio repositorio = new Repositorio();

    // Filtros
    @FXML private ComboBox<Evento> comboEventoFiltro;
    @FXML private TextField txtNombreFiltro;
    @FXML private TextField txtDNIFiltro;

    // Tabla
    @FXML private TableView<RolEvento> tablaParticipantes;
    @FXML private TableColumn<RolEvento, String> colEvento;
    @FXML private TableColumn<RolEvento, String> colNombre;
    @FXML private TableColumn<RolEvento, String> colDNI;
    @FXML private TableColumn<RolEvento, String> colTelefono;
    @FXML private TableColumn<RolEvento, String> colEmail;
    @FXML private TableColumn<RolEvento, String> colEstadoEvento;

    // Modal inscripción
    @FXML private StackPane modalOverlay;
    @FXML private ComboBox<Evento> comboEvento;
    @FXML private ComboBox<Persona> comboParticipante;

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

        // Cargar eventos en combos
        cargarEventosEnCombos();
        
        // Cargar personas en combo
        cargarPersonasEnCombo();

        // Configurar listeners para actualizar información del evento
        comboEvento.valueProperty().addListener((obs, oldVal, newVal) -> {
            actualizarInfoEvento(newVal);
            limpiarEstilos();
        });

        comboParticipante.valueProperty().addListener((obs, oldVal, newVal) -> {
            actualizarInfoParticipante(newVal);
            limpiarEstilos();
        });
    }

    private void cargarEventosEnCombos() {
        var eventos = repositorio.obtenerTodosLosEventos();
        comboEventoFiltro.setItems(FXCollections.observableArrayList(eventos));
        
        // Solo eventos confirmados para inscripción
        var eventosConfirmados = eventos.stream()
                                        .filter(e -> e.getEstado() == EstadoEvento.CONFIRMADO)
                                        .toList();
        comboEvento.setItems(FXCollections.observableArrayList(eventosConfirmados));
    }

    private void cargarPersonasEnCombo() {
        var personas = repositorio.obtenerTodasLasPersonas();
        comboParticipante.setItems(FXCollections.observableArrayList(personas));
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

    private void actualizarInfoEvento(Evento evento) {
        if (evento != null) {
            lblEstadoEvento.setText("Estado: " + evento.getEstado());
            lblTipoEvento.setText("Tipo: " + evento.getTipoEvento());
            lblFechaEvento.setText("Fecha: " + evento.getFechaInicio().toLocalDate());
            
            // Mostrar información de cupo si aplica
            if (evento instanceof IEventoConCupo eventoConCupo) {
                int participantesActuales = repositorio.contarParticipantesPorEvento(evento.getIdEvento());
                int cupoMaximo = eventoConCupo.getCupoMaximo();
                lblCupoDisponible.setText("Cupo: " + participantesActuales + "/" + cupoMaximo);
            } else {
                lblCupoDisponible.setText("Cupo: Sin límite");
            }
        } else {
            lblEstadoEvento.setText("Estado: -");
            lblTipoEvento.setText("Tipo: -");
            lblCupoDisponible.setText("Cupo: -");
            lblFechaEvento.setText("Fecha: -");
        }
    }

    private void actualizarInfoParticipante(Persona persona) {
        if (persona != null) {
            lblNombreParticipante.setText("Nombre: " + persona.getNombreCompleto());
            lblDniParticipante.setText("DNI: " + persona.getDni());
            lblTelefonoParticipante.setText("Teléfono: " + (persona.getTelefono() != null ? persona.getTelefono() : "-"));
            lblEmailParticipante.setText("Email: " + (persona.getEmail() != null ? persona.getEmail() : "-"));
        } else {
            lblNombreParticipante.setText("Nombre: -");
            lblDniParticipante.setText("DNI: -");
            lblTelefonoParticipante.setText("Teléfono: -");
            lblEmailParticipante.setText("Email: -");
        }
    }

    private void limpiarEstilos() {
        comboEvento.getStyleClass().remove("campo-invalido");
        comboParticipante.getStyleClass().remove("campo-invalido");
    }

    @FXML
    private void altaParticipante() {
        // ===== VALIDACIONES EN CONTROLLER =====
        limpiarEstilos();
        boolean invalido = false;

        if (comboEvento.getValue() == null) {
            comboEvento.getStyleClass().add("campo-invalido");
            invalido = true;
        }
        if (comboParticipante.getValue() == null) {
            comboParticipante.getStyleClass().add("campo-invalido");
            invalido = true;
        }

        if (invalido) {
            mostrarAlerta("Campos incompletos", "Debe seleccionar un evento y un participante.");
            return;
        }

        Evento evento = comboEvento.getValue();
        Persona persona = comboParticipante.getValue();

        // Validar que no esté ya inscripto
        if (repositorio.estaInscripto(persona.getIdPersona(), evento.getIdEvento())) {
            mostrarAlerta("Ya inscripto", "Esta persona ya está inscripta en el evento seleccionado.");
            return;
        }

        try {
            // ===== DELEGAR AL MODELO =====
            RolEvento inscripcion = RolEvento.inscribirParticipante(evento, persona);

            // ===== PERSISTIR =====
            repositorio.guardarRolEvento(inscripcion);

            // ===== ACTUALIZAR UI =====
            mostrarAlerta("Éxito", "Participante inscripto correctamente.", Alert.AlertType.INFORMATION);
            cerrarModal();
            // Actualizar información del evento por si cambió el cupo
            actualizarInfoEvento(evento);
            // TODO: Actualizar tabla

        } catch (IllegalStateException e) {
            // Errores de lógica de negocio del modelo
            mostrarAlerta("Error de inscripción", e.getMessage());
        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo inscribir al participante: " + e.getMessage());
        }
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
        RolEvento participanteSeleccionado = tablaParticipantes.getSelectionModel().getSelectedItem();
        
        if (participanteSeleccionado != null) {
            // Verificar si se puede cancelar la inscripción
            if (!participanteSeleccionado.puedeSerCancelada()) {
                mostrarAlerta("No se puede cancelar", "No se puede cancelar la inscripción de un evento finalizado.");
                return;
            }
            
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmar cancelación");
            confirmacion.setHeaderText(null);
            confirmacion.setContentText("¿Está seguro de que desea cancelar esta inscripción?");
            
            if (confirmacion.showAndWait().get() == ButtonType.OK) {
                try {
                    repositorio.eliminarRolEvento(participanteSeleccionado.getId());
                    mostrarAlerta("Éxito", "Inscripción cancelada correctamente.", Alert.AlertType.INFORMATION);
                    // TODO: Actualizar tabla
                } catch (Exception e) {
                    mostrarAlerta("Error", "No se pudo cancelar la inscripción: " + e.getMessage());
                }
            }
        } else {
            mostrarAlerta("Selección requerida", "Debe seleccionar una inscripción en la tabla para cancelar.");
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        mostrarAlerta(titulo, mensaje, Alert.AlertType.WARNING);
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}

