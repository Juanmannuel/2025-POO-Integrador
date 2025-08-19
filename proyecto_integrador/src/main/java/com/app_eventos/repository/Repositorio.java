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

public class Repositorio {

    private static final EntityManagerFactory EMF =
            Persistence.createEntityManagerFactory("app_eventosPU");

    private EntityManager em() { return EMF.createEntityManager(); }

    @FunctionalInterface
    private interface Fn<T> { T apply(EntityManager em); }

    // Helper de transacciones: abre, begin/commit/rollback y cierra.
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

    // Personas
    public ObservableList<Persona> listarPersonas() {
        return tx(em -> FXCollections.observableArrayList(
            em.createQuery("select p from Persona p order by p.apellido, p.nombre", Persona.class)
              .getResultList()
        ));
    }
    public Persona guardarPersona(Persona p){ return tx(em->{ em.persist(p); return p; }); }
    public Persona actualizarPersona(Persona p){ return tx(em-> em.merge(p)); }
    public void eliminarPersona(Persona p){ tx(em->{ em.remove(em.contains(p)?p:em.merge(p)); return null; }); }

    // Películas
    public ObservableList<Pelicula> listarPeliculas() {
        return tx(em -> FXCollections.observableArrayList(
            em.createQuery("select p from Pelicula p order by p.titulo", Pelicula.class)
              .getResultList()
        ));
    }
    public Pelicula guardarPelicula(Pelicula p){ return tx(em->{ em.persist(p); return p; }); }
    public Pelicula actualizarPelicula(Pelicula p){ return tx(em-> em.merge(p)); }
    public void eliminarPelicula(Pelicula p){ tx(em->{ em.remove(em.contains(p)?p:em.merge(p)); return null; }); }

    // Helpers privados

    // método auxiliar para verificar si una persona ya tiene un rol en un evento.
    private boolean personaTieneRolEnEvento(EntityManager em, long idEvento, long idPersona){
        return !em.createQuery(
            "select 1 from RolEvento r " +
            "where r.evento.idEvento = :ide and r.persona.idPersona = :idp", Integer.class)
            .setParameter("ide", idEvento)
            .setParameter("idp", idPersona)
            .setMaxResults(1)
            .getResultList()
            .isEmpty();
    }

    // método auxiliar para verificar si una persona es participante de un evento. 
    private boolean esParticipanteDeEvento(EntityManager em, Evento ev, long idPersona){
        Long idEv = ev.getIdEvento();
        if (ev instanceof Concierto){
            return !em.createQuery(
                "select 1 from Concierto c join c.participantes p " +
                "where c.idEvento = :id and p.idPersona = :idp", Integer.class)
                .setParameter("id", idEv)
                .setParameter("idp", idPersona)
                .setMaxResults(1)
                .getResultList()
                .isEmpty();
        } else if (ev instanceof Taller){
            return !em.createQuery(
                "select 1 from Taller t join t.participantes p " +
                "where t.idEvento = :id and p.idPersona = :idp", Integer.class)
                .setParameter("id", idEv)
                .setParameter("idp", idPersona)
                .setMaxResults(1)
                .getResultList()
                .isEmpty();
        } else if (ev instanceof CicloCine){
            return !em.createQuery(
                "select 1 from CicloCine c join c.participantes p " +
                "where c.idEvento = :id and p.idPersona = :idp", Integer.class)
                .setParameter("id", idEv)
                .setParameter("idp", idPersona)
                .setMaxResults(1)
                .getResultList()
                .isEmpty();
        }
        // los demás no tienen inscripción
        return false;
    }

