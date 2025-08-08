package com.app_eventos.controllers;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

import com.app_eventos.model.Evento;
import com.app_eventos.model.enums.EstadoEvento;
import com.app_eventos.model.enums.TipoEntrada;
import com.app_eventos.model.enums.TipoEvento;
import com.app_eventos.services.Servicio;
import com.app_eventos.utils.ComboBoxInicializador;

import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

import javafx.util.StringConverter;

import java.io.IOException;

public class ABMEventoController {
    private final Servicio servicio = Servicio.getInstance();

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
    @FXML private VBox contenedorAsignacionRoles;
    @FXML private TableColumn<Evento, Void> colAcciones;

    @FXML private TableView<Evento> tablaEventos;
    @FXML private TableColumn<Evento, String> colNombre;
    @FXML private TableColumn<Evento, TipoEvento> colTipo;
    @FXML private TableColumn<Evento, String> colFechaInicio;
    @FXML private TableColumn<Evento, String> colFechaFin;
    @FXML private TableColumn<Evento, EstadoEvento> colEstado;
    @FXML private TableColumn<Evento, String> colResponsables;
    @FXML private AsigRolEventoController controladorAsignacionRoles;

    @FXML
    public void initialize() {
        tablaEventos.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            double total = newWidth.doubleValue();

            colNombre.setPrefWidth(total * 0.20);        // 20%
            colTipo.setPrefWidth(total * 0.10);          // 10%
            colFechaInicio.setPrefWidth(total * 0.15);   // 15%
            colEstado.setPrefWidth(total * 0.10);        // 10%
            colFechaFin.setPrefWidth(total * 0.15);      // 15%
            colResponsables.setPrefWidth(total * 0.20);  // 20%
            colAcciones.setPrefWidth(total * 0.10);      // 10%
        });

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

        colNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombre()));
        colTipo.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getTipoEvento()));
        colFechaInicio.setCellValueFactory(data -> {
            String formateada = data.getValue().getFechaInicio().format(formatter);
            return new SimpleStringProperty(formateada);
        });
        colFechaFin.setCellValueFactory(data -> {
            String formateada = data.getValue().getFechaFin().format(formatter);
            return new SimpleStringProperty(formateada);
        });
        colEstado.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getEstado()));
        colResponsables.setCellValueFactory(data -> {
            String nombres = data.getValue().obtenerResponsables().stream()
                    .map(p -> p.getNombre() + " " + p.getApellido())
                    .collect(Collectors.joining(", "));
            return new SimpleStringProperty(nombres);
        });

        // Spinner de hora (de 00:00 a 23:59, con salto de 5 minutos)
        spinnerHoraInicio.setValueFactory(crearFactoryHora());
        spinnerHoraFin.setValueFactory(crearFactoryHora());

        ComboBoxInicializador.cargarTipoEvento(comboTipoEvento);
        ComboBoxInicializador.cargarEstadoEvento(comboEstado);

        // Cargar fragmento al cambiar tipo de evento
        comboTipoEvento.valueProperty().addListener((obs, oldVal, newVal) -> {
            cargarFragmentoEspecifico(newVal);
        });
        // Cargar controlador del fragmento de asignación de roles
        agregarBotonAsignarRol();
        
        // Cargar eventos en la tabla
        tablaEventos.setItems(FXCollections.observableArrayList(servicio.listarEventos()));
    }

    private void abrirModalAsignacionRoles(Evento evento) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/abm/abmEventoResources/asignacionRoles.fxml"));
            VBox vista = loader.load();
            // Guardar el controlador para acceder luego
            this.controladorAsignacionRoles = loader.getController();
            controladorAsignacionRoles.setEvento(evento);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Asignar Roles");
            dialog.getDialogPane().setContent(vista);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            dialog.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cargar el formulario de asignación de roles.");
        }
    }

    private SpinnerValueFactory<LocalTime> crearFactoryHora() {
        LocalTime horaInicial = LocalTime.of(7, 0);
        LocalTime horaFinal = LocalTime.of(23, 59);
        int intervaloMinutos = 5;

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
                setValue(value);
            }

            @Override
            public void decrement(int steps) {
                LocalTime next = value.minusMinutes(steps * intervaloMinutos);
                if (!next.isBefore(horaInicial)) {
                    value = next;
                    setValue(value);
                }
            }

            @Override
            public void increment(int steps) {
                LocalTime next = value.plusMinutes(steps * intervaloMinutos);
                if (!next.isAfter(horaFinal)) {
                    value = next;
                    setValue(value);
                }
            }
        };
    }

        private void agregarBotonAsignarRol() {
        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Asignar Rol");

            {
                btn.setOnAction(e -> {
                    Evento eventoSeleccionado = getTableView().getItems().get(getIndex());
                    if (eventoSeleccionado != null) {
                        abrirModalAsignacionRoles(eventoSeleccionado);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }
        private Object controladorFragmento; 
        private void cargarFragmentoEspecifico(TipoEvento tipo) {
        seccionDinamica.getChildren().clear();
        controladorFragmento = null; // reseteamos

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
            controladorFragmento = loader.getController(); // guardamos la referencia
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
        String nombre = txtNombre.getText();
        TipoEvento tipo = comboTipoEvento.getValue();
        LocalDate fechaInicio = dateInicio.getValue();
        LocalDate fechaFin = dateFin.getValue();
        LocalTime horaInicio = spinnerHoraInicio.getValue();
        LocalTime horaFin = spinnerHoraFin.getValue();
        EstadoEvento estado = comboEstado.getValue();

        try {
            if (tipo == TipoEvento.FERIA) {
                FeriaController feriaCtrl = (FeriaController) controladorFragmento;
                servicio.crearFeria(
                    nombre, fechaInicio, fechaFin, horaInicio, horaFin, estado,
                    feriaCtrl.getCantidadStands(),
                    feriaCtrl.getAmbienteSeleccionado()
                );
            } else if (tipo == TipoEvento.CONCIERTO) {
                if (controladorFragmento instanceof ConciertoController concCtrl) {
                    servicio.crearConcierto(
                    nombre, fechaInicio, fechaFin, horaInicio, horaFin, estado,
                    concCtrl.getTipoEntradaSeleccionada(),
                    concCtrl.getCupoMaximo());
                }
            } else if (tipo == TipoEvento.EXPOSICION) {
                if (controladorFragmento instanceof ExposicionController expoCtrl) {
                    servicio.crearExposicion(
                        nombre, fechaInicio, fechaFin, horaInicio, horaFin, estado,
                        expoCtrl.getTipoArteSeleccionado()
                    );
                }
            } else if (tipo == TipoEvento.TALLER) {
                if (controladorFragmento instanceof TallerController tallerCtrl) {
                    servicio.crearTaller(
                        nombre, fechaInicio, fechaFin, horaInicio, horaFin, estado,
                        tallerCtrl.getCupoMaximo(),
                        tallerCtrl.getModalidadSeleccionada()
                    );
                }
            } else if (tipo == TipoEvento.CICLO_CINE) {
                if (controladorFragmento instanceof CicloCineController cineCtrl) {
                    servicio.crearCicloCine(
                        nombre, fechaInicio, fechaFin, horaInicio, horaFin, estado,
                        cineCtrl.isPostCharla(),
                        cineCtrl.getCupoMaximo(),
                        cineCtrl.getPeliculasTexto() // por ahora como texto
                    );
                }
            }

        // se refresca la tabla
        tablaEventos.getItems().setAll(FXCollections.observableArrayList(servicio.listarEventos()));
        cerrarModal();

    } catch (IllegalArgumentException | IllegalStateException ex) {
        mostrarAlerta("Error de validación", ex.getMessage());
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
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
    
    // Método para refrescar datos cuando se navega a esta ventana
    public void refrescarDatos() {
        tablaEventos.getItems().setAll(servicio.listarEventos());
    }
}
