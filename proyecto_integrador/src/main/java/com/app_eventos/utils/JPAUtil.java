package com.app_eventos.utils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/** Fabrica del EntityManager. */
public final class JPAUtil {
    private static final EntityManagerFactory EMF = Persistence.createEntityManagerFactory("app_eventosPU");
    
    private JPAUtil() {}
    
        /** Alias usado por todo el proyecto. */
    public static EntityManager em(){ 
        return EMF.createEntityManager(); 
    }

    /** Alias alternativo por compatibilidad. */
    public static EntityManager getEntityManager(){ return em(); }

    public static void close() {
    if (EMF != null && EMF.isOpen()) EMF.close();
  }
}
