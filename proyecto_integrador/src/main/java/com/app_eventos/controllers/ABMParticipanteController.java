package com.app_eventos.controllers;

import com.app_eventos.model.Evento;
import com.app_eventos.model.Persona;
import com.app_eventos.model.enums.EstadoEvento;
import com.app_eventos.model.enums.TipoEvento;
import com.app_eventos.model.interfaces.IEventoConCupo;
import com.app_eventos.services.Servicio;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.util.StringConverter;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador que coincide con abmParticipante.fxml.
 * - Lista participantes por evento.
 * - Alta/Baja de inscripciones.
 * - En el modal, el cupo se calcula contra BD: servicio.obtenerParticipantes(evento).size()
 */
public class ABMParticipanteController {

    // -------- filtros superiores --------
    @FXML private ComboBox<Evento> comboEventoFiltro;
    @FXML private TextField txtDNIFiltro;
    @FXML private TextField txtNombreFiltro;

    // -------- tabla principal --------
    @FXML private TableView<Fila> tablaParticipantes;
    @FXML private TableColumn<Fila, String> colEvento;
    @FXML private TableColumn<Fila, String> colNombre;
    @FXML private TableColumn<Fila, String> colDNI;
    @FXML private TableColumn<Fila, String> colTelefono;
    @FXML private TableColumn<Fila, String> colEmail;
    @FXML private TableColumn<Fila, EstadoEvento> colEstadoEvento;
    @FXML private TableColumn<Fila, String> colFechaAsignacion; // sin timestamp de alta: se deja "-"

    // -------- modal alta --------
    @FXML private StackPane modalOverlay;
    @FXML private ComboBox<Evento> comboEvento;
    @FXML private ComboBox<Persona> comboParticipante;

    // Info evento en modal
    @FXML private Label lblEstadoEvento;
    @FXML private Label lblTipoEvento;
    @FXML private Label lblCupoDisponible;
    @FXML private Label lblFechaEvento;

    // Info participante en modal
    @FXML private Label lblNombreParticipante;
    @FXML private Label lblDniParticipante;
    @FXML private Label lblTelefonoParticipante;
    @FXML private Label lblEmailParticipante;

    private final Servicio servicio = Servicio.getInstance();
    private final ObservableList<Fila> modeloTabla = FXCollections.observableArrayList();

