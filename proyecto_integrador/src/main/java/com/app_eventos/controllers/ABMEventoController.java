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
// Fragmentos
import com.app_eventos.controllers.*;

public class ABMEventoController {

    private final Servicio servicio = Servicio.getInstance();

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

    // ----------- Filtros (solo uno tiene fx:id en tu FXML) -----------
    @FXML private ComboBox<TipoEvento> comboTipoEventoFiltro; // OK en FXML
    private ComboBox<EstadoEvento> comboEstadoFiltro;          // lo detectamos por tipo
    private DatePicker dateDesdeFiltro;                        // lo detectamos por tipo
    private DatePicker dateHastaFiltro;                        // lo detectamos por tipo

    // ----------- Estado interno -----------
    private final ObservableList<Evento> modeloTabla = FXCollections.observableArrayList();
    private Object controladorFragmento;
    private Evento eventoEnEdicion;
    private boolean modoEdicion;

    @FXML
    public void initialize() {
        // 1) La tabla SIEMPRE usa el mismo modelo
        tablaEventos.setItems(modeloTabla);
        tablaEventos.setPlaceholder(new Label("Sin eventos"));

        // --- Tabla responsiva
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

        // 2) Columnas
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        colNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombre()));
        colTipo.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getTipoEvento()));
        colFechaInicio.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFechaInicio().format(fmt)));
        colFechaFin.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFechaFin().format(fmt)));
        colEstado.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getEstado()));

        // >>> Blindado contra lazy (aunque ya traemos con JOIN FETCH)
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

        // 3) Spinners + combos del modal
        spinnerHoraInicio.setValueFactory(factoryHora());
        spinnerHoraFin.setValueFactory(factoryHora());
        ComboBoxInicializador.cargarTipoEvento(comboTipoEvento);
        ComboBoxInicializador.cargarEstadoEvento(comboEstado);
        comboTipoEvento.valueProperty().addListener((o, a, b) -> cargarFragmentoEspecifico(b));

        // 4) Botón "Asignar rol" por fila
        agregarBotonAsignarRol();

        // 5) Filtros seguros (NO asumo índices; si falta alguno, no rompo)
        detectarFiltrosDeFormaSegura();

        // 6) Primer carga desde BD
        buscarYRefrescarTabla();
    }

    // ----------------- filtros seguros -----------------
    @SuppressWarnings("unchecked")
    private void detectarFiltrosDeFormaSegura() {
        try {
            // Contenedor padre: VBox(panel) -> [0]=HBox filtros, [1]=TableView
            if (!(tablaEventos.getParent() instanceof VBox panel)) return;
            if (panel.getChildren().isEmpty()) return;
            if (!(panel.getChildren().get(0) instanceof HBox hbox)) return;

            // Recorro hijos y agarro el primer ComboBox<EstadoEvento> y los 2 DatePicker
            DatePicker d1 = null, d2 = null;
            ComboBox<EstadoEvento> estadoCb = null;
            for (Node n : hbox.getChildren()) {
                if (estadoCb == null && n instanceof ComboBox<?> cb) {
                    // saltar el combo de tipo (ya lo tenemos por fx:id)
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

            // Configuración de opciones
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
        } catch (Exception e) {
            // Si algo falla, no frenamos la vista: simplemente se listará sin filtros.
            System.err.println("[ABMEvento] Advertencia: no se pudieron inicializar los filtros: " + e.getMessage());
        }
    }

    // ----------------- carga/refresh -----------------
    private void buscarYRefrescarTabla() {
        // Actualiza estados automáticos y persiste si cambian
        servicio.verificarEstadosEventos();

        LocalDate desde = (dateDesdeFiltro != null) ? dateDesdeFiltro.getValue() : null;
        LocalDate hasta = (dateHastaFiltro != null) ? dateHastaFiltro.getValue() : null;
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            mostrarAlerta("Rango inválido", "Fecha desde mayor que fecha hasta");
            return;
        }
        TipoEvento tipo = (comboTipoEventoFiltro != null) ? comboTipoEventoFiltro.getValue() : null;
        EstadoEvento estado = (comboEstadoFiltro != null) ? comboEstadoFiltro.getValue() : null;

        var resultados = servicio.buscarEventos(tipo, estado, desde, hasta);
        modeloTabla.setAll(resultados);   // importante mantener el mismo ObservableList
        tablaEventos.refresh();
    }

    // ----------------- helpers UI/modal -----------------
    private SpinnerValueFactory<LocalTime> factoryHora() {
        LocalTime min = LocalTime.of(6,0), max = LocalTime.of(23,59);
        int paso = 5;
        return new SpinnerValueFactory<>() {
            private LocalTime value = min;
            { setConverter(new StringConverter<>() {
                DateTimeFormatter f = DateTimeFormatter.ofPattern("HH:mm");
                public String toString(LocalTime t){ return t==null?"":t.format(f); }
                public LocalTime fromString(String s){ return LocalTime.parse(s,f); }
            }); setValue(value); }
            public void decrement(int steps){ LocalTime n=value.minusMinutes(steps*paso); if(!n.isBefore(min)){ value=n; setValue(value);} }
            public void increment(int steps){ LocalTime n=value.plusMinutes(steps*paso); if(!n.isAfter(max)){ value=n; setValue(value);} }
        };
    }

    private void agregarBotonAsignarRol() {
        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Asignar Rol");
            { btn.setOnAction(e -> {
                Evento ev = getTableView().getItems().get(getIndex());
                if (ev != null) abrirModalAsignacionRoles(ev);
            });}
            protected void updateItem(Void it, boolean empty){ super.updateItem(it, empty); setGraphic(empty?null:btn); }
        });
    }

    private void abrirModalAsignacionRoles(Evento evento) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/abm/abmEventoResources/asignacionRoles.fxml"));
            VBox vista = loader.load();
            AsigRolEventoController ctrl = loader.getController();
            ctrl.setEvento(evento);
            ctrl.setOnRolesChanged(ev -> tablaEventos.refresh());

            Dialog<ButtonType> dlg = new Dialog<>();
            dlg.setTitle("Asignar Roles");
            dlg.getDialogPane().setContent(vista);
            dlg.getDialogPane().getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
            dlg.setResizable(true);
            ((Button) dlg.getDialogPane().lookupButton(ButtonType.OK))
                    .addEventFilter(javafx.event.ActionEvent.ACTION, a -> {
                        try { evento.validarInvariantes(); }
                        catch (IllegalStateException ex){ a.consume(); mostrarAlerta("Validación", ex.getMessage()); }
                    });
            dlg.setOnHidden(e -> tablaEventos.refresh());
            dlg.showAndWait();
        } catch (IOException ex) {
            ex.printStackTrace();
            mostrarAlerta("Error", "No se pudo cargar la ventana de roles.");
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
        if (spinnerHoraInicio.getValueFactory()!=null) spinnerHoraInicio.getValueFactory().setValue(LocalTime.of(0,0));
        if (spinnerHoraFin.getValueFactory()!=null)    spinnerHoraFin.getValueFactory().setValue(LocalTime.of(0,0));
    }

    private static final EnumSet<EstadoEvento> ESTADOS_ALTA =
            EnumSet.of(EstadoEvento.PLANIFICACIÓN, EstadoEvento.CONFIRMADO, EstadoEvento.EJECUCIÓN);

    private void setEstadosParaAlta() {
        comboEstado.setItems(FXCollections.observableArrayList(ESTADOS_ALTA));
        comboEstado.getSelectionModel().select(EstadoEvento.PLANIFICACIÓN);
    }

    private void setEstadosParaEdicion() {
        comboEstado.setItems(FXCollections.observableArrayList(EstadoEvento.values()));
    }

    @FXML
    private void guardarEvento() {
        String nombre = txtNombre.getText();
        TipoEvento tipo = modoEdicion ? eventoEnEdicion.getTipoEvento(): comboTipoEvento.getValue();
        LocalDate fIni = dateInicio.getValue();
        LocalDate fFin = dateFin.getValue();
        LocalTime hIni = spinnerHoraInicio.getValue();
        LocalTime hFin = spinnerHoraFin.getValue();
        EstadoEvento estado = comboEstado.getValue();

        try {
            if (!modoEdicion) crearSegunTipo(nombre, tipo, fIni, fFin, hIni, hFin, estado);
            else              actualizarSegunTipo(eventoEnEdicion, nombre, tipo, fIni, fFin, hIni, hFin, estado);

            buscarYRefrescarTabla(); // RELEER BD
            cerrarModal();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            mostrarAlerta("Error de validación", ex.getMessage());
        }
    }

    private void crearSegunTipo(String nombre, TipoEvento tipo,
                                LocalDate fIni, LocalDate fFin, LocalTime hIni, LocalTime hFin, EstadoEvento estado) {
        if (tipo == TipoEvento.FERIA && controladorFragmento instanceof FeriaController c)
            servicio.crearFeria(nombre, fIni, fFin, hIni, hFin, estado, c.getCantidadStands(), c.getAmbienteSeleccionado());
        else if (tipo == TipoEvento.CONCIERTO && controladorFragmento instanceof ConciertoController c)
            servicio.crearConcierto(nombre, fIni, fFin, hIni, hFin, estado, c.getTipoEntradaSeleccionada(), c.getCupoMaximo());
        else if (tipo == TipoEvento.EXPOSICION && controladorFragmento instanceof ExposicionController c)
            servicio.crearExposicion(nombre, fIni, fFin, hIni, hFin, estado, c.getTipoArteSeleccionado());
        else if (tipo == TipoEvento.TALLER && controladorFragmento instanceof TallerController c)
            servicio.crearTaller(nombre, fIni, fFin, hIni, hFin, estado, c.getCupoMaximo(), c.getModalidadSeleccionada());
        else if (tipo == TipoEvento.CICLO_CINE && controladorFragmento instanceof CicloCineController c)
            servicio.crearCicloCine(nombre, fIni, fFin, hIni, hFin, estado, c.isPostCharla(), c.getCupoMaximo(), c.getPeliculasSeleccionadas());
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
        Alert a = new Alert(Alert.AlertType.WARNING); a.setTitle(t); a.setHeaderText(null); a.setContentText(m); a.showAndWait();
    }
}
