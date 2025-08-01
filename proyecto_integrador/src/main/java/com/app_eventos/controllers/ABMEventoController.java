package com.app_eventos.controllers;

import com.app_eventos.model.enums.TipoEvento;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.util.StringConverter;

import java.io.IOException;

public class ABMEventoController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtDescripcion;
    @FXML private DatePicker dateInicio;
    @FXML private DatePicker dateFin;
    @FXML private ComboBox<TipoEvento> comboTipoEvento;
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

        comboTipoEvento.setOnAction(event ->
                cargarFormTipoEvento(comboTipoEvento.getValue()));
    }

    private void cargarFormTipoEvento(TipoEvento tipoSeleccionado) {
        seccionDinamica.getChildren().clear();

        if (tipoSeleccionado == null) return;

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
}
