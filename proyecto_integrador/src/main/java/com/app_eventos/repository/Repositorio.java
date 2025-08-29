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

// Acceso a datos con JPA. Sin SQL nativo. Baja lógica con atributo 'activo'.
public class Repositorio {

    private static final EntityManagerFactory EMF =
            Persistence.createEntityManagerFactory("app_eventosPU");

    private EntityManager em() { return EMF.createEntityManager(); }

    // Patrón Template Method para operaciones transaccionales
    private abstract static class UnidadDeTrabajo<T> {
        protected abstract T ejecutar(EntityManager em);
    }

    // Método de utilidad para ejecutar una transacción
    private <T> T ejecutarTransaccion(UnidadDeTrabajo<T> trabajo) {
        EntityManager em = em();
        EntityTransaction t = em.getTransaction();
        try {
            t.begin();
            T out = trabajo.ejecutar(em);
            t.commit();
            return out;
        } catch (RuntimeException ex) {
            if (t.isActive()) t.rollback();
            throw ex;
        } finally {
            em.close();
        }
    }

    // Personas
    public ObservableList<Persona> listarPersonas() {
        return ejecutarTransaccion(new UnidadDeTrabajo<ObservableList<Persona>>() {
            @Override protected ObservableList<Persona> ejecutar(EntityManager em) {
                return FXCollections.observableArrayList(
                    em.createQuery(
                        "select p from Persona p where p.activo = true order by p.apellido, p.nombre",
                        Persona.class
                    ).getResultList()
                );
            }
        });
    }

    public Persona guardarPersona(Persona p){
        return ejecutarTransaccion(new UnidadDeTrabajo<Persona>() {
            @Override protected Persona ejecutar(EntityManager em) {
                if (!p.isActivo()) p.setActivo(true);
                em.persist(p);
                return p;
            }
        });
    }

    public Persona actualizarPersona(Persona p){
        return ejecutarTransaccion(new UnidadDeTrabajo<Persona>() {
            @Override protected Persona ejecutar(EntityManager em) {
                return em.merge(p);
            }
        });
    }

    // Baja lógica: no borra fila
    public void eliminarPersona(Persona p){
        ejecutarTransaccion(new UnidadDeTrabajo<Void>() {
            @Override protected Void ejecutar(EntityManager em) {
                Persona m = em.find(Persona.class, p.getIdPersona());
                if (m != null) {
                    m.setActivo(false);
                    em.merge(m);
                }
                return null;
            }
        });
    }

    // Películas
    public ObservableList<Pelicula> listarPeliculas() {
        return ejecutarTransaccion(new UnidadDeTrabajo<ObservableList<Pelicula>>() {
            @Override protected ObservableList<Pelicula> ejecutar(EntityManager em) {
                return FXCollections.observableArrayList(
                    em.createQuery(
                        "select p from Pelicula p where p.activo = true order by p.titulo",
                        Pelicula.class
                    ).getResultList()
                );
            }
        });
    }

    public Pelicula guardarPelicula(Pelicula p){
        return ejecutarTransaccion(new UnidadDeTrabajo<Pelicula>() {
            @Override protected Pelicula ejecutar(EntityManager em) {
                if (!p.isActivo()) p.setActivo(true);
                em.persist(p);
                return p;
            }
        });
    }

    public Pelicula actualizarPelicula(Pelicula p){
        return ejecutarTransaccion(new UnidadDeTrabajo<Pelicula>() {
            @Override protected Pelicula ejecutar(EntityManager em) {
                return em.merge(p);
            }
        });
    }

    // Baja lógica: no borra fila
    public void eliminarPelicula(Pelicula p){
        ejecutarTransaccion(new UnidadDeTrabajo<Void>() {
            @Override protected Void ejecutar(EntityManager em) {
                Pelicula m = em.find(Pelicula.class, p.getIdPelicula());
                if (m != null) {
                    m.setActivo(false);
                    em.merge(m);
                }
                return null;
            }
        });
    }

