package com.app_eventos.controllers;

import com.app_eventos.model.Persona;
import com.app_eventos.services.Servicio;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

import java.util.Optional;

public class ABMPersonaController {

    // Filtros de búsqueda por nombre y DNI
    @FXML private TextField txtNombreFiltro;
    @FXML private TextField txtDNIFiltro;

    // Tabla y columnas
    @FXML private TableView<Persona> tablaPersonas;
    @FXML private TableColumn<Persona, String> colNombre;
    @FXML private TableColumn<Persona, String> colApellido;
    @FXML private TableColumn<Persona, String> colDNI;
    @FXML private TableColumn<Persona, String> colTelefono;
    @FXML private TableColumn<Persona, String> colEmail;

    // Modal emergente con campos del formulario
    @FXML private StackPane modalOverlay;
    @FXML private TextField txtDNI;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtEmail;

    private final Servicio servicio = Servicio.getInstance();

    // Persona actualmente seleccionada en la tabla 
    private Persona personaSeleccionada = null;

    // Para saber si estamos modificando (true) o creando (false)
    private boolean modoEdicion = false;

    @FXML
    public void initialize() {
        // Configuración de las columnas
        tablaPersonas.widthProperty().addListener((obs, oldWidth, newWidth) -> { 
            double total = newWidth.doubleValue();

            colDNI.setPrefWidth(total * 0.15);        // 15%
            colNombre.setPrefWidth(total * 0.20);     // 20%
            colApellido.setPrefWidth(total * 0.20);   // 20%
            colTelefono.setPrefWidth(total * 0.15);   // 15%
            colEmail.setPrefWidth(total * 0.30);      // 30%
        });

        // Configuración de las columnas para que se muestren datos del modelo Persona
        colNombre.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getNombre()));
        colApellido.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getApellido()));
        colDNI.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDni()));
        colTelefono.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getTelefono()));
        colEmail.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getEmail()));

        // Cargar la tabla con la lista completa de personas
        tablaPersonas.setItems(servicio.obtenerPersonas());

        // Filtros reactivos: cuando el usuario escribe, se filtran los resultados
        txtNombreFiltro.setOnKeyReleased(this::filtrar);
        txtDNIFiltro.setOnKeyReleased(this::filtrar);

        // Escuchar clics en la tabla para seleccionar personas
        tablaPersonas.setOnMouseClicked(this::onSeleccionarFila);

        // Deseleccionar persona si se hace clic fuera de la tabla
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

        // Ocultar el modal al principio
        modalOverlay.setVisible(false);
    }

    // Abre el modal para crear una nueva persona.
    @FXML
    public void mostrarModal() {
        modoEdicion = false;
        limpiarFormulario();
        modalOverlay.setVisible(true);
    }

    // Abre el modal para modificar una persona seleccionada.
    @FXML
    public void modificarPersona() {
        if (personaSeleccionada == null) {
            mostrarAlerta("Debe seleccionar una persona para modificar.");
            return;
        }

        modoEdicion = true;

        // Precargar datos en los campos del modal
        txtNombre.setText(personaSeleccionada.getNombre());
        txtApellido.setText(personaSeleccionada.getApellido());
        txtDNI.setText(personaSeleccionada.getDni());
        txtTelefono.setText(personaSeleccionada.getTelefono());
        txtEmail.setText(personaSeleccionada.getEmail());

        modalOverlay.setVisible(true);
    }

    // Elimina una persona seleccionada con confirmación.
    @FXML
    public void eliminarPersona() {
        if (personaSeleccionada == null) {
            mostrarAlerta("Debe seleccionar una persona para eliminar.");
            return;
        }

        // Mostrar alerta de confirmación
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmación");
        confirmacion.setHeaderText("¿Está seguro que desea eliminar a " + personaSeleccionada + "?");

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            servicio.eliminarPersona(personaSeleccionada);
            personaSeleccionada = null;
        }
    }

    // Guarda una nueva persona o actualiza una existente, según el modo actual.
    @FXML
    public void altaPersona() {
        try {
            // Crear persona con validaciones incluidas en su constructor
            Persona nueva = new Persona(
                txtNombre.getText(),
                txtApellido.getText(),
                txtDNI.getText(),
                txtTelefono.getText(),
                txtEmail.getText()
            );

            if (modoEdicion) {
                // Actualizar persona seleccionada
                servicio.actualizarPersona(personaSeleccionada, nueva);
                tablaPersonas.refresh();
            } else {
                // Guardar nueva persona
                servicio.guardarPersona(nueva);
            }

            // Refrescar tabla con lista actualizada
            tablaPersonas.setItems(servicio.obtenerPersonas());

            // Cerrar y limpiar el modal
            modalOverlay.setVisible(false);
            limpiarFormulario();
            personaSeleccionada = null;

        } catch (Exception e) {
            mostrarAlerta(e.getMessage());  // Mostrar error si alguna validación falla
        }
    }

    // Cierra el formulario emergente sin guardar cambios.
    @FXML
    public void cerrarModal() {
        modalOverlay.setVisible(false);
        limpiarFormulario();
    }

    // Aplica el filtro a la tabla según nombre y/o DNI ingresados.
    private void filtrar(KeyEvent e) {
        ObservableList<Persona> filtradas = servicio.filtrarPersonas(
            txtNombreFiltro.getText(),
            txtDNIFiltro.getText()
        );
        tablaPersonas.setItems(filtradas);
    }

    // Guarda en memoria la persona seleccionada en la tabla.
    private void onSeleccionarFila(MouseEvent e) {
        personaSeleccionada = tablaPersonas.getSelectionModel().getSelectedItem();
    }

    // Limpia todos los campos del formulario (modal).
    private void limpiarFormulario() {
        txtNombre.clear();
        txtApellido.clear();
        txtDNI.clear();
        txtTelefono.clear();
        txtEmail.clear();
    }

    // Muestra un mensaje de error en pantalla.
    private void mostrarAlerta(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
    
    // Método para refrescar datos cuando se navega a esta ventana
    public void refrescarDatos() {
        tablaPersonas.setItems(servicio.obtenerPersonas());
    }
}
