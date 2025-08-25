package com.app_eventos.controllers;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

// Dominio
import com.app_eventos.model.*;
import com.app_eventos.model.enums.*;
import com.app_eventos.services.Servicio;
// Utils
import com.app_eventos.utils.ComboBoxInicializador;
import com.app_eventos.utils.TimePicker;

public class ABMEventoController {

    private Servicio servicio = Servicio.getInstance();

    @FXML private TextField txtNombre;
    @FXML private DatePicker dateInicio;
    @FXML private DatePicker dateFin;
    @FXML private StackPane modalOverlay;
    @FXML private ComboBox<TipoEvento> comboTipoEvento;
    @FXML private ComboBox<EstadoEvento> comboEstado;
    @FXML private TimePicker timePickerInicio;
    @FXML private TimePicker timePickerFin;
    @FXML private Pane seccionDinamica;
    @FXML private TableColumn<Evento, Void> colAcciones;

    // Tabla 
    @FXML private TableView<Evento> tablaEventos;
    @FXML private TableColumn<Evento, String> colNombre;
    @FXML private TableColumn<Evento, TipoEvento> colTipo;
    @FXML private TableColumn<Evento, String> colFechaInicio;
    @FXML private TableColumn<Evento, String> colFechaFin;
    @FXML private TableColumn<Evento, EstadoEvento> colEstado;
    @FXML private TableColumn<Evento, String> colResponsables;

    // Filtros
    @FXML private ComboBox<TipoEvento> comboTipoEventoFiltro;
    private ComboBox<EstadoEvento> comboEstadoFiltro;
    private DatePicker dateDesdeFiltro;
    private DatePicker dateHastaFiltro;

    // Estado interno
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

        // Configurar valores por defecto para los TimePicker
        timePickerInicio.setValue(LocalTime.of(9, 0)); // 9:00 por defecto
        timePickerFin.setValue(LocalTime.of(10, 0));   // 10:00 por defecto
        ComboBoxInicializador.cargarTipoEvento(comboTipoEvento);
        ComboBoxInicializador.cargarEstadoEvento(comboEstado);
        comboTipoEvento.valueProperty().addListener((o, a, b) -> cargarFragmentoEspecifico(b));

        agregarBotonAsignarRol();
        detectarFiltros();

