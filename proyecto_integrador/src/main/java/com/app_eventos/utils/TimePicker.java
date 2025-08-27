package com.app_eventos.utils;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

// Componente personalizado para selección de hora
public class TimePicker extends VBox {
    
    private final ObjectProperty<LocalTime> value = new SimpleObjectProperty<>();
    private final ComboBox<Integer> comboHora;
    private final ComboBox<Integer> comboMinuto;
    private final Label lblSeparador;
    
    private static final int horaMin = 6;
    private static final int horaMax = 23;
    private static final int intervalo = 5;
    
    public TimePicker() {
        this(null);
    }
    
    public TimePicker(LocalTime initialValue) {
        setSpacing(5);
        setAlignment(Pos.CENTER_LEFT);
        
        // crear combos para hora y minutos
        comboHora = new ComboBox<>();
        comboMinuto = new ComboBox<>();
        lblSeparador = new Label(":");
        
        // configurar estilos
        comboHora.setPrefWidth(80);
        comboMinuto.setPrefWidth(80);
        lblSeparador.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        // cargar opciones de horas
        List<Integer> horas = new ArrayList<>();
        for (int i = horaMin; i <= horaMax; i++) {
            horas.add(i);
        }
        comboHora.setItems(javafx.collections.FXCollections.observableArrayList(horas));
        
        // cargar opciones de minutos
        List<Integer> minutos = new ArrayList<>();
        for (int i = 0; i < 60; i += intervalo) {
            minutos.add(i);
        }
        comboMinuto.setItems(javafx.collections.FXCollections.observableArrayList(minutos));
        
        // configurar conversores para mostrar formato 00:00
        comboHora.setConverter(new StringConverter<Integer>() {
            @Override
            public String toString(Integer object) {
                return object == null ? "" : String.format("%02d", object);
            }
            
            @Override
            public Integer fromString(String string) {
                try {
                    return Integer.parseInt(string);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        });
        
        comboMinuto.setConverter(new StringConverter<Integer>() {
            @Override
            public String toString(Integer object) {
                return object == null ? "" : String.format("%02d", object);
            }
            
            @Override
            public Integer fromString(String string) {
                try {
                    return Integer.parseInt(string);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        });
        
        // configurar valores por defecto
        if (initialValue != null) {
            setValue(initialValue);
        } else {
            comboHora.setValue(9);
            comboMinuto.setValue(0);
        }
        
        // configurar listeners para actualizar el valor (_ los parámetros no usados)
        comboHora.valueProperty().addListener((_, _, _) -> updateTimeValue());
        comboMinuto.valueProperty().addListener((_, _, _) -> updateTimeValue());
        HBox timeBox = new HBox(5);
        timeBox.setAlignment(Pos.CENTER_LEFT);
        timeBox.getChildren().addAll(comboHora, lblSeparador, comboMinuto);
        
        getChildren().add(timeBox);
        getStyleClass().add("time-picker");
    }
    
    private void updateTimeValue() {
        Integer hora = comboHora.getValue();
        Integer minuto = comboMinuto.getValue();
        
        if (hora != null && minuto != null) {
            try {
                LocalTime newTime = LocalTime.of(hora, minuto);
                if (!newTime.equals(value.get())) {
                    value.set(newTime);
                }
            } catch (Exception e) {
                // hora o minuto inválido, no hacer nada
            }
        }
    }
    
    // Obtiene el valor de tiempo seleccionado
    public LocalTime getValue() {
        return value.get();
    }
    
    // Establece el valor de tiempo
    public void setValue(LocalTime time) {
        if (time != null) {
            comboHora.setValue(time.getHour());
            comboMinuto.setValue(time.getMinute());
            value.set(time);
        } else {
            comboHora.setValue(null);
            comboMinuto.setValue(null);
            value.set(null);
        }
    }
    
    // Propiedad observable del valor
    public ObjectProperty<LocalTime> valueProperty() {
        return value;
    }
    
    // Habilita o deshabilita el componente
    public void setTimePickerDisable(boolean disable) {
        setDisable(disable);
        comboHora.setDisable(disable);
        comboMinuto.setDisable(disable);
    }
    
    // Obtiene el combo de hora
    public ComboBox<Integer> getComboHora() {
        return comboHora;
    }
    
    // Obtiene el combo de minutos
    public ComboBox<Integer> getComboMinuto() {
        return comboMinuto;
    }
}
