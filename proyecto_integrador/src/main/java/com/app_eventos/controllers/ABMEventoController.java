package com.app_eventos.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

import com.app_eventos.model.enums.EstadoEvento;
import com.app_eventos.model.enums.TipoEntrada;
import com.app_eventos.model.enums.TipoEvento;
import com.app_eventos.utils.ComboBoxInicializador;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javafx.util.StringConverter;

import java.io.IOException;



public class ABMEventoController {

    // Campos de búsqueda y modal
    @FXML private TextField txtNombre;
    @FXML private DatePicker dateInicio;
    @FXML private DatePicker dateFin;
    @FXML private StackPane modalOverlay;
    @FXML private ComboBox<TipoEvento> comboTipoEvento;
    @FXML private ComboBox<TipoEntrada> comboTipoEntrada;
    @FXML private ComboBox<EstadoEvento> comboEstado;
    @FXML private Spinner<LocalTime> spinnerHoraInicio;
    @FXML private Spinner<LocalTime> spinnerHoraFin;
    @FXML private Pane seccionDinamica;
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

        // Spinner de hora (de 00:00 a 23:59, con salto de 30 minutos)
        spinnerHoraInicio.setValueFactory(crearFactoryHora());
        spinnerHoraFin.setValueFactory(crearFactoryHora());

        // Evita edición manual incorrecta
        spinnerHoraInicio.setEditable(false);
        spinnerHoraFin.setEditable(false);

        ComboBoxInicializador.cargarTipoEvento(comboTipoEvento);
        ComboBoxInicializador.cargarEstadoEvento(comboEstado);

        // Cargar fragmento al cambiar tipo de evento
        comboTipoEvento.valueProperty().addListener((obs, oldVal, newVal) -> {cargarFragmentoEspecifico(newVal);
            comboTipoEvento.getStyleClass().remove("campo-invalido");
        });

    }

    private SpinnerValueFactory<LocalTime> crearFactoryHora() {
        LocalTime horaInicial = LocalTime.of(8, 0);
        LocalTime horaFinal = LocalTime.of(22, 0);
        int intervaloMinutos = 30;

        return new SpinnerValueFactory<>() {
            private LocalTime value = horaInicial;

            {
                setConverter(new StringConverter<>() {
                    final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

                    @Override
                    public String toString(LocalTime time) {
                        return time != null ? time.format(formatter) : "";
                    }

                    @Override
                    public LocalTime fromString(String s) {
                        return LocalTime.parse(s, formatter);
                    }
                });
                setValue(horaInicial);
            }

            @Override
            public void decrement(int steps) {
                LocalTime next = value.minusMinutes(steps * intervaloMinutos);
                if (!next.isBefore(horaInicial)) setValue(next);
            }

            @Override
            public void increment(int steps) {
                LocalTime next = value.plusMinutes(steps * intervaloMinutos);
                if (!next.isAfter(horaFinal)) setValue(next);
            }
        };
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
            e.printStackTrace();
        }
    }

    // -- Modal --
    @FXML
    private void mostrarModal() {
        modalOverlay.setVisible(true);
        modalOverlay.toFront();
    }

    @FXML
    private void cerrarModal() {
        txtNombre.clear();
        dateInicio.setValue(null);
        dateFin.setValue(null);
        comboTipoEvento.getSelectionModel().clearSelection();
        seccionDinamica.getChildren().clear();
        spinnerHoraInicio.getValueFactory().setValue(LocalTime.of(0, 0));
        spinnerHoraFin.getValueFactory().setValue(LocalTime.of(0, 0));
        modalOverlay.setVisible(false);
    }

    @FXML
    private void guardarEvento() {
    // Captura de datos desde la vista
    String nombre = txtNombre.getText();
    TipoEvento tipo = comboTipoEvento.getValue();
    LocalDate fechaInicio = dateInicio.getValue();
    LocalDate fechaFin = dateFin.getValue();
    LocalTime horaInicio = spinnerHoraInicio.getValue();
    LocalTime horaFin = spinnerHoraFin.getValue();
    EstadoEvento estado = comboEstado.getValue();

    // En este punto, se podría pasar todo a la capa de servicio (aún no implementada)
    // Por ahora, simplemente cerramos el modal
    cerrarModal();
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
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}