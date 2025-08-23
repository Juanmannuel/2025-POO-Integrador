package com.app_eventos.utils;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Componente personalizado para selección de hora más cómodo que los spinners
 * Permite seleccionar hora y minutos de forma intuitiva
 */
public class TimePicker extends VBox {
    
    private final ObjectProperty<LocalTime> value = new SimpleObjectProperty<>();
    private final ComboBox<Integer> comboHora;
    private final ComboBox<Integer> comboMinuto;
    private final Label lblSeparador;
    
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final int MIN_HOUR = 6;
    private static final int MAX_HOUR = 23;
    private static final int MINUTE_STEP = 5;
    
    public TimePicker() {
        this(null);
    }
    
    public TimePicker(LocalTime initialValue) {
        setSpacing(5);
        setAlignment(Pos.CENTER_LEFT);
        
        // Crear combos para hora y minutos
        comboHora = new ComboBox<>();
        comboMinuto = new ComboBox<>();
        lblSeparador = new Label(":");
        
        // Configurar estilos
        comboHora.setPrefWidth(80);
        comboMinuto.setPrefWidth(80);
        lblSeparador.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Cargar opciones de horas
        List<Integer> horas = new ArrayList<>();
        for (int i = MIN_HOUR; i <= MAX_HOUR; i++) {
            horas.add(i);
        }
        comboHora.setItems(javafx.collections.FXCollections.observableArrayList(horas));
        
        // Cargar opciones de minutos
        List<Integer> minutos = new ArrayList<>();
        for (int i = 0; i < 60; i += MINUTE_STEP) {
            minutos.add(i);
        }
        comboMinuto.setItems(javafx.collections.FXCollections.observableArrayList(minutos));
        
        // Configurar conversores para mostrar formato 00:00
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
        
        // Configurar valores por defecto
        if (initialValue != null) {
            setValue(initialValue);
        } else {
            // Valor por defecto: 9:00
            comboHora.setValue(9);
            comboMinuto.setValue(0);
        }
        
        // Configurar listeners para actualizar el valor
        comboHora.valueProperty().addListener((obs, oldVal, newVal) -> updateTimeValue());
        comboMinuto.valueProperty().addListener((obs, oldVal, newVal) -> updateTimeValue());
        
        // Layout horizontal: [Hora] : [Minutos]
        HBox timeBox = new HBox(5);
        timeBox.setAlignment(Pos.CENTER_LEFT);
        timeBox.getChildren().addAll(comboHora, lblSeparador, comboMinuto);
        
        getChildren().add(timeBox);
        
        // Aplicar estilos CSS
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
                // Ignorar errores de tiempo inválido
            }
        }
    }
    
    /**
     * Obtiene el valor de tiempo seleccionado
     */
    public LocalTime getValue() {
        return value.get();
    }
    
    /**
     * Establece el valor de tiempo
     */
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
    
    /**
     * Propiedad observable del valor
     */
    public ObjectProperty<LocalTime> valueProperty() {
        return value;
    }
    
    /**
     * Habilita o deshabilita el componente
     */
    public void setTimePickerDisable(boolean disable) {
        setDisable(disable);
        comboHora.setDisable(disable);
        comboMinuto.setDisable(disable);
    }
    
    /**
     * Obtiene el combo de hora (útil para estilos personalizados)
     */
    public ComboBox<Integer> getComboHora() {
        return comboHora;
    }
    
    /**
     * Obtiene el combo de minutos (útil para estilos personalizados)
     */
    public ComboBox<Integer> getComboMinuto() {
        return comboMinuto;
    }
}
