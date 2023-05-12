package com.pcandroiddev.noteworthybackend.dao;

import jakarta.persistence.EntityManager;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Dao {

    private static final Logger log = Logger.getAnonymousLogger();
    private static final ThreadLocal sessionThread = new ThreadLocal();
    //        private static final SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
    private static final SessionFactory sessionFactory = new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();

    protected Dao() {
    }

    public static EntityManager getEntityManager() {
        EntityManager entityManager = (EntityManager) Dao.sessionThread.get();

        if (entityManager == null) {
            entityManager = sessionFactory.createEntityManager();
            Dao.sessionThread.set(entityManager);
        }

        return entityManager;
    }

    /* public static Session getSession() {
         Session session = (Session) Dao.sessionThread.get();

         if (session == null) {
             session = sessionFactory.openSession();
             Dao.sessionThread.set(session);
         }
         return session;
     }
 */
    protected void begin() {
        getEntityManager().getTransaction().begin();
    }

    protected void commit() {
        getEntityManager().getTransaction().commit();
    }

    protected void rollback() {
        try {
            getEntityManager().getTransaction().rollback();
        } catch (HibernateException e) {
            log.log(Level.WARNING, "Cannot rollback", e);
        }
        try {
            getEntityManager().close();
        } catch (HibernateException e) {
            log.log(Level.WARNING, "Cannot close", e);
        }
        Dao.sessionThread.set(null);
    }

    public static void close() {
        getEntityManager().close();
        Dao.sessionThread.set(null);
    }
}