    // Valida rol permitido por tipo (INSTRUCTOR/CURADOR) y restricciones de rol.
    private void validarReglasDeRol(EntityManager em, Evento ev, long idPersona, TipoRol rol){
        // Permisos por tipo
        boolean permitido =
            (ev instanceof Concierto && (rol==TipoRol.ORGANIZADOR || rol==TipoRol.ARTISTA)) ||
            (ev instanceof Taller    && (rol==TipoRol.ORGANIZADOR || rol==TipoRol.INSTRUCTOR)) ||
            (ev instanceof Exposicion&& (rol==TipoRol.ORGANIZADOR || rol==TipoRol.CURADOR)) ||
            (ev instanceof Feria     && (rol==TipoRol.ORGANIZADOR)) ||
            (ev instanceof CicloCine && (rol==TipoRol.ORGANIZADOR));
        if (!permitido)
            throw new IllegalArgumentException("Rol "+rol+" no permitido para el evento seleccionado.");

        // Único INSTRUCTOR
        if (ev instanceof Taller){
            Long cantInst = em.createQuery(
                "select count(r) from RolEvento r where r.evento.idEvento=:ide and r.rol=:rol", Long.class)
                .setParameter("ide", ev.getIdEvento())
                .setParameter("rol", TipoRol.INSTRUCTOR)
                .getSingleResult();
            if (rol == TipoRol.INSTRUCTOR && cantInst > 0)
                throw new IllegalStateException("El taller ya tiene un INSTRUCTOR asignado.");
        }

        // Único CURADOR
        if (ev instanceof Exposicion){
            Long cantCur = em.createQuery(
                "select count(r) from RolEvento r where r.evento.idEvento=:ide and r.rol=:rol", Long.class)
                .setParameter("ide", ev.getIdEvento())
                .setParameter("rol", TipoRol.CURADOR)
                .getSingleResult();
            if (rol == TipoRol.CURADOR && cantCur > 0)
                throw new IllegalStateException("La exposición ya tiene un CURADOR asignado.");
        }

        // No puede tener rol si ya es participante
        if (esParticipanteDeEvento(em, ev, idPersona))
            throw new IllegalStateException("La persona ya está inscripta como participante en este evento.");
    }

    // Roles de eventos
    public RolEvento asignarRol(Evento evento, Persona persona, TipoRol rol) {
        return tx(em -> {
            // Cargar entidades administradas
            Evento ev = em.find(Evento.class, evento.getIdEvento());
            Persona pe = em.getReference(Persona.class, persona.getIdPersona());

            long idEv = ev.getIdEvento();
            long idPe = pe.getIdPersona();

            // un rol por (persona, evento)
            if (personaTieneRolEnEvento(em, idEv, idPe))
                throw new IllegalStateException("La persona ya tiene un rol asignado en este evento.");

            // reglas (incluye chequeo de no participante)
            validarReglasDeRol(em, ev, idPe, rol);

            // ver si ya existe el rol
            RolEvento existente = em.createQuery(
                "select r from RolEvento r " +
                "where r.evento.idEvento = :ide and r.persona.idPersona = :idp and r.rol = :rol",
                RolEvento.class
            ).setParameter("ide", idEv)
             .setParameter("idp", idPe)
             .setParameter("rol", rol)
             .setMaxResults(1)
             .getResultStream()
             .findFirst()
             .orElse(null);
            if (existente != null) return existente;

            // Persistir
            RolEvento nuevo = new RolEvento(ev, pe, rol);
            em.persist(nuevo);
            return nuevo;
        });
    }

    // Elimina el rol de una persona en un evento.
    public void eliminarRol(Evento evento, Persona persona, TipoRol rol) {
        tx(em -> {
            em.createQuery("delete from RolEvento r " +
                           "where r.evento.idEvento=:ide and r.persona.idPersona=:idp and r.rol=:rol")
              .setParameter("ide", evento.getIdEvento())
              .setParameter("idp", persona.getIdPersona())
              .setParameter("rol", rol)
              .executeUpdate();
            return null;
        });
    }

    // Roles de un evento (con persona precargada)
    public ObservableList<RolEvento> obtenerRolesDeEvento(Evento evento) {
        return tx(em -> FXCollections.observableArrayList(
            em.createQuery(
                "select r from RolEvento r " +
                "join fetch r.persona p " +
                "where r.evento.idEvento=:id " +
                "order by r.id desc",
                RolEvento.class
            ).setParameter("id", evento.getIdEvento())
             .getResultList()
        ));
    }

    // Filtro general de roles por evento/persona/dni.
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

