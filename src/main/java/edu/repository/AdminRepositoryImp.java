package edu.repository;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import edu.entity.Course;

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
    public List<Course> paginateCourses(int page, int size) {
        Session session = sessionFactory.openSession();
        try {
            String hql = "FROM Course";
            Query<Course> query = session.createQuery(hql, Course.class);
            query.setFirstResult((page - 1) * size);
            query.setMaxResults(size);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            session.close();
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
            session.merge(course); // Thay vì update()
            session.flush(); // Thêm flush
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

}
