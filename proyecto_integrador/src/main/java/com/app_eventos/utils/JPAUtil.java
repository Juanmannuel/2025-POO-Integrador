package com.app_eventos.utils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public final class JPAUtil {

    private static final String PU_NAME = "app_eventosPU";
    private static final EntityManagerFactory EMF = Persistence.createEntityManagerFactory(PU_NAME);

    private JPAUtil() {}

    /** Obtiene un EntityManager nuevo. Cerrar siempre con em.close(). */
    public static EntityManager em() {
        return EMF.createEntityManager();
    }

    /** Alias por compatibilidad. */
    public static EntityManager getEntityManager() {
        return em();
    }

    /** Acceso al EMF si alguna API lo requiere. */
    public static EntityManagerFactory getFactory() {
        return EMF;
    }

    /** Cierra el EMF al apagar la app. Llamar una sola vez. */
    public static void close() {
        if (EMF != null && EMF.isOpen()) EMF.close();
    }
}
