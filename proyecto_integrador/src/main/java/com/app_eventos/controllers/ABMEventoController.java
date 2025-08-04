package com.app_eventos.controllers;

import com.app_eventos.model.enums.TipoEvento;
import com.app_eventos.model.enums.EstadoEvento;
import com.app_eventos.model.enums.TipoEntrada;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.util.StringConverter;
import java.util.Arrays;

import java.io.IOException;


public class ABMEventoController {

    // Campos de búsqueda y modal
    @FXML private TextField txtNombre;
    @FXML private DatePicker dateInicio;
    @FXML private DatePicker dateFin;
    @FXML private ComboBox<TipoEvento> comboTipoEvento;
    @FXML private ComboBox<TipoEntrada> comboTipoEntrada;
    @FXML private ComboBox<EstadoEvento> comboEstado;
    @FXML private StackPane modalOverlay;
    @FXML private Pane seccionDinamica;
    @FXML private Spinner<Integer> spinnerDuracion;

    @FXML
    public void initialize() {

        // Cargar enum TipoEvento
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

        // Cargar enum TipoEntrada
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

        // Cargar enum TipoEvento
        comboEstado.getItems().setAll(
            Arrays.stream(EstadoEvento.values())
            .filter(estado -> estado != EstadoEvento.CANCELADO && estado != EstadoEvento.FINALIZADO)
            .toList()
        );
        comboEstado.setConverter(new StringConverter<>() {
            @Override
            public String toString(EstadoEvento estado) {
                return estado != null ? estado.name().charAt(0) + estado.name().substring(1).toLowerCase() : "";
            }

            @Override
            public EstadoEvento fromString(String s) {
                return EstadoEvento.valueOf(s.toUpperCase());
            }
        });

        // Configurar Spinner de duración
        spinnerDuracion.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 365, 1));
        spinnerDuracion.setEditable(false);

        // Cargar fragmento al cambiar tipo de evento
        comboTipoEvento.valueProperty().addListener((obs, oldVal, newVal) -> {cargarFragmentoEspecifico(newVal);
            comboTipoEvento.getStyleClass().remove("campo-invalido");
        });

        // Listeners para limpiar el estilo de error si el usuario lo corrige
        txtNombre.textProperty().addListener((obs, oldVal, newVal) -> txtNombre.getStyleClass().remove("campo-invalido"));
        dateInicio.valueProperty().addListener((obs, oldVal, newVal) -> dateInicio.getStyleClass().remove("campo-invalido"));
        dateFin.valueProperty().addListener((obs, oldVal, newVal) -> dateFin.getStyleClass().remove("campo-invalido"));
        comboTipoEntrada.valueProperty().addListener((obs, oldVal, newVal) -> comboTipoEntrada.getStyleClass().remove("campo-invalido"));
        comboEstado.valueProperty().addListener((obs, oldVal, newVal) -> comboEstado.getStyleClass().remove("campo-invalido"));

    }

    private void cargarFragmentoEspecifico(TipoEvento tipo) {
        seccionDinamica.getChildren().clear();
        if (tipo == null) return;

        String rutaFXML = switch (tipo) {
            case FERIA -> "/fxml/abm/abmEventoResources/feria.fxml";
            case TALLER -> "/fxml/abm/abmEventoResources/taller.fxml";
            case EXPOSICION -> "/fxml/abm/abmEventoResources/exposicion.fxml";
            case CONCIERTO -> "/fxml/abm/abmEventoResources/concierto.fxml";
            case CICLO_CINE -> "/fxml/abm/abmEventoResources/ciclo_cine.fxml";
        };

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(rutaFXML));
            Node nodo = loader.load();
            seccionDinamica.getChildren().add(nodo);
        } catch (IOException e) {
            System.err.println("Error al cargar el fragmento: " + rutaFXML);
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
        spinnerDuracion.getValueFactory().setValue(1);
        txtNombre.clear();
        dateInicio.setValue(null);
        dateFin.setValue(null);
        comboTipoEvento.getSelectionModel().clearSelection();
        seccionDinamica.getChildren().clear();

        modalOverlay.setVisible(false);
    }

    @FXML
    private void guardarEvento() {
        // Limpia estilos anteriores
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
            mostrarAlerta("Campos incompletos", "Complete todos los campos obligatorios.");
            return;
        }

        if (dateFin.getValue().isBefore(dateInicio.getValue())) {
            dateFin.getStyleClass().add("campo-invalido");
            mostrarAlerta("Error en fechas", "La fecha de fin no puede ser anterior a la fecha de inicio.");
            return;
        }

        if (comboEstado.getValue() == null) {
            comboEstado.getStyleClass().add("campo-invalido");
            invalido = true;
        }

        cerrarModal();
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
}