    // Participantes 
    public void altaParticipante(Evento evento, Persona persona) {
        tx(em -> {
            Evento e = em.find(Evento.class, evento.getIdEvento());
            Persona p = em.getReference(Persona.class, persona.getIdPersona());

            long idEv = e.getIdEvento();
            long idPe = p.getIdPersona();

            // No inscribir si tiene algún rol en el evento
            if (personaTieneRolEnEvento(em, idEv, idPe))
                throw new IllegalStateException("No se puede inscribir: la persona tiene un rol en este evento.");

            // Estado/corte de inscripción
            LocalDateTime now = LocalDateTime.now();
            if (e.getEstado() != EstadoEvento.CONFIRMADO || now.isAfter(e.getFechaFin()))
                throw new IllegalStateException("No se permite inscribir para este evento.");

            // Por subtipo con cupo (consultas por conteo + id)
            if (e instanceof Concierto c) {
                // duplicado
                boolean ya = !em.createQuery(
                    "select 1 from Concierto c join c.participantes p " +
                    "where c.idEvento=:id and p.idPersona=:idp", Integer.class)
                    .setParameter("id", idEv)
                    .setParameter("idp", idPe)
                    .setMaxResults(1)
                    .getResultList().isEmpty();
                if (ya) throw new IllegalArgumentException("La persona ya está inscripta.");

                // cupo
                Long inscritos = em.createQuery(
                    "select count(p) from Concierto c join c.participantes p " +
                    "where c.idEvento=:id", Long.class)
                    .setParameter("id", idEv)
                    .getSingleResult();
                if (inscritos >= c.getCupoMaximo())
                    throw new IllegalStateException("Cupo completo.");

                c.getParticipantes().add(p);

            } else if (e instanceof Taller t) {
                boolean ya = !em.createQuery(
                    "select 1 from Taller t join t.participantes p " +
                    "where t.idEvento=:id and p.idPersona=:idp", Integer.class)
                    .setParameter("id", idEv)
                    .setParameter("idp", idPe)
                    .setMaxResults(1)
                    .getResultList().isEmpty();
                if (ya) throw new IllegalArgumentException("La persona ya está inscripta.");

                Long inscritos = em.createQuery(
                    "select count(p) from Taller t join t.participantes p " +
                    "where t.idEvento=:id", Long.class)
                    .setParameter("id", idEv)
                    .getSingleResult();
                if (inscritos >= t.getCupoMaximo())
                    throw new IllegalStateException("Cupo completo.");

                t.getParticipantes().add(p);

            } else if (e instanceof CicloCine cc) {
                boolean ya = !em.createQuery(
                    "select 1 from CicloCine c join c.participantes p " +
                    "where c.idEvento=:id and p.idPersona=:idp", Integer.class)
                    .setParameter("id", idEv)
                    .setParameter("idp", idPe)
                    .setMaxResults(1)
                    .getResultList().isEmpty();
                if (ya) throw new IllegalArgumentException("La persona ya está inscripta.");

                Long inscritos = em.createQuery(
                    "select count(p) from CicloCine c join c.participantes p " +
                    "where c.idEvento=:id", Long.class)
                    .setParameter("id", idEv)
                    .getSingleResult();
                if (inscritos >= cc.getCupoMaximo())
                    throw new IllegalStateException("Cupo completo.");

                cc.getParticipantes().add(p);

            } else {
                throw new IllegalArgumentException("El evento no admite inscripción.");
            }
            return null;
        });
    }

