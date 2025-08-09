package com.app_eventos.controllers;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import com.app_eventos.model.Pelicula;
import com.app_eventos.model.enums.TipoPelicula;
import com.app_eventos.services.Servicio;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.util.StringConverter;
import javafx.scene.control.SpinnerValueFactory;

public class ABMPeliculaController {

    // Tabla
    @FXML private TableView<Pelicula> tablaPeliculas;
    @FXML private TableColumn<Pelicula, String> colTitulo;
    @FXML private TableColumn<Pelicula, String> colDuracion;
    @FXML private TableColumn<Pelicula, String> colTipo;

    // Filtros
    @FXML private TextField txtNombreFiltro;
    @FXML private TextField txtIDFiltro;

    // Modal
    @FXML private StackPane modalOverlay;
    @FXML private TextField txtTitulo;
    @FXML private Spinner<LocalTime> spinnerDuracion;
    @FXML private ComboBox<TipoPelicula> comboTipoPelicula;

    private final Servicio servicio = Servicio.getInstance();
    private Pelicula peliculaSeleccionada;
    private boolean modoEdicion;
    private static final DateTimeFormatter HHMM = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    public void initialize() {
        // Ancho columnas
        tablaPeliculas.widthProperty().addListener((obs, oldW, newW) -> {
            double total = newW.doubleValue();
            colTitulo.setPrefWidth(total * 0.60);
            colDuracion.setPrefWidth(total * 0.20);
            colTipo.setPrefWidth(total * 0.20);
        });

        // Columnas
        colTitulo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitulo()));
        colDuracion.setCellValueFactory(d -> {
            int min = d.getValue().getDuracionMinutos();
            String hhmm = String.format("%d:%02d", min / 60, min % 60);
            return new SimpleStringProperty(hhmm);
        });
        colTipo.setCellValueFactory(d -> {
            var p = d.getValue();
            String etiqueta = (p.getTipo() != null) ? p.getTipo().getEtiqueta() : "-";
            return new SimpleStringProperty(etiqueta);
        });

        // Datos
        tablaPeliculas.setItems(servicio.obtenerPeliculas());

        // Combo TipoPelicula
        comboTipoPelicula.setItems(FXCollections.observableArrayList(TipoPelicula.values()));
        comboTipoPelicula.getSelectionModel().select(TipoPelicula.DOS_D);

        comboTipoPelicula.setCellFactory(listView -> new ListCell<>() {
            @Override protected void updateItem(TipoPelicula item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.getEtiqueta());
            }
        });
        comboTipoPelicula.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(TipoPelicula item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.getEtiqueta());
            }
        });

        // Spinner duración HH:mm (paso 5 min, rango 00:01..09:59)
        spinnerDuracion.setValueFactory(
            crearFactoryDuracion(LocalTime.of(0, 1), LocalTime.of(9, 59), 5)
        );
        spinnerDuracion.setEditable(true);
    } // <-- ¡cierra initialize!

    // ===== fuera de initialize =====
    private SpinnerValueFactory<LocalTime> crearFactoryDuracion(LocalTime min, LocalTime max, int stepMin) {
        return new SpinnerValueFactory<>() {
            private LocalTime value = LocalTime.of(1, 30);

            {
                setConverter(new StringConverter<>() {
                    @Override public String toString(LocalTime t) { return (t == null) ? "" : t.format(HHMM); }
                    @Override public LocalTime fromString(String s) { return LocalTime.parse(s, HHMM); }
                });
                setValue(value);
            }

            @Override public void decrement(int steps) {
                LocalTime next = value.minusMinutes(steps * stepMin);
                if (!next.isBefore(min)) { value = next; setValue(value); }
            }
            @Override public void increment(int steps) {
                LocalTime next = value.plusMinutes(steps * stepMin);
                if (!next.isAfter(max)) { value = next; setValue(value); }
            }
        };
    }

    @FXML
    public void mostrarModal() {
        modoEdicion = false;
        limpiarFormulario();
        modalOverlay.setVisible(true);
    }

    @FXML
    public void modificarPelicula() {
        peliculaSeleccionada = tablaPeliculas.getSelectionModel().getSelectedItem();
        if (peliculaSeleccionada == null) { error("Seleccione una película."); return; }
        modoEdicion = true;

        txtTitulo.setText(peliculaSeleccionada.getTitulo());
        int min = peliculaSeleccionada.getDuracionMinutos();
        spinnerDuracion.getValueFactory().setValue(LocalTime.of(min / 60, min % 60));
        comboTipoPelicula.getSelectionModel().select(peliculaSeleccionada.getTipo());

        modalOverlay.setVisible(true);
    }

    private int getDuracionMinutosTotales() {
        LocalTime t = spinnerDuracion.getValue();
        return t.getHour() * 60 + t.getMinute();
    }

    @FXML
    public void altaPelicula() {
        try {
            String titulo = txtTitulo.getText();
            int duracionMin = getDuracionMinutosTotales();
            TipoPelicula tipo = comboTipoPelicula.getValue();

            Pelicula nueva = new Pelicula(titulo, duracionMin, tipo);

            if (modoEdicion) {
                servicio.actualizarPelicula(peliculaSeleccionada, nueva);
            } else {
                servicio.guardarPelicula(nueva);
            }

            tablaPeliculas.refresh();
            cerrarModal();
        } catch (Exception e) {
            error(e.getMessage());
        }
    }

    @FXML
    public void cerrarModal() {
        modalOverlay.setVisible(false);
        limpiarFormulario();
        peliculaSeleccionada = null;
        modoEdicion = false;
    }

    private void limpiarFormulario() {
        txtTitulo.clear();
        spinnerDuracion.getValueFactory().setValue(LocalTime.of(1, 30));
        comboTipoPelicula.getSelectionModel().select(TipoPelicula.DOS_D);
    }

    private void error(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }

    public void refrescarDatos() {
        tablaPeliculas.setItems(servicio.obtenerPeliculas());
        tablaPeliculas.refresh();
    }

    @FXML
    public void eliminarPelicula() {
        Pelicula sel = tablaPeliculas.getSelectionModel().getSelectedItem();
        if (sel == null) { error("Seleccione una película para eliminar."); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText(null);
        confirm.setTitle("Confirmación");
        confirm.setContentText("¿Eliminar \"" + sel.getTitulo() + "\"?");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                servicio.eliminarPelicula(sel);
                tablaPeliculas.getSelectionModel().clearSelection();
                refrescarDatos();
            }
        });
    }
}