    // CicloCine y películas
    public CicloCine findCicloCineConPeliculas(Long id) {
        return ejecutarTransaccion(new UnidadDeTrabajo<CicloCine>() {
            @Override protected CicloCine ejecutar(EntityManager em) {
                return em.createQuery(
                        "select c from CicloCine c left join fetch c.peliculas where c.idEvento = :id",
                        CicloCine.class)
                    .setParameter("id", id)
                    .getSingleResult();
            }
        });
    }

    // Sincroniza la colección. El ORM actualiza la join-table.
    public CicloCine actualizarPeliculasCiclo(Long idCiclo, java.util.List<Pelicula> nuevas) {
        return ejecutarTransaccion(new UnidadDeTrabajo<CicloCine>() {
            @Override protected CicloCine ejecutar(EntityManager em) {
                CicloCine ciclo = em.find(CicloCine.class, idCiclo);
                if (ciclo == null) throw new IllegalArgumentException("Ciclo no encontrado: " + idCiclo);

                ciclo.getPeliculas().size(); // inicializar

                Set<Long> nuevasIds = new HashSet<>();
                if (nuevas != null) {
                    for (Pelicula p : nuevas) {
                        if (p != null && p.getIdPelicula() != null && p.isActivo()) nuevasIds.add(p.getIdPelicula());
                    }
                }
                Set<Pelicula> nuevasManaged = new HashSet<>();
                for (Long id : nuevasIds) nuevasManaged.add(em.getReference(Pelicula.class, id));

                ciclo.getPeliculas().retainAll(nuevasManaged);
                for (Pelicula pm : nuevasManaged)
                    if (!ciclo.getPeliculas().contains(pm)) ciclo.getPeliculas().add(pm);

                em.flush();
                return em.createQuery(
                        "select c from CicloCine c left join fetch c.peliculas where c.idEvento = :id",
                        CicloCine.class)
                    .setParameter("id", idCiclo)
                    .getSingleResult();
            }
        });
    }

    // Métodos privados de apoyo
    private boolean personaTieneRolEnEvento(EntityManager em, long idEvento, long idPersona){
        Long cnt = em.createQuery(
            "select count(r) from RolEvento r " +
            "where r.evento.idEvento = :ide and r.persona.idPersona = :idp and r.activo = true",
            Long.class)
            .setParameter("ide", idEvento)
            .setParameter("idp", idPersona)
            .getSingleResult();
        return cnt != null && cnt > 0;
    }

    private boolean esParticipanteDeEvento(EntityManager em, Evento ev, long idPersona){
        Long idEv = ev.getIdEvento();
        if (ev instanceof Concierto){
            Long cnt = em.createQuery(
                "select count(p) from Concierto c join c.participantes p " +
                "where c.idEvento = :id and p.idPersona = :idp and p.activo = true", Long.class)
                .setParameter("id", idEv)
                .setParameter("idp", idPersona)
                .getSingleResult();
            return cnt != null && cnt > 0;
        } else if (ev instanceof Taller){
            Long cnt = em.createQuery(
                "select count(p) from Taller t join t.participantes p " +
                "where t.idEvento = :id and p.idPersona = :idp and p.activo = true", Long.class)
                .setParameter("id", idEv)
                .setParameter("idp", idPersona)
                .getSingleResult();
            return cnt != null && cnt > 0;
        } else if (ev instanceof CicloCine){
            Long cnt = em.createQuery(
                "select count(p) from CicloCine c join c.participantes p " +
                "where c.idEvento = :id and p.idPersona = :idp and p.activo = true", Long.class)
                .setParameter("id", idEv)
                .setParameter("idp", idPersona)
                .getSingleResult();
            return cnt != null && cnt > 0;
        }
        return false;
    }

