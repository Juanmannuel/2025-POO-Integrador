package com.app_eventos.controllers;

import com.app_eventos.model.enums.TipoPelicula;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class CicloCineController {

    @FXML private CheckBox chkPostCharla;
    @FXML private TextField txtCupoMaximo;
    @FXML private TextArea txtPeliculas;                 // títulos, uno por línea
    @FXML private ComboBox<TipoPelicula> comboTipoPelicula; // 2D / 3D

    @FXML
    public void initialize() {
        // Enum en el combo
        comboTipoPelicula.setItems(FXCollections.observableArrayList(TipoPelicula.values()));
        comboTipoPelicula.getSelectionModel().select(TipoPelicula.DOS_D);

        // Aceptar solo números en cupo
        txtCupoMaximo.textProperty().addListener((obs, oldV, newV) -> {
            if (newV != null && !newV.matches("\\d*")) {
                txtCupoMaximo.setText(newV.replaceAll("[^\\d]", ""));
            }
        });
    }

    // ==== Getters que usa ABMEventoController ====
    public boolean isPostCharla() {
        return chkPostCharla.isSelected();
    }

    public int getCupoMaximo() {
        String v = txtCupoMaximo.getText();
        if (v == null || v.isBlank()) throw new IllegalArgumentException("Debe indicar el cupo máximo.");
        int n = Integer.parseInt(v);
        if (n <= 0) throw new IllegalArgumentException("El cupo máximo debe ser mayor a 0.");
        return n;
    }

    /** Devuelve los títulos (uno por línea) tal como los escribió el usuario. */
    public String getPeliculasTexto() {
        return txtPeliculas.getText() == null ? "" : txtPeliculas.getText().trim();
    }

    /** 2D o 3D elegido. */
    public TipoPelicula getTipoPeliculasSeleccionado() {
        return comboTipoPelicula.getValue();
    }
}
