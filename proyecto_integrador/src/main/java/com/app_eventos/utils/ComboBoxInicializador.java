package com.app_eventos.utils;

import com.app_eventos.model.enums.EstadoEvento;
import com.app_eventos.model.enums.TipoEntrada;
import com.app_eventos.model.enums.TipoEvento;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;

public class ComboBoxInicializador {

    public static void cargarTipoEvento(ComboBox<TipoEvento> comboBox) {
        comboBox.getItems().setAll(TipoEvento.values());
        comboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(TipoEvento tipo) {
                return tipo != null ? tipo.name().replace("_", " ") : "";
            }

            @Override
            public TipoEvento fromString(String s) {
                return TipoEvento.valueOf(s.replace(" ", "_"));
            }
        });
    }

    public static void cargarTipoEntrada(ComboBox<TipoEntrada> comboBox) {
        comboBox.getItems().setAll(TipoEntrada.values());
        comboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(TipoEntrada entrada) {
                return entrada != null ? entrada.name().charAt(0) + entrada.name().substring(1).toLowerCase() : "";
            }

            @Override
            public TipoEntrada fromString(String s) {
                return TipoEntrada.valueOf(s.toUpperCase());
            }
        });
    }

    public static void cargarEstadoEvento(ComboBox<EstadoEvento> comboBox) {
        comboBox.getItems().setAll(EstadoEvento.values());
        comboBox.setConverter(null); // opcional: JavaFX usa el name() del enum
    }
}
