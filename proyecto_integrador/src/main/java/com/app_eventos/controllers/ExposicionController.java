package com.app_eventos.controllers;

import com.app_eventos.model.enums.TipoArte;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;

public class ExposicionController {

    @FXML
    private ComboBox<TipoArte> comboTipoArte;

    @FXML
    public void initialize() {
        comboTipoArte.getItems().setAll(TipoArte.values());
    }

    // Métodos getter
    public TipoArte getTipoArteSeleccionado() {
        return comboTipoArte.getValue();
    }

    // Métodos setter para cargar datos en modo edición
    public void setTipoArte(TipoArte tipoArte) {
        comboTipoArte.setValue(tipoArte);
    }
}