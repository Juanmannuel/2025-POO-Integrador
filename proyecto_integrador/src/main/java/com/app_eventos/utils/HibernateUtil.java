package com.app_eventos.utils;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;


public final class HibernateUtil {

    // Nombre de la unidad de persistencia
    private static final String PU_NAME = "app_eventosPU";

    // Un único EMF para toda la app
    private static final EntityManagerFactory EMF = buildEntityManagerFactory();

    private HibernateUtil(){}

    private static EntityManagerFactory buildEntityManagerFactory() {
        return Persistence.createEntityManagerFactory(PU_NAME);
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        return EMF;
    }

    public static void shutdown() {
        if (EMF != null && EMF.isOpen()) {
            EMF.close();
        }
    }
}
