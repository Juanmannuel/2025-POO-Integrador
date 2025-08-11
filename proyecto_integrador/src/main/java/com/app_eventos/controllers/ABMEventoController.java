package com.app_eventos.controllers;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

import com.app_eventos.model.CicloCine;
import com.app_eventos.model.Concierto;
import com.app_eventos.model.Evento;
import com.app_eventos.model.Exposicion;
import com.app_eventos.model.Feria;
import com.app_eventos.model.RolEvento;
import com.app_eventos.model.Taller;
import com.app_eventos.model.enums.EstadoEvento;
import com.app_eventos.model.enums.TipoEntrada;
import com.app_eventos.model.enums.TipoEvento;
import com.app_eventos.services.Servicio;
import com.app_eventos.utils.ComboBoxInicializador;
import javafx.collections.FXCollections;
import java.util.EnumSet;


import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
// MOD: import para acceder a los filtros por índice sin cambiar el FXML
import javafx.scene.layout.HBox;

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
    @FXML private ComboBox<TipoEntrada> comboTipoEntrada; // (si no se usa, lo podés quitar)
    @FXML private ComboBox<EstadoEvento> comboEstado;
    @FXML private Spinner<LocalTime> spinnerHoraInicio;
    @FXML private Spinner<LocalTime> spinnerHoraFin;
    @FXML private Pane seccionDinamica;
    @FXML private VBox contenedorAsignacionRoles;
    @FXML private TableColumn<Evento, Void> colAcciones;
    private Evento eventoEnEdicion = null;
    private boolean modoEdicion = false;
    

    @FXML private TableView<Evento> tablaEventos;
    @FXML private TableColumn<Evento, String> colNombre;
    @FXML private TableColumn<Evento, TipoEvento> colTipo;
    @FXML private TableColumn<Evento, String> colFechaInicio;
    @FXML private TableColumn<Evento, String> colFechaFin;
    @FXML private TableColumn<Evento, EstadoEvento> colEstado;
    @FXML private TableColumn<Evento, String> colResponsables;
    @FXML private AsigRolEventoController controladorAsignacionRoles;

    private Object controladorFragmento; // referencia al controller dinámico cargado

    // MOD: referencias a los filtros de la barra superior (solo uno tiene fx:id en tu FXML)
    @FXML private ComboBox<TipoEvento> comboTipoEventoFiltro; // existe en FXML
    // Los siguientes se obtienen por índice desde el HBox de filtros
    private ComboBox<EstadoEvento> comboEstadoFiltro; // MOD
    private DatePicker dateDesdeFiltro;               // MOD
    private DatePicker dateHastaFiltro;               // MOD

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

        // Mostrar responsables a partir de los roles del evento (evita método inexistente)
        colResponsables.setCellValueFactory(data -> {
            String nombres = data.getValue().getRoles().stream()
                    .map(RolEvento::getPersona)
                    .map(p -> p.getNombre() + " " + p.getApellido())
                    .collect(Collectors.joining(", "));
            return new SimpleStringProperty(nombres);
        });

        // Spinners de hora (00:00 a 23:59, salto 5 min)
        spinnerHoraInicio.setValueFactory(crearFactoryHora());
        spinnerHoraFin.setValueFactory(crearFactoryHora());

        // Combos
        ComboBoxInicializador.cargarTipoEvento(comboTipoEvento);
        ComboBoxInicializador.cargarEstadoEvento(comboEstado);

        // Cargar fragmento al cambiar tipo de evento
        comboTipoEvento.valueProperty().addListener((obs, oldVal, newVal) -> cargarFragmentoEspecifico(newVal));

        // Botón "Asignar Rol" en la tabla
        agregarBotonAsignarRol();

        // Cargar eventos en la tabla
        tablaEventos.setItems(FXCollections.observableArrayList(servicio.listarEventos()));

        // MOD: inicializar filtros del listado sin cambiar el FXML
        initFiltrosListado(); // MOD
        // MOD: carga inicial aplicando los filtros actuales
        buscarYRefrescarTabla(); // MOD
    }

    private void abrirModalAsignacionRoles(Evento evento) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/abm/abmEventoResources/asignacionRoles.fxml"));
            VBox vista = loader.load();
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
        LocalTime horaInicial = LocalTime.of(6, 0);
        LocalTime horaFinal = LocalTime.of(23, 59);
        int intervaloMinutos = 5;

        return new SpinnerValueFactory<>() {
            private LocalTime value = horaInicial;

            {
                setConverter(new StringConverter<>() {
                    final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                    @Override public String toString(LocalTime time) { return time != null ? time.format(formatter) : ""; }
                    @Override public LocalTime fromString(String s) { return LocalTime.parse(s, formatter); }
                });
                setValue(value);
            }

            @Override
            public void decrement(int steps) {
                LocalTime next = value.minusMinutes(steps * intervaloMinutos);
                if (!next.isBefore(horaInicial)) { value = next; setValue(value); }
            }

            @Override
            public void increment(int steps) {
                LocalTime next = value.plusMinutes(steps * intervaloMinutos);
                if (!next.isAfter(horaFinal)) { value = next; setValue(value); }
            }
        };
    }

    private static final EnumSet<EstadoEvento> ESTADOS_ALTA =
        EnumSet.of(EstadoEvento.PLANIFICACIÓN, EstadoEvento.CONFIRMADO, EstadoEvento.EJECUCIÓN);

    private void setEstadosParaAlta() {
        comboEstado.setItems(FXCollections.observableArrayList(ESTADOS_ALTA));
        comboEstado.getSelectionModel().select(EstadoEvento.PLANIFICACIÓN); // por defecto
    }

    private void setEstadosParaEdicion() {
        comboEstado.setItems(FXCollections.observableArrayList(EstadoEvento.values())); // todos
    }

    private void agregarBotonAsignarRol() {
        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Asignar Rol");
            {
                btn.setOnAction(e -> {
                    Evento eventoSeleccionado = getTableView().getItems().get(getIndex());
                    if (eventoSeleccionado != null) abrirModalAsignacionRoles(eventoSeleccionado);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    private void cargarFragmentoEspecifico(TipoEvento tipo) {
        seccionDinamica.getChildren().clear();
        controladorFragmento = null;

        if (tipo == null) return;

        String rutaFXML = switch (tipo) {
            case FERIA      -> "/fxml/abm/abmEventoResources/feria.fxml";
            case TALLER     -> "/fxml/abm/abmEventoResources/taller.fxml";
            case EXPOSICION -> "/fxml/abm/abmEventoResources/exposicion.fxml";
            case CONCIERTO  -> "/fxml/abm/abmEventoResources/concierto.fxml";
            case CICLO_CINE -> "/fxml/abm/abmEventoResources/cicloCine.fxml";
        };

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(rutaFXML));
            Node nodo = loader.load();
            controladorFragmento = loader.getController();
            seccionDinamica.getChildren().add(nodo);
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cargar el formulario específico.");
        }
    }

    private void limpiarFormularioBasico() {
        txtNombre.clear();
        dateInicio.setValue(null);
        dateFin.setValue(null);

        comboTipoEvento.getSelectionModel().clearSelection();
        comboEstado.getSelectionModel().clearSelection();

        seccionDinamica.getChildren().clear();

        // asegura que el Spinner tiene factory antes de setear valor
        if (spinnerHoraInicio.getValueFactory() != null) {
            spinnerHoraInicio.getValueFactory().setValue(LocalTime.of(0, 0));
        }
        if (spinnerHoraFin.getValueFactory() != null) {
            spinnerHoraFin.getValueFactory().setValue(LocalTime.of(0, 0));
        }
    }

    // -- Modal --
    @FXML
    private void mostrarModal() {
        modoEdicion = false;
        eventoEnEdicion = null;
        comboTipoEvento.setDisable(false);     // habilitado para alta
        limpiarFormularioBasico();
        setEstadosParaAlta();
        modalOverlay.setVisible(true);
    }

    @FXML
    private void cerrarModal() {
        modalOverlay.setVisible(false);
        modoEdicion = false;
        eventoEnEdicion = null;
        comboTipoEvento.setDisable(false);
        limpiarFormularioBasico();
        controladorFragmento = null;
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
            if (!modoEdicion) {
                // ALTA (tu código actual)
                crearSegunTipo(nombre, tipo, fIni, fFin, hIni, hFin, estado);
            } else {
                // EDICIÓN
                actualizarSegunTipo(eventoEnEdicion, nombre, tipo, fIni, fFin, hIni, hFin, estado);
            }

            tablaEventos.getItems().setAll(servicio.listarEventos());
            cerrarModal();

            // MOD: refresco con filtros activos en vez de setAll fijamente
            buscarYRefrescarTabla(); // MOD

        } catch (IllegalArgumentException | IllegalStateException ex) {
            mostrarAlerta("Error de validación", ex.getMessage());
        }
    }

    private void crearSegunTipo(String nombre, TipoEvento tipo,
                                LocalDate fIni, LocalDate fFin, LocalTime hIni, LocalTime hFin, EstadoEvento estado) {
        if (tipo == TipoEvento.FERIA && controladorFragmento instanceof FeriaController c) {
            servicio.crearFeria(nombre, fIni, fFin, hIni, hFin, estado, c.getCantidadStands(), c.getAmbienteSeleccionado());
        } else if (tipo == TipoEvento.CONCIERTO && controladorFragmento instanceof ConciertoController c) {
            servicio.crearConcierto(nombre, fIni, fFin, hIni, hFin, estado, c.getTipoEntradaSeleccionada(), c.getCupoMaximo());
        } else if (tipo == TipoEvento.EXPOSICION && controladorFragmento instanceof ExposicionController c) {
            servicio.crearExposicion(nombre, fIni, fFin, hIni, hFin, estado, c.getTipoArteSeleccionado());
        } else if (tipo == TipoEvento.TALLER && controladorFragmento instanceof TallerController c) {
            servicio.crearTaller(nombre, fIni, fFin, hIni, hFin, estado, c.getCupoMaximo(), c.getModalidadSeleccionada());
        } else if (tipo == TipoEvento.CICLO_CINE && controladorFragmento instanceof CicloCineController c) {
            servicio.crearCicloCine(nombre, fIni, fFin, hIni, hFin, estado, c.isPostCharla(), c.getCupoMaximo(), c.getPeliculasSeleccionadas());
        }
    }

    private void actualizarSegunTipo(Evento original, String nombre, TipoEvento tipo,
                                    LocalDate fIni, LocalDate fFin, LocalTime hIni, LocalTime hFin, EstadoEvento estado) {
        switch (tipo) {
            case FERIA -> {
                if (controladorFragmento instanceof FeriaController c) {
                    servicio.actualizarFeria((com.app_eventos.model.Feria) original,
                            nombre, fIni, fFin, hIni, hFin, estado, c.getCantidadStands(), c.getAmbienteSeleccionado());
                }
            }
            case CONCIERTO -> {
                if (controladorFragmento instanceof ConciertoController c) {
                    servicio.actualizarConcierto((com.app_eventos.model.Concierto) original,
                            nombre, fIni, fFin, hIni, hFin, estado, c.getTipoEntradaSeleccionada(), c.getCupoMaximo());
                }
            }
            case EXPOSICION -> {
                if (controladorFragmento instanceof ExposicionController c) {
                    servicio.actualizarExposicion((com.app_eventos.model.Exposicion) original,
                            nombre, fIni, fFin, hIni, hFin, estado, c.getTipoArteSeleccionado());
                }
            }
            case TALLER -> {
                if (controladorFragmento instanceof TallerController c) {
                    servicio.actualizarTaller((com.app_eventos.model.Taller) original,
                            nombre, fIni, fFin, hIni, hFin, estado, c.getCupoMaximo(), c.getModalidadSeleccionada());
                }
            }
            case CICLO_CINE -> {
                if (controladorFragmento instanceof CicloCineController c) {
                    servicio.actualizarCicloCine((com.app_eventos.model.CicloCine) original,
                            nombre, fIni, fFin, hIni, hFin, estado, c.isPostCharla(), c.getCupoMaximo(), c.getPeliculasSeleccionadas());
                }
            }
        }
    }


    @FXML
    private void modificarEvento() {
        Evento e = tablaEventos.getSelectionModel().getSelectedItem();
        if (e == null) {
            mostrarAlerta("Selección requerida", "Debe seleccionar un evento en la tabla para modificar.");
            return;
        }
        modoEdicion = true;
        eventoEnEdicion = e;
        comboTipoEvento.setDisable(true);

        setEstadosParaEdicion();
        comboEstado.getSelectionModel().select(e.getEstado());
        // básicos
        txtNombre.setText(e.getNombre());
        dateInicio.setValue(e.getFechaInicio().toLocalDate());
        dateFin.setValue(e.getFechaFin().toLocalDate());
        spinnerHoraInicio.getValueFactory().setValue(e.getFechaInicio().toLocalTime());
        spinnerHoraFin.getValueFactory().setValue(e.getFechaFin().toLocalTime());
        comboEstado.getSelectionModel().select(e.getEstado());

        // específico por tipo
        comboTipoEvento.getSelectionModel().select(e.getTipoEvento());
        cargarFragmentoEspecifico(e.getTipoEvento()); // carga el FXML y deja controladorFragmento listo

        // precargar fragmento
        switch (e.getTipoEvento()) {
        case FERIA -> {
            if (controladorFragmento instanceof FeriaController c && e instanceof Feria f) {
                c.setValores(f.getCantidadStands(), f.getAmbiente());
            }
        }
        case CONCIERTO -> {
            if (controladorFragmento instanceof ConciertoController c && e instanceof Concierto x) {
                c.setValores(x.getTipoEntrada(), x.getCupoMaximo());
            }
        }
        case EXPOSICION -> {
            if (controladorFragmento instanceof ExposicionController c && e instanceof Exposicion x) {
                c.setValores(x.getTipoArte());
            }
        }
        case TALLER -> {
            if (controladorFragmento instanceof TallerController c && e instanceof Taller x) {
                c.setValores(x.getCupoMaximo(), x.getModalidad());
            }
        }
        case CICLO_CINE -> {
            if (controladorFragmento instanceof CicloCineController c && e instanceof CicloCine x) {
                c.setCupoMaximo(x.getCupoMaximo());
                c.setPostCharla(x.isPostCharla());
                c.preseleccionarPeliculas(x.getPeliculas());
            }
        }
    }

        modalOverlay.setVisible(true);
        modalOverlay.toFront();
    }
    
    @FXML
    private void eliminarEvento() {
        Evento sel = tablaEventos.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mostrarAlerta("Selección requerida", "Debe seleccionar un evento en la tabla para eliminar.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText(null);
        confirm.setTitle("Confirmar eliminación");
        confirm.setContentText("¿Eliminar el evento \"" + sel.getNombre() + "\"?");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    servicio.eliminarEvento(sel);          // <-- por objeto

                    tablaEventos.getItems().setAll(servicio.listarEventos());
                    tablaEventos.getSelectionModel().clearSelection();

                    // refrescar listado respetando filtros
                    buscarYRefrescarTabla(); // MOD
                } catch (IllegalStateException | IllegalArgumentException ex) {
                    mostrarAlerta("No se pudo eliminar", ex.getMessage());
                }
            }
        });
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
        // MOD: aplicar filtros vigentes tras refrescar
        buscarYRefrescarTabla(); // MOD
    }

    // Búsqueda en la grilla

    // inicializa referencias a los filtros del HBox sin cambiar fx:id
    private void initFiltrosListado() {
        // VBox contenedor de la tabla es el padre directo de tablaEventos
        VBox panelContenedor = (VBox) tablaEventos.getParent();
        // El HBox de filtros es su primer hijo
        HBox hboxFiltros = (HBox) panelContenedor.getChildren().get(0);

        // Accedemos a los filtros por índice, ya que no tienen fx:id
        this.comboEstadoFiltro = (ComboBox<EstadoEvento>) hboxFiltros.getChildren().get(3);
        this.dateDesdeFiltro   = (DatePicker)            hboxFiltros.getChildren().get(5);
        this.dateHastaFiltro   = (DatePicker)            hboxFiltros.getChildren().get(7);

        // Poblado de combos
        comboTipoEventoFiltro.setItems(FXCollections.observableArrayList(TipoEvento.values()));
        comboTipoEventoFiltro.getSelectionModel().clearSelection(); // null = Todos

        comboEstadoFiltro.setItems(FXCollections.observableArrayList(EstadoEvento.values()));
        comboEstadoFiltro.getSelectionModel().clearSelection();     // null = Todos

        // Listeners para disparar la búsqueda
        comboTipoEventoFiltro.valueProperty().addListener((o,a,b) -> buscarYRefrescarTabla());
        comboEstadoFiltro.valueProperty().addListener((o,a,b) -> buscarYRefrescarTabla());
        dateDesdeFiltro.valueProperty().addListener((o,a,b) -> buscarYRefrescarTabla());
        dateHastaFiltro.valueProperty().addListener((o,a,b) -> buscarYRefrescarTabla());
    }

    // ejecuta la búsqueda en Servicio y refresca la TableView
    private void buscarYRefrescarTabla() {
        LocalDate desde = (dateDesdeFiltro != null) ? dateDesdeFiltro.getValue() : null;
        LocalDate hasta = (dateHastaFiltro != null) ? dateHastaFiltro.getValue() : null;

        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            mostrarAlerta("Rango inválido", "Fecha desde mayor que fecha hasta");
            return;
        }

        TipoEvento tipo = (comboTipoEventoFiltro != null) ? comboTipoEventoFiltro.getValue() : null;
        EstadoEvento estado = (comboEstadoFiltro != null) ? comboEstadoFiltro.getValue() : null;

        var resultados = servicio.buscarEventos(tipo, estado, desde, hasta);
        tablaEventos.setItems(FXCollections.observableArrayList(resultados));
    }
}
