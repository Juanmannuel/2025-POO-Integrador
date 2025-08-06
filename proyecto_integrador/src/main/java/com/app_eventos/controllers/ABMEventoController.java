package com.app_eventos.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

import com.app_eventos.model.Evento;
import com.app_eventos.model.enums.EstadoEvento;
import com.app_eventos.model.enums.TipoEntrada;
import com.app_eventos.model.enums.TipoEvento;
import com.app_eventos.repository.Repositorio;
import com.app_eventos.utils.ComboBoxInicializador;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.time.LocalDate;


public class ABMEventoController {

    private Repositorio repositorio = new Repositorio();

    // Campos de búsqueda y modal
    @FXML private TextField txtNombre;
    @FXML private DatePicker dateInicio;
    @FXML private DatePicker dateFin;
    @FXML private StackPane modalOverlay;
    @FXML private ComboBox<TipoEvento> comboTipoEvento;
    @FXML private ComboBox<TipoEntrada> comboTipoEntrada;
    @FXML private ComboBox<EstadoEvento> comboEstado;
    @FXML private Pane seccionDinamica;
    @FXML private Spinner<Integer> spinnerDuracion;
    @FXML private TableView<?> tablaEventos;

    @FXML private TableColumn<?, ?> colNombre;
    @FXML private TableColumn<?, ?> colTipo;
    @FXML private TableColumn<?, ?> colFechaInicio;
    @FXML private TableColumn<?, ?> colDuracion;
    @FXML private TableColumn<?, ?> colEstado;
    @FXML private TableColumn<?, ?> colResponsables;


