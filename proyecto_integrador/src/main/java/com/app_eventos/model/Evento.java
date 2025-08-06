package com.app_eventos.model;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.app_eventos.model.enums.EstadoEvento;
import com.app_eventos.model.enums.TipoEvento;
import com.app_eventos.model.enums.TipoRol;

public abstract class Evento {

    private Long idEvento;
    private String nombre;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private EstadoEvento estado;
    private TipoEvento tipoEvento;
    private List<RolEvento> roles = new ArrayList<>();

    // Constructor con TipoEvento
    public Evento(String nombre, LocalDateTime fechaInicio, LocalDateTime fechaFin, TipoEvento tipoEvento) {
        this.nombre = nombre;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.tipoEvento = tipoEvento;
        this.estado = EstadoEvento.PLANIFICACIÓN;
    }

    // Constructor vacío
    public Evento() {
        this.estado = EstadoEvento.PLANIFICACIÓN;
    }

    // Métodos modelo rico
    public void agregarResponsable(Persona persona) {
        // Verificar que no esté ya agregado
        boolean yaExiste = this.roles.stream()
            .anyMatch(rol -> rol.getPersona().equals(persona) && 
                           rol.getRol() == TipoRol.ORGANIZADOR);
        
        if (yaExiste) {
            throw new IllegalStateException("Esta persona ya está agregada como responsable");
        }
        
        RolEvento nuevoRol = new RolEvento(this, persona, TipoRol.ORGANIZADOR);
        this.roles.add(nuevoRol);
    }

    public void quitarResponsable(Persona persona) {
        this.roles.removeIf(rol -> rol.getPersona().equals(persona) && rol.getRol() == TipoRol.ORGANIZADOR);
    }

    public void BorrarResponsable(Persona persona) {
        this.roles.removeIf(rol -> rol.getPersona().equals(persona) && rol.getRol() == TipoRol.ORGANIZADOR);
    }

    public void actualizarResponsables(java.util.List<Persona> nuevosResponsables) {
        // Limpiar responsables actuales
        this.roles.removeIf(rol -> rol.getRol() == TipoRol.ORGANIZADOR);
        
        // Agregar nuevos responsables
        for (Persona responsable : nuevosResponsables) {
            RolEvento nuevoRol = new RolEvento(this, responsable, TipoRol.ORGANIZADOR);
            this.roles.add(nuevoRol);
        }
    }

    public List<Persona> obtenerResponsables() {
        return this.roles.stream()
                .filter(rol -> rol.getRol() == TipoRol.ORGANIZADOR)
                .map(RolEvento::getPersona)
                .toList();
    }

    public Duration getDuracionEstimada() {
        if (fechaInicio == null || fechaFin == null) {
            return Duration.ZERO;
        }
        // Calcular duración incluyendo el día final
        return Duration.between(fechaInicio, fechaFin.plusDays(1));
    }
    
    // FACTORY METHOD - Lógica de negocio para crear eventos según tipo
    public static Evento crearEvento(TipoEvento tipo, String nombre, 
                                   LocalDate fechaInicio, LocalDate fechaFin) {
        // Crear instancia del tipo específico
        Evento evento = switch (tipo) {
            case CONCIERTO -> new Concierto();
            case TALLER -> new Taller();
            case EXPOSICION -> new Exposicion();
            case FERIA -> new Feria();
            case CICLO_CINE -> new CicloCine();
        };
        
        // Configurar datos comunes
        evento.setNombre(nombre);
        evento.setFechaInicio(fechaInicio.atStartOfDay());
        evento.setFechaFin(fechaFin.atStartOfDay());
        evento.setTipoEvento(tipo);
        evento.setEstado(EstadoEvento.PLANIFICACIÓN);
        
        return evento;
    }
    
    // Lógica de negocio para cambio de estado
    public void confirmarEvento() {
        if (this.fechaInicio.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("No se puede confirmar un evento con fecha pasada");
        }
        this.estado = EstadoEvento.CONFIRMADO;
    }
    
    public void iniciarEvento() {
        if (this.estado != EstadoEvento.CONFIRMADO) {
            throw new IllegalStateException("Solo se pueden iniciar eventos confirmados");
        }
        this.estado = EstadoEvento.EJECUCIÓN;
    }
    
    public void finalizarEvento() {
        if (this.estado != EstadoEvento.EJECUCIÓN) {
            throw new IllegalStateException("Solo se pueden finalizar eventos en ejecución");
        }
        this.estado = EstadoEvento.FINALIZADO;
    }
    
    public void cancelarEvento() {
        if (this.estado == EstadoEvento.FINALIZADO) {
            throw new IllegalStateException("No se puede cancelar un evento finalizado");
        }
        this.estado = EstadoEvento.CANCELADO;
    }
    
    // Lógica de negocio para validar inscripciones
    public void validarInscripcion(Persona persona) {
        if (this.estado != EstadoEvento.CONFIRMADO) {
            throw new IllegalStateException("Solo se puede inscribir a eventos confirmados");
        }
        if (this.estado == EstadoEvento.FINALIZADO) {
            throw new IllegalStateException("No se puede inscribir a eventos finalizados");
        }
    }

