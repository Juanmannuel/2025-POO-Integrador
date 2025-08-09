package com.app_eventos.controllers;

import com.app_eventos.model.Pelicula;
import com.app_eventos.services.Servicio;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.util.StringConverter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class CicloCineController {

    @FXML private RadioButton radioSi;
    @FXML private RadioButton radioNo;
    @FXML private Spinner<Integer> spinnerCupoMaximo;
    @FXML private TextField txtFiltro;
    @FXML private Label lblContador;
    @FXML private ListView<Pelicula> listaPeliculas;

    private final Servicio servicio = Servicio.getInstance();
    private final ObservableList<Pelicula> todas = FXCollections.observableArrayList();
    private FilteredList<Pelicula> filtradas;

    // Selección persistente
    private final Set<Pelicula> seleccionadas = new HashSet<>();

    @FXML
    public void initialize() {
        spinnerCupoMaximo.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10000, 0)
        );
        spinnerCupoMaximo.setEditable(true);

        // Cargar películas desde servicio
        todas.setAll(servicio.obtenerPeliculas());
        filtradas = new FilteredList<>(todas, p -> true);
        listaPeliculas.setItems(filtradas);

        // Celda con checkbox persistente y formato "Xh Ymin"
        listaPeliculas.setCellFactory(CheckBoxListCell.forListView(
            pelicula -> {
                SimpleBooleanProperty prop = new SimpleBooleanProperty(seleccionadas.contains(pelicula));
                prop.addListener((obs, wasSelected, isNowSelected) -> {
                    if (isNowSelected) seleccionadas.add(pelicula);
                    else seleccionadas.remove(pelicula);
                    actualizarContador();
                });
                return prop;
            },
            new StringConverter<Pelicula>() {
                @Override
                public String toString(Pelicula pelicula) {
                    if (pelicula == null) return "";
                    return pelicula.getTitulo() + " (" +
                           formatoHorasMinutos(pelicula.getDuracionMinutos()) + ", " +
                           (pelicula.getTipo() != null ? pelicula.getTipo().getEtiqueta() : "-") +
                           ")";
                }

                @Override
                public Pelicula fromString(String string) {
                    return null; // no se usa
                }
            }
        ));

        // Filtro por título
        txtFiltro.textProperty().addListener((obs, old, text) -> {
            Predicate<Pelicula> pred = (text == null || text.isBlank())
                ? p -> true
                : p -> p.getTitulo().toLowerCase().contains(text.toLowerCase());
            filtradas.setPredicate(pred);
        });

        actualizarContador();
    }

    private void actualizarContador() {
        lblContador.setText(seleccionadas.size() + " seleccionadas");
    }

    private static String formatoHorasMinutos(int minutos) {
        int horas = minutos / 60;
        int mins = minutos % 60;
        return String.format("%dh %dmin", horas, mins);
    }

    // === Getters usados por ABMEventoController ===
    public boolean isPostCharla() {
        return radioSi.isSelected();
    }

    public int getCupoMaximo() {
        return spinnerCupoMaximo.getValue();
    }

    public List<Pelicula> getPeliculasSeleccionadas() {
        return List.copyOf(seleccionadas);
    }
}
