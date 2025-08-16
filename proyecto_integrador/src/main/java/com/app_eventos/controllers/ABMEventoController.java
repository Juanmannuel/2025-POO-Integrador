package com.app_eventos.controllers;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
// <<< NUEVO
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;

// Dominio
import com.app_eventos.model.*;
import com.app_eventos.model.enums.*;
import com.app_eventos.services.Servicio;
// Utils
import com.app_eventos.utils.ComboBoxInicializador;

public class ABMEventoController {

    private Servicio servicio = Servicio.getInstance();

    // ----------- UI (modal) -----------
    @FXML private TextField txtNombre;
    @FXML private DatePicker dateInicio;
    @FXML private DatePicker dateFin;
    @FXML private StackPane modalOverlay;
    @FXML private ComboBox<TipoEvento> comboTipoEvento;
    @FXML private ComboBox<EstadoEvento> comboEstado;
    @FXML private Spinner<LocalTime> spinnerHoraInicio;
    @FXML private Spinner<LocalTime> spinnerHoraFin;
    @FXML private Pane seccionDinamica;
    @FXML private TableColumn<Evento, Void> colAcciones;

    // ----------- Tabla -----------
    @FXML private TableView<Evento> tablaEventos;
    @FXML private TableColumn<Evento, String> colNombre;
    @FXML private TableColumn<Evento, TipoEvento> colTipo;
    @FXML private TableColumn<Evento, String> colFechaInicio;
    @FXML private TableColumn<Evento, String> colFechaFin;
    @FXML private TableColumn<Evento, EstadoEvento> colEstado;
    @FXML private TableColumn<Evento, String> colResponsables;

    // ----------- Filtros -----------
    @FXML private ComboBox<TipoEvento> comboTipoEventoFiltro;
    private ComboBox<EstadoEvento> comboEstadoFiltro;          
    private DatePicker dateDesdeFiltro;                        
    private DatePicker dateHastaFiltro;                        

    // ----------- Estado interno -----------
    private final ObservableList<Evento> modeloTabla = FXCollections.observableArrayList();
    private Object controladorFragmento;
    private Evento eventoEnEdicion;
    private boolean modoEdicion;

