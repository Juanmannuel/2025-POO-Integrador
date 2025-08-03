package com.app_eventos.controllers;

import com.app_eventos.model.enums.TipoEvento;
import com.app_eventos.model.enums.TipoEntrada;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.util.StringConverter;

import java.io.IOException;

public class ABMEventoController {

    // Campos de búsqueda y modal
    @FXML private TextField txtNombre;
    @FXML private TextField txtDescripcion;
    @FXML private DatePicker dateInicio;
    @FXML private DatePicker dateFin;
    @FXML private ComboBox<TipoEvento> comboTipoEvento;
    @FXML private ComboBox<TipoEntrada> comboTipoEntrada;
    @FXML private StackPane modalOverlay;
    @FXML private Pane seccionDinamica;

    @FXML
    public void initialize() {
        comboTipoEvento.getItems().setAll(TipoEvento.values());
        comboTipoEvento.setConverter(new StringConverter<>() {
            @Override
            public String toString(TipoEvento tipo) {
                return tipo != null ? tipo.name().replace("_", " ") : "";
            }
            @Override
            public TipoEvento fromString(String s) {
                return TipoEvento.valueOf(s.replace(" ", "_"));
            }
        });

        comboTipoEntrada.getItems().setAll(TipoEntrada.values());
        comboTipoEntrada.setConverter(new StringConverter<>() {
        @Override
        public String toString(TipoEntrada entrada) {
            return entrada != null ? entrada.name().charAt(0) + entrada.name().substring(1).toLowerCase() : "";
        }

        @Override
        public TipoEntrada fromString(String s) {
            return TipoEntrada.valueOf(s.toUpperCase());
        }
        });

        comboTipoEvento.setOnAction(event -> cargarFormTipoEvento(comboTipoEvento.getValue()));
        
        txtNombre.textProperty().addListener((obs, oldVal, newVal) -> txtNombre.getStyleClass().remove("campo-invalido"));

        comboTipoEvento.valueProperty().addListener((obs, oldVal, newVal) -> comboTipoEvento.getStyleClass().remove("campo-invalido"));

        dateInicio.valueProperty().addListener((obs, oldVal, newVal) -> dateInicio.getStyleClass().remove("campo-invalido"));

        dateFin.valueProperty().addListener((obs, oldVal, newVal) -> dateFin.getStyleClass().remove("campo-invalido"));

        comboTipoEntrada.valueProperty().addListener((obs, oldVal, newVal) -> comboTipoEntrada.getStyleClass().remove("campo-invalido"));
        
    }

    private void cargarFormTipoEvento(TipoEvento tipoSeleccionado) {
        if (seccionDinamica == null || tipoSeleccionado == null) return;
        seccionDinamica.getChildren().clear();

        try {
            Node fragmento = switch (tipoSeleccionado) {
                case FERIA -> FXMLLoader.load(getClass().getResource("/fxml/abm/fragmentos/feria.fxml"));
                case TALLER -> FXMLLoader.load(getClass().getResource("/fxml/abm/fragmentos/taller.fxml"));
                case EXPOSICION -> FXMLLoader.load(getClass().getResource("/fxml/abm/fragmentos/exposicion.fxml"));
                case CONCIERTO -> FXMLLoader.load(getClass().getResource("/fxml/abm/fragmentos/concierto.fxml"));
                case CICLO_CINE -> FXMLLoader.load(getClass().getResource("/fxml/abm/fragmentos/ciclo_cine.fxml"));
            };
            seccionDinamica.getChildren().add(fragmento);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // -- Modal --
    @FXML
    private void mostrarModal() {
        modalOverlay.setVisible(true);
        modalOverlay.toFront();
    }

    @FXML
    private void cerrarModal() {
        limpiarEstilos();

        txtNombre.clear();
        txtDescripcion.clear();
        dateInicio.setValue(null);
        dateFin.setValue(null);

        modalOverlay.setVisible(false);
    }

    private void mostrarAlerta(String titulo, String mensaje) {
    Alert alerta = new Alert(Alert.AlertType.WARNING);
    alerta.setTitle(titulo);
    alerta.setHeaderText(null);
    alerta.setContentText(mensaje);
    alerta.showAndWait();
    }

    private void limpiarEstilos() {
    txtNombre.getStyleClass().remove("campo-invalido");
    comboTipoEvento.getStyleClass().remove("campo-invalido");
    dateInicio.getStyleClass().remove("campo-invalido");
    dateFin.getStyleClass().remove("campo-invalido");
    comboTipoEntrada.getStyleClass().remove("campo-invalido");
    }

    @FXML
    private void guardarEvento() {
        // Limpia estados anteriores
        limpiarEstilos();

        boolean invalido = false;

        if (txtNombre.getText().isBlank()) {
            txtNombre.getStyleClass().add("campo-invalido");
            invalido = true;
        }

        if (comboTipoEvento.getValue() == null) {
            comboTipoEvento.getStyleClass().add("campo-invalido");
            invalido = true;
        }

        if (dateInicio.getValue() == null) {
            dateInicio.getStyleClass().add("campo-invalido");
            invalido = true;
        }

        if (dateFin.getValue() == null) {
            dateFin.getStyleClass().add("campo-invalido");
            invalido = true;
        }

        if (comboTipoEntrada.getValue() == null) {
            comboTipoEntrada.getStyleClass().add("campo-invalido");
            invalido = true;
        }

        if (invalido) {
            mostrarAlerta("Campos incompletos", "Todos los campos son obligatorios.");
            return;
        }

        if (dateFin.getValue().isBefore(dateInicio.getValue())) {
            dateFin.getStyleClass().add("campo-invalido");
            mostrarAlerta("Error en fechas", "La fecha de fin no puede ser anterior a la fecha de inicio.");
            return;
        }
            // Acá continuaría con la lógica de guardar evento
            cerrarModal();
        }
    }
