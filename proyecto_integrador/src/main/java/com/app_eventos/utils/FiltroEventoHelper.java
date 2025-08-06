package com.app_eventos.utils;

import com.app_eventos.model.Evento;
import com.app_eventos.model.enums.EstadoEvento;
import com.app_eventos.model.enums.TipoEvento;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableView;

import java.util.List;

public class FiltroEventoHelper {
    
    public static void configurarComboConOpcionTodos(ComboBox<TipoEvento> combo) {
        ComboBoxInicializador.cargarTipoEvento(combo);
        combo.getItems().add(0, null);
        combo.setButtonCell(new ListCell<TipoEvento>() {
            @Override
            protected void updateItem(TipoEvento item, boolean empty) {
                super.updateItem(item, empty);
                setText(item == null ? "Todos" : item.toString());
            }
        });
    }
    
    public static void configurarComboConOpcionTodos(ComboBox<EstadoEvento> combo, boolean esEstado) {
        if (esEstado) {
            ComboBoxInicializador.cargarEstadoEvento(combo);
            combo.getItems().add(0, null);
            combo.setButtonCell(new ListCell<EstadoEvento>() {
                @Override
                protected void updateItem(EstadoEvento item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(item == null ? "Todos" : item.toString());
                }
            });
        }
    }
    
    public static void aplicarFiltros(List<Evento> todosLosEventos, 
                                    TableView<Evento> tabla,
                                    ComboBox<TipoEvento> comboTipo,
                                    ComboBox<EstadoEvento> comboEstado,
                                    DatePicker fechaDesde,
                                    DatePicker fechaHasta) {
        
        var eventosFiltrados = todosLosEventos.stream()
            .filter(evento -> {
                // Filtro por tipo
                if (comboTipo.getValue() != null && 
                    !evento.getTipoEvento().equals(comboTipo.getValue())) {
                    return false;
                }
                
                // Filtro por estado
                if (comboEstado.getValue() != null && 
                    !evento.getEstado().equals(comboEstado.getValue())) {
                    return false;
                }
                
                // Filtro por fecha desde
                if (fechaDesde.getValue() != null && 
                    evento.getFechaInicio().toLocalDate().isBefore(fechaDesde.getValue())) {
                    return false;
                }
                
                // Filtro por fecha hasta
                if (fechaHasta.getValue() != null && 
                    evento.getFechaInicio().toLocalDate().isAfter(fechaHasta.getValue())) {
                    return false;
                }
                
                return true;
            })
            .toList();
            
        tabla.setItems(FXCollections.observableArrayList(eventosFiltrados));
    }
}