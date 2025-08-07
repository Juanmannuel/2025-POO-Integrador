package com.app_eventos.controllers;

import com.app_eventos.model.Persona;
import com.app_eventos.model.enums.TipoEntrada;
import com.app_eventos.services.Servicio;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class ConciertoController {

    @FXML private ComboBox<TipoEntrada> comboTipoEntradaConcierto;
    @FXML private Spinner<Integer> spinnerCupoMaximo;
    @FXML private ListView<Persona> listViewArtistas;

    private final Servicio servicio = new Servicio();

    @FXML
    public void initialize() {
        comboTipoEntradaConcierto.getItems().setAll(TipoEntrada.values());
        comboTipoEntradaConcierto.setPromptText("Seleccione una opción");

        spinnerCupoMaximo.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 5000, 0)
        );
        spinnerCupoMaximo.setEditable(false);

        // Cargar artistas (filtrar por rol ARTISTA si tenés ese dato)
        ObservableList<Persona> personas = servicio.obtenerPersonas();
        listViewArtistas.setItems(personas);
        listViewArtistas.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    public TipoEntrada getTipoEntradaSeleccionado() {
        return comboTipoEntradaConcierto.getValue();
    }

    public int getCupoMaximo() {
        return spinnerCupoMaximo.getValue();
    }

    public List<Persona> getArtistasSeleccionados() {
        return listViewArtistas.getSelectionModel().getSelectedItems();
    }
}