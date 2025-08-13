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

// Controles JavaFX
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

    @FXML private Label lblMesAnio;         // Encabezado del calendario (texto mes/año)
    @FXML private VBox panelCalendario;     // Contenedor en el que se dibuja la cuadrícula

    @FXML private Label lblDiaSeleccionado; // Texto “Eventos del X de Mes de Año”
    @FXML private VBox vboxEventosDia;      // Lista de eventos del día seleccionado
    @FXML private ScrollPane scrollEventosDia;

    // --- Filtros por tipo de evento ---
    @FXML private CheckBox chkFerias;
    @FXML private CheckBox chkConciertos;
    @FXML private CheckBox chkExposiciones;
    @FXML private CheckBox chkTalleres;
    @FXML private CheckBox chkCiclosCine;

    // Lista maestra (TODOS los eventos) y la lista que realmente se pinta (filtrada)
    // IMPORTANTE: hoy están vacías y se rellenan desde otra pantalla (ABMEvento) con una lista EN MEMORIA.
    // Cuando quieras que esto lea directamente de BD, tenés que:
    // 1) pedirle al Servicio una lista (que a su vez consulte al Repositorio)
    // 2) setear aquí "todosLosEventos" desde esa consulta.
    private List<Evento> todosLosEventos;
    private List<Evento> eventosFiltrados;

    // Mes/Año que se está mostrando
    private LocalDate fechaActual;

    @FXML
    public void initialize() {
        // Inicialmente, sin cargar desde BD (esto es UI pura).
        this.todosLosEventos = new ArrayList<>();
        this.eventosFiltrados = new ArrayList<>();

        // Mes que se ve de entrada
        fechaActual = LocalDate.now();

        // Dibuja el calendario con la lista actual (vacía al inicio)
        dibujarCalendario();

        // (Opcional) Aquí podrías inicializar métricas de dashboard con datos reales del Servicio/Repositorio
        // lblTotalEventos.setText(...); lblTotalPersonas.setText(...); etc.
    }

    // Handler de botón “Aplicar filtros”
    // Lee los checkboxes, filtra "todosLosEventos" y redibuja el calendario.
    @FXML
    private void aplicarFiltros() {
        Set<TipoEvento> tiposSeleccionados = new HashSet<>();
        if (chkFerias.isSelected()) tiposSeleccionados.add(TipoEvento.FERIA);
        if (chkConciertos.isSelected()) tiposSeleccionados.add(TipoEvento.CONCIERTO);
        if (chkExposiciones.isSelected()) tiposSeleccionados.add(TipoEvento.EXPOSICION);
        if (chkTalleres.isSelected()) tiposSeleccionados.add(TipoEvento.TALLER);
        if (chkCiclosCine.isSelected()) tiposSeleccionados.add(TipoEvento.CICLO_CINE);

        // Filtra la lista maestra
        this.eventosFiltrados = this.todosLosEventos.stream()
                .filter(evento -> tiposSeleccionados.contains(evento.getTipoEvento()))
                .collect(Collectors.toList());

        // Redibuja
        dibujarCalendario();
    }

    // Dibuja la cuadrícula del calendario para el mes actual (fechaActual)
    private void dibujarCalendario() {
        String mes = fechaActual.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
        String anio = String.valueOf(fechaActual.getYear());
        lblMesAnio.setText(mes.substring(0, 1).toUpperCase() + mes.substring(1) + " " + anio);

        GridPane calendarioGrid = new GridPane();
        calendarioGrid.getStyleClass().add("calendario-grid-contenido");

        // 7 columnas (Dom-Sáb), con HGrow para que se expandan
        for (int i = 0; i < 7; i++) {
            ColumnConstraints colConst = new ColumnConstraints();
            colConst.setHgrow(Priority.ALWAYS);
            calendarioGrid.getColumnConstraints().add(colConst);
        }

        // Encabezado con días de semana
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
        // Primer día del mes (0=Domingo)
        int primerDiaDelMes = fechaActual.withDayOfMonth(1).getDayOfWeek().getValue() % 7;

        for (int i = 0; i < 6; i++) {          // filas
            for (int j = 0; j < 7; j++) {      // columnas
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

        // Reemplaza el contenido del panel con el calendario recién armado
        panelCalendario.getChildren().clear();
        panelCalendario.getChildren().add(calendarioGrid);
    }

    // Crea una celda clicable para un día. Toma los eventos desde "eventosFiltrados".
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

        // Buscar eventos del día (en la lista filtrada, NO en BD)
        List<Evento> eventosDelDia = eventosFiltrados.stream()
            .filter(e -> e.getFechaInicio().toLocalDate().equals(fechaActual.withDayOfMonth(dia)))
            .collect(Collectors.toList());

        // Si hay eventos, dibuja puntitos de color por tipo
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

        // Al hacer click, muestra la lista del lateral
        celdaBoton.setOnAction(e -> mostrarEventosDelDia(dia, eventosDelDia));

        return celdaBoton;
    }

    // Muestra la lista lateral “Eventos del <día>”
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

    // Mapea tipo de evento -> color para puntitos del calendario
    private Color getColorPorTipo(TipoEvento tipo) {
        if (tipo == null) return Color.LIGHTGRAY;
        return switch (tipo) {
            case TALLER      -> Color.DODGERBLUE;
            case CONCIERTO   -> Color.ORANGERED;
            case FERIA       -> Color.FORESTGREEN;
            case EXPOSICION  -> Color.GOLD;
            case CICLO_CINE  -> Color.MEDIUMPURPLE;
            default          -> Color.LIGHTGRAY;
        };
    }

    // Crea celdas vacías (fuera del rango del mes)
    private Node crearCeldaVacia() {
        VBox celdaVacia = new VBox();
        celdaVacia.getStyleClass().add("calendario-celda-vacia");
        return celdaVacia;
    }

    // Navegación en el calendario
    @FXML private void mesAnterior()     { fechaActual = fechaActual.minusMonths(1); dibujarCalendario(); }
    @FXML private void mesSiguiente()    { fechaActual = fechaActual.plusMonths(1);  dibujarCalendario(); }
    @FXML private void irHoy()           { fechaActual = LocalDate.now();            dibujarCalendario(); }
    @FXML private void actualizarCalendario() { dibujarCalendario(); }

    // ====== NOTA IMPORTANTE SOBRE PERSISTENCIA EN ESTA VISTA ======
    // Si querés que el calendario se alimente con eventos de la BD:
    // - Agregá un Servicio.getInstance().listarEventos() que traiga desde el Repositorio (select a BD)
    // - Setealo aquí: this.todosLosEventos = servicio.listarEventos();
    // - Luego llamá aplicarFiltros() o dibujarCalendario().
}
