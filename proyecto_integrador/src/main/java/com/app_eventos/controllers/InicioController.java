package com.app_eventos.controllers;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class InicioController {

    // --- Variables FXML (Inyectadas desde el archivo FXML) ---
    @FXML private Label lblTotalEventos;
    @FXML private Label lblEventosActivos;
    @FXML private Label lblTotalPersonas;
    @FXML private Label lblInscripciones;
    @FXML private Label lblMesAnio;
    @FXML private VBox panelCalendario; // El VBox contenedor del FXML
    @FXML private Label lblDiaSeleccionado;
    @FXML private VBox vboxEventosDia;

    private LocalDate fechaActual;

    @FXML
    public void initialize() {
        fechaActual = LocalDate.now();
        dibujarCalendario();
    }

    private void dibujarCalendario() {
        String mes = fechaActual.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
        String anio = String.valueOf(fechaActual.getYear());
        lblMesAnio.setText(mes.substring(0, 1).toUpperCase() + mes.substring(1) + " " + anio);

        GridPane calendarioGrid = new GridPane();
        calendarioGrid.getStyleClass().add("calendario-grid");

        calendarioGrid.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(calendarioGrid, Priority.ALWAYS);
        HBox.setHgrow(calendarioGrid, Priority.ALWAYS);

        for (int i = 0; i < 7; i++) {
            ColumnConstraints colConst = new ColumnConstraints();
            colConst.setHgrow(Priority.ALWAYS);
            calendarioGrid.getColumnConstraints().add(colConst);
        }

        String[] diasSemana = {"Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb"};
        for (int i = 0; i < 7; i++) {
            Label lblDia = new Label(diasSemana[i]);
            lblDia.getStyleClass().add("calendario-header");
            GridPane.setConstraints(lblDia, i, 0);
            calendarioGrid.getChildren().add(lblDia);
        }

        YearMonth anioMes = YearMonth.from(fechaActual);
        int diasEnMes = anioMes.lengthOfMonth();
        int primerDiaDelMes = fechaActual.withDayOfMonth(1).getDayOfWeek().getValue() % 7;

        for (int dia = 1; dia <= diasEnMes; dia++) {
            int columna = (primerDiaDelMes + dia - 1) % 7;
            int fila = (primerDiaDelMes + dia - 1) / 7 + 1;
            Node celdaDia = crearCeldaDia(dia);
            GridPane.setConstraints(celdaDia, columna, fila);
            calendarioGrid.getChildren().add(celdaDia);
        }
        
        panelCalendario.getChildren().clear();
        panelCalendario.getChildren().add(calendarioGrid);
    }
    
    /**
     * ESTE ES EL MÉTODO QUE HEMOS RESTAURADO
     * Crea el panel visual para un solo día del calendario.
     */
    private Node crearCeldaDia(int dia) {
        VBox celda = new VBox();
        celda.setAlignment(Pos.CENTER); // Centramos el contenido
        celda.getStyleClass().add("calendario-celda");
        
        Label lblNumeroDia = new Label(String.valueOf(dia));
        lblNumeroDia.getStyleClass().add("calendario-numero-dia");
        
        celda.getChildren().add(lblNumeroDia);
        return celda; // La línea 'return' que faltaba
    }
    
    // --- Métodos de los botones ---
    @FXML private void mesAnterior() {
        fechaActual = fechaActual.minusMonths(1);
        dibujarCalendario();
    }
    @FXML private void mesSiguiente() {
        fechaActual = fechaActual.plusMonths(1);
        dibujarCalendario();
    }
    @FXML private void irHoy() {
        fechaActual = LocalDate.now();
        dibujarCalendario();
    }
    @FXML private void actualizarCalendario() {
        dibujarCalendario();
    }
}