        buscarYRefrescarTabla();
    }

    @SuppressWarnings("unchecked")
    private void detectarFiltros() {
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

        servicio.verificarEstadosEventos();
   
        LocalDate desde = (dateDesdeFiltro != null) ? dateDesdeFiltro.getValue() : null;
        LocalDate hasta = (dateHastaFiltro != null) ? dateHastaFiltro.getValue() : null;
        
        TipoEvento tipo = (comboTipoEventoFiltro != null) ? comboTipoEventoFiltro.getValue() : null;
        EstadoEvento estado = (comboEstadoFiltro != null) ? comboEstadoFiltro.getValue() : null;

        var resultados = servicio.buscarEventos(tipo, estado, desde, hasta);
        modeloTabla.setAll(resultados);
        tablaEventos.refresh();
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
        timePickerInicio.setValue(LocalTime.of(9, 0)); // Reset a 9:00
        timePickerFin.setValue(LocalTime.of(10, 0));   // Reset a 10:00
    }

    private void setEstadosParaAlta() {
        comboEstado.setItems(FXCollections.observableArrayList(EstadoEvento.PLANIFICACIÓN));
        comboEstado.getSelectionModel().select(EstadoEvento.PLANIFICACIÓN);
        comboEstado.setDisable(true); // bloquea cambios en el alta
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
                    timePickerInicio.getValue(), timePickerFin.getValue(),
                    comboEstado.getValue()
                );
            } else {
                actualizarSegunTipo(
                    eventoEnEdicion, txtNombre.getText(), tipo,
                    dateInicio.getValue(), dateFin.getValue(),
                    timePickerInicio.getValue(), timePickerFin.getValue(),
                    comboEstado.getValue()
                );
            }

            buscarYRefrescarTabla();
            cerrarModal();

        } catch (IllegalArgumentException | IllegalStateException ex) { // mantiene captura de validaciones de dominio
            mostrarAlerta("Error de validación", ex.getMessage());
        }
    }

    private void crearSegunTipo(String nombre, TipoEvento tipo,
            LocalDate fIni, LocalDate fFin, LocalTime hIni, LocalTime hFin, EstadoEvento estado) {

        // no permitir fechas anteriores a hoy en ALTA
        Evento.validarFechasAlta(fIni, fFin);

        switch (tipo) {
            case FERIA -> {
                FeriaController c = (FeriaController) controladorFragmento;
                servicio.crearFeria(nombre, fIni, fFin, hIni, hFin, estado,
                        c.getCantidadStands(), c.getAmbienteSeleccionado());
            }
            case CONCIERTO -> {
                ConciertoController c = (ConciertoController) controladorFragmento;
                servicio.crearConcierto(nombre, fIni, fFin, hIni, hFin, estado,
                        c.getTipoEntradaSeleccionada(), c.getCupoMaximo());
            }
            case EXPOSICION -> {
                ExposicionController c = (ExposicionController) controladorFragmento;
                servicio.crearExposicion(nombre, fIni, fFin, hIni, hFin, estado,
                        c.getTipoArteSeleccionado());
            }
            case TALLER -> {
                TallerController c = (TallerController) controladorFragmento;
                servicio.crearTaller(nombre, fIni, fFin, hIni, hFin, estado,
                        c.getCupoMaximo(), c.getModalidadSeleccionada());
            }
            case CICLO_CINE -> {
                CicloCineController c = (CicloCineController) controladorFragmento;
                servicio.crearCicloCine(nombre, fIni, fFin, hIni, hFin, estado,
                        c.isPostCharla(), c.getCupoMaximo(), c.getPeliculasSeleccionadas());
            }
        }
    }


    private void actualizarSegunTipo(Evento original, String nombre, TipoEvento tipo,
                                     LocalDate fIni, LocalDate fFin, LocalTime hIni, LocalTime hFin, EstadoEvento estado) {

        // bloquear MODIFICACIONES si está en ejecución o finalizado
        original.validarPuedeModificar();

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
        if (e == null) { mostrarAlerta("Selección requerida", "Debe seleccionar un evento para modificar."); return; }
        modoEdicion = true; eventoEnEdicion = e; comboTipoEvento.setDisable(true);
        setEstadosParaEdicion();

        txtNombre.setText(e.getNombre());
        dateInicio.setValue(e.getFechaInicio().toLocalDate());
        dateFin.setValue(e.getFechaFin().toLocalDate());
        timePickerInicio.setValue(e.getFechaInicio().toLocalTime());
        timePickerFin.setValue(e.getFechaFin().toLocalTime());
        comboEstado.getSelectionModel().select(e.getEstado());

        comboTipoEvento.getSelectionModel().select(e.getTipoEvento());
        cargarFragmentoEspecifico(e.getTipoEvento());
        switch (e.getTipoEvento()) {
            case FERIA -> { if (controladorFragmento instanceof FeriaController c && e instanceof Feria f) c.setValores(f.getCantidadStands(), f.getAmbiente()); }
            case CONCIERTO -> { if (controladorFragmento instanceof ConciertoController c && e instanceof Concierto x) c.setValores(x.getTipoEntrada(), x.getCupoMaximo()); }
            case EXPOSICION -> { if (controladorFragmento instanceof ExposicionController c && e instanceof Exposicion x) c.setValores(x.getTipoArte()); }
            case TALLER -> { if (controladorFragmento instanceof TallerController c && e instanceof Taller x) c.setValores(x.getCupoMaximo(), x.getModalidad()); }
            case CICLO_CINE -> { if (controladorFragmento instanceof CicloCineController c && e instanceof CicloCine x) { CicloCine ccDet = servicio.obtenerPeliculas(x.getIdEvento());
                c.setCupoMaximo(x.getCupoMaximo());
                c.setPostCharla(x.isPostCharla());
                // Usar la colección inicializada
                c.preseleccionarPeliculas(ccDet.getPeliculas());}}
        }
        modalOverlay.setVisible(true); modalOverlay.toFront();
    }

    @FXML
    private void eliminarEvento() {
        Evento sel = tablaEventos.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAlerta("Selección requerida", "Debe seleccionar un evento para dar de baja."); return; }
        Alert c = new Alert(Alert.AlertType.CONFIRMATION, "¿Está seguro que desea eliminar \""+sel.getNombre()+"\"?", ButtonType.OK, ButtonType.CANCEL);
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
