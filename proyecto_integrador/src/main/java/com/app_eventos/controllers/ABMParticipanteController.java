package com.app_eventos.controllers;

import com.app_eventos.model.Evento;
import com.app_eventos.model.Persona;
import com.app_eventos.model.RolEvento;
import com.app_eventos.model.enums.TipoRol;
import com.app_eventos.model.interfaces.IEventoConCupo;
import com.app_eventos.services.Servicio;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.util.StringConverter;

import java.time.format.DateTimeFormatter;

public class ABMParticipanteController {

    private final Servicio servicio = Servicio.getInstance();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Filtros
    @FXML private ComboBox<Evento> comboEventoFiltro;
    @FXML private TextField txtNombreFiltro;
    @FXML private TextField txtDNIFiltro;

    // Tabla
    @FXML private TableView<RolEvento> tablaParticipantes;
    @FXML private TableColumn<RolEvento, String> colEvento;
    @FXML private TableColumn<RolEvento, String> colNombre;
    @FXML private TableColumn<RolEvento, String> colDNI;
    @FXML private TableColumn<RolEvento, String> colTelefono;
    @FXML private TableColumn<RolEvento, String> colEmail;
    @FXML private TableColumn<RolEvento, String> colEstadoEvento;

    // Modal inscripción
    @FXML private StackPane modalOverlay;
    @FXML private ComboBox<Evento> comboEvento;
    @FXML private ComboBox<Persona> comboParticipante;

    @FXML private Label lblEstadoEvento;
    @FXML private Label lblTipoEvento;
    @FXML private Label lblCupoDisponible;
    @FXML private Label lblFechaEvento;

    @FXML private Label lblNombreParticipante;
    @FXML private Label lblDniParticipante;
    @FXML private Label lblTelefonoParticipante;
    @FXML private Label lblEmailParticipante;

    @FXML
    public void initialize() {
        inicializarComponentes();
        configurarTabla();
        configurarConverters();
        configurarListeners();
        cargarDatos();
    }
    
    private void inicializarComponentes() {
        modalOverlay.setVisible(false);
    }
    
