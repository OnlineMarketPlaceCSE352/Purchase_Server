package com.project.util;

import jakarta.persistence.EntityManagerFactory;
import lombok.Getter;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.jinq.jpa.JinqJPAStreamProvider;

public class HibernateUtil {
    @Getter
    private static final SessionFactory sessionFactory = buildSessionFactory();

    @Getter
    private static final JinqJPAStreamProvider jinqProvider;

    static {
        EntityManagerFactory emf = sessionFactory.unwrap(EntityManagerFactory.class);
        jinqProvider = new JinqJPAStreamProvider(emf);
    }


    private static SessionFactory buildSessionFactory() {
        try {
            return new Configuration().configure().buildSessionFactory();
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static void shutdown() {
        getSessionFactory().close();
    }
}