package com.app_eventos.controllers;

import com.app_eventos.model.Evento;
import com.app_eventos.model.enums.EstadoEvento;
import com.app_eventos.model.enums.TipoEntrada;
import com.app_eventos.model.enums.TipoEvento;
import com.app_eventos.model.enums.TipoPelicula;
import com.app_eventos.services.Servicio;
import com.app_eventos.utils.ComboBoxInicializador;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

public class ABMEventoController {

    private final Servicio servicio = Servicio.getInstance();

    // Campos del modal principal
    @FXML private TextField txtNombre;
    @FXML private DatePicker dateInicio;
    @FXML private DatePicker dateFin;
    @FXML private StackPane modalOverlay;
    @FXML private ComboBox<TipoEvento> comboTipoEvento;
    @FXML private ComboBox<TipoEntrada> comboTipoEntrada; // (lo usan fragmentos como Concierto)
    @FXML private ComboBox<EstadoEvento> comboEstado;
    @FXML private Spinner<LocalTime> spinnerHoraInicio;
    @FXML private Spinner<LocalTime> spinnerHoraFin;
    @FXML private Pane seccionDinamica;                  // aquí se inyecta el fragmento específico

    // Tabla
    @FXML private TableView<Evento> tablaEventos;
    @FXML private TableColumn<Evento, String> colNombre;
    @FXML private TableColumn<Evento, TipoEvento> colTipo;
    @FXML private TableColumn<Evento, String> colFechaInicio;
    @FXML private TableColumn<Evento, String> colFechaFin;
    @FXML private TableColumn<Evento, EstadoEvento> colEstado;
    @FXML private TableColumn<Evento, String> colResponsables;
    @FXML private TableColumn<Evento, Void> colAcciones;

    // Referencia al controlador del fragmento cargado (FeriaController, ConciertoController, etc.)
    private Object controladorFragmento;