    private void configurarTabla() {
        colEvento.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getEvento().getNombre()));
        colNombre.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getPersona().getNombre() + " " + data.getValue().getPersona().getApellido()));
        colDNI.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getPersona().getDni()));
        colTelefono.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getPersona().getTelefono()));
        colEmail.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getPersona().getEmail()));
        colEstadoEvento.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getEvento().getEstado().toString()));
        
        tablaParticipantes.setItems(servicio.obtenerParticipantesInscritos());
    }
    
    private void configurarConverters() {
        StringConverter<Evento> eventoConverter = new StringConverter<Evento>() {
            @Override
            public String toString(Evento evento) {
                return evento != null ? evento.getNombre() : "";
            }
            
            @Override
            public Evento fromString(String string) {
                return null;
            }
        };
        
        comboEventoFiltro.setConverter(eventoConverter);
        comboEvento.setConverter(eventoConverter);
    }
    
    private void configurarListeners() {
        comboEvento.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                mostrarInformacionEvento(newVal);
            }
        });
        
        comboParticipante.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                mostrarInformacionPersona(newVal);
            }
        });
    }
    
    public void refrescarDatos() {
        cargarDatos();
        tablaParticipantes.setItems(servicio.obtenerParticipantesInscritos());
    }
    
    private void cargarDatos() {
        ObservableList<Evento> eventos = javafx.collections.FXCollections.observableArrayList(servicio.listarEventos());
        comboEventoFiltro.setItems(eventos);
        comboEvento.setItems(eventos);
        
        ObservableList<Persona> personas = servicio.obtenerPersonas();
        comboParticipante.setItems(personas);
    }
    
    private int contarParticipantesEnEvento(Evento evento) {
        return (int) servicio.obtenerParticipantesInscritos().stream()
            .filter(rol -> rol.getEvento().equals(evento) && rol.getRol() == TipoRol.PARTICIPANTE)
            .count();
    }
    
    private void mostrarInformacionEvento(Evento evento) {
        lblEstadoEvento.setText("Estado: " + evento.getEstado());
        lblTipoEvento.setText("Tipo: " + evento.getTipoEvento());
        lblFechaEvento.setText("Fecha: " + evento.getFechaInicio().format(FORMATTER));
        
        if (evento instanceof IEventoConCupo eventoConCupo) {
            int participantesEnEvento = contarParticipantesEnEvento(evento);
            int cupoDisponible = eventoConCupo.getCupoMaximo() - participantesEnEvento;
            lblCupoDisponible.setText("Cupo disponible: " + cupoDisponible);
        } else {
            lblCupoDisponible.setText("Cupo: Sin límite");
        }
    }
    
    private void mostrarInformacionPersona(Persona persona) {
        lblNombreParticipante.setText("Nombre: " + persona.getNombre() + " " + persona.getApellido());
        lblDniParticipante.setText("DNI: " + persona.getDni());
        lblTelefonoParticipante.setText("Teléfono: " + persona.getTelefono());
        lblEmailParticipante.setText("Email: " + persona.getEmail());
    }

    private void limpiarModalAlta() {
        comboEvento.getSelectionModel().clearSelection();
        comboParticipante.getSelectionModel().clearSelection();

        lblEstadoEvento.setText("Estado: -");
        lblTipoEvento.setText("Tipo: -");
        lblCupoDisponible.setText("Cupo: -");
        lblFechaEvento.setText("Fecha: -");

        lblNombreParticipante.setText("Nombre: -");
        lblDniParticipante.setText("DNI: -");
        lblTelefonoParticipante.setText("Teléfono: -");
        lblEmailParticipante.setText("Email: -");
    }

    @FXML
    private void mostrarModalAlta() {
        limpiarModalAlta();
        modalOverlay.setVisible(true);
    }

    @FXML
    private void cerrarModal() {
        modalOverlay.setVisible(false);
    }

    @FXML
    private void altaParticipante() {
        Evento eventoSeleccionado = comboEvento.getValue();
        Persona personaSeleccionada = comboParticipante.getValue();
        
        if (eventoSeleccionado == null || personaSeleccionada == null) {
            mostrarAlerta("Error", "Debe seleccionar un evento y una persona.");
            return;
        }
        
        if (estaInscrito(eventoSeleccionado, personaSeleccionada)) {
            mostrarAlerta("Error", "Esta persona ya está inscrita en el evento seleccionado.");
            return;
        }
        
        if (!hayCupoDisponible(eventoSeleccionado)) {
            mostrarAlerta("Error", "El evento no tiene cupo disponible.");
            return;
        }
        
        servicio.inscribirParticipante(eventoSeleccionado, personaSeleccionada);
        cerrarModal();
        tablaParticipantes.setItems(servicio.obtenerParticipantesInscritos());
        mostrarAlerta("Éxito", "Participante inscrito correctamente.");
    }
    
    private boolean estaInscrito(Evento evento, Persona persona) {
        return servicio.obtenerParticipantesInscritos().stream()
            .anyMatch(rol -> rol.getEvento().equals(evento) && 
                           rol.getPersona().equals(persona) &&
                           rol.getRol() == TipoRol.PARTICIPANTE);
    }
    
    private boolean hayCupoDisponible(Evento evento) {
        if (evento instanceof IEventoConCupo eventoConCupo) {
            int participantesEnEvento = contarParticipantesEnEvento(evento);
            return eventoConCupo.getCupoMaximo() - participantesEnEvento > 0;
        }
        return true;
    }

    @FXML
    private void modificarParticipante() {
        RolEvento participanteSeleccionado = tablaParticipantes.getSelectionModel().getSelectedItem();

        if (participanteSeleccionado != null) {
            mostrarAlerta("Información", "Participante: " + participanteSeleccionado.getPersona().getNombre() + 
                         " en evento: " + participanteSeleccionado.getEvento().getNombre());
        } else {
            mostrarAlerta("Error", "Debe seleccionar un participante de la tabla para modificar.");
        }
    }

    @FXML
    private void bajaParticipante() {
        RolEvento participanteSeleccionado = tablaParticipantes.getSelectionModel().getSelectedItem();
        
        if (participanteSeleccionado != null) {
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmar eliminación");
            confirmacion.setHeaderText(null);
            confirmacion.setContentText("¿Está seguro que desea eliminar a " + 
                                     participanteSeleccionado.getPersona().getNombre() + 
                                     " del evento " + participanteSeleccionado.getEvento().getNombre() + "?");
            
            confirmacion.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    servicio.eliminarParticipante(participanteSeleccionado.getEvento(), 
                                                participanteSeleccionado.getPersona());
                    tablaParticipantes.setItems(servicio.obtenerParticipantesInscritos());
                    mostrarAlerta("Éxito", "Participante eliminado correctamente.");
                }
            });
        } else {
            mostrarAlerta("Error", "Debe seleccionar un participante para eliminar.");
        }
    }
    
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}