    private static final DateTimeFormatter FECHAS =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    // -------- init --------
    @FXML
    public void initialize() {
        // combos
        comboEventoFiltro.setConverter(eventoConverter());
        comboEvento.setConverter(eventoConverter());
        comboParticipante.setConverter(personaConverter());

        // cargar datos iniciales en combos
        recargarEventosEnCombos();
        comboParticipante.setItems(servicio.obtenerPersonas());

        // listeners de filtros
        comboEventoFiltro.valueProperty().addListener((o,a,b)->refrescarTabla());
        txtDNIFiltro.textProperty().addListener((o,a,b)->refrescarTabla());
        txtNombreFiltro.textProperty().addListener((o,a,b)->refrescarTabla());

        // listeners del modal para pintar info
        comboEvento.valueProperty().addListener((o,a,b)->pintarInfoEvento(b));
        comboParticipante.valueProperty().addListener((o,a,b)->pintarInfoPersona(b));

        // columnas
        colEvento.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().evento().getNombre()));
        colNombre.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().persona().getApellido()+", "+d.getValue().persona().getNombre()));
        colDNI.setCellValueFactory(d -> new SimpleStringProperty(nullSafe(d.getValue().persona().getDni())));
        colTelefono.setCellValueFactory(d -> new SimpleStringProperty(nullSafe(d.getValue().persona().getTelefono())));
        colEmail.setCellValueFactory(d -> new SimpleStringProperty(nullSafe(d.getValue().persona().getEmail())));
        colEstadoEvento.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().evento().getEstado()));
        colFechaAsignacion.setCellValueFactory(d -> new SimpleStringProperty("-")); // no se guarda timestamp

        tablaParticipantes.setItems(modeloTabla);

        modalOverlay.setVisible(false);

        // primera carga
        refrescarTabla();
    }

    // ========= acciones de toolbar =========

    @FXML
    public void mostrarModalAlta() {
        limpiarModal();
        // por defecto selecciono el evento elegido en filtro
        comboEvento.getSelectionModel().select(comboEventoFiltro.getValue());
        pintarInfoEvento(comboEvento.getValue());
        modalOverlay.setVisible(true);
    }

    @FXML
    public void cerrarModal() {
        modalOverlay.setVisible(false); 
        limpiarModal();
    }

    @FXML
    public void altaParticipante() {
        Evento e = comboEvento.getValue();
        Persona p = comboParticipante.getValue();
        if (e == null || p == null) { alertWarn("Debe seleccionar evento y participante."); return; }
        try {
            // inscribe y persiste en tabla puente ManyToMany
            servicio.inscribirParticipante(e, p);
            cerrarModal();
            refrescarTabla();
        } catch (Exception ex) {
            alertErr(ex.getMessage());
        }
    }

    @FXML
    public void modificarParticipante() {
        // en este ABM no hay edición de inscripción, solo alta/baja
        alertWarn("No hay campos para modificar. Use Baja y vuelva a dar el alta si corresponde.");
    }

    @FXML
    public void bajaParticipante() {
        Fila sel = tablaParticipantes.getSelectionModel().getSelectedItem();
        if (sel == null) { alertWarn("Seleccione una fila."); return; }
        try {
            servicio.desinscribirParticipante(sel.evento(), sel.persona());
            refrescarTabla();
        } catch (Exception ex) {
            alertErr(ex.getMessage());
        }
    }

    // ========= lógica de tabla =========

    private void refrescarTabla() {
        modeloTabla.clear();

        Evento filtroEvento = comboEventoFiltro.getValue();
        String filtroDni = txtDNIFiltro.getText() == null ? "" : txtDNIFiltro.getText().trim();
        String filtroNombre = txtNombreFiltro.getText() == null ? "" : txtNombreFiltro.getText().trim().toLowerCase();

        List<Evento> baseEventos = (filtroEvento != null)
                ? List.of(filtroEvento)
                : servicio.listarEventos();

        List<Fila> filas = new ArrayList<>();
        for (Evento e : baseEventos) {
            // Traer participantes DESDE BD para evitar lazy sobre entidad detach
            var participantes = servicio.obtenerParticipantes(e);
            for (Persona p : participantes) {
                boolean pasa = true;
                if (!filtroDni.isBlank())    pasa &= p.getDni()!=null && p.getDni().contains(filtroDni);
                if (!filtroNombre.isBlank()) pasa &= (p.getNombre()+" "+p.getApellido()).toLowerCase().contains(filtroNombre);
                if (pasa) filas.add(new Fila(e,p));
            }
        }
        modeloTabla.setAll(filas);
        tablaParticipantes.refresh();
    }

    // ========= helpers modal =========

    private void pintarInfoEvento(Evento e) {
        if (e == null) {
            lblEstadoEvento.setText("Estado: -");
            lblTipoEvento.setText("Tipo: -");
            lblCupoDisponible.setText("Cupo: -");
            lblFechaEvento.setText("Fecha: -");
            return;
        }
        lblEstadoEvento.setText("Estado: " + e.getEstado());
        lblTipoEvento.setText("Tipo: " + tipoToLabel(e.getTipoEvento()));

        // >>> CLAVE: calcular inscriptos consultando a BD, NO haciendo e.getParticipantes()
        int inscriptos = servicio.obtenerParticipantes(e).size();
        if (e instanceof IEventoConCupo c) {
            lblCupoDisponible.setText("Cupo: " + inscriptos + " / " + c.getCupoMaximo());
        } else {
            lblCupoDisponible.setText("Cupo: -");
        }

        lblFechaEvento.setText("Fecha: " +
                e.getFechaInicio().format(FECHAS) + " a " + e.getFechaFin().format(FECHAS));
    }

    private void pintarInfoPersona(Persona p) {
        if (p == null) {
            lblNombreParticipante.setText("Nombre: -");
            lblDniParticipante.setText("DNI: -");
            lblTelefonoParticipante.setText("Teléfono: -");
            lblEmailParticipante.setText("Email: -");
            return;
        }
        lblNombreParticipante.setText("Nombre: " + p.getApellido() + ", " + p.getNombre());
        lblDniParticipante.setText("DNI: " + nullSafe(p.getDni()));
        lblTelefonoParticipante.setText("Teléfono: " + nullSafe(p.getTelefono()));
        lblEmailParticipante.setText("Email: " + nullSafe(p.getEmail()));
    }

    private void limpiarModal() {
        comboEvento.getSelectionModel().clearSelection();
        comboParticipante.getSelectionModel().clearSelection();
        pintarInfoEvento(null);
        pintarInfoPersona(null);
    }

    private void recargarEventosEnCombos() {
        var eventos = FXCollections.observableArrayList(servicio.listarEventos());
        comboEventoFiltro.setItems(eventos);
        comboEvento.setItems(eventos);
    }

    // ========= util =========

    private StringConverter<Evento> eventoConverter() {
        return new StringConverter<>() {
            @Override public String toString(Evento e) { return e==null? "" : e.getNombre(); }
            @Override public Evento fromString(String s) { return null; }
        };
    }

    private StringConverter<Persona> personaConverter() {
        return new StringConverter<>() {
            @Override public String toString(Persona p) {
                if (p == null) return "";
                String dni = p.getDni() == null ? "-" : p.getDni();
                return dni + " - " + p.getApellido() + ", " + p.getNombre();
            }
            @Override public Persona fromString(String s) { return null; }
        };
    }

    private static String nullSafe(String s){ return s==null? "-" : s; }

    private static String tipoToLabel(TipoEvento t){
        if (t == null) return "-";
        return switch (t) {
            case FERIA -> "FERIA";
            case CONCIERTO -> "CONCIERTO";
            case EXPOSICION -> "EXPOSICIÓN";
            case TALLER -> "TALLER";
            case CICLO_CINE -> "CICLO_CINE";
        };
    }

    private void alertErr(String m){ show(Alert.AlertType.ERROR, "Error", m); }
    private void alertWarn(String m){ show(Alert.AlertType.WARNING, "Aviso", m); }
    private void show(Alert.AlertType t, String h, String m){
        Alert a = new Alert(t); a.setHeaderText(h); a.setContentText(m); a.showAndWait();
    }

    // ========= fila de la tabla =========
    public record Fila(Evento evento, Persona persona) {}
}