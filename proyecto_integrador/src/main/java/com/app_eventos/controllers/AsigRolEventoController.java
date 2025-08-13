package com.app_eventos.controllers;

import com.app_eventos.model.Evento;
import com.app_eventos.model.Exposicion;
import com.app_eventos.model.Persona;
import com.app_eventos.model.RolEvento;
import com.app_eventos.model.Taller;
import com.app_eventos.model.enums.TipoRol;
import com.app_eventos.services.Servicio;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.util.StringConverter;

import java.util.function.Consumer;

public class AsigRolEventoController {

    @FXML private ComboBox<Persona> comboPersona;
    @FXML private ComboBox<TipoRol> comboTipoRol;
    @FXML private TableView<RolEvento> tablaRoles;
    @FXML private TableColumn<RolEvento, String> colDni;
    @FXML private TableColumn<RolEvento, String> colNombre;
    @FXML private TableColumn<RolEvento, TipoRol> colRol;

    private final ObservableList<RolEvento> rolesEvento = FXCollections.observableArrayList();
    private final Servicio servicio = Servicio.getInstance();
    private Evento evento;

    // Callback al controlador padre para refrescar la grilla principal
    private Consumer<Evento> onRolesChanged;

    @FXML
    public void initialize() {
        // Personas
        comboPersona.setItems(servicio.obtenerPersonas());
        comboPersona.setConverter(new StringConverter<>() {
            @Override public String toString(Persona p) {
                if (p == null) return "";
                String dni = p.getDni() != null ? p.getDni() : "-";
                String nombre = p.getNombre() != null ? p.getNombre() : "";
                String apellido = p.getApellido() != null ? p.getApellido() : "";
                return dni + " - " + nombre + " " + apellido;
            }
            @Override public Persona fromString(String s) { return null; }
        });

        // Tabla
        tablaRoles.setPlaceholder(new Label("Tabla sin contenido"));
        colDni.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getPersona().getDni()));
        colNombre.setCellValueFactory(data ->
            new SimpleStringProperty(
                data.getValue().getPersona().getNombre() + " " + data.getValue().getPersona().getApellido()
            )
        );
        colRol.setCellValueFactory(new PropertyValueFactory<>("rol"));

        tablaRoles.setItems(rolesEvento);
    }

    @FXML
    public void agregarRol() {
        if (evento == null) {
            mostrarError("Error", "No hay evento seleccionado.");
            return;
        }
        Persona persona = comboPersona.getValue();
        TipoRol rol = comboTipoRol.getValue();

        if (persona == null || rol == null) {
            mostrarAdvertencia("Atención", "Debe seleccionar una persona y un rol.");
            return;
        }

        try {
            // Derivá a métodos específicos cuando apliquen topes
            if (evento instanceof Taller && rol == TipoRol.INSTRUCTOR) {
                ((Taller) evento).asignarInstructor(persona);
            } else if (evento instanceof Exposicion && rol == TipoRol.CURADOR) {
                ((Exposicion) evento).asignarCurador(persona);
            } else {
                // Resto de roles por el camino genérico
                evento.agregarResponsable(persona, rol);
            }

            refrescarTabla();
            notificarCambio();

            comboPersona.getSelectionModel().clearSelection();
            comboTipoRol.getSelectionModel().clearSelection();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            mostrarError("No se pudo agregar el rol", ex.getMessage());
        }
    }

    @FXML
    public void eliminarRolSeleccionado() {
        if (evento == null) {
            mostrarError("Error", "No hay evento seleccionado.");
            return;
        }
        RolEvento seleccionado = tablaRoles.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAdvertencia("Atención", "Debe seleccionar la fila a eliminar.");
            return;
        }
        try {
            evento.borrarResponsable(seleccionado.getPersona(), seleccionado.getRol());
            refrescarTabla();
            notificarCambio();
        } catch (IllegalStateException ex) {
            mostrarError("No se pudo eliminar", ex.getMessage());
        }
    }

    // Si tenés botón Aceptar en el modal:
    @FXML
    public void aceptar() {
        if (evento == null) return;
        try {
            evento.validarInvariantes(); // "Todo evento debe tener al menos un organizador."
            notificarCambio();
            cerrarVentana();
        } catch (IllegalStateException ex) {
            mostrarError("Validación", ex.getMessage());
        }
    }

    private void cerrarVentana() {
        if (tablaRoles.getScene() != null && tablaRoles.getScene().getWindow() != null) {
            tablaRoles.getScene().getWindow().hide();
        }
    }

    private void refrescarTabla() {
        rolesEvento.setAll(evento.getRoles());
        tablaRoles.refresh();
    }

    private void notificarCambio() {
        if (onRolesChanged != null && evento != null) {
            onRolesChanged.accept(evento);
        }
    }

    private void mostrarAdvertencia(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    // ----- API para el padre -----
    /** El padre debe pasar el evento seleccionado al abrir el modal. */
    public void setEvento(Evento evento) {
        this.evento = evento;
        if (evento != null) {
            comboTipoRol.getItems().setAll(evento.rolesPermitidosParaAsignacion());
            refrescarTabla();
        } else {
            comboTipoRol.getItems().clear();
            rolesEvento.clear();
        }
    }

    /** El padre puede registrar un callback para refrescar la grilla principal. */
    public void setOnRolesChanged(Consumer<Evento> onRolesChanged) {
        this.onRolesChanged = onRolesChanged;
    }

    public ObservableList<RolEvento> getRolesAsignados() {
        return rolesEvento;
    }
}
