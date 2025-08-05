package com.app_eventos.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;

import com.app_eventos.model.*;
import com.app_eventos.model.enums.*;
import com.app_eventos.model.interfaces.IEventoConCupo;
import com.app_eventos.model.interfaces.IEventoConInscripcion;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class ABMParticipantesController {

    // Referencias a elementos del FXML - Tabla y filtros
    @FXML private TableView<ParticipanteDTO> tablaParticipantes;
    @FXML private TableColumn<ParticipanteDTO, String> colEvento;
    @FXML private TableColumn<ParticipanteDTO, String> colNombre;
    @FXML private TableColumn<ParticipanteDTO, String> colDNI;
    @FXML private TableColumn<ParticipanteDTO, String> colTelefono;
    @FXML private TableColumn<ParticipanteDTO, String> colEmail;
    @FXML private TableColumn<ParticipanteDTO, String> colEstadoEvento;

    @FXML private ComboBox<Evento> comboEventoFiltro;
    @FXML private TextField txtNombreFiltro;
    @FXML private TextField txtDNIFiltro;
    @FXML private Label lblInfoCupo;
    @FXML private Pagination paginador;

    // Modal Inscribir
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

    // Modal Detalles
    @FXML private StackPane modalDetallesOverlay;
    @FXML private Label lblDetalleEvento;
    @FXML private Label lblDetalleParticipante;
    @FXML private Label lblDetalleEstado;
    @FXML private Label lblDetalleTipo;
    @FXML private Label lblDetalleFecha;
    @FXML private TextArea txtDetalleInfo;

    // Datos simulados (en un proyecto real vendrían de repositorios)
    private ObservableList<Evento> eventos;
    private ObservableList<Persona> personas;
    private ObservableList<RolEvento> participaciones;
    private ObservableList<ParticipanteDTO> participantesActuales;

    @FXML
    public void initialize() {
        inicializarDatosSimulados();
        configurarTabla();
        configurarFiltros();
        configurarModales();
        cargarDatos();
        actualizarLista();
    }

    private void inicializarDatosSimulados() {
        // Crear personas simuladas
        personas = FXCollections.observableArrayList();
        personas.add(new Persona("Ana", "García", "12345678", "555-0001", "ana.garcia@email.com"));
        personas.add(new Persona("Carlos", "López", "23456789", "555-0002", "carlos.lopez@email.com"));
        personas.add(new Persona("María", "Rodríguez", "34567890", "555-0003", "maria.rodriguez@email.com"));
        personas.add(new Persona("Juan", "Pérez", "45678901", "555-0004", "juan.perez@email.com"));
        personas.add(new Persona("Laura", "Martín", "56789012", "555-0005", "laura.martin@email.com"));

        // Crear eventos simulados de diferentes tipos
        eventos = FXCollections.observableArrayList();
        
        // Taller con cupo
        Taller taller = new Taller("Taller de Fotografía", 
                                   LocalDateTime.now().plusDays(15), 
                                   LocalDateTime.now().plusDays(15).plusHours(3), 
                                   20, Modalidad.PRESENCIAL);
        taller.setEstado(EstadoEvento.CONFIRMADO);
        eventos.add(taller);
        
        // Concierto sin cupo
        Concierto concierto = new Concierto("Concierto de Rock", 
                                           LocalDateTime.now().plusDays(30), 
                                           LocalDateTime.now().plusDays(30).plusHours(4), 
                                           TipoEntrada.GRATUITA);
        concierto.setEstado(EstadoEvento.CONFIRMADO);
        eventos.add(concierto);
        
        // Evento en planificación
        Taller tallerPlanificacion = new Taller("Taller de Pintura", 
                                               LocalDateTime.now().plusDays(45), 
                                               LocalDateTime.now().plusDays(45).plusHours(2), 
                                               15, Modalidad.VIRTUAL);
        tallerPlanificacion.setEstado(EstadoEvento.PLANIFICACIÓN);
        eventos.add(tallerPlanificacion);

        // Crear participaciones simuladas
        participaciones = FXCollections.observableArrayList();
        participaciones.add(new RolEvento(taller, personas.get(0), TipoRol.PARTICIPANTE));
        participaciones.add(new RolEvento(taller, personas.get(1), TipoRol.PARTICIPANTE));
        participaciones.add(new RolEvento(concierto, personas.get(2), TipoRol.PARTICIPANTE));
        participaciones.add(new RolEvento(concierto, personas.get(3), TipoRol.PARTICIPANTE));
        
        // Actualizar contadores de inscriptos
        taller.setInscriptos(2);
        concierto.setInscriptos(2);
    }

    private void configurarTabla() {
        colEvento.setCellValueFactory(new PropertyValueFactory<>("nombreEvento"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        colDNI.setCellValueFactory(new PropertyValueFactory<>("dni"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colEstadoEvento.setCellValueFactory(new PropertyValueFactory<>("estadoEvento"));
        
        participantesActuales = FXCollections.observableArrayList();
        tablaParticipantes.setItems(participantesActuales);
    }

    private void configurarFiltros() {
        comboEventoFiltro.setItems(eventos);
        comboEventoFiltro.setConverter(new javafx.util.StringConverter<Evento>() {
            @Override
            public String toString(Evento evento) {
                return evento != null ? evento.getNombre() : "";
            }

            @Override
            public Evento fromString(String string) {
                return null;
            }
        });

        // Listeners para filtros automáticos
        comboEventoFiltro.setOnAction(_ -> actualizarLista());
        txtNombreFiltro.textProperty().addListener((_, _, _) -> actualizarLista());
        txtDNIFiltro.textProperty().addListener((_, _, _) -> actualizarLista());
    }

    private void configurarModales() {
        // Configurar combo de eventos para inscripción
        List<Evento> eventosConfirmados = eventos.stream()
                .filter(e -> e.getEstado() == EstadoEvento.CONFIRMADO)
                .collect(Collectors.toList());
        
        comboEvento.setItems(FXCollections.observableArrayList(eventosConfirmados));
        comboEvento.setConverter(new javafx.util.StringConverter<Evento>() {
            @Override
            public String toString(Evento evento) {
                return evento != null ? evento.getNombre() : "";
            }

            @Override
            public Evento fromString(String string) {
                return null;
            }
        });

        // Configurar combo de participantes
        comboParticipante.setItems(personas);
        comboParticipante.setConverter(new javafx.util.StringConverter<Persona>() {
            @Override
            public String toString(Persona persona) {
                return persona != null ? persona.toString() : "";
            }

            @Override
            public Persona fromString(String string) {
                return null;
            }
        });

        // Listeners para actualizar información automáticamente
        comboEvento.setOnAction(_ -> actualizarInfoEvento());
        comboParticipante.setOnAction(_ -> actualizarInfoParticipante());
    }

    private void cargarDatos() {
        actualizarInfoCupo();
    }

    @FXML
    private void actualizarLista() {
        participantesActuales.clear();
        
        List<RolEvento> participacionesFiltradas = participaciones.stream()
                .filter(p -> p.getRol() == TipoRol.PARTICIPANTE)
                .collect(Collectors.toList());

        // Aplicar filtros
        Evento eventoSeleccionado = comboEventoFiltro.getValue();
        if (eventoSeleccionado != null) {
            participacionesFiltradas = participacionesFiltradas.stream()
                    .filter(p -> p.getEvento().equals(eventoSeleccionado))
                    .collect(Collectors.toList());
        }

        String filtroNombre = txtNombreFiltro.getText();
        if (filtroNombre != null && !filtroNombre.trim().isEmpty()) {
            participacionesFiltradas = participacionesFiltradas.stream()
                    .filter(p -> p.getPersona().getNombre().toLowerCase().contains(filtroNombre.toLowerCase()) ||
                               p.getPersona().getApellido().toLowerCase().contains(filtroNombre.toLowerCase()))
                    .collect(Collectors.toList());
        }

        String filtroDNI = txtDNIFiltro.getText();
        if (filtroDNI != null && !filtroDNI.trim().isEmpty()) {
            participacionesFiltradas = participacionesFiltradas.stream()
                    .filter(p -> p.getPersona().getDni().contains(filtroDNI))
                    .collect(Collectors.toList());
        }

        // Convertir a DTOs para la tabla
        for (RolEvento participacion : participacionesFiltradas) {
            ParticipanteDTO dto = new ParticipanteDTO(
                    participacion.getEvento().getNombre(),
                    participacion.getPersona().toString(),
                    participacion.getPersona().getDni(),
                    participacion.getPersona().getTelefono(),
                    participacion.getPersona().getEmail(),
                    participacion.getEvento().getEstado().toString(),
                    participacion.getEvento(),
                    participacion.getPersona()
            );
            participantesActuales.add(dto);
        }

        actualizarInfoCupo();
    }

    private void actualizarInfoCupo() {
        Evento eventoSeleccionado = comboEventoFiltro.getValue();
        if (eventoSeleccionado instanceof IEventoConCupo && eventoSeleccionado instanceof Taller) {
            Taller taller = (Taller) eventoSeleccionado;
            lblInfoCupo.setText(String.format("Cupo: %d/%d disponibles", 
                taller.getCupoMaximo() - taller.getInscriptos(), taller.getCupoMaximo()));
        } else if (eventoSeleccionado != null) {
            lblInfoCupo.setText("Evento sin límite de cupo");
        } else {
            lblInfoCupo.setText("");
        }
    }

    @FXML
    private void mostrarModalInscribir() {
        limpiarFormularioInscripcion();
        modalOverlay.setVisible(true);
    }

    @FXML
    private void cerrarModal() {
        modalOverlay.setVisible(false);
    }

    @FXML
    private void cerrarModalDetalles() {
        modalDetallesOverlay.setVisible(false);
    }

    private void limpiarFormularioInscripcion() {
        comboEvento.setValue(null);
        comboParticipante.setValue(null);
        lblEstadoEvento.setText("Estado: -");
        lblTipoEvento.setText("Tipo: -");
        lblCupoDisponible.setText("Cupo: -");
        lblFechaEvento.setText("Fecha: -");
        lblNombreParticipante.setText("Nombre: -");
        lblDniParticipante.setText("DNI: -");
        lblTelefonoParticipante.setText("Teléfono: -");
        lblEmailParticipante.setText("Email: -");
    }

    private void actualizarInfoEvento() {
        Evento evento = comboEvento.getValue();
        if (evento != null) {
            lblEstadoEvento.setText("Estado: " + evento.getEstado());
            lblTipoEvento.setText("Tipo: " + evento.getTipoEvento());
            lblFechaEvento.setText("Fecha: " + evento.getFechaInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            
            if (evento instanceof IEventoConCupo && evento instanceof Taller) {
                Taller taller = (Taller) evento;
                lblCupoDisponible.setText(String.format("Cupo: %d/%d disponibles", 
                    taller.getCupoMaximo() - taller.getInscriptos(), taller.getCupoMaximo()));
            } else {
                lblCupoDisponible.setText("Cupo: Sin límite");
            }
        }
    }

    private void actualizarInfoParticipante() {
        Persona participante = comboParticipante.getValue();
        if (participante != null) {
            lblNombreParticipante.setText("Nombre: " + participante.toString());
            lblDniParticipante.setText("DNI: " + participante.getDni());
            lblTelefonoParticipante.setText("Teléfono: " + participante.getTelefono());
            lblEmailParticipante.setText("Email: " + participante.getEmail());
        }
    }

    @FXML
    private void inscribirParticipante() {
        try {
            Evento evento = comboEvento.getValue();
            Persona participante = comboParticipante.getValue();

            if (evento == null || participante == null) {
                mostrarAlerta("Error", "Debe seleccionar un evento y un participante", Alert.AlertType.WARNING);
                return;
            }

            // Verificar que no esté ya inscrito
            boolean yaInscrito = participaciones.stream()
                    .anyMatch(p -> p.getEvento().equals(evento) && 
                                 p.getPersona().equals(participante) && 
                                 p.getRol() == TipoRol.PARTICIPANTE);

            if (yaInscrito) {
                mostrarAlerta("Error", "El participante ya está inscrito en este evento", Alert.AlertType.WARNING);
                return;
            }

            // Intentar inscribir usando la lógica del modelo
            if (evento instanceof IEventoConInscripcion) {
                IEventoConInscripcion eventoConInscripcion = (IEventoConInscripcion) evento;
                eventoConInscripcion.inscribir(participante);
                
                // Agregar la participación
                RolEvento nuevaParticipacion = new RolEvento(evento, participante, TipoRol.PARTICIPANTE);
                participaciones.add(nuevaParticipacion);
                
                mostrarAlerta("Éxito", "Participante inscrito correctamente", Alert.AlertType.INFORMATION);
                cerrarModal();
                actualizarLista();
            } else {
                mostrarAlerta("Error", "Este evento no permite inscripciones", Alert.AlertType.ERROR);
            }

        } catch (IllegalStateException e) {
            mostrarAlerta("Error de Inscripción", e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) {
            mostrarAlerta("Error", "Error inesperado: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void desinscribirParticipante() {
        ParticipanteDTO seleccionado = tablaParticipantes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Selección requerida", "Debe seleccionar un participante para des-inscribir", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Des-inscripción");
        confirmacion.setHeaderText("¿Está seguro de des-inscribir al participante?");
        confirmacion.setContentText(seleccionado.getNombreCompleto() + " del evento " + seleccionado.getNombreEvento());

        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            // Buscar y remover la participación
            participaciones.removeIf(p -> 
                p.getEvento().equals(seleccionado.getEvento()) && 
                p.getPersona().equals(seleccionado.getPersona()) && 
                p.getRol() == TipoRol.PARTICIPANTE);

            // Decrementar contador si es evento con inscripción
            if (seleccionado.getEvento() instanceof IEventoConInscripcion) {
                if (seleccionado.getEvento() instanceof Taller) {
                    Taller taller = (Taller) seleccionado.getEvento();
                    taller.setInscriptos(Math.max(0, taller.getInscriptos() - 1));
                } else if (seleccionado.getEvento() instanceof Concierto) {
                    Concierto concierto = (Concierto) seleccionado.getEvento();
                    concierto.setInscriptos(Math.max(0, concierto.getInscriptos() - 1));
                }
            }

            mostrarAlerta("Éxito", "Participante des-inscrito correctamente", Alert.AlertType.INFORMATION);
            actualizarLista();
        }
    }

    @FXML
    private void verDetallesParticipante() {
        ParticipanteDTO seleccionado = tablaParticipantes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Selección requerida", "Debe seleccionar un participante para ver detalles", Alert.AlertType.WARNING);
            return;
        }

        // Llenar información del modal de detalles
        lblDetalleEvento.setText(seleccionado.getNombreEvento());
        lblDetalleParticipante.setText(seleccionado.getNombreCompleto());
        lblDetalleEstado.setText(seleccionado.getEstadoEvento());
        lblDetalleTipo.setText(seleccionado.getEvento().getTipoEvento().toString());
        lblDetalleFecha.setText(seleccionado.getEvento().getFechaInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

        // Información adicional específica del tipo de evento
        StringBuilder infoAdicional = new StringBuilder();
        Evento evento = seleccionado.getEvento();
        
        if (evento instanceof Taller) {
            Taller taller = (Taller) evento;
            infoAdicional.append("TALLER\n");
            infoAdicional.append("Modalidad: ").append(taller.getModalidad()).append("\n");
            infoAdicional.append("Cupo máximo: ").append(taller.getCupoMaximo()).append("\n");
            infoAdicional.append("Inscriptos actuales: ").append(taller.getInscriptos()).append("\n");
            if (taller.getInstructor() != null) {
                infoAdicional.append("Instructor: ").append(taller.getInstructor().toString());
            }
        } else if (evento instanceof Concierto) {
            Concierto concierto = (Concierto) evento;
            infoAdicional.append("CONCIERTO\n");
            infoAdicional.append("Tipo de entrada: ").append(concierto.getTipoEntrada()).append("\n");
            infoAdicional.append("Inscriptos: ").append(concierto.getInscriptos()).append("\n");
            if (!concierto.getArtistas().isEmpty()) {
                infoAdicional.append("Artistas: ");
                concierto.getArtistas().forEach(artista -> 
                    infoAdicional.append(artista.toString()).append(", "));
            }
        }

        txtDetalleInfo.setText(infoAdicional.toString());
        modalDetallesOverlay.setVisible(true);
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    // Clase DTO para la tabla de participantes
    public static class ParticipanteDTO {
        private final String nombreEvento;
        private final String nombreCompleto;
        private final String dni;
        private final String telefono;
        private final String email;
        private final String estadoEvento;
        private final Evento evento;
        private final Persona persona;

        public ParticipanteDTO(String nombreEvento, String nombreCompleto, String dni, 
                              String telefono, String email, String estadoEvento,
                              Evento evento, Persona persona) {
            this.nombreEvento = nombreEvento;
            this.nombreCompleto = nombreCompleto;
            this.dni = dni;
            this.telefono = telefono;
            this.email = email;
            this.estadoEvento = estadoEvento;
            this.evento = evento;
            this.persona = persona;
        }

        // Getters necesarios para las columnas de la tabla
        public String getNombreEvento() { return nombreEvento; }
        public String getNombreCompleto() { return nombreCompleto; }
        public String getDni() { return dni; }
        public String getTelefono() { return telefono; }
        public String getEmail() { return email; }
        public String getEstadoEvento() { return estadoEvento; }
        public Evento getEvento() { return evento; }
        public Persona getPersona() { return persona; }
    }
}