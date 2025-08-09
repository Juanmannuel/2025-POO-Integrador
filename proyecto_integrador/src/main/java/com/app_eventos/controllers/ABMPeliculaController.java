// ABMPeliculaController.java (fragmentos clave REESCRITOS para usar enum en el ComboBox)

package com.app_eventos.controllers;

import com.app_eventos.model.Pelicula;
import com.app_eventos.model.enums.TipoPelicula;
import com.app_eventos.services.Servicio;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

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
    @FXML private TextField txtDuracionValor;
    @FXML private ComboBox<String> comboDuracionUnidad;     // "Minutos" | "Horas"
    @FXML private ComboBox<TipoPelicula> comboTipoPelicula; // ✅ enum directo

    private final Servicio servicio = Servicio.getInstance();
    private Pelicula peliculaSeleccionada;
    private boolean modoEdicion;

    @FXML
    public void initialize() {
        // Configuración de las columnas
        tablaPeliculas.widthProperty().addListener((obs, oldWidth, newWidth) -> { 
            double total = newWidth.doubleValue();

            colTitulo.setPrefWidth(total * 0.60);        // 60%
            colDuracion.setPrefWidth(total * 0.20);     // 20%
            colTipo.setPrefWidth(total * 0.20);   // 20%
        });

        // Columnas
        colTitulo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitulo()));
        colDuracion.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDuracionMinutos() + " min"));
        colTipo.setCellValueFactory(d -> {
            var p = d.getValue();
            String etiqueta = (p.getTipo() != null) ? p.getTipo().getEtiqueta() : "-";
            return new SimpleStringProperty(etiqueta);
        });

        // Datos
        tablaPeliculas.setItems(servicio.obtenerPeliculas());

        // Combos
        comboDuracionUnidad.setItems(FXCollections.observableArrayList("Minutos", "Horas"));
        comboDuracionUnidad.getSelectionModel().select("Minutos");
        comboTipoPelicula.setItems(FXCollections.observableArrayList(TipoPelicula.values()));
        comboTipoPelicula.getSelectionModel().select(TipoPelicula.DOS_D);

    // ✅ Personaliza cómo se muestran los ítems en la lista desplegable del ComboBox
    comboTipoPelicula.setCellFactory(listView -> new ListCell<>() {
        @Override
        protected void updateItem(TipoPelicula item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null); // Si no hay valor, dejamos la celda vacía
            } else {
                // Mostramos la etiqueta definida en el enum en vez del nombre de la constante
                setText(item.getEtiqueta());
            }
        }
    });

    // ✅ Personaliza cómo se muestra el ítem seleccionado (parte visible del ComboBox)
    comboTipoPelicula.setButtonCell(new ListCell<>() {
        @Override
        protected void updateItem(TipoPelicula item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null); // Si no hay selección, se muestra vacío
            } else {
                // Mostramos la etiqueta también en la parte seleccionada
                setText(item.getEtiqueta());
            }
        }
    });

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

        // Precarga con enum
        txtTitulo.setText(peliculaSeleccionada.getTitulo());
        txtDuracionValor.setText(String.valueOf(peliculaSeleccionada.getDuracionMinutos()));
        comboDuracionUnidad.getSelectionModel().select("Minutos"); // el modelo guarda en min
        comboTipoPelicula.getSelectionModel().select(peliculaSeleccionada.getTipo());

        modalOverlay.setVisible(true);
    }

    @FXML
    public void altaPelicula() {
        try {
            String titulo = txtTitulo.getText();
            int valor = Integer.parseInt(txtDuracionValor.getText().trim());
            String unidad = comboDuracionUnidad.getValue();
            int duracionMin = "Horas".equals(unidad) ? valor * 60 : valor;

            // ✅ uso directo del enum
            TipoPelicula tipo = comboTipoPelicula.getValue();

            Pelicula nueva = new Pelicula(titulo, duracionMin, tipo);

            if (modoEdicion) {
                servicio.actualizarPelicula(peliculaSeleccionada, nueva);
            } else {
                servicio.guardarPelicula(nueva);
            }

            tablaPeliculas.refresh();
            cerrarModal();
        } catch (NumberFormatException e) {
            error("La duración debe ser un número entero.");
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
        txtDuracionValor.clear();
        comboDuracionUnidad.getSelectionModel().select("Minutos");
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
        if (sel == null) {
            error("Seleccione una película para eliminar.");
            return;
        }

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