    public String descripcionDetallada() {
        return nombre + " (" + tipoEvento + ") - " +
                "Inicio: " + fechaInicio + ", Fin: " + fechaFin + ", Estado: " + estado;
    }

    // Métodos de presentación de dominio
    public String getDuracionFormateada() {
        long dias = getDuracionEstimada().toDays();
        return dias + " día" + (dias != 1 ? "s" : "");
    }

    public String getEstadoDescriptivo() {
        return switch (estado) {
            case PLANIFICACIÓN -> "En planificación";
            case CONFIRMADO -> "Confirmado";
            case EJECUCIÓN -> "En ejecución";
            case FINALIZADO -> "Finalizado";
            case CANCELADO -> "Cancelado";
        };
    }

    // Método mejorado para cambio de estado
    public void cambiarEstado(EstadoEvento nuevoEstado) {
        if (nuevoEstado == this.estado) {
            return; // No hacer nada si ya está en ese estado
        }
        
        switch (nuevoEstado) {
            case CONFIRMADO -> confirmarEvento();
            case EJECUCIÓN -> {
                if (this.estado == EstadoEvento.PLANIFICACIÓN) {
                    confirmarEvento();
                }
                iniciarEvento();
            }
            case FINALIZADO -> {
                if (this.estado == EstadoEvento.PLANIFICACIÓN) {
                    confirmarEvento();
                }
                if (this.estado == EstadoEvento.CONFIRMADO) {
                    iniciarEvento();
                }
                finalizarEvento();
            }
            case CANCELADO -> cancelarEvento();
            case PLANIFICACIÓN -> {
                // Permitir volver a planificación solo desde confirmado
                if (this.estado == EstadoEvento.CONFIRMADO) {
                    this.estado = EstadoEvento.PLANIFICACIÓN;
                } else {
                    throw new IllegalStateException("No se puede volver a planificación desde " + this.estado);
                }
            }
        }
    }

    // Validaciones de negocio
    public void validarParaCreacion() {
        if (fechaInicio != null && fechaInicio.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("La fecha de inicio no puede ser en el pasado para eventos nuevos");
        }
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalStateException("El nombre del evento es obligatorio");
        }
    }

    // Método para validar campos básicos de cualquier evento
    public void validarCamposBasicos() {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalStateException("El nombre del evento es obligatorio");
        }
        if (tipoEvento == null) {
            throw new IllegalStateException("Debe seleccionar un tipo de evento");
        }
        if (fechaInicio == null) {
            throw new IllegalStateException("La fecha de inicio es obligatoria");
        }
        if (fechaFin == null) {
            throw new IllegalStateException("La fecha de fin es obligatoria");
        }
        if (estado == null) {
            throw new IllegalStateException("Debe seleccionar un estado");
        }
    }

    // Método para validar lógica de fechas
    public void validarFechas(boolean esNuevo) {
        if (fechaFin.isBefore(fechaInicio)) {
            throw new IllegalStateException("La fecha de fin no puede ser anterior a la fecha de inicio");
        }
        if (esNuevo && fechaInicio.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("La fecha de inicio no puede ser anterior a hoy para eventos nuevos");
        }
    }

    // Método para validar datos específicos según el tipo
    public void validarDatosEspecificos() {
        // Por defecto no hace nada, las subclases implementan sus validaciones
        switch (this.tipoEvento) {
            case CONCIERTO -> ((Concierto) this).validarDatos();
            case TALLER -> ((Taller) this).validarDatos();
            case CICLO_CINE -> ((CicloCine) this).validarDatos();
            case EXPOSICION -> ((Exposicion) this).validarDatos();
            case FERIA -> ((Feria) this).validarDatos();
        }
    }

    // Método principal de validación que combina todas las validaciones
    public void validarCompleto(boolean esNuevo) {
        validarCamposBasicos();
        validarFechas(esNuevo);
        validarDatosEspecificos();
        if (esNuevo) {
            validarParaCreacion();
        }
    }

    // Getters y setters

    public Long getIdEvento() {
        return idEvento;
    }

    public void setIdEvento(Long idEvento) {
        this.idEvento = idEvento;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        if (fechaInicio != null && fechaFin.isBefore(fechaInicio)) {
            throw new IllegalStateException("La fecha de fin no puede ser anterior a la fecha de inicio");
        }
        this.fechaFin = fechaFin;
    }

    public EstadoEvento getEstado() {
        return estado;
    }

    public void setEstado(EstadoEvento estado) {
        this.estado = estado;
    }

    public TipoEvento getTipoEvento() {
        return tipoEvento;
    }

    public void setTipoEvento(TipoEvento tipoEvento) {
        this.tipoEvento = tipoEvento;
    }

    public List<RolEvento> getRoles() {
        return roles;
    }

    public void setRoles(List<RolEvento> roles) {
        this.roles = roles;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Evento evento = (Evento) obj;
        return idEvento != null && idEvento.equals(evento.idEvento);
    }

    @Override
    public int hashCode() {
        return idEvento != null ? idEvento.hashCode() : 0;
    }
}