    private void validarReglasDeRol(EntityManager em, Evento ev, long idPersona, TipoRol rol){
        boolean permitido =
            (ev instanceof Concierto && (rol==TipoRol.ORGANIZADOR || rol==TipoRol.ARTISTA)) ||
            (ev instanceof Taller    && (rol==TipoRol.ORGANIZADOR || rol==TipoRol.INSTRUCTOR)) ||
            (ev instanceof Exposicion&& (rol==TipoRol.ORGANIZADOR || rol==TipoRol.CURADOR)) ||
            (ev instanceof Feria     && (rol==TipoRol.ORGANIZADOR)) ||
            (ev instanceof CicloCine && (rol==TipoRol.ORGANIZADOR));
        if (!permitido) throw new IllegalArgumentException("Rol "+rol+" no permitido para el evento.");

        if (!ev.isActivo()) throw new IllegalStateException("El evento está inactivo.");
        Persona per = em.find(Persona.class, idPersona);
        if (per == null || !per.isActivo()) throw new IllegalStateException("La persona está inactiva.");

        if (ev instanceof Taller){
            Long cantInst = em.createQuery(
                "select count(r) from RolEvento r where r.evento.idEvento=:ide and r.rol=:rol and r.activo=true",
                Long.class)
                .setParameter("ide", ev.getIdEvento())
                .setParameter("rol", TipoRol.INSTRUCTOR)
                .getSingleResult();
            if (rol == TipoRol.INSTRUCTOR && cantInst > 0)
                throw new IllegalStateException("El taller ya tiene un INSTRUCTOR.");
        }
        if (ev instanceof Exposicion){
            Long cantCur = em.createQuery(
                "select count(r) from RolEvento r where r.evento.idEvento=:ide and r.rol=:rol and r.activo=true",
                Long.class)
                .setParameter("ide", ev.getIdEvento())
                .setParameter("rol", TipoRol.CURADOR)
                .getSingleResult();
            if (rol == TipoRol.CURADOR && cantCur > 0)
                throw new IllegalStateException("La exposición ya tiene un CURADOR.");
        }

        if (esParticipanteDeEvento(em, ev, idPersona))
            throw new IllegalStateException("La persona ya está inscripta como participante en este evento.");
    }

    // Roles
    public RolEvento asignarRol(Evento evento, Persona persona, TipoRol rol) {
        return ejecutarTransaccion(new UnidadDeTrabajo<RolEvento>() {
            @Override protected RolEvento ejecutar(EntityManager em) {
                Evento ev = em.find(Evento.class, evento.getIdEvento());
                Persona pe = em.getReference(Persona.class, persona.getIdPersona());

                if (!ev.isActivo() || !pe.isActivo())
                    throw new IllegalStateException("Evento o persona inactivos.");

                long idEv = ev.getIdEvento();
                long idPe = pe.getIdPersona();

                if (personaTieneRolEnEvento(em, idEv, idPe))
                    throw new IllegalStateException("La persona ya tiene un rol en este evento.");

                validarReglasDeRol(em, ev, idPe, rol);

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

                if (existente != null) {
                    if (!existente.isActivo()) { existente.setActivo(true); em.merge(existente); }
                    return existente;
                }

                RolEvento nuevo = new RolEvento(ev, pe, rol);
                nuevo.setActivo(true);
                em.persist(nuevo);
                return nuevo;
            }
        });
    }

    // Baja lógica del rol (no borra persona ni evento)
    public void eliminarRol(Evento evento, Persona persona, TipoRol rol) {
        ejecutarTransaccion(new UnidadDeTrabajo<Void>() {
            @Override protected Void ejecutar(EntityManager em) {
                em.createQuery(
                    "update RolEvento r set r.activo = false " +
                    "where r.evento.idEvento=:ide and r.persona.idPersona=:idp and r.rol=:rol")
                  .setParameter("ide", evento.getIdEvento())
                  .setParameter("idp", persona.getIdPersona())
                  .setParameter("rol", rol)
                  .executeUpdate();
                return null;
            }
        });
    }

    public ObservableList<RolEvento> obtenerRolesDeEvento(Evento evento) {
        return ejecutarTransaccion(new UnidadDeTrabajo<ObservableList<RolEvento>>() {
            @Override protected ObservableList<RolEvento> ejecutar(EntityManager em) {
                return FXCollections.observableArrayList(
                    em.createQuery(
                        "select r from RolEvento r " +
                        "join fetch r.persona p " +
                        "where r.evento.idEvento=:id and r.activo = true " +
                        "order by r.id desc",
                        RolEvento.class
                    ).setParameter("id", evento.getIdEvento())
                     .getResultList()
                );
            }
        });
    }

