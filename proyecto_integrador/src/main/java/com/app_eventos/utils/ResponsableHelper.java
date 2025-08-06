package com.app_eventos.utils;

import com.app_eventos.model.Persona;
import com.app_eventos.repository.Repositorio;
import javafx.collections.FXCollections;
import javafx.scene.control.*;

public class ResponsableHelper {
    
    public static void configurarListViewResponsables(ListView<Persona> listView) {
        listView.setCellFactory(lv -> new ListCell<Persona>() {
            @Override
            protected void updateItem(Persona persona, boolean empty) {
                super.updateItem(persona, empty);
                setText(empty || persona == null ? null : persona.getNombreCompleto());
            }
        });
    }
    
    public static void configurarComboPersonas(ComboBox<Persona> combo) {
        combo.setCellFactory(lv -> new ListCell<Persona>() {
            @Override
            protected void updateItem(Persona persona, boolean empty) {
                super.updateItem(persona, empty);
                setText(empty || persona == null ? null : persona.getNombreCompleto());
            }
        });
        
        combo.setButtonCell(new ListCell<Persona>() {
            @Override
            protected void updateItem(Persona persona, boolean empty) {
                super.updateItem(persona, empty);
                setText(empty || persona == null ? "Seleccionar persona" : persona.getNombreCompleto());
            }
        });
    }
    
    public static void actualizarPersonasDisponibles(ComboBox<Persona> combo, Repositorio repositorio) {
        var todasLasPersonas = repositorio.obtenerTodasLasPersonas();
        combo.setItems(FXCollections.observableArrayList(todasLasPersonas));
    }
    
    public static boolean agregarResponsable(ListView<Persona> listView, ComboBox<Persona> combo) {
        Persona personaSeleccionada = combo.getValue();
        if (personaSeleccionada != null) {
            if (!listView.getItems().contains(personaSeleccionada)) {
                listView.getItems().add(personaSeleccionada);
                combo.getSelectionModel().clearSelection();
                return true;
            }
        }
        return false;
    }
    
    public static boolean quitarResponsable(ListView<Persona> listView) {
        Persona personaSeleccionada = listView.getSelectionModel().getSelectedItem();
        if (personaSeleccionada != null) {
            listView.getItems().remove(personaSeleccionada);
            return true;
        }
        return false;
    }
}