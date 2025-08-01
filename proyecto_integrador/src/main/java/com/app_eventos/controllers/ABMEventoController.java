package com.app_eventos.controllers;

import com.app_eventos.model.enums.TipoEvento;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.util.StringConverter;

public class ABMEventoController {

    // Campos comunes a todos los eventos
    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtDescripcion;

    @FXML
    private DatePicker dateInicio;

    @FXML
    private DatePicker dateFin;

    @FXML
    private ComboBox<TipoEvento> comboTipoEvento;

    // Contenedor para secciones dinámicas según tipo de evento
    @FXML
    private Pane seccionDinamica;

    @FXML
    public void initialize() {
        // Inicializar ComboBox de tipo de evento
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

        comboTipoEvento.setOnAction(event -> cargarSeccionEspecifica(comboTipoEvento.getValue()));
    }

    private void cargarSeccionEspecifica(TipoEvento tipoSeleccionado) {
        // Acá iría la lógica para cargar la parte dinámica (FXML parcial)
        // dependiendo del tipo seleccionado
        System.out.println("Tipo seleccionado: " + tipoSeleccionado);
        seccionDinamica.getChildren().clear();

        switch (tipoSeleccionado) {
            case FERIA:
                // Cargar sección Feria
                break;
            case TALLER:
                // Cargar sección Taller
                break;
            case EXPOSICION:
                // Cargar sección Exposición
                break;
            case CONCIERTO:
                // Cargar sección Concierto
                break;
            case CICLO_CINE:
                // Cargar sección Ciclo Cine
                break;
        }
    }
}