    @FXML
    public void initialize() {
        tablaEventos.setItems(modeloTabla);
        tablaEventos.setPlaceholder(new Label("Sin eventos"));

        tablaEventos.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            double total = newWidth.doubleValue();
            colNombre.setPrefWidth(total * 0.20);
            colTipo.setPrefWidth(total * 0.10);
            colFechaInicio.setPrefWidth(total * 0.15);
            colEstado.setPrefWidth(total * 0.10);
            colFechaFin.setPrefWidth(total * 0.15);
            colResponsables.setPrefWidth(total * 0.20);
            colAcciones.setPrefWidth(total * 0.10);
        });

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        colNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombre()));
        colTipo.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getTipoEvento()));
        colFechaInicio.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFechaInicio().format(fmt)));
        colFechaFin.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFechaFin().format(fmt)));
        colEstado.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getEstado()));

        // Evita problemas si roles es lazy; ya viene con fetch en Servicio.listarEventos()
        colResponsables.setCellValueFactory(d -> {
            try {
                var roles = d.getValue().getRoles();
                String s = roles == null ? "" :
                        roles.stream()
                             .map(r -> r.getPersona().getNombre()+" "+r.getPersona().getApellido()+" ("+r.getRol().name()+")")
                             .distinct()
                             .reduce((a,b)->a+", "+b)
                             .orElse("");
                return new SimpleStringProperty(s);
            } catch (Exception ex) {
                return new SimpleStringProperty("");
            }
        });

        spinnerHoraInicio.setValueFactory(factoryHora());
        spinnerHoraFin.setValueFactory(factoryHora());
        ComboBoxInicializador.cargarTipoEvento(comboTipoEvento);
        ComboBoxInicializador.cargarEstadoEvento(comboEstado);
        comboTipoEvento.valueProperty().addListener((o, a, b) -> cargarFragmentoEspecifico(b));

        agregarBotonAsignarRol();
        detectarFiltrosDeFormaSegura();

        buscarYRefrescarTabla();
    }

    @SuppressWarnings("unchecked")
    private void detectarFiltrosDeFormaSegura() {
        if (!(tablaEventos.getParent() instanceof VBox panel)) return;
        if (panel.getChildren().isEmpty()) return;
        if (!(panel.getChildren().get(0) instanceof HBox hbox)) return;

        DatePicker d1 = null, d2 = null;
        ComboBox<EstadoEvento> estadoCb = null;
        for (Node n : hbox.getChildren()) {
            if (estadoCb == null && n instanceof ComboBox<?> cb) {
                if (cb != comboTipoEventoFiltro && (cb.getItems().isEmpty() || cb.getItems().get(0) instanceof EstadoEvento)) {
                    estadoCb = (ComboBox<EstadoEvento>) cb;
                }
            }
            if (n instanceof DatePicker dp) {
                if (d1 == null) d1 = dp; else if (d2 == null) d2 = dp;
            }
        }
        comboEstadoFiltro = estadoCb;
        dateDesdeFiltro = d1;
        dateHastaFiltro = d2;

        if (comboTipoEventoFiltro != null) {
            comboTipoEventoFiltro.setItems(FXCollections.observableArrayList(TipoEvento.values()));
            comboTipoEventoFiltro.getSelectionModel().clearSelection();
            comboTipoEventoFiltro.valueProperty().addListener((o,a,b)->buscarYRefrescarTabla());
        }
        if (comboEstadoFiltro != null) {
            comboEstadoFiltro.setItems(FXCollections.observableArrayList(EstadoEvento.values()));
            comboEstadoFiltro.getSelectionModel().clearSelection();
            comboEstadoFiltro.valueProperty().addListener((o,a,b)->buscarYRefrescarTabla());
        }
        if (dateDesdeFiltro != null)  dateDesdeFiltro.valueProperty().addListener((o,a,b)->buscarYRefrescarTabla());
        if (dateHastaFiltro != null)  dateHastaFiltro.valueProperty().addListener((o,a,b)->buscarYRefrescarTabla());
    }

    private void buscarYRefrescarTabla() {
        try {
            servicio.verificarEstadosEventos();
        } catch (RuntimeException ex) { // <<< NUEVO: por si algo falla en verificación automática
            // No detenemos la UI; solo informamos
            mostrarAlerta("Aviso", "No se pudieron verificar algunos estados automáticamente: " + ex.getMessage());
        }

        LocalDate desde = (dateDesdeFiltro != null) ? dateDesdeFiltro.getValue() : null;
        LocalDate hasta = (dateHastaFiltro != null) ? dateHastaFiltro.getValue() : null;
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            mostrarAlerta("Rango inválido", "Fecha desde mayor que fecha hasta");
            return;
        }
        TipoEvento tipo = (comboTipoEventoFiltro != null) ? comboTipoEventoFiltro.getValue() : null;
        EstadoEvento estado = (comboEstadoFiltro != null) ? comboEstadoFiltro.getValue() : null;

        var resultados = servicio.buscarEventos(tipo, estado, desde, hasta);
        modeloTabla.setAll(resultados);
        tablaEventos.refresh();
    }

    private SpinnerValueFactory<LocalTime> factoryHora() {
        final LocalTime min  = LocalTime.of(6, 0);
        final LocalTime max  = LocalTime.of(23, 59);
        final int paso = 5;

        return new SpinnerValueFactory<>() {
            private final DateTimeFormatter f = DateTimeFormatter.ofPattern("HH:mm");
            {
                setConverter(new StringConverter<>() {
                    @Override public String toString(LocalTime t) { return t == null ? "" : f.format(t); }
                    @Override public LocalTime fromString(String s) {
                        if (s == null) return null;
                        s = s.trim();
                        if (s.isEmpty()) return null;
                        try {
                            LocalTime t = LocalTime.parse(s, f);
                            if (t.isBefore(min)) t = min;
                            if (t.isAfter(max))  t = max;
                            return t;
                        } catch (Exception e) {
                            // Entrada inválida: no cambiar el valor actual
                            return getValue();
                        }
                    }
                });
                setValue(null); // empieza vacío
            }

            @Override public void decrement(int steps) {
                LocalTime v = getValue();
                if (v == null) {
                    setValue(max); // primer decremento -> max
                } else {
                    LocalTime n = v.minusMinutes(steps * paso);
                    if (n.isBefore(min)) n = min;
                    setValue(n);
                }
            }

            @Override public void increment(int steps) {
                LocalTime v = getValue();
                if (v == null) {
                    setValue(min); // primer incremento -> min
                } else {
                    LocalTime n = v.plusMinutes(steps * paso);
                    if (n.isAfter(max)) n = max;
                    setValue(n);
                }
            }
        };
    }

    private void agregarBotonAsignarRol() {
        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Asignar Rol");
            {
                btn.setOnAction(e -> {
                    Evento ev = getTableView().getItems().get(getIndex());
                    if (ev != null) abrirModalAsignacionRoles(ev);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                Evento ev = getTableView().getItems().get(getIndex());
                boolean habilitado = ev != null
                        && ev.getEstado() != EstadoEvento.EJECUCIÓN
                        && ev.getEstado() != EstadoEvento.FINALIZADO;

                btn.setDisable(!habilitado);
                setGraphic(btn);
            }
        });
    }

        private void abrirModalAsignacionRoles(Evento evento) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/abm/abmEventoResources/asignacionRoles.fxml"));
            VBox vista = loader.load();
            AsigRolEventoController ctrl = loader.getController();
            ctrl.setEvento(evento);
            ctrl.setOnRolesChanged(ev -> buscarYRefrescarTabla());

            Dialog<ButtonType> dlg = new Dialog<>();
            dlg.setTitle("Asignar Roles");
            dlg.getDialogPane().setContent(vista);
            dlg.getDialogPane().getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
            dlg.setResizable(true);

            dlg.setOnHidden(e -> buscarYRefrescarTabla());
            dlg.showAndWait();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void cargarFragmentoEspecifico(TipoEvento tipo) {
        seccionDinamica.getChildren().clear();
        controladorFragmento = null;
        if (tipo == null) return;
        String fxml = switch (tipo) {
            case FERIA      -> "/fxml/abm/abmEventoResources/feria.fxml";
            case TALLER     -> "/fxml/abm/abmEventoResources/taller.fxml";
            case EXPOSICION -> "/fxml/abm/abmEventoResources/exposicion.fxml";
            case CONCIERTO  -> "/fxml/abm/abmEventoResources/concierto.fxml";
            case CICLO_CINE -> "/fxml/abm/abmEventoResources/cicloCine.fxml";
        };
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Node n = loader.load();
            controladorFragmento = loader.getController();
            seccionDinamica.getChildren().add(n);
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cargar el formulario específico.");
        }
    }

    @FXML private void mostrarModal() {
        modoEdicion = false; eventoEnEdicion = null; comboTipoEvento.setDisable(false);
        limpiarFormulario(); setEstadosParaAlta(); modalOverlay.setVisible(true);
    }

    @FXML private void cerrarModal() {
        modalOverlay.setVisible(false); modoEdicion = false; eventoEnEdicion = null;
        comboTipoEvento.setDisable(false); limpiarFormulario(); controladorFragmento = null;
    }

    private void limpiarFormulario() {
        txtNombre.clear(); dateInicio.setValue(null); dateFin.setValue(null);
        comboTipoEvento.getSelectionModel().clearSelection();
        comboEstado.getSelectionModel().clearSelection();
        seccionDinamica.getChildren().clear();
        if (spinnerHoraInicio.getValueFactory()!=null) spinnerHoraInicio.getValueFactory().setValue(null);
        if (spinnerHoraFin.getValueFactory()!=null)    spinnerHoraFin.getValueFactory().setValue(null);
    }

    private static EnumSet<EstadoEvento> ESTADOS_ALTA =
            EnumSet.of(EstadoEvento.PLANIFICACIÓN, EstadoEvento.CONFIRMADO);

    private void setEstadosParaAlta() {
        comboEstado.setItems(FXCollections.observableArrayList(ESTADOS_ALTA));
        comboEstado.getSelectionModel().select(EstadoEvento.PLANIFICACIÓN);
    }

    private void setEstadosParaEdicion() {
        EstadoEvento actual = (eventoEnEdicion != null) ? eventoEnEdicion.getEstado() : EstadoEvento.PLANIFICACIÓN;
        if (actual == EstadoEvento.PLANIFICACIÓN) {
            comboEstado.setItems(FXCollections.observableArrayList(EstadoEvento.PLANIFICACIÓN, EstadoEvento.CONFIRMADO));
            comboEstado.setDisable(false);
        } else {
            comboEstado.setItems(FXCollections.observableArrayList(actual)); // solo el actual
            comboEstado.getSelectionModel().select(actual);
            comboEstado.setDisable(true); // bloquea cambio manual
        }
    }

    @FXML
    private void guardarEvento() {
        try {
            TipoEvento tipo = modoEdicion ? eventoEnEdicion.getTipoEvento() : comboTipoEvento.getValue();

            if (!modoEdicion && tipo == null) {
                throw new IllegalArgumentException("Seleccione un tipo de evento.");
            }

            if (!modoEdicion) {
                crearSegunTipo(
                    txtNombre.getText(), tipo,
                    dateInicio.getValue(), dateFin.getValue(),
                    spinnerHoraInicio.getValue(), spinnerHoraFin.getValue(),
                    comboEstado.getValue()
                );
            } else {
                actualizarSegunTipo(
                    eventoEnEdicion, txtNombre.getText(), tipo,
                    dateInicio.getValue(), dateFin.getValue(),
                    spinnerHoraInicio.getValue(), spinnerHoraFin.getValue(),
                    comboEstado.getValue()
                );
            }

            buscarYRefrescarTabla();
            cerrarModal();

        } catch (IllegalArgumentException | IllegalStateException ex) { // <<< mantiene captura de validaciones de dominio
            mostrarAlerta("Error de validación", ex.getMessage());
        }
    }

    private void crearSegunTipo(String nombre, TipoEvento tipo,
                LocalDate fIni, LocalDate fFin, LocalTime hIni, LocalTime hFin, EstadoEvento estado) {
        if (tipo == null) throw new IllegalArgumentException("Seleccione un tipo de evento.");
        if (controladorFragmento == null)
            throw new IllegalStateException("Complete los datos específicos del tipo seleccionado.");

        switch (tipo) {
            case FERIA -> {
                if (!(controladorFragmento instanceof FeriaController c))
                    throw new IllegalStateException("Formulario de Feria no cargado.");
                servicio.crearFeria(nombre, fIni, fFin, hIni, hFin, estado, c.getCantidadStands(), c.getAmbienteSeleccionado());
            }
            case CONCIERTO -> {
                if (!(controladorFragmento instanceof ConciertoController c))
                    throw new IllegalStateException("Formulario de Concierto no cargado.");
                servicio.crearConcierto(nombre, fIni, fFin, hIni, hFin, estado, c.getTipoEntradaSeleccionada(), c.getCupoMaximo());
            }
            case EXPOSICION -> {
                if (!(controladorFragmento instanceof ExposicionController c))
                    throw new IllegalStateException("Formulario de Exposición no cargado.");
                servicio.crearExposicion(nombre, fIni, fFin, hIni, hFin, estado, c.getTipoArteSeleccionado());
            }
            case TALLER -> {
                if (!(controladorFragmento instanceof TallerController c))
                    throw new IllegalStateException("Formulario de Taller no cargado.");
                servicio.crearTaller(nombre, fIni, fFin, hIni, hFin, estado, c.getCupoMaximo(), c.getModalidadSeleccionada());
            }
            case CICLO_CINE -> {
                if (!(controladorFragmento instanceof CicloCineController c))
                    throw new IllegalStateException("Formulario de Ciclo de Cine no cargado.");
                servicio.crearCicloCine(nombre, fIni, fFin, hIni, hFin, estado, c.isPostCharla(), c.getCupoMaximo(), c.getPeliculasSeleccionadas());
            }
        }
    }

    private void actualizarSegunTipo(Evento original, String nombre, TipoEvento tipo,
                                     LocalDate fIni, LocalDate fFin, LocalTime hIni, LocalTime hFin, EstadoEvento estado) {
        switch (tipo) {
            case FERIA -> {
                if (controladorFragmento instanceof FeriaController c && original instanceof Feria f)
                    servicio.actualizarFeria(f, nombre, fIni, fFin, hIni, hFin, estado, c.getCantidadStands(), c.getAmbienteSeleccionado());
            }
            case CONCIERTO -> {
                if (controladorFragmento instanceof ConciertoController c && original instanceof Concierto x)
                    servicio.actualizarConcierto(x, nombre, fIni, fFin, hIni, hFin, estado, c.getTipoEntradaSeleccionada(), c.getCupoMaximo());
            }
            case EXPOSICION -> {
                if (controladorFragmento instanceof ExposicionController c && original instanceof Exposicion x)
                    servicio.actualizarExposicion(x, nombre, fIni, fFin, hIni, hFin, estado, c.getTipoArteSeleccionado());
            }
            case TALLER -> {
                if (controladorFragmento instanceof TallerController c && original instanceof Taller x)
                    servicio.actualizarTaller(x, nombre, fIni, fFin, hIni, hFin, estado, c.getCupoMaximo(), c.getModalidadSeleccionada());
            }
            case CICLO_CINE -> {
                if (controladorFragmento instanceof CicloCineController c && original instanceof CicloCine x)
                    servicio.actualizarCicloCine(x, nombre, fIni, fFin, hIni, hFin, estado, c.isPostCharla(), c.getCupoMaximo(), c.getPeliculasSeleccionadas());
            }
        }
    }

    @FXML
    private void modificarEvento() {
        Evento e = tablaEventos.getSelectionModel().getSelectedItem();
        if (e == null) { mostrarAlerta("Selección requerida", "Seleccione un evento."); return; }
        modoEdicion = true; eventoEnEdicion = e; comboTipoEvento.setDisable(true);
        setEstadosParaEdicion();

        txtNombre.setText(e.getNombre());
        dateInicio.setValue(e.getFechaInicio().toLocalDate());
        dateFin.setValue(e.getFechaFin().toLocalDate());
        spinnerHoraInicio.getValueFactory().setValue(e.getFechaInicio().toLocalTime());
        spinnerHoraFin.getValueFactory().setValue(e.getFechaFin().toLocalTime());
        comboEstado.getSelectionModel().select(e.getEstado());

        comboTipoEvento.getSelectionModel().select(e.getTipoEvento());
        cargarFragmentoEspecifico(e.getTipoEvento());
        switch (e.getTipoEvento()) {
            case FERIA -> { if (controladorFragmento instanceof FeriaController c && e instanceof Feria f) c.setValores(f.getCantidadStands(), f.getAmbiente()); }
            case CONCIERTO -> { if (controladorFragmento instanceof ConciertoController c && e instanceof Concierto x) c.setValores(x.getTipoEntrada(), x.getCupoMaximo()); }
            case EXPOSICION -> { if (controladorFragmento instanceof ExposicionController c && e instanceof Exposicion x) c.setValores(x.getTipoArte()); }
            case TALLER -> { if (controladorFragmento instanceof TallerController c && e instanceof Taller x) c.setValores(x.getCupoMaximo(), x.getModalidad()); }
            case CICLO_CINE -> { if (controladorFragmento instanceof CicloCineController c && e instanceof CicloCine x) {
                c.setCupoMaximo(x.getCupoMaximo()); c.setPostCharla(x.isPostCharla()); c.preseleccionarPeliculas(x.getPeliculas()); } }
        }
        modalOverlay.setVisible(true); modalOverlay.toFront();
    }

    @FXML
    private void eliminarEvento() {
        Evento sel = tablaEventos.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAlerta("Selección requerida", "Seleccione un evento."); return; }
        Alert c = new Alert(Alert.AlertType.CONFIRMATION, "¿Eliminar \""+sel.getNombre()+"\"?", ButtonType.OK, ButtonType.CANCEL);
        c.setHeaderText(null); c.setTitle("Confirmar eliminación");
        c.showAndWait().ifPresent(b -> {
            if (b == ButtonType.OK) {
                try { servicio.eliminarEvento(sel); buscarYRefrescarTabla(); tablaEventos.getSelectionModel().clearSelection(); }
                catch (RuntimeException ex){ mostrarAlerta("No se pudo eliminar", ex.getMessage()); }
            }
        });
    }

    private void mostrarAlerta(String t, String m) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(t); a.setHeaderText(null); a.setContentText(m); a.showAndWait();
    }
}