    public void bajaParticipante(Evento evento, Persona persona) {
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
                    em.createQuery(
                        "select p from Concierto c join c.participantes p " +
                        "where c.idEvento=:id order by p.apellido, p.nombre", Persona.class)
                      .setParameter("id", id).getResultList());
            else if (evento instanceof Taller)
                return FXCollections.observableArrayList(
                    em.createQuery(
                        "select p from Taller t join t.participantes p " +
                        "where t.idEvento=:id order by p.apellido, p.nombre", Persona.class)
                      .setParameter("id", id).getResultList());
            else if (evento instanceof CicloCine)
                return FXCollections.observableArrayList(
                    em.createQuery(
                        "select p from CicloCine c join c.participantes p " +
                        "where c.idEvento=:id order by p.apellido, p.nombre", Persona.class)
                      .setParameter("id", id).getResultList());
            return FXCollections.observableArrayList();
        });
    }

    // Eventos 
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
    public void eliminarEvento(Evento e){tx(em -> {
        Evento managed = em.contains(e) ? e : em.merge(e);
            if (managed instanceof CicloCine) {
                em.createQuery(
                    "update Pelicula p set p.cicloCine = null " + "where p.cicloCine.idEvento = :id").setParameter("id", managed.getIdEvento())
                .executeUpdate();
            }
            em.remove(managed);
            return null;
        });
    }

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

    // Personas elegibles para inscribirse en 'e': no tienen rol en el evento ni están inscriptas.
    public java.util.List<Persona> personasElegiblesParaInscripcion(Evento e) {
        return tx(em -> {
            Evento ev = em.find(Evento.class, e.getIdEvento());
            Long idEv = ev.getIdEvento();

            // base: personas sin rol en el evento
            String base = """
                select p from Persona p
                where not exists (
                    select 1 from RolEvento r
                    where r.evento.idEvento = :idEv and r.persona.idPersona = p.idPersona
                )
                """;
            String order = " order by p.apellido, p.nombre";

            if (ev instanceof Concierto) {
                String jpql = base + """
                    and not exists (
                        select 1 from Concierto c join c.participantes px
                        where c.idEvento = :idEv and px.idPersona = p.idPersona
                    )
                    """ + order;
                return em.createQuery(jpql, Persona.class)
                        .setParameter("idEv", idEv)
                        .getResultList();

            } else if (ev instanceof Taller) {
                String jpql = base + """
                    and not exists (
                        select 1 from Taller t join t.participantes px
                        where t.idEvento = :idEv and px.idPersona = p.idPersona
                    )
                    """ + order;
                return em.createQuery(jpql, Persona.class)
                        .setParameter("idEv", idEv)
                        .getResultList();

            } else if (ev instanceof CicloCine) {
                String jpql = base + """
                    and not exists (
                        select 1 from CicloCine c join c.participantes px
                        where c.idEvento = :idEv and px.idPersona = p.idPersona
                    )
                    """ + order;
                return em.createQuery(jpql, Persona.class)
                        .setParameter("idEv", idEv)
                        .getResultList();
            }

            // Otros tipos no admiten inscripción
            return java.util.Collections.emptyList();
        });
    }

    // CicloCine y Películas
    public void guardarCicloCinePeliculas(CicloCine ciclo, List<Long> peliculaIds) {
        tx(em -> {
            em.persist(ciclo);
            em.flush(); // asegura ID para usarlo como FK

            if (peliculaIds != null) {
                for (Long pid : peliculaIds) {
                    Pelicula p = em.getReference(Pelicula.class, pid);
                    p.setCicloCine(ciclo);
                    ciclo.getPeliculas().add(p);
                }
            }
            return null;
        });
    }

    public void actualizarCicloCinePeliculas(CicloCine ciclo, List<Long> peliculaIds) {
        tx(em -> {
            CicloCine managed = em.merge(ciclo);
            em.flush();

            List<Pelicula> actuales = em.createQuery(
                    "select p from Pelicula p where p.cicloCine.idEvento = :idCiclo",
                    Pelicula.class)
                .setParameter("idCiclo", managed.getIdEvento())
                .getResultList();

            for (Pelicula p : actuales) {
                p.setCicloCine(null); 
            }
            managed.getPeliculas().clear();

            if (peliculaIds != null) {
                for (Long pid : peliculaIds) {
                    Pelicula p = em.getReference(Pelicula.class, pid);
                    p.setCicloCine(managed);
                    managed.getPeliculas().add(p);
                }
            }
            return managed;
        });
    }

    // Devuelve el ciclo con la colección peliculas inicializada (JOIN FETCH)
    public CicloCine obtenerPeliculasCicloCine(Long idCiclo) {
        return tx(em -> em.createQuery(
            "select distinct c from CicloCine c " +
            "left join fetch c.peliculas " +
            "where c.idEvento = :id", CicloCine.class)
            .setParameter("id", idCiclo)
            .getSingleResult()
        );
    }

    public ObservableList<Pelicula> listarPeliculasDeCiclo(Long idCiclo) {
        return tx(em -> FXCollections.observableArrayList(
            em.createQuery(
                "select p from Pelicula p " +
                "where p.cicloCine.idEvento = :id " +
                "order by p.titulo", Pelicula.class)
            .setParameter("id", idCiclo)
            .getResultList()
        ));
    }
}