    public ObservableList<RolEvento> filtrarRoles(String ne, String np, String dni) {
        return ejecutarTransaccion(new UnidadDeTrabajo<ObservableList<RolEvento>>() {
            @Override protected ObservableList<RolEvento> ejecutar(EntityManager em) {
                StringBuilder jpql = new StringBuilder(
                    "select r from RolEvento r join r.evento e join r.persona p where r.activo = true and e.activo = true and p.activo = true");
                Map<String,Object> params = new HashMap<>();
                if (ne != null && !ne.isBlank()) { jpql.append(" and lower(e.nombre) like :ne"); params.put("ne","%"+ne.toLowerCase()+"%"); }
                if (np != null && !np.isBlank()) { jpql.append(" and (lower(p.nombre) like :np or lower(p.apellido) like :np)"); params.put("np","%"+np.toLowerCase()+"%"); }
                if (dni!= null && !dni.isBlank()){ jpql.append(" and p.dni like :dni"); params.put("dni","%"+dni+"%"); }
                jpql.append(" order by r.id desc");
                TypedQuery<RolEvento> q = em.createQuery(jpql.toString(), RolEvento.class);
                params.forEach(q::setParameter);
                return FXCollections.observableArrayList(q.getResultList());
            }
        });
    }

    // Participantes
    public void agregarParticipante(Evento evento, Persona persona) {
        ejecutarTransaccion(new UnidadDeTrabajo<Void>() {
            @Override protected Void ejecutar(EntityManager em) {
                Evento e = em.find(Evento.class, evento.getIdEvento());
                Persona p = em.getReference(Persona.class, persona.getIdPersona());

                if (!e.isActivo() || !p.isActivo())
                    throw new IllegalStateException("Evento o persona inactivos.");

                long idEv = e.getIdEvento();
                long idPe = p.getIdPersona();

                if (personaTieneRolEnEvento(em, idEv, idPe))
                    throw new IllegalStateException("No se puede inscribir: la persona tiene un rol activo en este evento.");

                LocalDateTime now = LocalDateTime.now();
                if (e.getEstado() != EstadoEvento.CONFIRMADO || now.isAfter(e.getFechaFin()))
                    throw new IllegalStateException("No se permite inscribir para este evento.");

                if (e instanceof Concierto c) {
                    Long cnt = em.createQuery(
                        "select count(p) from Concierto c join c.participantes p " +
                        "where c.idEvento=:id and p.idPersona=:idp and p.activo = true", Long.class)
                        .setParameter("id", idEv)
                        .setParameter("idp", idPe)
                        .getSingleResult();
                    if (cnt != null && cnt > 0) throw new IllegalArgumentException("La persona ya está inscripta.");
                    Long inscritos = em.createQuery(
                        "select count(p) from Concierto c join c.participantes p where c.idEvento=:id and p.activo = true", Long.class)
                        .setParameter("id", idEv)
                        .getSingleResult();
                    if (inscritos >= c.getCupoMaximo())
                        throw new IllegalStateException("Cupo completo.");
                    c.getParticipantes().add(p);

                } else if (e instanceof Taller t) {
                    Long cnt = em.createQuery(
                        "select count(p) from Taller t join t.participantes p " +
                        "where t.idEvento=:id and p.idPersona=:idp and p.activo = true", Long.class)
                        .setParameter("id", idEv)
                        .setParameter("idp", idPe)
                        .getSingleResult();
                    if (cnt != null && cnt > 0) throw new IllegalArgumentException("La persona ya está inscripta.");
                    Long inscritos = em.createQuery(
                        "select count(p) from Taller t join t.participantes p where t.idEvento=:id and p.activo = true", Long.class)
                        .setParameter("id", idEv)
                        .getSingleResult();
                    if (inscritos >= t.getCupoMaximo())
                        throw new IllegalStateException("Cupo completo.");
                    t.getParticipantes().add(p);

                } else if (e instanceof CicloCine cc) {
                    Long cnt = em.createQuery(
                        "select count(p) from CicloCine c join c.participantes p " +
                        "where c.idEvento=:id and p.idPersona=:idp and p.activo = true", Long.class)
                        .setParameter("id", idEv)
                        .setParameter("idp", idPe)
                        .getSingleResult();
                    if (cnt != null && cnt > 0) throw new IllegalArgumentException("La persona ya está inscripta.");
                    Long inscritos = em.createQuery(
                        "select count(p) from CicloCine c join c.participantes p where c.idEvento=:id and p.activo = true", Long.class)
                        .setParameter("id", idEv)
                        .getSingleResult();
                    if (inscritos >= cc.getCupoMaximo())
                        throw new IllegalStateException("Cupo completo.");
                    cc.getParticipantes().add(p);

                } else {
                    throw new IllegalArgumentException("El evento no admite inscripción.");
                }
                return null;
            }
        });
    }

