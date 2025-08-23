package com.app_eventos.controllers;

import com.app_eventos.model.Persona;
import com.app_eventos.services.Servicio;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import org.hibernate.exception.ConstraintViolationException;

import java.util.Optional;

public class ABMPersonaController {

    // Filtros
    @FXML private TextField txtNombreFiltro;
    @FXML private TextField txtDNIFiltro;

    // Tabla
    @FXML private TableView<Persona> tablaPersonas;
    @FXML private TableColumn<Persona, String> colNombre;
    @FXML private TableColumn<Persona, String> colApellido;
    @FXML private TableColumn<Persona, String> colDNI;
    @FXML private TableColumn<Persona, String> colTelefono;
    @FXML private TableColumn<Persona, String> colEmail;

    // Modal
    @FXML private StackPane modalOverlay;
    @FXML private TextField txtDNI;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtEmail;

    private final Servicio servicio = Servicio.getInstance();

    private Persona personaSeleccionada = null;
    private boolean modoEdicion = false;

    @FXML
    public void initialize() {
        // Responsivo
        tablaPersonas.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            double total = newWidth.doubleValue();
            colDNI.setPrefWidth(total * 0.15);
            colNombre.setPrefWidth(total * 0.20);
            colApellido.setPrefWidth(total * 0.20);
            colTelefono.setPrefWidth(total * 0.15);
            colEmail.setPrefWidth(total * 0.30);
        });

        // Mapeo columnas
        colNombre.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getNombre()));
        colApellido.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getApellido()));
        colDNI.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getDni()));
        colTelefono.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getTelefono()));
        colEmail.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getEmail()));

        // Carga inicial
        tablaPersonas.setItems(servicio.obtenerPersonas());

        // Filtros reactivos
        txtNombreFiltro.setOnKeyReleased(this::filtrar);
        txtDNIFiltro.setOnKeyReleased(this::filtrar);

        // Selección
        tablaPersonas.setOnMouseClicked(this::onSeleccionarFila);

        // Deseleccionar al click afuera
        tablaPersonas.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnMousePressed(event -> {
                    if (!tablaPersonas.isHover()) {
                        tablaPersonas.getSelectionModel().clearSelection();
                        personaSeleccionada = null;
                    }
                });
            }
        });

        modalOverlay.setVisible(false);
    }

    // Acciones Toolbar / Modal

    @FXML
    public void mostrarModal() {
        modoEdicion = false;
        limpiarFormulario();
        modalOverlay.setVisible(true);
    }

    @FXML
    public void altaPersona() {
        try {
            // Validar duplicados antes de crear/actualizar
            if (!modoEdicion) {
                // Validación para nueva persona
                if (existePersonaConDNI(txtDNI.getText())) {
                    error("Ya existe una persona con ese DNI.");
                    return;
                }
                if (existePersonaConTelefono(txtTelefono.getText())) {
                    error("Ya existe una persona con ese teléfono.");
                    return;
                }
            } else {
                // Validación para edición (excluir la persona actual)
                if (existePersonaConDNI(txtDNI.getText(), personaSeleccionada.getIdPersona())) {
                    error("Ya existe otra persona con ese DNI.");
                    return;
                }
                if (existePersonaConTelefono(txtTelefono.getText(), personaSeleccionada.getIdPersona())) {
                    error("Ya existe otra persona con ese teléfono.");
                    return;
                }
            }

            Persona nueva = new Persona(
                txtNombre.getText(),
                txtApellido.getText(),
                txtDNI.getText(),
                txtTelefono.getText(),
                txtEmail.getText()
            );

            if (modoEdicion) {
                servicio.actualizarPersona(personaSeleccionada, nueva);
            } else {
                servicio.guardarPersona(nueva);
            }
            // Limpiar formulario
            refrescarDatos();

            // Cerrar modal
            cerrarModal();

        } catch (RuntimeException ex) {error(mensajeDNI(ex)); } // Valida y traduce errores de unicidad/datos
        catch (Exception ex) {
            error(ex.getMessage());
        }
    }

    @FXML
    public void modificarPersona() {
        if (personaSeleccionada == null) {
            advertencia("Debe seleccionar una persona para modificar.");
            return;
        }
        modoEdicion = true;

        txtNombre.setText(personaSeleccionada.getNombre());
        txtApellido.setText(personaSeleccionada.getApellido());
        txtDNI.setText(personaSeleccionada.getDni());
        txtTelefono.setText(personaSeleccionada.getTelefono());
        txtEmail.setText(personaSeleccionada.getEmail());

        modalOverlay.setVisible(true);
    }

    @FXML
    public void eliminarPersona() {
        if (personaSeleccionada == null) {
            advertencia("Debe seleccionar una persona para dar de baja.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmación");
        confirm.setHeaderText("¿Desea dar de baja a " + personaSeleccionada + "?");
        confirm.setContentText("Esta acción no se puede deshacer.");
        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            try {
                servicio.eliminarPersona(personaSeleccionada);
                personaSeleccionada = null;
                // Releer desde BD
                refrescarDatos();
            } catch (RuntimeException ex) { error(mensajeDNI(ex)); }
        }
    }

    @FXML
    public void cerrarModal() {
        modalOverlay.setVisible(false);
        limpiarFormulario();
        personaSeleccionada = null;
        modoEdicion = false;
    }

    // Filtros / Tabla

    private void filtrar(KeyEvent e) {
        ObservableList<Persona> filtradas = servicio.filtrarPersonas(
            txtNombreFiltro.getText(),
            txtDNIFiltro.getText()
        );
        tablaPersonas.setItems(filtradas);
    }

    private void onSeleccionarFila(MouseEvent e) {
        personaSeleccionada = tablaPersonas.getSelectionModel().getSelectedItem();
    }

    public void refrescarDatos() {
        tablaPersonas.setItems(servicio.obtenerPersonas());
        tablaPersonas.refresh();
    }

    // Helpers

    private void limpiarFormulario() {
        txtNombre.clear();
        txtApellido.clear();
        txtDNI.clear();
        txtTelefono.clear();
        txtEmail.clear();
    }

    private void advertencia(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Advertencia");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void error(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private String mensajeDNI(Throwable ex) {
        Throwable cause = ex;
        while (cause.getCause() != null) cause = cause.getCause();

        // Hibernate constraint
        if (cause instanceof ConstraintViolationException cve) {
            String constraint = cve.getConstraintName() == null ? "" : cve.getConstraintName().toLowerCase();
            String message = cve.getMessage() == null ? "" : cve.getMessage().toLowerCase();
            
            if (constraint.contains("dni") || constraint.contains("uk") || constraint.contains("unique") || 
                message.contains("dni") || message.contains("unique constraint")) {
                return "Ya existe una persona con ese DNI.";
            }
            if (constraint.contains("telefono") || constraint.contains("uk") || constraint.contains("unique") || 
                message.contains("telefono") || message.contains("unique constraint")) {
                return "Ya existe una persona con ese teléfono.";
            }
            // Fallback para cualquier otra restricción única
            if (constraint.contains("uk") || constraint.contains("unique") || 
                message.contains("unique constraint")) {
                return "Ya existe una persona con los datos ingresados.";
            }
        }

        // Validaciones propias
        if (ex instanceof IllegalArgumentException || ex.getCause() instanceof IllegalArgumentException) {
            return ex.getMessage();
        }

        // Fallback
        String m = ex.getMessage();
        return (m == null || m.isBlank()) ? "Ocurrió un error inesperado." : m;
    }

    // Métodos de validación para duplicados
    private boolean existePersonaConDNI(String dni) {
        if (dni == null || dni.trim().isEmpty()) return false;
        String dniLimpio = dni.replaceAll("\\D+", ""); // Solo dígitos
        ObservableList<Persona> personas = servicio.obtenerPersonas();
        return personas.stream()
                .anyMatch(p -> dniLimpio.equals(p.getDni()));
    }

    private boolean existePersonaConDNI(String dni, Long idExcluir) {
        if (dni == null || dni.trim().isEmpty()) return false;
        String dniLimpio = dni.replaceAll("\\D+", ""); // Solo dígitos
        ObservableList<Persona> personas = servicio.obtenerPersonas();
        return personas.stream()
                .anyMatch(p -> dniLimpio.equals(p.getDni()) && !idExcluir.equals(p.getIdPersona()));
    }

    private boolean existePersonaConTelefono(String telefono) {
        if (telefono == null || telefono.trim().isEmpty()) return false;
        String telefonoLimpio = telefono.replaceAll("\\D+", ""); // Solo dígitos
        ObservableList<Persona> personas = servicio.obtenerPersonas();
        return personas.stream()
                .anyMatch(p -> telefonoLimpio.equals(p.getTelefono()));
    }

    private boolean existePersonaConTelefono(String telefono, Long idExcluir) {
        if (telefono == null || telefono.trim().isEmpty()) return false;
        String telefonoLimpio = telefono.replaceAll("\\D+", ""); // Solo dígitos
        ObservableList<Persona> personas = servicio.obtenerPersonas();
        return personas.stream()
                .anyMatch(p -> telefonoLimpio.equals(p.getTelefono()) && !idExcluir.equals(p.getIdPersona()));
    }
}