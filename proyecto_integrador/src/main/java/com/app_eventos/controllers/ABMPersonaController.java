package com.app_eventos.controllers;

import com.app_eventos.model.Persona;
import com.app_eventos.model.enums.TipoRol;
import com.app_eventos.repository.Repositorio;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.collections.FXCollections;
import javafx.scene.control.cell.PropertyValueFactory;

public class ABMPersonaController {

    private Repositorio repositorio = Repositorio.getInstance();

    // Filtros
    @FXML private ComboBox<TipoRol> comboTipoRolFiltro;
    @FXML private TextField txtNombreFiltro;
    @FXML private TextField txtDNIFiltro;

    // Tabla
    @FXML private TableView<Persona> tablaPersonas;
    @FXML private TableColumn<Persona, String> colNombre;
    @FXML private TableColumn<Persona, String> colDNI;
    @FXML private TableColumn<Persona, String> colTelefono;
    @FXML private TableColumn<Persona, String> colEmail;
    @FXML private TableColumn<Persona, String> colRol;

    // Modal
    @FXML private StackPane modalOverlay;
    @FXML private TextField txtDNI;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtEmail;
    @FXML private ComboBox<TipoRol> comboRol;

    // Modal modificación
    @FXML private StackPane modalModificarOverlay;

    @FXML
    public void initialize() {
        // Inicializar combos
        comboTipoRolFiltro.setItems(FXCollections.observableArrayList(TipoRol.values()));
        comboRol.setItems(FXCollections.observableArrayList(TipoRol.values()));
        modalOverlay.setVisible(false);  // Ocultar modal por defecto
        
        // Configurar columnas de la tabla
        configurarColumnasTabla();
        
        // Cargar datos iniciales
        actualizarTabla();
    }

    private void configurarColumnasTabla() {
        colNombre.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNombreCompleto()));
        colDNI.setCellValueFactory(new PropertyValueFactory<>("dni"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty("Sin rol asignado")); // Por ahora
    }

    private void actualizarTabla() {
        var personas = repositorio.obtenerTodasLasPersonas();
        tablaPersonas.setItems(FXCollections.observableArrayList(personas));
    }

    private void limpiarCampos() {
        txtDNI.clear();
        txtNombre.clear();
        txtApellido.clear();
        txtTelefono.clear();
        txtEmail.clear();
        comboRol.getSelectionModel().clearSelection();
    }

    @FXML
    private void mostrarModal() {
        limpiarCampos();
        modalOverlay.setVisible(true);
    }

    @FXML
    private void cerrarModal() {
        modalOverlay.setVisible(false);
    }

    private void limpiarEstilos() {
        txtDNI.getStyleClass().remove("campo-invalido");
        txtNombre.getStyleClass().remove("campo-invalido");
        txtApellido.getStyleClass().remove("campo-invalido");
        txtEmail.getStyleClass().remove("campo-invalido");
        comboRol.getStyleClass().remove("campo-invalido");
    }

    @FXML
    private void altaPersona() {
        // ===== VALIDACIONES EN CONTROLLER =====
        limpiarEstilos();
        boolean invalido = false;

        // Validar campos obligatorios
        if (txtDNI.getText().isBlank()) {
            txtDNI.getStyleClass().add("campo-invalido");
            invalido = true;
        }
        if (txtNombre.getText().isBlank()) {
            txtNombre.getStyleClass().add("campo-invalido");
            invalido = true;
        }
        if (txtApellido.getText().isBlank()) {
            txtApellido.getStyleClass().add("campo-invalido");
            invalido = true;
        }
        if (comboRol.getValue() == null) {
            comboRol.getStyleClass().add("campo-invalido");
            invalido = true;
        }

        // Validar formato DNI
        if (!txtDNI.getText().isBlank() && !txtDNI.getText().matches("\\d{7,10}")) {
            txtDNI.getStyleClass().add("campo-invalido");
            mostrarAlerta("DNI inválido", "El DNI debe contener solo números y tener entre 7 y 10 dígitos.");
            return;
        }

        // Validar formato email (si no está vacío)
        if (!txtEmail.getText().isBlank() && 
            !txtEmail.getText().matches("^[\\w-.]+@[\\w-]+\\.[a-zA-Z]{2,}$")) {
            txtEmail.getStyleClass().add("campo-invalido");
            mostrarAlerta("Email inválido", "El formato del email no es válido.");
            return;
        }

        if (invalido) {
            mostrarAlerta("Campos incompletos", "Complete todos los campos obligatorios.");
            return;
        }

        // Validar DNI único
        if (repositorio.existePersonaConDni(txtDNI.getText().trim())) {
            txtDNI.getStyleClass().add("campo-invalido");
            mostrarAlerta("DNI duplicado", "Ya existe una persona con ese DNI.");
            return;
        }

        try {
            // ===== DELEGAR AL MODELO =====
            Persona nuevaPersona = Persona.crearPersona(
                txtNombre.getText().trim(),
                txtApellido.getText().trim(),
                txtDNI.getText().trim(),
                txtTelefono.getText().trim(),
                txtEmail.getText().trim()
            );

            // ===== PERSISTIR =====
            repositorio.guardarPersona(nuevaPersona);

            // ===== ACTUALIZAR UI =====
            mostrarAlerta("Éxito", "Persona guardada correctamente.", Alert.AlertType.INFORMATION);
            cerrarModal();
            actualizarTabla(); // Actualizar la tabla con la nueva persona

        } catch (IllegalArgumentException e) {
            // Manejar errores del modelo (aunque no deberían ocurrir tras validaciones)
            mostrarAlerta("Error", e.getMessage());
        }
    }

    @FXML
    private void modificarPersona() {
        Object personaSeleccionada = tablaPersonas.getSelectionModel().getSelectedItem();

        if (personaSeleccionada != null) {
            // Acá se cargan los datos de la persona seleccionada
        } else {
        // mostrar alerta si no hay selección
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle("Selección requerida");
        alerta.setHeaderText(null);
        alerta.setContentText("Debe seleccionar una persona en la tabla para modificar.");
        alerta.showAndWait();
        }
    }

    @FXML
    private void bajaPersona() {
        Object personaSeleccionada = tablaPersonas.getSelectionModel().getSelectedItem();
        
        if (personaSeleccionada != null) {
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmar eliminación");
            confirmacion.setHeaderText(null);
            confirmacion.setContentText("¿Está seguro de que desea eliminar esta persona?");
            
            if (confirmacion.showAndWait().get() == ButtonType.OK) {
                try {
                    // TODO: Obtener ID de la persona seleccionada
                    // repositorio.eliminarPersona(persona.getIdPersona());
                    mostrarAlerta("Éxito", "Persona eliminada correctamente.", Alert.AlertType.INFORMATION);
                    // TODO: Actualizar tabla
                } catch (Exception e) {
                    mostrarAlerta("Error", "No se pudo eliminar la persona: " + e.getMessage());
                }
            }
        } else {
            mostrarAlerta("Selección requerida", "Debe seleccionar una persona en la tabla para eliminar.");
        }
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