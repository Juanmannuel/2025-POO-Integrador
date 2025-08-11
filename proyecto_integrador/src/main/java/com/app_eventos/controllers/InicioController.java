package com.app_eventos.controllers;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import com.app_eventos.model.Evento;
import com.app_eventos.model.enums.TipoEvento;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class InicioController {

    @FXML private Label lblTotalEventos;
    @FXML private Label lblEventosActivos;
    @FXML private Label lblTotalPersonas;
    @FXML private Label lblInscripciones;
    @FXML private Label lblMesAnio;
    @FXML private VBox panelCalendario;
    @FXML private Label lblDiaSeleccionado;
    @FXML private VBox vboxEventosDia;
    @FXML private ScrollPane scrollEventosDia;

    // VARIABLES PARA LOS FILTROS
    @FXML private CheckBox chkFerias;
    @FXML private CheckBox chkConciertos;
    @FXML private CheckBox chkExposiciones;
    @FXML private CheckBox chkTalleres;
    @FXML private CheckBox chkCiclosCine;

    private List<Evento> todosLosEventos; // Lista maestra con todos los eventos
    private List<Evento> eventosFiltrados; // Lista que se mostrará en el calendario
    private LocalDate fechaActual;

    @FXML
    public void initialize() {
        // Lista vacía hasta que conectemos con la persistencia
        this.todosLosEventos = new ArrayList<>();
        this.eventosFiltrados = new ArrayList<>();

        fechaActual = LocalDate.now();
        dibujarCalendario();
    }

    /**
     * NUEVO: Se ejecuta al pulsar "Aplicar Filtros".
     * Lee los checkboxes, filtra la lista de eventos y redibuja el calendario.
     */
    @FXML
    private void aplicarFiltros() {
        Set<TipoEvento> tiposSeleccionados = new HashSet<>();
        if (chkFerias.isSelected()) tiposSeleccionados.add(TipoEvento.FERIA);
        if (chkConciertos.isSelected()) tiposSeleccionados.add(TipoEvento.CONCIERTO);
        if (chkExposiciones.isSelected()) tiposSeleccionados.add(TipoEvento.EXPOSICION);
        if (chkTalleres.isSelected()) tiposSeleccionados.add(TipoEvento.TALLER);
        if (chkCiclosCine.isSelected()) tiposSeleccionados.add(TipoEvento.CICLO_CINE);

        // Filtramos la lista maestra
        this.eventosFiltrados = this.todosLosEventos.stream()
                .filter(evento -> tiposSeleccionados.contains(evento.getTipoEvento()))
                .collect(Collectors.toList());
        
        // Volvemos a dibujar el calendario, pero ahora usará la lista filtrada
        dibujarCalendario();
    }

    /**
     * Dibuja la cuadrícula completa del calendario para el mes actual.
     */
    private void dibujarCalendario() {
        String mes = fechaActual.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
        String anio = String.valueOf(fechaActual.getYear());
        lblMesAnio.setText(mes.substring(0, 1).toUpperCase() + mes.substring(1) + " " + anio);

        GridPane calendarioGrid = new GridPane();
        calendarioGrid.getStyleClass().add("calendario-grid-contenido");

        for (int i = 0; i < 7; i++) {
            ColumnConstraints colConst = new ColumnConstraints();
            colConst.setHgrow(Priority.ALWAYS);
            calendarioGrid.getColumnConstraints().add(colConst);
        }

        String[] diasSemana = {"Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb"};
        for (int i = 0; i < 7; i++) {
            Label lblDia = new Label(diasSemana[i]);
            lblDia.setMaxWidth(Double.MAX_VALUE);
            lblDia.setAlignment(Pos.CENTER);
            lblDia.getStyleClass().add("calendario-header");
            calendarioGrid.add(lblDia, i, 0);
        }

        YearMonth anioMes = YearMonth.from(fechaActual);
        int diasEnMes = anioMes.lengthOfMonth();
        int primerDiaDelMes = fechaActual.withDayOfMonth(1).getDayOfWeek().getValue() % 7;

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 7; j++) {
                int diaCalculado = (i * 7) + j - primerDiaDelMes + 1;
                Node celda;
                if (diaCalculado > 0 && diaCalculado <= diasEnMes) {
                    celda = crearCeldaDia(diaCalculado);
                } else {
                    celda = crearCeldaVacia();
                }
                calendarioGrid.add(celda, j, i + 1);
            }
        }
        
        panelCalendario.getChildren().clear();
        panelCalendario.getChildren().add(calendarioGrid);
    }

    /**
     * Crea un botón clicable para un día del calendario.
     * MODIFICADO: Ahora usa la lista 'eventosFiltrados'.
     */
    private Node crearCeldaDia(int dia) {
        Button celdaBoton = new Button();
        celdaBoton.setMaxWidth(Double.MAX_VALUE);
        celdaBoton.setMinHeight(80);
        celdaBoton.getStyleClass().add("calendario-celda");
        
        VBox contenidoCelda = new VBox(5);
        contenidoCelda.setAlignment(Pos.TOP_CENTER);
        
        Label lblNumeroDia = new Label(String.valueOf(dia));
        lblNumeroDia.getStyleClass().add("calendario-numero-dia");
        contenidoCelda.getChildren().add(lblNumeroDia);

        // Busca si hay eventos para este día en la LISTA FILTRADA
        List<Evento> eventosDelDia = eventosFiltrados.stream()
            .filter(e -> e.getFechaInicio().toLocalDate().equals(fechaActual.withDayOfMonth(dia)))
            .collect(Collectors.toList());

        if (!eventosDelDia.isEmpty()) {
            HBox contenedorIconos = new HBox(3);
            contenedorIconos.setAlignment(Pos.CENTER);
            for (Evento evento : eventosDelDia) {
                Circle punto = new Circle(5, getColorPorTipo(evento.getTipoEvento()));
                contenedorIconos.getChildren().add(punto);
            }
            contenidoCelda.getChildren().add(contenedorIconos);
        }
        
        celdaBoton.setGraphic(contenidoCelda);
        celdaBoton.setOnAction(e -> mostrarEventosDelDia(dia, eventosDelDia));
        
        return celdaBoton;
    }
    
    /**
     * Actualiza el panel lateral con los eventos del día seleccionado.
     */
    private void mostrarEventosDelDia(int dia, List<Evento> eventos) {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
        lblDiaSeleccionado.setText("Eventos del " + fechaActual.withDayOfMonth(dia).format(formato));
        
        vboxEventosDia.getChildren().clear();
        
        if (eventos.isEmpty()) {
            vboxEventosDia.getChildren().add(new Label("No hay eventos para este día."));
        } else {
            for (Evento evento : eventos) {
                Label lblEvento = new Label("• " + evento.getNombre());
                vboxEventosDia.getChildren().add(lblEvento);
            }
        }
    }

    /**
     * Devuelve un color diferente para cada tipo de evento.
     */
    private Color getColorPorTipo(TipoEvento tipo) {
        if (tipo == null) return Color.LIGHTGRAY;
        return switch (tipo) {
            case TALLER -> Color.DODGERBLUE;
            case CONCIERTO -> Color.ORANGERED;
            case FERIA -> Color.FORESTGREEN;
            case EXPOSICION -> Color.GOLD;
            case CICLO_CINE -> Color.MEDIUMPURPLE;
            default -> Color.LIGHTGRAY;
        };
    }

    // Crea un panel vacío con estilo para rellenar la cuadrícula.
    private Node crearCeldaVacia() {
        VBox celdaVacia = new VBox();
        celdaVacia.getStyleClass().add("calendario-celda-vacia");
        return celdaVacia;
    }
    
    @FXML private void mesAnterior() { fechaActual = fechaActual.minusMonths(1); dibujarCalendario(); }
    @FXML private void mesSiguiente() { fechaActual = fechaActual.plusMonths(1); dibujarCalendario(); }
    @FXML private void irHoy() { fechaActual = LocalDate.now(); dibujarCalendario(); }
    @FXML private void actualizarCalendario() { dibujarCalendario(); }
}