    @FXML
    public void initialize() {
         tablaEventos.widthProperty().addListener((obs, oldWidth, newWidth) -> { 
            double total = newWidth.doubleValue();

        colNombre.setPrefWidth(total * 0.20);        // 20%
        colTipo.setPrefWidth(total * 0.10);          // 10%
        colFechaInicio.setPrefWidth(total * 0.15);   // 15%
        colDuracion.setPrefWidth(total * 0.10);      // 10%
        colEstado.setPrefWidth(total * 0.15);        // 15%
        colResponsables.setPrefWidth(total * 0.30);  // 30%
        });

        ComboBoxInicializador.cargarTipoEvento(comboTipoEvento);
        ComboBoxInicializador.cargarEstadoEvento(comboEstado);

        // Configuración del spinner de duración
        spinnerDuracion.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 365, 1));
        spinnerDuracion.setEditable(false);

        // Cargar fragmento al cambiar tipo de evento
        comboTipoEvento.valueProperty().addListener((obs, oldVal, newVal) -> {cargarFragmentoEspecifico(newVal);
            comboTipoEvento.getStyleClass().remove("campo-invalido");
        });

        // Listeners para limpiar el estilo de error si el usuario lo corrige
        txtNombre.textProperty().addListener((obs, oldVal, newVal) -> txtNombre.getStyleClass().remove("campo-invalido"));
        dateInicio.valueProperty().addListener((obs, oldVal, newVal) -> dateInicio.getStyleClass().remove("campo-invalido"));
        dateFin.valueProperty().addListener((obs, oldVal, newVal) -> dateFin.getStyleClass().remove("campo-invalido"));
        comboEstado.valueProperty().addListener((obs, oldVal, newVal) -> comboEstado.getStyleClass().remove("campo-invalido"));

    }

    private void cargarFragmentoEspecifico(TipoEvento tipo) {
        seccionDinamica.getChildren().clear();
        if (tipo == null) return;

        String rutaFXML = switch (tipo) {
            case FERIA -> "/fxml/abm/abmEventoResources/feria.fxml";
            case TALLER -> "/fxml/abm/abmEventoResources/taller.fxml";
            case EXPOSICION -> "/fxml/abm/abmEventoResources/exposicion.fxml";
            case CONCIERTO -> "/fxml/abm/abmEventoResources/concierto.fxml";
            case CICLO_CINE -> "/fxml/abm/abmEventoResources/cicloCine.fxml";
            default -> null;
        };

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(rutaFXML));
            Node nodo = loader.load();
            seccionDinamica.getChildren().add(nodo);
        } catch (IOException e) {
            System.err.println("Error al cargar el fragmento: " + rutaFXML);
            e.printStackTrace();
        }
    }

    private void limpiarEstilos() {
        txtNombre.getStyleClass().remove("campo-invalido");
        comboTipoEvento.getStyleClass().remove("campo-invalido");
        dateInicio.getStyleClass().remove("campo-invalido");
        dateFin.getStyleClass().remove("campo-invalido");
    }

    // -- Modal --
    @FXML
    private void mostrarModal() {
        modalOverlay.setVisible(true);
        modalOverlay.toFront();
    }

    @FXML
    private void cerrarModal() {
        limpiarEstilos();
        spinnerDuracion.getValueFactory().setValue(1);
        txtNombre.clear();
        dateInicio.setValue(null);
        dateFin.setValue(null);
        comboTipoEvento.getSelectionModel().clearSelection();
        seccionDinamica.getChildren().clear();

        modalOverlay.setVisible(false);
    }

    @FXML
    private void guardarEvento() {
        // ===== VALIDACIONES EN CONTROLLER =====
        limpiarEstilos();
        boolean invalido = false;

        // Validar campos obligatorios
        if (txtNombre.getText().isBlank()) {
            txtNombre.getStyleClass().add("campo-invalido");
            invalido = true;
        }
        if (comboTipoEvento.getValue() == null) {
            comboTipoEvento.getStyleClass().add("campo-invalido");
            invalido = true;
        }
        if (dateInicio.getValue() == null) {
            dateInicio.getStyleClass().add("campo-invalido");
            invalido = true;
        }
        if (dateFin.getValue() == null) {
            dateFin.getStyleClass().add("campo-invalido");
            invalido = true;
        }
        if (comboEstado.getValue() == null) {
            comboEstado.getStyleClass().add("campo-invalido");
            invalido = true;
        }

        if (invalido) {
            mostrarAlerta("Campos incompletos", "Complete todos los campos obligatorios.");
            return;
        }

        // Validar lógica de fechas
        if (dateFin.getValue().isBefore(dateInicio.getValue())) {
            dateFin.getStyleClass().add("campo-invalido");
            mostrarAlerta("Error en fechas", "La fecha de fin no puede ser anterior a la fecha de inicio.");
            return;
        }

        // Validar que la fecha de inicio no sea en el pasado (para eventos nuevos)
        if (dateInicio.getValue().isBefore(LocalDate.now())) {
            dateInicio.getStyleClass().add("campo-invalido");
            mostrarAlerta("Fecha inválida", "La fecha de inicio no puede ser anterior a hoy.");
            return;
        }

        try {
            // ===== DELEGAR AL MODELO =====
            Evento nuevoEvento = Evento.crearEvento(
                comboTipoEvento.getValue(),
                txtNombre.getText().trim(),
                dateInicio.getValue(),
                dateFin.getValue()
            );

            // Aplicar estado específico si no es planificación
            EstadoEvento estadoSeleccionado = comboEstado.getValue();
            if (estadoSeleccionado != EstadoEvento.PLANIFICACIÓN) {
                // Usar métodos del modelo para cambio de estado
                switch (estadoSeleccionado) {
                    case CONFIRMADO -> nuevoEvento.confirmarEvento();
                    case EJECUCIÓN -> {
                        nuevoEvento.confirmarEvento();
                        nuevoEvento.iniciarEvento();
                    }
                    case FINALIZADO -> {
                        nuevoEvento.confirmarEvento();
                        nuevoEvento.iniciarEvento();
                        nuevoEvento.finalizarEvento();
                    }
                    case CANCELADO -> nuevoEvento.cancelarEvento();
                }
            }

            // ===== PERSISTIR =====
            repositorio.guardarEvento(nuevoEvento);

            // ===== ACTUALIZAR UI =====
            mostrarAlerta("Éxito", "Evento guardado correctamente.", Alert.AlertType.INFORMATION);
            cerrarModal();
            // TODO: Actualizar tabla

        } catch (IllegalStateException e) {
            // Errores de lógica de negocio del modelo
            mostrarAlerta("Error de validación", e.getMessage());
        } catch (Exception e) {
            // Otros errores
            mostrarAlerta("Error", "No se pudo guardar el evento: " + e.getMessage());
        }
    }

    @FXML
    private void modificarEvento() {
        Object eventoSeleccionado = tablaEventos.getSelectionModel().getSelectedItem();
        if (eventoSeleccionado != null) {
            // Acá se cargan los datos del evento seleccionado en los campos del modal
            modalOverlay.setVisible(true);
        } else {
            mostrarAlerta("Selección requerida", "Debe seleccionar un evento en la tabla para modificar.");
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