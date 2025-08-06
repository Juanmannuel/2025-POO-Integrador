package com.app_eventos.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

import com.app_eventos.model.*;
import com.app_eventos.model.enums.EstadoEvento;
import com.app_eventos.model.enums.TipoEntrada;
import com.app_eventos.model.enums.TipoEvento;
import com.app_eventos.repository.Repositorio;
import com.app_eventos.utils.*;
import com.app_eventos.utils.ComboBoxInicializador;
import javafx.collections.FXCollections;
import javafx.scene.control.*;

import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.io.IOException;


public class ABMEventoController {

    private Repositorio repositorio = Repositorio.getInstance();
    private Evento eventoEnEdicion = null; // Para saber si estamos modificando

    // Campos de búsqueda y modal
    @FXML private TextField txtNombre;
    @FXML private DatePicker dateInicio;
    @FXML private DatePicker dateFin;
    @FXML private StackPane modalOverlay;
    @FXML private ComboBox<TipoEvento> comboTipoEvento;

    @FXML private ComboBox<EstadoEvento> comboEstado;
    @FXML private Pane seccionDinamica;
    @FXML private Spinner<Integer> spinnerDuracion;
    @FXML private TableView<Evento> tablaEventos;
    
    // Filtros
    @FXML private ComboBox<TipoEvento> comboTipoEventoFiltro;
    @FXML private ComboBox<EstadoEvento> comboEstadoFiltro;
    @FXML private DatePicker fechaDesde;
    @FXML private DatePicker fechaHasta;
    
    // Responsables
    @FXML private ListView<Persona> listViewResponsables;
    @FXML private ComboBox<Persona> comboPersonasDisponibles;

    @FXML private TableColumn<Evento, String> colNombre;
    @FXML private TableColumn<Evento, String> colTipo;
    @FXML private TableColumn<Evento, String> colFechaInicio;
    @FXML private TableColumn<Evento, String> colDuracion;
    @FXML private TableColumn<Evento, String> colEstado;
    @FXML private TableColumn<Evento, String> colResponsables;


    @FXML
    public void initialize() {
        configurarTabla();
        configurarComboBoxes();
        configurarFiltros();
        configurarResponsables();
        configurarSpinnerDuracion();
        configurarListeners();
        actualizarTabla();
    }

