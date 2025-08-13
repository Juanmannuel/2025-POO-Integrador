package com.app_eventos.repository;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import com.app_eventos.model.*;
import com.app_eventos.model.enums.EstadoEvento;
import com.app_eventos.model.enums.TipoEvento;
import com.app_eventos.model.enums.TipoRol;

public class Repositorio {

    private static final EntityManagerFactory EMF =
            Persistence.createEntityManagerFactory("app_eventosPU");

    private EntityManager em() { return EMF.createEntityManager(); }

    private <T> T tx(FunctionWithEm<T> work) {
        EntityManager em = em();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T out = work.apply(em);
            tx.commit();
            return out;
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            throw ex;
        } finally { em.close(); }
    }

    @FunctionalInterface
    private interface FunctionWithEm<T> { T apply(EntityManager em); }

    // --- PERSONAS ------------------------------------------------------------
    public ObservableList<Persona> listarPersonas() {
        return tx(em -> FXCollections.observableArrayList(
            em.createQuery("select p from Persona p order by p.apellido, p.nombre", Persona.class)
              .getResultList()
        ));
    }
    public Persona guardarPersona(Persona p){ return tx(em->{ em.persist(p); return p;}); }
    public Persona actualizarPersona(Persona p){ return tx(em-> em.merge(p)); }
    public void eliminarPersona(Persona p){ tx(em->{ Persona m = em.contains(p)?p:em.merge(p); em.remove(m); return null;}); }

    // --- PELÍCULAS -----------------------------------------------------------
    public ObservableList<Pelicula> listarPeliculas() {
        return tx(em -> FXCollections.observableArrayList(
            em.createQuery("select p from Pelicula p order by p.titulo", Pelicula.class)
              .getResultList()
        ));
    }
    public Pelicula guardarPelicula(Pelicula p){ return tx(em->{ em.persist(p); return p;}); }
    public Pelicula actualizarPelicula(Pelicula p){ return tx(em-> em.merge(p)); }
    public void eliminarPelicula(Pelicula p){ tx(em->{ Pelicula m = em.contains(p)?p:em.merge(p); em.remove(m); return null;}); }

    // --- EVENTOS (JOIN FETCH para evitar LazyInitialization) -----------------
    public <T extends Evento> T guardarEvento(T e){ return tx(em->{ em.persist(e); return e;}); }
    public <T extends Evento> T actualizarEvento(T e){ return tx(em-> em.merge(e)); }
    public void eliminarEvento(Evento e){ tx(em->{ Evento m = em.contains(e)?e:em.merge(e); em.remove(m); return null;}); }

    /** Trae eventos con roles y persona ya inicializados. */
    public List<Evento> listarEventos() {
        return tx(em ->
            em.createQuery(
                "select distinct e " +
                "from Evento e " +
                "left join fetch e.roles r " +
                "left join fetch r.persona " +
                "order by e.fechaInicio", Evento.class
            ).getResultList()
        );
    }

    /** Búsqueda con filtros opcionales + JOIN FETCH de roles/persona. */
    public List<Evento> buscarEventos(TipoEvento tipo, EstadoEvento estado,
                                      LocalDate desde, LocalDate hasta) {
        return tx(em -> {
            StringBuilder jpql = new StringBuilder(
                "select distinct e from Evento e " +
                "left join fetch e.roles r " +
                "left join fetch r.persona " +
                "where 1=1"
            );
            Map<String,Object> params = new HashMap<>();

            if (tipo != null)   { jpql.append(" and e.tipoEvento = :tipo");   params.put("tipo", tipo); }
            if (estado != null) { jpql.append(" and e.estado = :estado");     params.put("estado", estado); }

            LocalDateTime from = (desde==null)?null:desde.atStartOfDay();
            LocalDateTime to   = (hasta==null)?null:hasta.atTime(LocalTime.of(23,59,59));
            if (from != null) { jpql.append(" and e.fechaFin >= :from"); params.put("from", from); }
            if (to   != null) { jpql.append(" and e.fechaInicio <= :to"); params.put("to", to); }

            jpql.append(" order by e.fechaInicio");

            TypedQuery<Evento> q = em.createQuery(jpql.toString(), Evento.class);
            params.forEach(q::setParameter);
            return q.getResultList();
        });
    }

    // --- ROLES / PARTICIPACIONES --------------------------------------------
    public RolEvento guardarRolEvento(RolEvento r){ return tx(em->{ em.persist(r); return r;}); }
    public RolEvento actualizarRolEvento(RolEvento r){ return tx(em-> em.merge(r)); }

    public ObservableList<RolEvento> obtenerRolesActivos() {
        return tx(em -> FXCollections.observableArrayList(
            em.createQuery(
                "select r from RolEvento r where r.deletedAt is null order by r.id desc",
                RolEvento.class
            ).getResultList()
        ));
    }

    public ObservableList<RolEvento> obtenerSoloParticipantes() {
        return tx(em -> FXCollections.observableArrayList(
            em.createQuery(
                "select r from RolEvento r " +
                "where r.deletedAt is null and r.rol = :rol " +
                "order by r.id desc", RolEvento.class
            ).setParameter("rol", TipoRol.PARTICIPANTE).getResultList()
        ));
    }

    public ObservableList<RolEvento> filtrarRoles(String nombreEvento, String nombrePersona, String dni) {
        return tx(em -> {
            String ne = nombreEvento==null? "": nombreEvento.trim().toLowerCase();
            String np = nombrePersona==null? "": nombrePersona.trim().toLowerCase();
            String d  = dni==null? "": dni.trim();

            StringBuilder jpql = new StringBuilder(
                "select r from RolEvento r join r.evento e join r.persona p " +
                "where r.deletedAt is null"
            );
            Map<String,Object> params = new HashMap<>();
            if (!ne.isBlank()) { jpql.append(" and lower(e.nombre) like :ne"); params.put("ne","%"+ne+"%"); }
            if (!np.isBlank()) { jpql.append(" and (lower(p.nombre) like :np or lower(p.apellido) like :np)"); params.put("np","%"+np+"%"); }
            if (!d.isBlank())  { jpql.append(" and p.dni like :dni"); params.put("dni","%"+d+"%"); }
            jpql.append(" order by r.id desc");

            TypedQuery<RolEvento> q = em.createQuery(jpql.toString(), RolEvento.class);
            params.forEach(q::setParameter);
            return FXCollections.observableArrayList(q.getResultList());
        });
    }

    public ObservableList<RolEvento> filtrarSoloParticipantes(String nombreEvento, String nombrePersona, String dni) {
        return tx(em -> {
            String ne = nombreEvento==null? "": nombreEvento.trim().toLowerCase();
            String np = nombrePersona==null? "": nombrePersona.trim().toLowerCase();
            String d  = dni==null? "": dni.trim();

            StringBuilder jpql = new StringBuilder(
                "select r from RolEvento r join r.evento e join r.persona p " +
                "where r.deletedAt is null and r.rol = :rol"
            );
            Map<String,Object> params = new HashMap<>();
            params.put("rol", TipoRol.PARTICIPANTE);
            if (!ne.isBlank()) { jpql.append(" and lower(e.nombre) like :ne"); params.put("ne","%"+ne+"%"); }
            if (!np.isBlank()) { jpql.append(" and (lower(p.nombre) like :np or lower(p.apellido) like :np)"); params.put("np","%"+np+"%"); }
            if (!d.isBlank())  { jpql.append(" and p.dni like :dni"); params.put("dni","%"+d+"%"); }
            jpql.append(" order by r.id desc");

            TypedQuery<RolEvento> q = em.createQuery(jpql.toString(), RolEvento.class);
            params.forEach(q::setParameter);
            return FXCollections.observableArrayList(q.getResultList());
        });
    }
}
