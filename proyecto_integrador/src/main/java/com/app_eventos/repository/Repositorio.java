package com.app_eventos.repository;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import com.app_eventos.model.*;
import com.app_eventos.model.enums.*;

/** Acceso a datos con JPA. */
public class Repositorio {

    private static final EntityManagerFactory EMF =
            Persistence.createEntityManagerFactory("app_eventosPU");

    private EntityManager em() { return EMF.createEntityManager(); }

    @FunctionalInterface
    private interface Fn<T> { T apply(EntityManager em); }

    /** Helper de transacciones: abre EM, begin/commit/rollback y cierra. */
    private <T> T tx(Fn<T> work) {
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
        } finally {
            em.close();
        }
    }

    // ---------- Personas ----------
    public ObservableList<Persona> listarPersonas() {
        return tx(em -> FXCollections.observableArrayList(
            em.createQuery("select p from Persona p order by p.apellido, p.nombre", Persona.class)
              .getResultList()
        ));
    }
    public Persona guardarPersona(Persona p){ return tx(em->{ em.persist(p); return p; }); }
    public Persona actualizarPersona(Persona p){ return tx(em-> em.merge(p)); }
    public void eliminarPersona(Persona p){ tx(em->{ em.remove(em.contains(p)?p:em.merge(p)); return null; }); }

    // ---------- Películas ----------
    public ObservableList<Pelicula> listarPeliculas() {
        return tx(em -> FXCollections.observableArrayList(
            em.createQuery("select p from Pelicula p order by p.titulo", Pelicula.class)
              .getResultList()
        ));
    }
    public Pelicula guardarPelicula(Pelicula p){ return tx(em->{ em.persist(p); return p; }); }
    public Pelicula actualizarPelicula(Pelicula p){ return tx(em-> em.merge(p)); }
    public void eliminarPelicula(Pelicula p){ tx(em->{ em.remove(em.contains(p)?p:em.merge(p)); return null; }); }

    // ---------- Helpers privados ----------
    /** ¿La persona ya tiene algún rol en el evento? */
    private boolean personaTieneRolEnEvento(EntityManager em, Evento ev, Persona pe){
        return !em.createQuery(
            "select r.id from RolEvento r where r.evento=:e and r.persona=:p", Long.class)
            .setParameter("e", ev)
            .setParameter("p", pe)
            .setMaxResults(1)
            .getResultList()
            .isEmpty();
    }

    /** ¿La persona ya está inscripta como participante en el evento? */
    private boolean esParticipanteDeEvento(EntityManager em, Evento ev, Persona pe){
        if (ev instanceof Concierto){
            Long cnt = em.createQuery(
                "select count(p) from Concierto c join c.participantes p " +
                "where c.idEvento=:id and p=:p", Long.class)
                .setParameter("id", ev.getIdEvento())
                .setParameter("p", pe)
                .getSingleResult();
            return cnt > 0;
        } else if (ev instanceof Taller){
            Long cnt = em.createQuery(
                "select count(p) from Taller t join t.participantes p " +
                "where t.idEvento=:id and p=:p", Long.class)
                .setParameter("id", ev.getIdEvento())
                .setParameter("p", pe)
                .getSingleResult();
            return cnt > 0;
        } else if (ev instanceof CicloCine){
            Long cnt = em.createQuery(
                "select count(p) from CicloCine c join c.participantes p " +
                "where c.idEvento=:id and p=:p", Long.class)
                .setParameter("id", ev.getIdEvento())
                .setParameter("p", pe)
                .getSingleResult();
            return cnt > 0;
        }
        return false; // los demás no tienen inscripción
    }

    /** Valida rol permitido por tipo de evento + unicidad (INSTRUCTOR/CURADOR) y no-participante. */
    private void validarReglasDeRol(EntityManager em, Evento ev, Persona pe, TipoRol rol){
        // Permisos por tipo
        boolean permitido =
            (ev instanceof Concierto && (rol==TipoRol.ORGANIZADOR || rol==TipoRol.ARTISTA)) ||
            (ev instanceof Taller    && (rol==TipoRol.ORGANIZADOR || rol==TipoRol.INSTRUCTOR)) ||
            (ev instanceof Exposicion&& (rol==TipoRol.ORGANIZADOR || rol==TipoRol.CURADOR)) ||
            (ev instanceof Feria     && (rol==TipoRol.ORGANIZADOR)) ||
            (ev instanceof CicloCine && (rol==TipoRol.ORGANIZADOR));
        if (!permitido)
            throw new IllegalArgumentException("Rol "+rol+" no permitido para el evento seleccionado.");

        // Único INSTRUCTOR (Taller)
        if (ev instanceof Taller && rol==TipoRol.INSTRUCTOR){
            Long cant = em.createQuery(
                "select count(r) from RolEvento r where r.evento=:e and r.rol=:rol", Long.class)
                .setParameter("e", ev)
                .setParameter("rol", TipoRol.INSTRUCTOR)
                .getSingleResult();
            if (cant > 0) throw new IllegalStateException("El taller ya tiene un INSTRUCTOR asignado.");
        }
        // Único CURADOR (Exposición)
        if (ev instanceof Exposicion && rol==TipoRol.CURADOR){
            Long cant = em.createQuery(
                "select count(r) from RolEvento r where r.evento=:e and r.rol=:rol", Long.class)
                .setParameter("e", ev)
                .setParameter("rol", TipoRol.CURADOR)
                .getSingleResult();
            if (cant > 0) throw new IllegalStateException("La exposición ya tiene un CURADOR asignado.");
        }

        // No puede tener rol si ya es participante
        if (esParticipanteDeEvento(em, ev, pe))
            throw new IllegalStateException("La persona ya está inscripta como participante en este evento.");
    }

    // ---------- Roles ----------
    /**
     * Asigna y persiste un rol cumpliendo reglas:
     *  - una persona NO puede tener más de un rol en el mismo evento
     *  - roles permitidos por tipo + unicidad (INSTRUCTOR/CURADOR)
     *  - no responsable si ya es participante
     *  - idempotente si ya existe exactamente el mismo rol
     */
    public RolEvento asignarRol(Evento evento, Persona persona, TipoRol rol) {
        return tx(em -> {
            // find para obtener la subclase real (Taller/Concierto/etc.)
            Evento ev = em.find(Evento.class, evento.getIdEvento());
            Persona pe = em.getReference(Persona.class, persona.getIdPersona());

            // Un rol por (persona, evento)
            if (personaTieneRolEnEvento(em, ev, pe))
                throw new IllegalStateException("La persona ya tiene un rol asignado en este evento.");

            // Reglas del dominio
            validarReglasDeRol(em, ev, pe, rol);

            // Idempotencia
            RolEvento existente = em.createQuery(
                "select r from RolEvento r where r.evento=:e and r.persona=:p and r.rol=:r",
                RolEvento.class
            ).setParameter("e", ev)
             .setParameter("p", pe)
             .setParameter("r", rol)
             .getResultStream()
             .findFirst()
             .orElse(null);
            if (existente != null) return existente;

            // Persistir (fecha_asignacion se setea en ctor/prePersist)
            RolEvento nuevo = new RolEvento(ev, pe, rol);
            em.persist(nuevo);
            return nuevo;
        });
    }

    /** Elimina el rol de una persona en un evento. */
    public void eliminarRol(Evento evento, Persona persona, TipoRol rol) {
        tx(em -> {
            Evento ev = em.getReference(Evento.class, evento.getIdEvento());
            Persona pe = em.getReference(Persona.class, persona.getIdPersona());
            em.createQuery("delete from RolEvento r where r.evento=:e and r.persona=:p and r.rol=:r")
              .setParameter("e", ev)
              .setParameter("p", pe)
              .setParameter("r", rol)
              .executeUpdate();
            return null;
        });
    }

    /** Roles de un evento (con persona precargada) ordenados por fecha asignación DESC. */
    public ObservableList<RolEvento> obtenerRolesDeEvento(Evento evento) {
        return tx(em -> FXCollections.observableArrayList(
            em.createQuery(
                "select r from RolEvento r " +
                "join fetch r.persona p " +
                "where r.evento.idEvento=:id " +
                "order by r.id desc",              // ← antes: r.fechaAsignacion desc, r.id desc
                RolEvento.class
            ).setParameter("id", evento.getIdEvento())
            .getResultList()
        ));
    }

    /** Filtro general de roles por evento/persona/dni. */
    public ObservableList<RolEvento> filtrarRoles(String ne, String np, String dni) {
        return tx(em -> {
            StringBuilder jpql = new StringBuilder(
                "select r from RolEvento r join r.evento e join r.persona p where 1=1");
            Map<String,Object> params = new HashMap<>();
            if (ne != null && !ne.isBlank()) { jpql.append(" and lower(e.nombre) like :ne"); params.put("ne","%"+ne.toLowerCase()+"%"); }
            if (np != null && !np.isBlank()) { jpql.append(" and (lower(p.nombre) like :np or lower(p.apellido) like :np)"); params.put("np","%"+np.toLowerCase()+"%"); }
            if (dni!= null && !dni.isBlank()){ jpql.append(" and p.dni like :dni"); params.put("dni","%"+dni+"%"); }
            jpql.append(" order by r.id desc"); 
            TypedQuery<RolEvento> q = em.createQuery(jpql.toString(), RolEvento.class);
            params.forEach(q::setParameter);
            return FXCollections.observableArrayList(q.getResultList());
        });
    }

    // ---------- Participantes ----------
    public void agregarParticipante(Evento evento, Persona persona) {
        tx(em -> {
            Evento e = em.find(Evento.class, evento.getIdEvento());
            Persona p = em.getReference(Persona.class, persona.getIdPersona());

            // No inscribir si tiene algún rol en el evento
            if (personaTieneRolEnEvento(em, e, p))
                throw new IllegalStateException("No se puede inscribir: la persona tiene un rol en este evento.");

            // Estado/corte de inscripción
            LocalDateTime now = LocalDateTime.now();
            if (e.getEstado() != EstadoEvento.CONFIRMADO || now.isAfter(e.getFechaFin()))
                throw new IllegalStateException("No se permite inscribir para este evento.");

            // Por subtipo con cupo
            if (e instanceof Concierto c) {
                if (c.getParticipantes().contains(p)) throw new IllegalArgumentException("La persona ya está inscripta.");
                if (c.getParticipantes().size() >= c.getCupoMaximo()) throw new IllegalStateException("Cupo completo.");
                c.getParticipantes().add(p);
            } else if (e instanceof Taller t) {
                if (t.getParticipantes().contains(p)) throw new IllegalArgumentException("La persona ya está inscripta.");
                if (t.getParticipantes().size() >= t.getCupoMaximo()) throw new IllegalStateException("Cupo completo.");
                t.getParticipantes().add(p);
            } else if (e instanceof CicloCine cc) {
                if (cc.getParticipantes().contains(p)) throw new IllegalArgumentException("La persona ya está inscripta.");
                if (cc.getParticipantes().size() >= cc.getCupoMaximo()) throw new IllegalStateException("Cupo completo.");
                cc.getParticipantes().add(p);
            } else {
                throw new IllegalArgumentException("El evento no admite inscripción.");
            }
            return null;
        });
    }

    public void quitarParticipante(Evento evento, Persona persona) {
        tx(em -> {
            Evento e = em.find(Evento.class, evento.getIdEvento());
            Persona p = em.getReference(Persona.class, persona.getIdPersona());
            if (e instanceof Concierto c)        c.getParticipantes().remove(p);
            else if (e instanceof Taller t)      t.getParticipantes().remove(p);
            else if (e instanceof CicloCine cc)  cc.getParticipantes().remove(p);
            else throw new IllegalArgumentException("El evento no admite inscripción.");
            return null;
        });
    }

    public ObservableList<Persona> obtenerParticipantes(Evento evento) {
        return tx(em -> {
            Long id = evento.getIdEvento();
            if (evento instanceof Concierto)
                return FXCollections.observableArrayList(
                    em.createQuery("select p from Concierto c join c.participantes p where c.idEvento=:id order by p.apellido, p.nombre", Persona.class)
                      .setParameter("id", id).getResultList());
            else if (evento instanceof Taller)
                return FXCollections.observableArrayList(
                    em.createQuery("select p from Taller t join t.participantes p where t.idEvento=:id order by p.apellido, p.nombre", Persona.class)
                      .setParameter("id", id).getResultList());
            else if (evento instanceof CicloCine)
                return FXCollections.observableArrayList(
                    em.createQuery("select p from CicloCine c join c.participantes p where c.idEvento=:id order by p.apellido, p.nombre", Persona.class)
                      .setParameter("id", id).getResultList());
            return FXCollections.observableArrayList();
        });
    }

    // ---------- Eventos ----------
    public List<Evento> listarEventos() {
        return tx(em ->
            em.createQuery(
                "select distinct e from Evento e " +
                "left join fetch e.roles r " +
                "left join fetch r.persona p " +
                "order by e.fechaInicio", Evento.class
            ).getResultList()
        );
    }

    public <T extends Evento> T guardarEvento(T e){ return tx(em->{ em.persist(e); return e; }); }
    public <T extends Evento> T actualizarEvento(T e){ return tx(em-> em.merge(e)); }
    public void eliminarEvento(Evento e){ tx(em->{ em.remove(em.contains(e)?e:em.merge(e)); return null; }); }

    public List<Evento> buscarEventos(TipoEvento tipo, EstadoEvento estado, LocalDate desde, LocalDate hasta) {
        return tx(em -> {
            StringBuilder jpql = new StringBuilder(
                "select distinct e from Evento e left join fetch e.roles r left join fetch r.persona p where 1=1");
            Map<String,Object> params = new HashMap<>();

            if (tipo != null){ jpql.append(" and e.tipoEvento = :tipo"); params.put("tipo", tipo); }
            if (estado != null){ jpql.append(" and e.estado = :estado"); params.put("estado", estado); }

            LocalDateTime from = (desde == null) ? null : desde.atStartOfDay();
            LocalDateTime to   = (hasta == null) ? null : hasta.atTime(LocalTime.of(23,59,59));

            if (from != null){ jpql.append(" and e.fechaFin >= :from"); params.put("from", from); }
            if (to != null){ jpql.append(" and e.fechaInicio <= :to"); params.put("to", to); }

            jpql.append(" order by e.fechaInicio");

            TypedQuery<Evento> q = em.createQuery(jpql.toString(), Evento.class);
            params.forEach(q::setParameter);
            return q.getResultList();
        });
    }

    /** Personas elegibles para inscribirse en 'e': no tienen rol en el evento ni están inscriptas. */
    public java.util.List<Persona> personasElegiblesParaInscripcion(Evento e) {
        return tx(em -> {
            // obtener la subclase real
            Evento ev = em.find(Evento.class, e.getIdEvento());

            // base: personas sin rol en el evento
            String base = """
                select p from Persona p
                where p not in (select r.persona from RolEvento r where r.evento.idEvento = :id)
                """;

            String order = " order by p.apellido, p.nombre";

            if (ev instanceof Concierto) {
                String jpql = base + """
                    and p not in (
                        select p2 from Concierto c join c.participantes p2
                        where c.idEvento = :id
                    )
                    """ + order;
                return em.createQuery(jpql, Persona.class)
                        .setParameter("id", ev.getIdEvento())
                        .getResultList();

            } else if (ev instanceof Taller) {
                String jpql = base + """
                    and p not in (
                        select p2 from Taller t join t.participantes p2
                        where t.idEvento = :id
                    )
                    """ + order;
                return em.createQuery(jpql, Persona.class)
                        .setParameter("id", ev.getIdEvento())
                        .getResultList();

            } else if (ev instanceof CicloCine) {
                String jpql = base + """
                    and p not in (
                        select p2 from CicloCine c join c.participantes p2
                        where c.idEvento = :id
                    )
                    """ + order;
                return em.createQuery(jpql, Persona.class)
                        .setParameter("id", ev.getIdEvento())
                        .getResultList();
            }

            // Otros tipos no admiten inscripción
            return java.util.Collections.emptyList();
        });
    }

}