    public void quitarParticipante(Evento evento, Persona persona) {
        ejecutarTransaccion(new UnidadDeTrabajo<Void>() {
            @Override protected Void ejecutar(EntityManager em) {
                Evento e = em.find(Evento.class, evento.getIdEvento());
                Persona p = em.getReference(Persona.class, persona.getIdPersona());
                if (e instanceof Concierto c)        c.getParticipantes().remove(p);
                else if (e instanceof Taller t)      t.getParticipantes().remove(p);
                else if (e instanceof CicloCine cc)  cc.getParticipantes().remove(p);
                else throw new IllegalArgumentException("El evento no admite inscripción.");
                return null;
            }
        });
    }

    public ObservableList<Persona> obtenerParticipantes(Evento evento) {
        return ejecutarTransaccion(new UnidadDeTrabajo<ObservableList<Persona>>() {
            @Override protected ObservableList<Persona> ejecutar(EntityManager em) {
                Long id = evento.getIdEvento();
                if (evento instanceof Concierto)
                    return FXCollections.observableArrayList(
                        em.createQuery(
                            "select p from Concierto c join c.participantes p " +
                            "where c.idEvento=:id and p.activo = true order by p.apellido, p.nombre", Persona.class)
                          .setParameter("id", id).getResultList());
                else if (evento instanceof Taller)
                    return FXCollections.observableArrayList(
                        em.createQuery(
                            "select p from Taller t join t.participantes p " +
                            "where t.idEvento=:id and p.activo = true order by p.apellido, p.nombre", Persona.class)
                          .setParameter("id", id).getResultList());
                else if (evento instanceof CicloCine)
                    return FXCollections.observableArrayList(
                        em.createQuery(
                            "select p from CicloCine c join c.participantes p " +
                            "where c.idEvento=:id and p.activo = true order by p.apellido, p.nombre", Persona.class)
                          .setParameter("id", id).getResultList());
                return FXCollections.observableArrayList();
            }
        });
    }

    // Eventos
    public List<Evento> listarEventos() {
        return ejecutarTransaccion(new UnidadDeTrabajo<List<Evento>>() {
            @Override protected List<Evento> ejecutar(EntityManager em) {
                return em.createQuery(
                    "select distinct e from Evento e " +
                    "left join fetch e.roles r " +
                    "left join fetch r.persona p " +
                    "where e.activo = true and (r is null or r.activo = true) " +
                    "order by e.fechaInicio", Evento.class
                ).getResultList();
            }
        });
    }

    public <T extends Evento> T guardarEvento(T e){
        return ejecutarTransaccion(new UnidadDeTrabajo<T>() {
            @Override protected T ejecutar(EntityManager em) {
                if (!e.isActivo()) e.setActivo(true);
                em.persist(e);
                return e;
            }
        });
    }

    public <T extends Evento> T actualizarEvento(T e){
        return ejecutarTransaccion(new UnidadDeTrabajo<T>() {
            @Override protected T ejecutar(EntityManager em) {
                return em.merge(e);
            }
        });
    }