    private void configurarTabla() {
        // Configurar ancho de columnas proporcional
        tablaEventos.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            double total = newWidth.doubleValue();
            colNombre.setPrefWidth(total * 0.20);        // 20%
            colTipo.setPrefWidth(total * 0.10);          // 10%
            colFechaInicio.setPrefWidth(total * 0.15);   // 15%
            colDuracion.setPrefWidth(total * 0.10);      // 10%
            colEstado.setPrefWidth(total * 0.15);        // 15%
            colResponsables.setPrefWidth(total * 0.30);  // 30%
        });
        configurarColumnasTabla();
    }

    private void configurarComboBoxes() {
        ComboBoxInicializador.cargarTipoEvento(comboTipoEvento);
        ComboBoxInicializador.cargarEstadoEvento(comboEstado);
    }

    private void configurarFiltros() {
        FiltroEventoHelper.configurarComboConOpcionTodos(comboTipoEventoFiltro);
        FiltroEventoHelper.configurarComboConOpcionTodos(comboEstadoFiltro, true);
        
        // Listeners para filtrar automáticamente
        comboTipoEventoFiltro.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        comboEstadoFiltro.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        fechaDesde.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        fechaHasta.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
    }

    private void configurarResponsables() {
        ResponsableHelper.configurarListViewResponsables(listViewResponsables);
        ResponsableHelper.configurarComboPersonas(comboPersonasDisponibles);
        ResponsableHelper.actualizarPersonasDisponibles(comboPersonasDisponibles, repositorio);
    }

    private void configurarSpinnerDuracion() {
        spinnerDuracion.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 365, 1));
        spinnerDuracion.setEditable(false);
    }

    private void configurarListeners() {
        // Listener para cargar fragmento específico
        comboTipoEvento.valueProperty().addListener((obs, oldVal, newVal) -> {
            cargarFragmentoEspecifico(newVal);
            comboTipoEvento.getStyleClass().remove("campo-invalido");
        });

        // Listeners para limpiar estilos de error
        configurarListenersValidacion();
        
        // Listener para sincronizar spinner con fechas
        spinnerDuracion.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (dateInicio.getValue() != null && newVal != null) {
                dateFin.setValue(dateInicio.getValue().plusDays(newVal - 1));
            }
        });
    }

    private void configurarListenersValidacion() {
        txtNombre.textProperty().addListener((obs, oldVal, newVal) -> 
            txtNombre.getStyleClass().remove("campo-invalido"));
        dateInicio.valueProperty().addListener((obs, oldVal, newVal) -> {
            dateInicio.getStyleClass().remove("campo-invalido");
            actualizarDuracion();
        });
        dateFin.valueProperty().addListener((obs, oldVal, newVal) -> {
            dateFin.getStyleClass().remove("campo-invalido");
            actualizarDuracion();
        });
        comboEstado.valueProperty().addListener((obs, oldVal, newVal) -> 
            comboEstado.getStyleClass().remove("campo-invalido"));
    }

    private void configurarColumnasTabla() {
        // Usar SimpleStringProperty para todas las columnas para asegurar actualización
        colNombre.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNombre()));
        colTipo.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTipoEvento().toString()));
        colFechaInicio.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getFechaInicio().toLocalDate().toString()));
        colDuracion.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDuracionFormateada()));
        colEstado.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getEstado().toString()));
        colResponsables.setCellValueFactory(cellData -> {
            int cantidad = cellData.getValue().obtenerResponsables().size();
            return new javafx.beans.property.SimpleStringProperty(
                cantidad + " responsable" + (cantidad != 1 ? "s" : ""));
        });
    }

    private void aplicarFiltros() {
        var todosLosEventos = repositorio.obtenerTodosLosEventos();
        FiltroEventoHelper.aplicarFiltros(todosLosEventos, tablaEventos, 
                                        comboTipoEventoFiltro, comboEstadoFiltro, 
                                        fechaDesde, fechaHasta);
    }

    @FXML
    private void limpiarFiltros() {
        comboTipoEventoFiltro.setValue(null);
        comboEstadoFiltro.setValue(null);
        fechaDesde.setValue(null);
        fechaHasta.setValue(null);
        actualizarTabla(); // Mostrar todos los eventos
    }

    private void actualizarTabla() {
        // Siempre obtener una nueva lista del repositorio para asegurar datos actualizados
        var eventos = repositorio.obtenerTodosLosEventos();
        
        // Aplicar filtros si están activos
        if (comboTipoEventoFiltro != null && 
            (comboTipoEventoFiltro.getValue() != null || comboEstadoFiltro.getValue() != null || 
             fechaDesde.getValue() != null || fechaHasta.getValue() != null)) {
            // Usar el helper de filtros pero con los datos frescos
            FiltroEventoHelper.aplicarFiltros(eventos, tablaEventos, 
                                            comboTipoEventoFiltro, comboEstadoFiltro, 
                                            fechaDesde, fechaHasta);
        } else {
            // Crear una nueva ObservableList para forzar el refresh
            tablaEventos.setItems(FXCollections.observableArrayList(eventos));
        }
        
        // Forzar refresh de la tabla
        tablaEventos.refresh();
    }

    // Variable para guardar el controller del fragmento actual
    private Object controllerFragmentoActual = null;

    private void cargarFragmentoEspecifico(TipoEvento tipo) {
        seccionDinamica.getChildren().clear();
        controllerFragmentoActual = null; // Limpiar controller anterior
        
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
            controllerFragmentoActual = loader.getController(); // Guardar referencia al controller
            seccionDinamica.getChildren().add(nodo);
        } catch (IOException e) {
            System.err.println("Error al cargar el fragmento: " + rutaFXML);
            e.printStackTrace();
        }
    }

    private void actualizarDuracion() {
        if (dateInicio.getValue() != null && dateFin.getValue() != null) {
            long dias = java.time.temporal.ChronoUnit.DAYS.between(dateInicio.getValue(), dateFin.getValue()) + 1;
            if (dias > 0 && dias <= 365) {
                spinnerDuracion.getValueFactory().setValue((int) dias);
            }
        }
    }

    private void limpiarEstilos() {
        var campos = java.util.List.of(txtNombre, comboTipoEvento, dateInicio, dateFin, comboEstado);
        campos.forEach(campo -> campo.getStyleClass().remove("campo-invalido"));
    }

    // -- Modal --
    @FXML
    private void mostrarModal() {
        // Actualizar personas disponibles cada vez que se abre el modal
        ResponsableHelper.actualizarPersonasDisponibles(comboPersonasDisponibles, repositorio);
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
        comboEstado.getSelectionModel().clearSelection();
        seccionDinamica.getChildren().clear();
        
        // Limpiar responsables
        listViewResponsables.getItems().clear();
        comboPersonasDisponibles.getSelectionModel().clearSelection();
        
        // Limpiar modo edición y controller fragmento
        eventoEnEdicion = null;
        controllerFragmentoActual = null;

        modalOverlay.setVisible(false);
    }

    @FXML
    private void guardarEvento() {
        // ===== LIMPIAR ESTILOS DE UI =====
        limpiarEstilos();

        try {
            // ===== CREAR O MODIFICAR EVENTO =====
            Evento nuevoEvento;
            if (eventoEnEdicion != null) {
                // Modo modificación - actualizar evento existente
                nuevoEvento = eventoEnEdicion;
                nuevoEvento.setNombre(txtNombre.getText().trim());
                nuevoEvento.setFechaInicio(dateInicio.getValue().atStartOfDay());
                nuevoEvento.setFechaFin(dateFin.getValue().atStartOfDay());
                // No cambiar tipo en modificación por simplicidad
            } else {
                // Modo creación - crear nuevo evento
                nuevoEvento = Evento.crearEvento(
                    comboTipoEvento.getValue(),
                    txtNombre.getText().trim(),
                    dateInicio.getValue(),
                    dateFin.getValue()
                );
            }

            // ===== CAPTURAR DATOS ESPECÍFICOS DEL TIPO =====
            DatosEspecificosHelper.capturarDatosEspecificos(nuevoEvento, controllerFragmentoActual);
            
            // ===== AGREGAR RESPONSABLES USANDO LÓGICA DEL MODELO =====
            java.util.List<Persona> responsablesSeleccionados = new java.util.ArrayList<>(listViewResponsables.getItems());
            nuevoEvento.actualizarResponsables(responsablesSeleccionados);

            // ===== VALIDAR USANDO LÓGICA DEL MODELO =====
            boolean esNuevo = (eventoEnEdicion == null);
            nuevoEvento.validarCompleto(esNuevo);

            // ===== APLICAR ESTADO USANDO LÓGICA DEL MODELO =====
            EstadoEvento estadoSeleccionado = comboEstado.getValue();
            if (esNuevo && estadoSeleccionado != EstadoEvento.PLANIFICACIÓN) {
                nuevoEvento.cambiarEstado(estadoSeleccionado);
            } else if (!esNuevo) {
                nuevoEvento.cambiarEstado(estadoSeleccionado);
            }

            // ===== PERSISTIR =====
            repositorio.guardarEvento(nuevoEvento);

            // ===== ACTUALIZAR UI =====
            String mensaje = eventoEnEdicion != null ? "Evento modificado correctamente." : "Evento guardado correctamente.";
            mostrarAlerta("Éxito", mensaje, Alert.AlertType.INFORMATION);
            cerrarModal();
            actualizarTabla(); // Actualizar la tabla

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
        Evento eventoSeleccionado = obtenerEventoSeleccionado("modificar");
        if (eventoSeleccionado != null) {
            // Actualizar personas disponibles
            ResponsableHelper.actualizarPersonasDisponibles(comboPersonasDisponibles, repositorio);
            // Cargar los datos del evento seleccionado en los campos del modal
            cargarDatosEnModal(eventoSeleccionado);
            modalOverlay.setVisible(true);
        }
    }

    @FXML
    private void bajaEvento() {
        Evento eventoSeleccionado = obtenerEventoSeleccionado("eliminar");
        if (eventoSeleccionado != null && confirmarEliminacion(eventoSeleccionado)) {
            try {
                repositorio.eliminarEvento(eventoSeleccionado.getIdEvento());
                mostrarAlerta("Éxito", "Evento eliminado correctamente.", Alert.AlertType.INFORMATION);
                actualizarTabla();
            } catch (Exception e) {
                mostrarAlerta("Error", "No se pudo eliminar el evento: " + e.getMessage());
            }
        }
    }

    private Evento obtenerEventoSeleccionado(String accion) {
        Evento eventoSeleccionado = tablaEventos.getSelectionModel().getSelectedItem();
        if (eventoSeleccionado == null) {
            mostrarAlerta("Selección requerida", "Debe seleccionar un evento en la tabla para " + accion + ".");
        }
        return eventoSeleccionado;
    }

    private boolean confirmarEliminacion(Evento evento) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("¿Está seguro que desea eliminar el evento '" + evento.getNombre() + "'?");
        return confirmacion.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private void cargarDatosEnModal(Evento evento) {
        // Establecer modo edición
        eventoEnEdicion = evento;
        
        // Cargar datos básicos
        txtNombre.setText(evento.getNombre());
        dateInicio.setValue(evento.getFechaInicio().toLocalDate());
        dateFin.setValue(evento.getFechaFin().toLocalDate());
        comboTipoEvento.setValue(evento.getTipoEvento());
        comboEstado.setValue(evento.getEstado());
        
        // Calcular y cargar duración usando el método del modelo
        long duracionDias = evento.getDuracionEstimada().toDays();
        spinnerDuracion.getValueFactory().setValue((int) duracionDias);
        
        // Cargar responsables
        listViewResponsables.getItems().clear();
        listViewResponsables.getItems().addAll(evento.obtenerResponsables());
        
        // Cargar datos específicos del tipo de evento
        cargarFragmentoEspecifico(evento.getTipoEvento());
        
        // Esperar un ciclo para que el fragmento se cargue completamente
        javafx.application.Platform.runLater(() -> {
            DatosEspecificosHelper.cargarDatosEspecificos(evento, controllerFragmentoActual);
        });
    }

    @FXML
    private void agregarResponsable() {
        Persona personaSeleccionada = comboPersonasDisponibles.getValue();
        if (personaSeleccionada == null) {
            mostrarAlerta("Selección requerida", "Debe seleccionar una persona para agregar como responsable.");
            return;
        }
        
        // Verificar duplicados usando lógica de UI
        if (listViewResponsables.getItems().contains(personaSeleccionada)) {
            mostrarAlerta("Responsable duplicado", "Esta persona ya está agregada como responsable.");
            return;
        }
        
        // Agregar a la lista de UI
        listViewResponsables.getItems().add(personaSeleccionada);
        comboPersonasDisponibles.getSelectionModel().clearSelection();
    }

    @FXML
    private void quitarResponsable() {
        Persona personaSeleccionada = listViewResponsables.getSelectionModel().getSelectedItem();
        if (personaSeleccionada == null) {
            mostrarAlerta("Selección requerida", "Debe seleccionar un responsable de la lista para quitar.");
            return;
        }
        
        // Quitar de la lista de UI
        listViewResponsables.getItems().remove(personaSeleccionada);
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