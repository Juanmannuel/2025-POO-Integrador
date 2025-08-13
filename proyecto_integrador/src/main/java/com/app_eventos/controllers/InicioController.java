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
import com.app_eventos.model.Persona;
import com.app_eventos.model.enums.EstadoEvento;
import com.app_eventos.model.enums.TipoEvento;
import com.app_eventos.services.Servicio;

import javafx.fxml.FXML; // <-- AÑADE ESTA LÍNEA
import javafx.geometry.Pos;
import javafx.scene.Node;
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
    @FXML private CheckBox chkFerias;
    @FXML private CheckBox chkConciertos;
    @FXML private CheckBox chkExposiciones;
    @FXML private CheckBox chkTalleres;
    @FXML private CheckBox chkCiclosCine;

    private List<Evento> todosLosEventos;
    private List<Evento> eventosFiltrados;
    private List<Persona> listaDePersonas;
    private LocalDate fechaActual;
    private final Servicio servicio = Servicio.getInstance(); // <-- AÑADE ESTA LÍNEA

    @FXML
    public void initialize() {
        this.todosLosEventos = servicio.listarEventos();
        this.eventosFiltrados = new ArrayList<>(this.todosLosEventos);
        this.listaDePersonas = servicio.obtenerPersonas(); 

        fechaActual = LocalDate.now();

        actualizarEstadisticas();
        dibujarCalendario();
    }

    private void actualizarEstadisticas() {
        long totalEventos = todosLosEventos.size();
        long eventosActivos = todosLosEventos.stream()
            .filter(e -> e.getEstado() == EstadoEvento.CONFIRMADO)
            .count();
        long totalPersonas = listaDePersonas.size();

        lblTotalEventos.setText(String.valueOf(totalEventos));
        lblEventosActivos.setText(String.valueOf(eventosActivos));
        lblTotalPersonas.setText(String.valueOf(totalPersonas));

        // Aquí podrías contar inscripciones reales si las tienes
        lblInscripciones.setText("0");
    }

    @FXML
    private void aplicarFiltros() {
        Set<TipoEvento> tiposSeleccionados = new HashSet<>();
        if (chkFerias.isSelected()) tiposSeleccionados.add(TipoEvento.FERIA);
        if (chkConciertos.isSelected()) tiposSeleccionados.add(TipoEvento.CONCIERTO);
        if (chkExposiciones.isSelected()) tiposSeleccionados.add(TipoEvento.EXPOSICION);
        if (chkTalleres.isSelected()) tiposSeleccionados.add(TipoEvento.TALLER);
        if (chkCiclosCine.isSelected()) tiposSeleccionados.add(TipoEvento.CICLO_CINE);

        if (tiposSeleccionados.isEmpty()) {
            eventosFiltrados = new ArrayList<>(todosLosEventos);
        } else {
            eventosFiltrados = todosLosEventos.stream()
                    .filter(evento -> tiposSeleccionados.contains(evento.getTipoEvento()))
                    .collect(Collectors.toList());
        }

        dibujarCalendario();
    }

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

     private Node crearCeldaDia(int dia) {
        // 1. Usamos un VBox como la celda principal, no un Button
        VBox celda = new VBox(2);
        celda.setAlignment(Pos.TOP_LEFT);
        celda.getStyleClass().add("calendario-celda");
        
        // 2. --- AQUÍ ESTÁ LA MAGIA: Forzamos un tamaño fijo ---
        celda.setPrefHeight(90); // Altura preferida
        celda.setMinHeight(90);  // No te encojas
        celda.setMaxHeight(90);  // No te estires

        // 3. El resto del contenido va dentro de este VBox
        Label lblNumeroDia = new Label(String.valueOf(dia));
        lblNumeroDia.getStyleClass().add("calendario-numero-dia");
        celda.getChildren().add(lblNumeroDia);

        List<Evento> eventosDelDia = eventosFiltrados.stream()
            .filter(e -> e.getFechaInicio().toLocalDate().equals(fechaActual.withDayOfMonth(dia)))
            .collect(Collectors.toList());

        for (Evento evento : eventosDelDia) {
            HBox contenedorEvento = new HBox(5);
            contenedorEvento.setAlignment(Pos.CENTER_LEFT);

            Circle punto = new Circle(5, getColorPorTipo(evento.getTipoEvento()));
            String tipoTexto = capitalizar(evento.getTipoEvento().toString());
            Label lblTipoEvento = new Label(tipoTexto);
            lblTipoEvento.getStyleClass().add("calendario-nombre-eventon-pequeno");

            contenedorEvento.getChildren().addAll(punto, lblTipoEvento);
            celda.getChildren().add(contenedorEvento);
        }
        
        // 4. Hacemos que toda la celda (el VBox) sea clicable
        celda.setOnMouseClicked(e -> mostrarEventosDelDia(dia, eventosDelDia));

        return celda;
    }

    private void mostrarEventosDelDia(int dia, List<Evento> eventos) {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
        lblDiaSeleccionado.setText("Eventos del " + fechaActual.withDayOfMonth(dia).format(formato));

        vboxEventosDia.getChildren().clear();

        if (eventos.isEmpty()) {
            vboxEventosDia.getChildren().add(new Label("No hay eventos para este día."));
        } else {
            DateTimeFormatter formatoHora = DateTimeFormatter.ofPattern("HH:mm"); 
            for (Evento evento : eventos) {
                Label lblEvento = new Label("• " + evento.getNombre());
                lblEvento.setStyle("-fx-font-weight: bold;"); 
            Label lblHorario = new Label("  De " + evento.getFechaInicio().format(formatoHora) + 
                            " a " + evento.getFechaFin().format(formatoHora) + " hs");
            

                vboxEventosDia.getChildren().addAll(lblEvento, lblHorario); // <-- MODIFICA ESTA LÍNEA
            }
        }
    }

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

    private Node crearCeldaVacia() {
        VBox celdaVacia = new VBox();
        celdaVacia.getStyleClass().add("calendario-celda-vacia");
        return celdaVacia;
    }

    private String capitalizar(String texto) {
        if (texto == null || texto.isEmpty()) return texto;
        return texto.substring(0, 1).toUpperCase() + texto.substring(1).toLowerCase();
    }

    @FXML private void mesAnterior() { fechaActual = fechaActual.minusMonths(1); dibujarCalendario(); }
    @FXML private void mesSiguiente() { fechaActual = fechaActual.plusMonths(1); dibujarCalendario(); }
    @FXML private void irHoy() { fechaActual = LocalDate.now(); dibujarCalendario(); }
    @FXML private void actualizarCalendario() { dibujarCalendario(); }
}
