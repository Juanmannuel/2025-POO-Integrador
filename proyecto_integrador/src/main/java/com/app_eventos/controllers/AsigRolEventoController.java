package com.app_eventos.controllers;

import com.app_eventos.model.Evento;
import com.app_eventos.model.Persona;
import com.app_eventos.model.RolEvento;
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

/**
 * Modal de asignación de roles..
 */
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
    private Consumer<Evento> onRolesChanged;

    @FXML
    public void initialize() {
        // Personas para el combo
        comboPersona.setItems(servicio.obtenerPersonas());
        comboPersona.setConverter(new StringConverter<>() {
            @Override public String toString(Persona p) {
                if (p == null) return "";
                String dni = p.getDni() != null ? p.getDni() : "-";
                return dni + " - " + p.getNombre() + " " + p.getApellido();
            }
            @Override public Persona fromString(String s) { return null; }
        });

        // Tabla
        tablaRoles.setPlaceholder(new Label("Tabla sin contenido"));
        colDni.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPersona().getDni()));
        colNombre.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getPersona().getNombre() + " " + d.getValue().getPersona().getApellido()
        ));
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
            mostrarAdvertencia("Atención", "Debe seleccionar persona y rol.");
            return;
        }
        try {
            // Persistir (evita duplicados persona+evento+rol)
            var creado = servicio.asignarRol(evento, persona, rol);
            if (creado == null) {
                mostrarAdvertencia("Aviso", "Ese rol ya está asignado a esa persona.");
            }
            // Refrescar tabla desde BD
            refrescarTablaDesdeBD();
            notificarCambio();

            comboPersona.getSelectionModel().clearSelection();
            comboTipoRol.getSelectionModel().clearSelection();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            mostrarError("No se pudo agregar", ex.getMessage());
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
            servicio.eliminarRol(evento, seleccionado.getPersona(), seleccionado.getRol());
            refrescarTablaDesdeBD();
            notificarCambio();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            mostrarError("No se pudo eliminar", ex.getMessage());
        }
    }

    @FXML
    public void aceptar() {
        if (evento == null) { cerrarVentana(); return; }
        boolean tieneOrganizador = rolesEvento.stream().anyMatch(r -> r.getRol() == TipoRol.ORGANIZADOR);
        if (!tieneOrganizador) {
            mostrarAdvertencia("Validación", "Todo evento debe tener al menos un organizador.");
            return;
        }
        notificarCambio();
        cerrarVentana();
    }

    private void cerrarVentana() {
        if (tablaRoles.getScene() != null && tablaRoles.getScene().getWindow() != null) {
            tablaRoles.getScene().getWindow().hide();
        }
    }

    private void refrescarTablaDesdeBD() {
        rolesEvento.setAll(servicio.obtenerRolesDeEvento(evento));
        tablaRoles.refresh();
    }

    private void notificarCambio() {
        if (onRolesChanged != null && evento != null) onRolesChanged.accept(evento);
    }

    private void mostrarAdvertencia(String titulo, String mensaje) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(titulo); a.setHeaderText(null); a.setContentText(mensaje); a.showAndWait();
    }
    private void mostrarError(String titulo, String mensaje) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(titulo); a.setHeaderText(null); a.setContentText(mensaje); a.showAndWait();
    }

    // ===== API para el padre =====
    public void setEvento(Evento evento) {
        this.evento = evento;
        if (evento != null) {
            // Limitar roles permitidos según tipo
            comboTipoRol.getItems().setAll(evento.rolesPermitidosParaAsignacion());
            refrescarTablaDesdeBD();
        } else {
            comboTipoRol.getItems().clear();
            rolesEvento.clear();
        }
    }
    public void setOnRolesChanged(Consumer<Evento> onRolesChanged) { this.onRolesChanged = onRolesChanged; }
    public ObservableList<RolEvento> getRolesAsignados() { return rolesEvento; }
}