    // “Eliminar” evento: desasociar y marcar inactivo. No borra filas.
    public void eliminarEvento(Evento e){
        ejecutarTransaccion(new UnidadDeTrabajo<Void>() {
            @Override protected Void ejecutar(EntityManager em) {
                Evento ev = em.find(Evento.class, e.getIdEvento());
                if (ev == null) return null;

                Long idEv = ev.getIdEvento();

                // Desactivar roles del evento (baja lógica de la asociación)
                em.createQuery("update RolEvento r set r.activo=false where r.evento.idEvento=:id")
                .setParameter("id", idEv)
                .executeUpdate();

                // Limpiar colecciones de participación / join-table
                if (ev instanceof Concierto c)        c.getParticipantes().clear();
                else if (ev instanceof Taller t)      t.getParticipantes().clear();
                else if (ev instanceof CicloCine cc){ cc.getParticipantes().clear(); cc.getPeliculas().clear(); }

                // Marcar evento como inactivo
                ev.setActivo(false);
                em.merge(ev);
                return null;
            }
        });
    }

    public List<Evento> buscarEventos(TipoEvento tipo, EstadoEvento estado, LocalDate desde, LocalDate hasta) {
        return ejecutarTransaccion(new UnidadDeTrabajo<List<Evento>>() {
            @Override protected List<Evento> ejecutar(EntityManager em) {
                StringBuilder jpql = new StringBuilder(
                    "select distinct e from Evento e left join fetch e.roles r left join fetch r.persona p " +
                    "where e.activo = true and (r is null or r.activo = true)");
                Map<String,Object> params = new HashMap<>();

                if (tipo   != null){ jpql.append(" and e.tipoEvento = :tipo");   params.put("tipo", tipo); }
                if (estado != null){ jpql.append(" and e.estado = :estado");     params.put("estado", estado); }

                LocalDateTime from = (desde == null) ? null : desde.atStartOfDay();
                LocalDateTime to   = (hasta == null) ? null : hasta.atTime(LocalTime.of(23,59,59));

                if (from != null){ jpql.append(" and e.fechaFin >= :from"); params.put("from", from); }
                if (to   != null){ jpql.append(" and e.fechaInicio <= :to"); params.put("to", to); }

                jpql.append(" order by e.fechaInicio");

                TypedQuery<Evento> q = em.createQuery(jpql.toString(), Evento.class);
                params.forEach(q::setParameter);
                return q.getResultList();
            }
        });
    }

    // Personas elegibles para inscribirse: activos y sin rol activo ni inscripción
    public java.util.List<Persona> personasElegiblesParaInscripcion(Evento e) {
        return ejecutarTransaccion(new UnidadDeTrabajo<java.util.List<Persona>>() {
            @Override protected java.util.List<Persona> ejecutar(EntityManager em) {
                Evento ev = em.find(Evento.class, e.getIdEvento());
                if (!ev.isActivo()) return java.util.Collections.emptyList();

                Long idEv = ev.getIdEvento();

                String base = """
                    select p from Persona p
                    where p.activo = true
                      and not exists (
                        select 1 from RolEvento r
                        where r.evento.idEvento = :idEv
                          and r.persona.idPersona = p.idPersona
                          and r.activo = true
                      )
                    """;

                String order = " order by p.apellido, p.nombre";

                if (ev instanceof Concierto) {
                    String jpql = base + """
                        and not exists (
                            select 1 from Concierto c join c.participantes px
                            where c.idEvento = :idEv and px.idPersona = p.idPersona and px.activo = true
                        )
                        """ + order;
                    return em.createQuery(jpql, Persona.class)
                            .setParameter("idEv", idEv)
                            .getResultList();

                } else if (ev instanceof Taller) {
                    String jpql = base + """
                        and not exists (
                            select 1 from Taller t join t.participantes px
                            where t.idEvento = :idEv and px.idPersona = p.idPersona and px.activo = true
                        )
                        """ + order;
                    return em.createQuery(jpql, Persona.class)
                            .setParameter("idEv", idEv)
                            .getResultList();

                } else if (ev instanceof CicloCine) {
                    String jpql = base + """
                        and not exists (
                            select 1 from CicloCine c join c.participantes px
                            where c.idEvento = :idEv and px.idPersona = p.idPersona and px.activo = true
                        )
                        """ + order;
                    return em.createQuery(jpql, Persona.class)
                            .setParameter("idEv", idEv)
                            .getResultList();
                }

                return java.util.Collections.emptyList();
            }
        });
    }
}
