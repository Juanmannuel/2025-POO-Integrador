package com.app_eventos.controllers;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import com.app_eventos.model.Evento;
import com.app_eventos.model.enums.TipoEvento;
import com.app_eventos.services.Servicio;

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

    // --- Widgets del dashboard/calendario ---
    @FXML private Label lblTotalEventos;
    @FXML private Label lblEventosActivos;
    @FXML private Label lblTotalPersonas;
    @FXML private Label lblInscripciones;

    @FXML private Label lblMesAnio;         // Encabezado del calendario (mes/año)
    @FXML private VBox panelCalendario;     // Contenedor del calendario

    @FXML private Label lblDiaSeleccionado; // “Eventos del X de Mes de Año”
    @FXML private VBox vboxEventosDia;      // Lista de eventos del día
    @FXML private ScrollPane scrollEventosDia;

    // --- Filtros por tipo de evento ---
    @FXML private CheckBox chkFerias;
    @FXML private CheckBox chkConciertos;
    @FXML private CheckBox chkExposiciones;
    @FXML private CheckBox chkTalleres;
    @FXML private CheckBox chkCiclosCine;

    // Datos
    private List<Evento> todosLosEventos;   // Traídos de BD para el mes visible
    private List<Evento> eventosFiltrados;  // Lista pintada (aplicando filtros)

    // Mes/Año que se está mostrando
    private LocalDate fechaActual;

    // Servicio de negocio (consulta BD)
    private final Servicio servicio = Servicio.getInstance();

    @FXML
    public void initialize() {
        this.todosLosEventos = new ArrayList<>();
        this.eventosFiltrados = new ArrayList<>();
        this.fechaActual = LocalDate.now();

        cargarEventosDelMes();  // <-- lee de BD
        dibujarCalendario();
    }

    // ----- Carga desde BD según el mes visible -----
    private void cargarEventosDelMes() {
        LocalDate inicioMes = fechaActual.withDayOfMonth(1);
        LocalDate finMes = fechaActual.with(TemporalAdjusters.lastDayOfMonth());

        // Trae eventos del rango (incluye todo lo necesario para la UI)
        this.todosLosEventos = servicio.listarEventosPorRango(
                inicioMes.atStartOfDay(),
                finMes.atTime(23, 59, 59)
        );

        // Por defecto, mostrar todos (sin filtros aplicados aún)
        this.eventosFiltrados = new ArrayList<>(this.todosLosEventos);

        // (Opcional) Métricas del dashboard
        if (lblTotalEventos != null)    lblTotalEventos.setText(String.valueOf(servicio.contarEventos()));
        if (lblEventosActivos != null)  lblEventosActivos.setText(String.valueOf(servicio.contarEventosActivos()));
        if (lblTotalPersonas != null)   lblTotalPersonas.setText(String.valueOf(servicio.contarPersonas()));
        if (lblInscripciones != null)   lblInscripciones.setText(String.valueOf(servicio.contarInscripciones()));
    }

    // ----- Filtros -----
    @FXML
    private void aplicarFiltros() {
        Set<TipoEvento> tiposSeleccionados = new HashSet<>();
        if (chkFerias.isSelected())      tiposSeleccionados.add(TipoEvento.FERIA);
        if (chkConciertos.isSelected())  tiposSeleccionados.add(TipoEvento.CONCIERTO);
        if (chkExposiciones.isSelected())tiposSeleccionados.add(TipoEvento.EXPOSICION);
        if (chkTalleres.isSelected())    tiposSeleccionados.add(TipoEvento.TALLER);
        if (chkCiclosCine.isSelected())  tiposSeleccionados.add(TipoEvento.CICLO_CINE);

        this.eventosFiltrados = this.todosLosEventos.stream()
                .filter(e -> tiposSeleccionados.isEmpty() || tiposSeleccionados.contains(e.getTipoEvento()))
                .collect(Collectors.toList());

        dibujarCalendario();
    }

    // ----- Dibujo del calendario -----
    private void dibujarCalendario() {
        String mes = fechaActual.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
        String anio = String.valueOf(fechaActual.getYear());
        lblMesAnio.setText(mes.substring(0, 1).toUpperCase() + mes.substring(1) + " " + anio);

        GridPane calendarioGrid = new GridPane();
        calendarioGrid.getStyleClass().add("calendario-grid-contenido");

        // 7 columnas (Dom-Sáb)
        for (int i = 0; i < 7; i++) {
            ColumnConstraints colConst = new ColumnConstraints();
            colConst.setHgrow(Priority.ALWAYS);
            calendarioGrid.getColumnConstraints().add(colConst);
        }

        // Encabezado días
        String[] diasSemana = {"Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb"};
        for (int i = 0; i < 7; i++) {
            Label lblDia = new Label(diasSemana[i]);
            lblDia.setMaxWidth(Double.MAX_VALUE);
            lblDia.setAlignment(Pos.CENTER);
            lblDia.getStyleClass().add("calendario-header");
            calendarioGrid.add(lblDia, i, 0);
        }

        // Celdas del mes
        YearMonth anioMes = YearMonth.from(fechaActual);
        int diasEnMes = anioMes.lengthOfMonth();
        int primerDiaDelMes = fechaActual.withDayOfMonth(1).getDayOfWeek().getValue() % 7; // 0=Domingo

        for (int i = 0; i < 6; i++) {          // filas
            for (int j = 0; j < 7; j++) {      // columnas
                int diaCalculado = (i * 7) + j - primerDiaDelMes + 1;
                Node celda = (diaCalculado > 0 && diaCalculado <= diasEnMes)
                        ? crearCeldaDia(diaCalculado)
                        : crearCeldaVacia();
                calendarioGrid.add(celda, j, i + 1);
            }
        }

        panelCalendario.getChildren().setAll(calendarioGrid);
    }

    // Celda clicable de un día
    private Node crearCeldaDia(int dia) {
        Button celdaBoton = new Button();
        celdaBoton.setMaxWidth(Double.MAX_VALUE);
        celdaBoton.setMinHeight(80);
        celdaBoton.getStyleClass().add("calendario-celda");

        VBox contenidoCelda = new VBox(5);
        contenidoCelda.setAlignment(Pos.TOP_CENTER);

        // Número de día
        Label lblNumeroDia = new Label(String.valueOf(dia));
        lblNumeroDia.getStyleClass().add("calendario-numero-dia");
        contenidoCelda.getChildren().add(lblNumeroDia);

        // Eventos del día (lista filtrada)
        LocalDate fechaDelDia = fechaActual.withDayOfMonth(dia);
        List<Evento> eventosDelDia = eventosFiltrados.stream()
                .filter(e -> e.getFechaInicio().toLocalDate().equals(fechaDelDia))
                .collect(Collectors.toList());

        // Puntitos por tipo
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

        // Click: muestra lista lateral
        celdaBoton.setOnAction(e -> mostrarEventosDelDia(dia, eventosDelDia));

        return celdaBoton;
    }

    // Lista lateral “Eventos del <día>”
    private void mostrarEventosDelDia(int dia, List<Evento> eventos) {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
        lblDiaSeleccionado.setText("Eventos del " + fechaActual.withDayOfMonth(dia).format(formato));

        vboxEventosDia.getChildren().clear();

        if (eventos.isEmpty()) {
            vboxEventosDia.getChildren().add(new Label("No hay eventos para este día."));
            return;
        }

        DateTimeFormatter formatoHora = DateTimeFormatter.ofPattern("HH:mm");
        for (Evento evento : eventos) {
            Label lblEvento = new Label("• " + evento.getNombre());
            lblEvento.setStyle("-fx-font-weight: bold;");

            Label lblHorario = new Label(
                "  De " + evento.getFechaInicio().format(formatoHora) +
                " a " + evento.getFechaFin().format(formatoHora) + " hs"
            );

            vboxEventosDia.getChildren().addAll(lblEvento, lblHorario);
        }
    }

    // Mapea tipo de evento -> color
    private Color getColorPorTipo(TipoEvento tipo) {
        if (tipo == null) return Color.LIGHTGRAY;
        return switch (tipo) {
            case TALLER     -> Color.DODGERBLUE;
            case CONCIERTO  -> Color.ORANGERED;
            case FERIA      -> Color.FORESTGREEN;
            case EXPOSICION -> Color.GOLD;
            case CICLO_CINE -> Color.MEDIUMPURPLE;
        };
    }

    // Celdas vacías (fuera del rango)
    private Node crearCeldaVacia() {
        VBox celdaVacia = new VBox();
        celdaVacia.getStyleClass().add("calendario-celda-vacia");
        return celdaVacia;
    }

    // Navegación (recarga desde BD)
    @FXML private void mesAnterior() {
        fechaActual = fechaActual.minusMonths(1);
        cargarEventosDelMes();
        dibujarCalendario();
    }

    @FXML private void mesSiguiente() {
        fechaActual = fechaActual.plusMonths(1);
        cargarEventosDelMes();
        dibujarCalendario();
    }

    @FXML private void irHoy() {
        fechaActual = LocalDate.now();
        cargarEventosDelMes();
        dibujarCalendario();
    }

    @FXML private void actualizarCalendario() {
        cargarEventosDelMes();
        dibujarCalendario();
    }
}
