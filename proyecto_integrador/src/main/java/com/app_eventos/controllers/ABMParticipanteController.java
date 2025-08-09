package com.app_eventos.controllers;

import com.app_eventos.model.Evento;
import com.app_eventos.model.Persona;
import com.app_eventos.model.RolEvento;
import com.app_eventos.model.enums.TipoRol;
import com.app_eventos.model.interfaces.IEventoConCupo;
import com.app_eventos.services.Servicio;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class ABMParticipanteController {

    private final Servicio servicio = Servicio.getInstance();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Filtros
    @FXML private ComboBox<Evento> comboEventoFiltro;
    @FXML private TextField txtNombreFiltro;
    @FXML private TextField txtDNIFiltro;

    // Tabla
    @FXML private TableView<RolEvento> tablaParticipantes;
    @FXML private TableColumn<RolEvento, String> colEvento;
    @FXML private TableColumn<RolEvento, String> colNombre;
    // No necesitamos colRol porque siempre es PARTICIPANTE
    @FXML private TableColumn<RolEvento, String> colDNI;
    @FXML private TableColumn<RolEvento, String> colTelefono;
    @FXML private TableColumn<RolEvento, String> colEmail;
    @FXML private TableColumn<RolEvento, String> colEstadoEvento;
    @FXML private TableColumn<RolEvento, String> colFechaAsignacion;

    // Modal inscripción
    @FXML private StackPane modalOverlay;
    @FXML private ComboBox<Evento> comboEvento;
    @FXML private ComboBox<Persona> comboParticipante;
    // Rol fijo para participantes - no necesita ComboBox
    private final TipoRol rolFijo = TipoRol.PARTICIPANTE;

    @FXML private Label lblEstadoEvento;
    @FXML private Label lblTipoEvento;
    @FXML private Label lblCupoDisponible;
    @FXML private Label lblFechaEvento;

    @FXML private Label lblNombreParticipante;
    @FXML private Label lblDniParticipante;
    @FXML private Label lblTelefonoParticipante;
    @FXML private Label lblEmailParticipante;
    
    // Estado interno del controlador
    private RolEvento participacionSeleccionada = null;
    private boolean modoEdicion = false;

    @FXML
    public void initialize() {
        // Configuración de la tabla responsive
        configurarTabla();
        
        // Cargar datos iniciales
        cargarDatosIniciales();
        
        // Configurar filtros reactivos
        configurarFiltros();
        
        // Configurar selección de tabla
        configurarSeleccionTabla();
        
        // Configurar modal
        modalOverlay.setVisible(false);
    }

    /**
     * Configura la tabla con columnas responsive y cell factories
     */
    private void configurarTabla() {
        // Configuración responsive de columnas
        tablaParticipantes.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            double total = newWidth.doubleValue();
            colEvento.setPrefWidth(total * 0.25);           // 25%
            colNombre.setPrefWidth(total * 0.20);           // 20%
            colDNI.setPrefWidth(total * 0.12);              // 12%
            colTelefono.setPrefWidth(total * 0.12);         // 12%
            colEmail.setPrefWidth(total * 0.15);            // 15%
            colEstadoEvento.setPrefWidth(total * 0.08);     // 8%
            colFechaAsignacion.setPrefWidth(total * 0.08);  // 8%
        });

        // Cell value factories
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        
        colEvento.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getEvento().getNombre()));
        
        colNombre.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getPersona().getNombre() + " " + 
                                   data.getValue().getPersona().getApellido()));
        
        // No necesitamos configurar colRol porque siempre es PARTICIPANTE
        
        colDNI.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getPersona().getDni()));
        
        colTelefono.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getPersona().getTelefono()));
        
        colEmail.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getPersona().getEmail()));
        
        colEstadoEvento.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getEvento().getEstado().toString()));
        
        colFechaAsignacion.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getFechaAsignacion().format(formatter)));
    }

    /**
     * Carga datos iniciales en la tabla y combos
     */
    private void cargarDatosIniciales() {
        // Cargar SOLO participantes (rol PARTICIPANTE) en la tabla
        tablaParticipantes.setItems(servicio.obtenerSoloParticipantes());
        
        // Cargar eventos en filtro
        comboEventoFiltro.setItems(servicio.obtenerEventosDisponibles());
        comboEventoFiltro.setPromptText("Todos los eventos");
        
        // Cargar datos del modal
        comboEvento.setItems(servicio.obtenerEventosDisponibles());
        comboParticipante.setItems(servicio.obtenerPersonas());
        // No necesitamos cargar roles - siempre es PARTICIPANTE
        
        // Configurar StringConverters para mejor visualización
        configurarStringConverters();
    }

    /**
     * Configura los StringConverters para los ComboBox
     */
    private void configurarStringConverters() {
        comboEvento.setConverter(new javafx.util.StringConverter<Evento>() {
            @Override
            public String toString(Evento evento) {
                return evento != null ? evento.getNombre() : "";
            }
            @Override
            public Evento fromString(String string) { return null; }
        });

        comboParticipante.setConverter(new javafx.util.StringConverter<Persona>() {
            @Override
            public String toString(Persona persona) {
                return persona != null ? persona.toString() : "";
            }
            @Override
            public Persona fromString(String string) { return null; }
        });
    }

    /**
     * Configura filtros reactivos siguiendo el patrón de ABMPersona
     */
    private void configurarFiltros() {
        txtNombreFiltro.setOnKeyReleased(this::aplicarFiltros);
        txtDNIFiltro.setOnKeyReleased(this::aplicarFiltros);
        
        comboEventoFiltro.setOnAction(e -> aplicarFiltros(null));
    }

    /**
     * Configura la selección de filas en la tabla
     */
    private void configurarSeleccionTabla() {
        tablaParticipantes.setOnMouseClicked(this::onSeleccionarFila);
        
        // Deseleccionar si se hace clic fuera de la tabla
        tablaParticipantes.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnMousePressed(event -> {
                    if (!tablaParticipantes.isHover()) {
                        tablaParticipantes.getSelectionModel().clearSelection();
                        participacionSeleccionada = null;
                    }
                });
            }
        });
    }

    // ⭐ MÉTODOS PRINCIPALES DEL ABM

    /**
     * Aplica filtros a la tabla de participaciones
     */
    private void aplicarFiltros(KeyEvent event) {
        String nombreEvento = comboEventoFiltro.getValue() != null ? 
                            comboEventoFiltro.getValue().getNombre() : "";
        String nombrePersona = txtNombreFiltro.getText();
        String dni = txtDNIFiltro.getText();

        ObservableList<RolEvento> filtradas = servicio.filtrarSoloParticipantes(
            nombreEvento, nombrePersona, dni);
        tablaParticipantes.setItems(filtradas);
    }

    /**
     * Maneja la selección de filas en la tabla
     */
    private void onSeleccionarFila(MouseEvent event) {
        participacionSeleccionada = tablaParticipantes.getSelectionModel().getSelectedItem();
    }

    /**
     * Limpia el formulario del modal
     */
    private void limpiarModal() {
        comboEvento.getSelectionModel().clearSelection();
        comboParticipante.getSelectionModel().clearSelection();
        // No hay comboRol - siempre es PARTICIPANTE

        lblEstadoEvento.setText("Estado: -");
        lblTipoEvento.setText("Tipo: -");
        lblCupoDisponible.setText("Cupo: -");
        lblFechaEvento.setText("Fecha: -");

        lblNombreParticipante.setText("Nombre: -");
        lblDniParticipante.setText("DNI: -");
        lblTelefonoParticipante.setText("Teléfono: -");
        lblEmailParticipante.setText("Email: -");
    }

    /**
     * Actualiza las etiquetas informativas cuando se selecciona un evento
     */
    private void actualizarInfoEvento(Evento evento) {
        if (evento == null) {
            lblEstadoEvento.setText("Estado: -");
            lblTipoEvento.setText("Tipo: -");
            lblCupoDisponible.setText("Cupo: -");
            lblFechaEvento.setText("Fecha: -");
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        
        lblEstadoEvento.setText("Estado: " + evento.getEstado());
        lblTipoEvento.setText("Tipo: " + evento.getTipoEvento());
        lblFechaEvento.setText("Fecha: " + evento.getFechaInicio().format(formatter));
        
        // Mostrar cupo si aplica
        if (evento instanceof IEventoConCupo) {
            IEventoConCupo eventoConCupo = (IEventoConCupo) evento;
            long participantesActuales = evento.contarParticipantesActivos();
            lblCupoDisponible.setText("Cupo: " + participantesActuales + "/" + eventoConCupo.getCupoMaximo());
        } else {
            lblCupoDisponible.setText("Cupo: Sin límite");
        }
    }

    /**
     * Actualiza las etiquetas informativas cuando se selecciona una persona
     */
    private void actualizarInfoPersona(Persona persona) {
        if (persona == null) {
            lblNombreParticipante.setText("Nombre: -");
            lblDniParticipante.setText("DNI: -");
            lblTelefonoParticipante.setText("Teléfono: -");
            lblEmailParticipante.setText("Email: -");
            return;
        }

        lblNombreParticipante.setText("Nombre: " + persona.getNombre() + " " + persona.getApellido());
        lblDniParticipante.setText("DNI: " + persona.getDni());
        lblTelefonoParticipante.setText("Teléfono: " + persona.getTelefono());
        lblEmailParticipante.setText("Email: " + persona.getEmail());
    }

    // ⭐ MÉTODOS FXML DEL ABM

    /**
     * Abre el modal para crear nueva participación
     */
    @FXML
    private void mostrarModalAlta() {
        modoEdicion = false;
        limpiarModal();
        
        // Configurar listeners para actualización dinámica
        comboEvento.setOnAction(e -> actualizarInfoEvento(comboEvento.getValue()));
        comboParticipante.setOnAction(e -> actualizarInfoPersona(comboParticipante.getValue()));
        
        modalOverlay.setVisible(true);
    }

    /**
     * Cierra el modal sin guardar cambios
     */
    @FXML
    private void cerrarModal() {
        modalOverlay.setVisible(false);
        limpiarModal();
        participacionSeleccionada = null;
    }

    /**
     * Asigna un rol a una persona en un evento (siguiendo modelo rico)
     */
    @FXML
    private void altaParticipante() {
        try {
            // 1. VALIDACIONES DE UI [[memory:5343529]]
            if (!validarCamposUI()) {
                return;
            }

            // 2. CREAR PARTICIPACIÓN (modelo rico valida reglas de negocio)
            RolEvento nuevaParticipacion = servicio.crearParticipacion(
                comboEvento.getValue(),
                comboParticipante.getValue(), 
                rolFijo // Siempre PARTICIPANTE
            );

            // 3. GUARDAR (service como intermediario)
            servicio.guardarParticipacion(nuevaParticipacion);

            // 4. ACTUALIZAR VISTA
            tablaParticipantes.setItems(servicio.obtenerSoloParticipantes());
            cerrarModal();
            
            mostrarAlerta("Éxito", "Participación asignada correctamente", Alert.AlertType.INFORMATION);

        } catch (IllegalStateException | IllegalArgumentException e) {
            // Errores de reglas de negocio del modelo rico
            mostrarAlerta("Regla de negocio", e.getMessage(), Alert.AlertType.WARNING);
        } catch (Exception e) {
            // Errores técnicos
            mostrarAlerta("Error", "Error inesperado: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Modifica una participación existente
     */
    @FXML
    private void modificarParticipante() {
        if (participacionSeleccionada == null) {
            mostrarAlerta("Selección requerida", 
                         "Debe seleccionar una participación de la tabla para modificar.", 
                         Alert.AlertType.WARNING);
            return;
        }

        if (!participacionSeleccionada.puedeModificar()) {
            mostrarAlerta("No se puede modificar", 
                         "Esta participación no se puede modificar (evento finalizado o participación inactiva).", 
                         Alert.AlertType.WARNING);
            return;
        }

        // Cargar datos en el modal para edición
        modoEdicion = true; // Indica que estamos editando una participación existente
        comboEvento.setValue(participacionSeleccionada.getEvento());
        comboParticipante.setValue(participacionSeleccionada.getPersona());
        // No hay comboRol - siempre es PARTICIPANTE
        
        actualizarInfoEvento(participacionSeleccionada.getEvento());
        actualizarInfoPersona(participacionSeleccionada.getPersona());
        
        modalOverlay.setVisible(true);
    }

    /**
     * Elimina una participación (borrado lógico)
     */
    @FXML
    private void bajaParticipante() {
        if (participacionSeleccionada == null) {
            mostrarAlerta("Selección requerida", 
                         "Debe seleccionar una participación de la tabla para eliminar.", 
                         Alert.AlertType.WARNING);
            return;
        }

        // Confirmación de eliminación
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmación");
        confirmacion.setHeaderText("¿Está seguro que desea eliminar esta participación?");
        confirmacion.setContentText(participacionSeleccionada.toString());

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                // El modelo rico maneja la lógica de borrado
                servicio.eliminarParticipacion(participacionSeleccionada);
                
                // Actualizar vista
                tablaParticipantes.setItems(servicio.obtenerSoloParticipantes());
                participacionSeleccionada = null;
                
                mostrarAlerta("Éxito", "Participación eliminada correctamente", Alert.AlertType.INFORMATION);
                
            } catch (Exception e) {
                mostrarAlerta("Error", "Error al eliminar participación: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    // ⭐ MÉTODOS AUXILIARES

    /**
     * Valida los campos de la UI antes de guardar [[memory:5343529]]
     */
    private boolean validarCamposUI() {
        if (comboEvento.getValue() == null) {
            mostrarAlerta("Campo requerido", "Debe seleccionar un evento", Alert.AlertType.WARNING);
            return false;
        }
        
        if (comboParticipante.getValue() == null) {
            mostrarAlerta("Campo requerido", "Debe seleccionar una persona", Alert.AlertType.WARNING);
            return false;
        }
        
        // No necesitamos validar rol - siempre es PARTICIPANTE
        
        return true;
    }

    /**
     * Muestra alertas al usuario
     */
    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
    
    /**
     * Método para refrescar datos cuando se navega a esta ventana
     */
    public void refrescarDatos() {
        // Recargar datos en la tabla
        tablaParticipantes.setItems(servicio.obtenerSoloParticipantes());
        
        // Recargar combos
        comboEventoFiltro.setItems(servicio.obtenerEventosDisponibles());
        comboEvento.setItems(servicio.obtenerEventosDisponibles());
        comboParticipante.setItems(servicio.obtenerPersonas());
    }
}

