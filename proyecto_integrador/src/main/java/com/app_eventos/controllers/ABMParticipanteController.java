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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ABMParticipanteController {

    // filtros superiores
    @FXML private ComboBox<Evento> comboEventoFiltro;
    @FXML private TextField txtDNIFiltro;
    @FXML private TextField txtNombreFiltro;

    // tabla principal
    @FXML private TableView<Fila> tablaParticipantes;
    @FXML private TableColumn<Fila, String> colEvento;
    @FXML private TableColumn<Fila, String> colNombre;
    @FXML private TableColumn<Fila, String> colDNI;
    @FXML private TableColumn<Fila, String> colTelefono;
    @FXML private TableColumn<Fila, String> colEmail;
    @FXML private TableColumn<Fila, EstadoEvento> colEstadoEvento;

    // modal alta
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

    private static final DateTimeFormatter FECHAS = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    // init
    @FXML
    public void initialize() {
        // Converters
        comboEventoFiltro.setConverter(eventoConverter());
        comboEvento.setConverter(eventoConverter());
        comboParticipante.setConverter(personaConverter());

        // Filtro: eventos que requieren inscripción
        recargarEventosFiltro();

        // Combo personas vacío hasta elegir evento en el modal
        comboParticipante.setItems(FXCollections.observableArrayList());
        comboParticipante.setPlaceholder(new Label("Seleccione un evento"));

        // Listeners filtros
        comboEventoFiltro.valueProperty().addListener((_,_,_)->refrescarTabla());
        txtDNIFiltro.textProperty().addListener((_,_,_)->refrescarTabla());
        txtNombreFiltro.textProperty().addListener((_,_,_)->refrescarTabla());

        // Listeners modal
        comboEvento.valueProperty().addListener((_,_,b)->{
            pintarInfoEvento(b);
            cargarPersonasElegibles(b); 
        });
        comboParticipante.valueProperty().addListener((_,_,b)->pintarInfoPersona(b));

        // Columnas
        colEvento.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().evento().getNombre()));
        colNombre.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().persona().getApellido()+", "+d.getValue().persona().getNombre()));
        colDNI.setCellValueFactory(d -> new SimpleStringProperty(nullSafe(d.getValue().persona().getDni())));
        colTelefono.setCellValueFactory(d -> new SimpleStringProperty(nullSafe(d.getValue().persona().getTelefono())));
        colEmail.setCellValueFactory(d -> new SimpleStringProperty(nullSafe(d.getValue().persona().getEmail())));
        colEstadoEvento.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().evento().getEstado()));

        tablaParticipantes.setItems(modeloTabla);
        modalOverlay.setVisible(false);

        // Tabla inicial
        refrescarTabla();
    }

    // acciones del modal

    @FXML
    public void mostrarModalAlta() {
        // solo eventos CONFIRMADOS y no vencidos
        var inscribibles = servicio.obtenerEventosParaInscripcion();
        comboEvento.setItems(inscribibles);

        if (inscribibles.isEmpty()) {
            alertWarn("No hay eventos abiertos a inscripción (deben estar CONFIRMADOS y no vencidos).");
            return;
        }

        limpiarModal(false); // no borrar items recién cargados

        // intentar preseleccionar el del filtro si aplica
        Evento selFiltro = comboEventoFiltro.getValue();
        if (selFiltro != null && esInscribibleAhora(selFiltro)) {
            seleccionarPorId(comboEvento, selFiltro.getIdEvento());
        } else {
            comboEvento.getSelectionModel().selectFirst();
        }

        // pintar info y cargar elegibles con la selección actual
        pintarInfoEvento(comboEvento.getValue());
        cargarPersonasElegibles(comboEvento.getValue());

        modalOverlay.setVisible(true);
    }

    @FXML
    public void cerrarModal() {
        modalOverlay.setVisible(false);
        limpiarModal(true);
    }

    @FXML
    public void altaParticipante() {
        Evento e = comboEvento.getValue();
        Persona p = comboParticipante.getValue();
        if (e == null || p == null) { alertWarn("Debe seleccionar evento y participante."); return; }
        try {
            servicio.inscribirParticipante(e, p); // valida estado/cupo/rol
            cerrarModal();
            refrescarTabla();
        } catch (Exception ex) {
            alertErr(ex.getMessage());
        }
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

    // tabla

    private void refrescarTabla() {
        modeloTabla.clear();

        Evento filtroEvento = comboEventoFiltro.getValue();
        String filtroDni = txtDNIFiltro.getText() == null ? "" : txtDNIFiltro.getText().trim();
        String filtroNombre = txtNombreFiltro.getText() == null ? "" : txtNombreFiltro.getText().trim().toLowerCase();

        List<Evento> baseEventos = (filtroEvento != null)
                ? List.of(filtroEvento)
                : eventosConCupo(); // todos los que requieren inscripción

        var filas = new ArrayList<Fila>();
        for (Evento e : baseEventos) {
            var participantes = servicio.obtenerParticipantes(e); // consulta a BD
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

    // helpers modal

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

    private void limpiarModal(boolean limpiarItems) {
        comboEvento.getSelectionModel().clearSelection();
        comboParticipante.getSelectionModel().clearSelection();
        pintarInfoEvento(null);
        pintarInfoPersona(null);
        if (limpiarItems) {
            comboEvento.setItems(FXCollections.observableArrayList());
            comboParticipante.setItems(FXCollections.observableArrayList());
        } else {
            comboParticipante.setItems(FXCollections.observableArrayList());
        }
        comboParticipante.setPlaceholder(new Label("Seleccione un evento"));
    }

    // listas de eventos

    // Eventos que requieren inscripción (para el combo del filtro).
    private List<Evento> eventosConCupo() {
        return servicio.listarEventos().stream()
                .filter(e -> e instanceof IEventoConCupo)
                .toList();
    }

    // Eventos que requieren inscripción y están CONFIRMADOS y no vencidos.
    private boolean esInscribibleAhora(Evento e) {
        if (!(e instanceof IEventoConCupo)) return false;
        LocalDateTime ahora = LocalDateTime.now();
        return e.getEstado() == EstadoEvento.CONFIRMADO
                && e.getFechaFin() != null
                && e.getFechaFin().isAfter(ahora);
    }

    private void recargarEventosFiltro() {
        var eventos = FXCollections.observableArrayList(eventosConCupo());
        Long idSel = (comboEventoFiltro.getValue() == null) ? null : comboEventoFiltro.getValue().getIdEvento();
        comboEventoFiltro.setItems(eventos);
        if (idSel != null) seleccionarPorId(comboEventoFiltro, idSel);
    }

    // Selecciona por ID (evita problemas de proxies/equals).
    private void seleccionarPorId(ComboBox<Evento> combo, Long id) {
        if (id == null) return;
        combo.getItems().stream()
                .filter(e -> Objects.equals(e.getIdEvento(), id))
                .findFirst()
                .ifPresent(e -> combo.getSelectionModel().select(e));
    }

    // personas elegibles

    // Llena el combo de personas con las elegibles para el evento (BD). 
    private void cargarPersonasElegibles(Evento e){
        if (e == null) {
            comboParticipante.setItems(FXCollections.observableArrayList());
            comboParticipante.setPlaceholder(new Label("Seleccione un evento"));
            return;
        }
        var libres = servicio.obtenerPersonasElegiblesParaEvento(e); // << NUEVO (va a BD)
        comboParticipante.setItems(libres);
        if (libres.isEmpty()) comboParticipante.setPlaceholder(new Label("No hay personas elegibles"));
    }

    // util

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

    // fila de la tabla
    public record Fila(Evento evento, Persona persona) {}
}
