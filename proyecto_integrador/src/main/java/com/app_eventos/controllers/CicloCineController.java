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

    // Selección basada en IDs (evita comparar instancias distintas)
    private final Set<Long> seleccionadasIds = new HashSet<>();

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
                Long id = (pelicula != null ? pelicula.getIdPelicula() : null);
                SimpleBooleanProperty prop = new SimpleBooleanProperty(id != null && seleccionadasIds.contains(id));
                prop.addListener((obs, wasSelected, isNowSelected) -> {
                    if (id == null) return;
                    if (isNowSelected) seleccionadasIds.add(id);
                    else seleccionadasIds.remove(id);
                    actualizarContador();
                });
                return prop;
            },
            new StringConverter<Pelicula>() {
                @Override
                public String toString(Pelicula pelicula) {
                    if (pelicula == null) return "";
                    String tipo = (pelicula.getTipo() != null ? pelicula.getTipo().getEtiqueta() : "-");
                    return pelicula.getTitulo() + " (" +
                           formatoHorasMinutos(pelicula.getDuracionMinutos()) + ", " +
                           tipo + ")";
                }
                @Override public Pelicula fromString(String string) { return null; }
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
        lblContador.setText(seleccionadasIds.size() + " seleccionadas");
    }

    private static String formatoHorasMinutos(int minutos) {
        int horas = minutos / 60;
        int mins = minutos % 60;
        return String.format("%dh %dmin", horas, mins);
    }

    // === Getters usados por ABMEventoController ===
    public boolean isPostCharla() { return radioSi.isSelected(); }
    public void setPostCharla(boolean v) { if (v) radioSi.setSelected(true); else radioNo.setSelected(true); }

    public int getCupoMaximo() { return spinnerCupoMaximo.getValue(); }
    public void setCupoMaximo(int v) { spinnerCupoMaximo.getValueFactory().setValue(v); }

    // Preselecciona por ID (funciona aunque las instancias sean distintas)
    public void preseleccionarPeliculas(List<Pelicula> pelis) {
        seleccionadasIds.clear();
        if (pelis != null) {
            for (Pelicula p : pelis) {
                if (p != null && p.getIdPelicula() != null) seleccionadasIds.add(p.getIdPelicula());
            }
        }
        listaPeliculas.refresh();
        actualizarContador();
    }

    // Devuelve las seleccionadas actuales (instancias de 'todas')
    public List<Pelicula> getPeliculasSeleccionadas() {
        return todas.stream()
                .filter(p -> p.getIdPelicula() != null && seleccionadasIds.contains(p.getIdPelicula()))
                .toList();
    }

    // Opcional: mismas seleccionadas pero respetando el orden visual
    public List<Pelicula> getPeliculasSeleccionadasEnOrden() {
        return listaPeliculas.getItems().stream()
                .filter(p -> p.getIdPelicula() != null && seleccionadasIds.contains(p.getIdPelicula()))
                .toList();
    }
}