    @FXML
    public void initialize() {
        // --- Ajuste responsivo de columnas ---
        tablaEventos.widthProperty().addListener((obs, oldW, newW) -> {
            double t = newW.doubleValue();
            colNombre.setPrefWidth(t * 0.20);
            colTipo.setPrefWidth(t * 0.10);
            colFechaInicio.setPrefWidth(t * 0.15);
            colEstado.setPrefWidth(t * 0.10);
            colFechaFin.setPrefWidth(t * 0.15);
            colResponsables.setPrefWidth(t * 0.20);
            colAcciones.setPrefWidth(t * 0.10);
        });

        // Mapeo columnas
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        colNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombre()));
        colTipo.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getTipoEvento()));
        colFechaInicio.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFechaInicio().format(fmt)));
        colFechaFin.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFechaFin().format(fmt)));
        colEstado.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getEstado()));
        colResponsables.setCellValueFactory(d ->
            new SimpleStringProperty(
                d.getValue().obtenerResponsables().stream()
                    .map(p -> p.getNombre() + " " + p.getApellido())
                    .collect(Collectors.joining(", "))
            )
        );

        // Spinners de hora (00:00 a 23:59 salteando de a 5)
        spinnerHoraInicio.setValueFactory(factoryHora());
        spinnerHoraFin.setValueFactory(factoryHora());

        // Combos de cabecera
        ComboBoxInicializador.cargarTipoEvento(comboTipoEvento);
        ComboBoxInicializador.cargarEstadoEvento(comboEstado);

        // Cambio de fragmento dinámico según tipo
        comboTipoEvento.valueProperty().addListener((obs, oldVal, newVal) -> cargarFragmentoEspecifico(newVal));

        // --- Botón "Asignar Rol" en cada fila ---
        agregarBotonAsignarRol();

        // --- Cargar datos iniciales ---
        refrescarDatos();
    }

    // ====== Helpers UI ======

    /** Fábrica de Spinner<LocalTime> con paso de 5 minutos. */
    private SpinnerValueFactory<LocalTime> factoryHora() {
        final LocalTime min = LocalTime.of(0, 0);
        final LocalTime max = LocalTime.of(23, 59);
        final int paso = 5;

        return new SpinnerValueFactory<>() {
            private LocalTime value = LocalTime.of(7, 0);
            {
                setConverter(new StringConverter<>() {
                    final DateTimeFormatter f = DateTimeFormatter.ofPattern("HH:mm");
                    @Override public String toString(LocalTime t) { return t != null ? t.format(f) : ""; }
                    @Override public LocalTime fromString(String s) { return LocalTime.parse(s, f); }
                });
                setValue(value);
            }
            @Override public void decrement(int steps) {
                LocalTime next = value.minusMinutes(steps * paso);
                if (!next.isBefore(min)) { value = next; setValue(value); }
            }
            @Override public void increment(int steps) {
                LocalTime next = value.plusMinutes(steps * paso);
                if (!next.isAfter(max)) { value = next; setValue(value); }
            }
        };
    }

    /** Inyecta el fragmento FXML del tipo de evento seleccionado. */
    private void cargarFragmentoEspecifico(TipoEvento tipo) {
        seccionDinamica.getChildren().clear();
        controladorFragmento = null;
        if (tipo == null) return;

        String ruta = switch (tipo) {
            case FERIA      -> "/fxml/abm/abmEventoResources/feria.fxml";
            case TALLER     -> "/fxml/abm/abmEventoResources/taller.fxml";
            case EXPOSICION -> "/fxml/abm/abmEventoResources/exposicion.fxml";
            case CONCIERTO  -> "/fxml/abm/abmEventoResources/concierto.fxml";
            case CICLO_CINE -> "/fxml/abm/abmEventoResources/cicloCine.fxml";
        };

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(ruta));
            Node nodo = loader.load();
            controladorFragmento = loader.getController();
            seccionDinamica.getChildren().add(nodo);
        } catch (IOException e) {
            e.printStackTrace();
            alerta("Error", "No se pudo cargar el formulario específico.");
        }
    }

    /** Agrega columna con botón "Asignar Rol" por cada fila. */
    private void agregarBotonAsignarRol() {
        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Asignar Rol");
            {
                btn.setOnAction(e -> {
                    Evento ev = getTableView().getItems().get(getIndex());
                    if (ev != null) abrirModalAsignacionRoles(ev);
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    /** Modal de asignación de roles para un evento. */
    private void abrirModalAsignacionRoles(Evento evento) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/abm/abmEventoResources/asignacionRoles.fxml"));
            VBox vista = loader.load();
            AsigRolEventoController ctrl = loader.getController();
            ctrl.setEvento(evento);

            Dialog<ButtonType> dlg = new Dialog<>();
            dlg.setTitle("Asignar Roles");
            dlg.getDialogPane().setContent(vista);
            dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            dlg.showAndWait();
        } catch (IOException ex) {
            ex.printStackTrace();
            alerta("Error", "No se pudo cargar la asignación de roles.");
        }
    }

    // ====== Modal principal ======

    @FXML private void mostrarModal() {
        limpiarModal();
        modalOverlay.setVisible(true);
        modalOverlay.toFront();
    }

    @FXML private void cerrarModal() {
        limpiarModal();
        modalOverlay.setVisible(false);
    }

    private void limpiarModal() {
        txtNombre.clear();
        dateInicio.setValue(null);
        dateFin.setValue(null);
        comboTipoEvento.getSelectionModel().clearSelection();
        comboEstado.getSelectionModel().clearSelection();
        spinnerHoraInicio.getValueFactory().setValue(LocalTime.of(7, 0));
        spinnerHoraFin.getValueFactory().setValue(LocalTime.of(7, 0));
        seccionDinamica.getChildren().clear();
        controladorFragmento = null;
    }

    // ====== Acciones ======

    /** Crea el evento según el tipo y refresca la tabla. */
    @FXML
    private void guardarEvento() {
        String nombre = txtNombre.getText();
        TipoEvento tipo = comboTipoEvento.getValue();
        LocalDate fIni = dateInicio.getValue();
        LocalDate fFin = dateFin.getValue();
        LocalTime hIni = spinnerHoraInicio.getValue();
        LocalTime hFin = spinnerHoraFin.getValue();
        EstadoEvento estado = comboEstado.getValue();

        try {
            if (tipo == null) throw new IllegalArgumentException("Debe seleccionar un tipo de evento.");
            if (fIni == null || fFin == null) throw new IllegalArgumentException("Debe seleccionar fechas.");
            if (hIni == null || hFin == null) throw new IllegalArgumentException("Debe seleccionar horas.");

            switch (tipo) {
                case FERIA -> {
                    FeriaController c = (FeriaController) controladorFragmento;
                    servicio.crearFeria(
                        nombre, fIni, fFin, hIni, hFin, estado,
                        c.getCantidadStands(),
                        c.getAmbienteSeleccionado()
                    );
                }
                case CONCIERTO -> {
                    ConciertoController c = (ConciertoController) controladorFragmento;
                    servicio.crearConcierto(
                        nombre, fIni, fFin, hIni, hFin, estado,
                        c.getTipoEntradaSeleccionada(),
                        c.getCupoMaximo()
                    );
                }
                case EXPOSICION -> {
                    ExposicionController c = (ExposicionController) controladorFragmento;
                    servicio.crearExposicion(
                        nombre, fIni, fFin, hIni, hFin, estado,
                        c.getTipoArteSeleccionado()
                    );
                }
                case TALLER -> {
                    TallerController c = (TallerController) controladorFragmento;
                    servicio.crearTaller(
                        nombre, fIni, fFin, hIni, hFin, estado,
                        c.getCupoMaximo(),
                        c.getModalidadSeleccionada()
                    );
                }
                case CICLO_CINE -> {
                    CicloCineController c = (CicloCineController) controladorFragmento;
                    // 🔴 Aquí usamos el tipo 2D/3D elegido en el fragmento
                    TipoPelicula tipoPeliculas = c.getTipoPeliculasSeleccionado();
                    servicio.crearCicloCine(
                        nombre, fIni, fFin, hIni, hFin, estado,
                        c.isPostCharla(),
                        c.getCupoMaximo(),
                        c.getPeliculasTexto(),   // títulos uno por línea
                        tipoPeliculas
                    );
                }
            }

            refrescarDatos();
            cerrarModal();

        } catch (IllegalArgumentException | IllegalStateException ex) {
            alerta("Error de validación", ex.getMessage());
        }
    }

    /** (Placeholder) Abre modal para editar. Deberías mapear datos del evento → UI. */
    @FXML
    private void modificarEvento() {
        Evento ev = tablaEventos.getSelectionModel().getSelectedItem();
        if (ev == null) {
            alerta("Selección requerida", "Debe seleccionar un evento en la tabla para modificar.");
            return;
        }
        // TODO: cargar datos del evento en el modal para edición
        mostrarModal();
    }

    // ====== Util ======

    private void alerta(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    /** Refresca la tabla desde el servicio. (lo usa MainController al navegar) */
    public void refrescarDatos() {
        tablaEventos.setItems(FXCollections.observableArrayList(servicio.listarEventos()));
        tablaEventos.refresh();
    }
}
