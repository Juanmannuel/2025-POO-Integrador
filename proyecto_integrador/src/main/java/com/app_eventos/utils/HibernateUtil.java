package com.app_eventos.utils;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Helper mínimo para inicializar y reutilizar un EntityManagerFactory JPA.
 *
 * NOTA IMPORTANTE:
 * - No usamos SessionFactory (eso es de Hibernate puro). Aquí trabajamos con JPA.
 * - El nombre de la unidad de persistencia debe coincidir con el de tu persistence.xml: "app_eventosPU".
 */
public final class HibernateUtil {

    // Nombre de la unidad de persistencia (persistence.xml -> <persistence-unit name="app_eventosPU">)
    private static final String PU_NAME = "app_eventosPU";

    // Un único EMF para toda la app (patrón singleton)
    private static final EntityManagerFactory EMF = buildEntityManagerFactory();

    private HibernateUtil() { /* util class */ }

    private static EntityManagerFactory buildEntityManagerFactory() {
        try {
            return Persistence.createEntityManagerFactory(PU_NAME);
        } catch (Throwable ex) {
            // Si falla acá, es porque hay un problema de configuración (persistence.xml, driver, URL, etc.)
            throw new ExceptionInInitializerError("Error al inicializar EntityManagerFactory: " + ex.getMessage());
        }
    }

    /**
     * Devuelve el EntityManagerFactory global.
     * Desde tu repositorio harás: HibernateUtil.getEntityManagerFactory().createEntityManager()
     */
    public static EntityManagerFactory getEntityManagerFactory() {
        return EMF;
    }

    /**
     * Cierra el EMF en el shutdown de la app (opcional).
     */
    public static void shutdown() {
        if (EMF != null && EMF.isOpen()) {
            EMF.close();
        }
    }
}
