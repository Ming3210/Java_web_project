package edu.repository;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import edu.entity.Course;

import java.util.Collections;
import java.util.List;
@Repository
public class AdminRepositoryImp implements AdminRepository{

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public List<Course> getAllCourses() {
        Session session = sessionFactory.openSession();
        try {
            String hql = "FROM Course";
            return session.createQuery(hql, Course.class).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            session.close();
        }
    }

    @Override
    public int countAllCourse() {
        Session session = sessionFactory.openSession();
        try {
            String hql = "SELECT COUNT(c.id) FROM Course c";
            Query<Long> query = session.createQuery(hql, Long.class);
            Long count = query.getSingleResult();
            return count.intValue();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Course> paginateCourses(int page, int size, String sortBy, String order, String status, String keyword) {
        try (Session session = sessionFactory.openSession()) {
            String hql = "FROM Course WHERE 1=1";

            if (!"ALL".equalsIgnoreCase(status)) {
                hql += " AND status = :status";
            }
            if (keyword != null && !keyword.trim().isEmpty()) {
                hql += " AND lower(name) LIKE :keyword";
            }

            hql += " ORDER BY " + sortBy + " " + order;

            Query<Course> query = session.createQuery(hql, Course.class);

            if (!"ALL".equalsIgnoreCase(status)) {
                query.setParameter("status", "ACTIVE".equalsIgnoreCase(status));
            }
            if (keyword != null && !keyword.trim().isEmpty()) {
                query.setParameter("keyword", "%" + keyword.toLowerCase() + "%");
            }

            query.setFirstResult((page - 1) * size);
            query.setMaxResults(size);

            return query.getResultList();
        }
    }


    @Override
    public boolean saveCourse(Course course) {
        Session session = sessionFactory.openSession();
        try {
            session.beginTransaction();
            session.saveOrUpdate(course);
            session.getTransaction().commit();
            return true;
        }
        catch (Exception e) {
            if (session.getTransaction() != null) {
                session.getTransaction().rollback();
            }
            e.printStackTrace();
            return false;
        } finally {
            session.close();
        }
    }

    @Override
    public Course getCourseById(int id) {
        Session session = sessionFactory.openSession();
        try {
            return session.get(Course.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            session.close();
        }
    }

    @Override
    public boolean editCourse(Course course) {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            session.update(course);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return false;
        } finally {
            session.close();
        }
    }

    @Override
    public boolean deleteCourse(int id) {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            Course course = session.get(Course.class, id);
            if (course != null) {
                session.delete(course);
                transaction.commit();
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return false;
        } finally {
            session.close();
        }
    }

    @Override
    public int countCoursesByStatusAndKeyword(String status, String keyword) {
        try (Session session = sessionFactory.openSession()) {
            String hql = "SELECT COUNT(*) FROM Course WHERE 1=1";

            if (!"ALL".equalsIgnoreCase(status)) {
                hql += " AND status = :status";
            }
            if (keyword != null && !keyword.trim().isEmpty()) {
                hql += " AND lower(name) LIKE :keyword";
            }

            Query<Long> query = session.createQuery(hql, Long.class);

            if (!"ALL".equalsIgnoreCase(status)) {
                query.setParameter("status", "ACTIVE".equalsIgnoreCase(status));
            }
            if (keyword != null && !keyword.trim().isEmpty()) {
                query.setParameter("keyword", "%" + keyword.toLowerCase() + "%");
            }

            return query.uniqueResult().intValue();
        }
    }

}
