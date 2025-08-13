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

    // --- Controles del FXML ---
    @FXML private ComboBox<Persona> comboPersona;     // Combo para elegir persona desde la lista del Servicio (en memoria)
    @FXML private ComboBox<TipoRol> comboTipoRol;     // Combo para elegir el rol a asignar (según lo permita el Evento)
    @FXML private TableView<RolEvento> tablaRoles;    // Tabla que muestra roles asignados al evento seleccionado
    @FXML private TableColumn<RolEvento, String> colDni;
    @FXML private TableColumn<RolEvento, String> colNombre;
    @FXML private TableColumn<RolEvento, TipoRol> colRol;

    // Lista observable que se vincula a la tabla
    private final ObservableList<RolEvento> rolesEvento = FXCollections.observableArrayList();

    // Servicio singleton. IMPORTANTE: en esta versión, las personas vienen de una lista en memoria.
    // La persistencia REAL de roles/participaciones se hace a través de Servicio -> Repositorio en otras pantallas (ABMParticipante).
    private final Servicio servicio = Servicio.getInstance();

    // Evento actual sobre el cual estamos agregando/eliminando roles (lo setea el padre antes de abrir el modal)
    private Evento evento;

    // Callback que el padre (ABMEventoController) registra para refrescar la grilla principal cuando cambian los roles
    private Consumer<Evento> onRolesChanged;

    @FXML
    public void initialize() {
        // --- POBLAR COMBO PERSONAS ---
        // Se usa la lista observable del Servicio (hoy en memoria). Desde aquí NO insertamos en BD; solo “pre-asignamos” en el modelo Evento.
        comboPersona.setItems(servicio.obtenerPersonas());

        // Mostrar en el combo: "DNI - Nombre Apellido"
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

        // --- CONFIG TABLA ---
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
        // Validación: debe existir un evento sobre el que operar
        if (evento == null) {
            mostrarError("Error", "No hay evento seleccionado.");
            return;
        }
        // Tomar selección de UI
        Persona persona = comboPersona.getValue();
        TipoRol rol = comboTipoRol.getValue();

        if (persona == null || rol == null) {
            mostrarAdvertencia("Atención", "Debe seleccionar persona y rol.");
            return;
        }

        try {
            // REGLAS DE NEGOCIO ESPECÍFICAS POR TIPO
            // Nota: esto afecta el modelo en memoria (lista de roles del Evento).
            // La persistencia de estos roles (si corresponde) la hace ABMParticipante/Servicio.guardarParticipacion(...)
            if (evento instanceof Taller && rol == TipoRol.INSTRUCTOR) {
                ((Taller) evento).asignarInstructor(persona); // aplica tope 1 instructor en el modelo
            } else if (evento instanceof Exposicion && rol == TipoRol.CURADOR) {
                ((Exposicion) evento).asignarCurador(persona); // aplica tope 1 curador en el modelo
            } else {
                // Resto de roles: agrega un RolEvento al conjunto del Evento
                evento.agregarResponsable(persona, rol);
            }

            // Refresca tabla y notifica al padre para que refresque la grilla principal
            refrescarTabla();
            notificarCambio();

            // Limpiar selección del modal
            comboPersona.getSelectionModel().clearSelection();
            comboTipoRol.getSelectionModel().clearSelection();

        } catch (IllegalArgumentException | IllegalStateException ex) {
            // Errores típicos: duplicado (misma persona+rol), violación de invariante de modelo, etc.
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
            // Quita el rol del conjunto del Evento (en memoria)
            evento.borrarResponsable(seleccionado.getPersona(), seleccionado.getRol());

            // Refresca UI y avisa al padre
            refrescarTabla();
            notificarCambio();

        } catch (IllegalStateException ex) {
            mostrarError("No se pudo eliminar", ex.getMessage());
        }
    }

    // Botón "Aceptar" del modal. Valida la invariante y cierra.
    // Importante: aquí NO se hace persistencia. Quien persiste “participantes” es ABMParticipanteController mediante Servicio/Repositorio.
    @FXML
    public void aceptar() {
        if (evento == null) return;
        try {
            // Invariante de dominio: “todo evento debe tener al menos un ORGANIZADOR”
            evento.validarInvariantes();
            notificarCambio();
            cerrarVentana();
        } catch (IllegalStateException ex) {
            mostrarError("Validación", ex.getMessage());
        }
    }

    // Cierra la ventana (el diálogo de asignación de roles)
    private void cerrarVentana() {
        if (tablaRoles.getScene() != null && tablaRoles.getScene().getWindow() != null) {
            tablaRoles.getScene().getWindow().hide();
        }
    }

    // Recarga los datos del evento actual en la tabla
    private void refrescarTabla() {
        rolesEvento.setAll(evento.getRoles());
        tablaRoles.refresh();
    }

    // Llama al callback para que el padre refresque la tabla principal
    private void notificarCambio() {
        if (onRolesChanged != null && evento != null) {
            onRolesChanged.accept(evento);
        }
    }

    // Helpers de UI para alertas
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

    // ===== API para el padre (ABMEventoController) =====

    /** El padre pasa el Evento sobre el que se gestionan roles. */
    public void setEvento(Evento evento) {
        this.evento = evento;
        if (evento != null) {
            // Limita el combo de roles a los permitidos por ese tipo de evento (reglas del dominio)
            comboTipoRol.getItems().setAll(evento.rolesPermitidosParaAsignacion());
            refrescarTabla();
        } else {
            comboTipoRol.getItems().clear();
            rolesEvento.clear();
        }
    }

    /** El padre registra un callback para saber cuándo cambian los roles. */
    public void setOnRolesChanged(Consumer<Evento> onRolesChanged) {
        this.onRolesChanged = onRolesChanged;
    }

    /** Devuelve los roles que se ven en la tabla (por si el padre los necesita). */
    public ObservableList<RolEvento> getRolesAsignados() {
        return rolesEvento;
    }
}
