package edu.repository;

import edu.entity.Enrollment;
import edu.entity.User;
import edu.utils.CourseStatus;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import edu.entity.Course;

import java.time.LocalDate;
import java.util.ArrayList;
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
        // update status to false instead of deleting
        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            Course course = session.get(Course.class, id);
            if (course != null) {
                course.setStatus(false); // Assuming status is a boolean field
                session.update(course);
                transaction.commit();
                return true;
            }
            return false; // Course not found
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

    public boolean enrollmentCourse(int userId, int courseId) {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();

            User user = session.get(User.class, userId);
            Course course = session.get(Course.class, courseId);

            if (user == null || course == null) {
                return false;
            }

            String hql = "FROM Enrollment WHERE user.id = :userId AND course.id = :courseId";
            Query query = session.createQuery(hql);
            query.setParameter("userId", userId);
            query.setParameter("courseId", courseId);

            if (!query.list().isEmpty()) {
                return false;
            }

            Enrollment enrollment = new Enrollment();
            enrollment.setUser(user);
            enrollment.setCourse(course);
            enrollment.setRegisteredAt(LocalDate.now());
            enrollment.setStatus(CourseStatus.WAITING);

            session.save(enrollment);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return false;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    @Override
    public List<Enrollment> getEnrollmentsByUserId(int userId) {
        Session session = sessionFactory.openSession();
        try {
            String hql = "FROM Enrollment e WHERE e.user.id = :userId";
            return session.createQuery(hql, Enrollment.class)
                    .setParameter("userId", userId)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            session.close();
        }
    }

    @Override
    public List<Enrollment> getEnrollmentsByUserIdAndFilter(int userId, String status, String keyword, int page, int size) {
        try (Session session = sessionFactory.openSession()) {
            StringBuilder hql = new StringBuilder("FROM Enrollment e WHERE e.user.id = :userId");

            if (status != null && !status.trim().isEmpty()) {
                hql.append(" AND e.status = :status");
            }
            if (keyword != null && !keyword.trim().isEmpty()) {
                hql.append(" AND lower(e.course.name) LIKE :keyword");
            }

            hql.append(" ORDER BY e.registeredAt DESC");

            Query<Enrollment> query = session.createQuery(hql.toString(), Enrollment.class);
            query.setParameter("userId", userId);

            if (status != null && !status.trim().isEmpty()) {
                try {
                    // PHẢI convert String thành Enum
                    CourseStatus enumStatus = CourseStatus.valueOf(status.toUpperCase());
                    query.setParameter("status", enumStatus);
                    System.out.println("Setting enum status: " + enumStatus);
                } catch (IllegalArgumentException ex) {
                    System.err.println("Invalid status value: " + status);
                    // Trả về empty list nếu status không hợp lệ
                    return Collections.emptyList();
                }
            }

            if (keyword != null && !keyword.trim().isEmpty()) {
                query.setParameter("keyword", "%" + keyword.toLowerCase() + "%");
            }

            query.setFirstResult((page - 1) * size);
            query.setMaxResults(size);

            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public List<User> paginateUsers(int page, int size, String sortBy, String order, String status, String keyword) {
        try (Session session = sessionFactory.openSession()) {
            String hql = "FROM User WHERE 1=1";

            if (!"ALL".equalsIgnoreCase(status)) {
                hql += " AND status = :status";
            }
            if (keyword != null && !keyword.trim().isEmpty()) {
                hql += " AND lower(username) LIKE :keyword OR lower(email) LIKE :keyword";
            }

            hql += " ORDER BY " + sortBy + " " + order;

            Query<User> query = session.createQuery(hql, User.class);

            if (!"ALL".equalsIgnoreCase(status)) {
                query.setParameter("status", "ACTIVE".equalsIgnoreCase(status));
            }
            if (keyword != null && !keyword.trim().isEmpty()) {
                query.setParameter("keyword", "%" + keyword.toLowerCase() + "%");
            }

            query.setFirstResult((page - 1) * size);
            query.setMaxResults(size);

            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public int countAllStudentsByStatusAndKeyword(String status, String keyword) {
        try (Session session = sessionFactory.openSession()) {
            String hql = "SELECT COUNT(*) FROM User WHERE 1=1";

            if (!"ALL".equalsIgnoreCase(status)) {
                hql += " AND status = :status";
            }
            if (keyword != null && !keyword.trim().isEmpty()) {
                hql += " AND lower(username) LIKE :keyword";
            }

            Query<Long> query = session.createQuery(hql, Long.class);

            if (!"ALL".equalsIgnoreCase(status)) {
                query.setParameter("status", "ACTIVE".equalsIgnoreCase(status));
            }
            if (keyword != null && !keyword.trim().isEmpty()) {
                query.setParameter("keyword", "%" + keyword.toLowerCase() + "%");
            }

            return query.uniqueResult().intValue();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public boolean updateUserStatus(int userId) {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            User user = session.get(User.class, userId);
            if (user != null) {
                user.setStatus(!user.isStatus());
                session.update(user);
                transaction.commit();
                return true;
            }
            return false;
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
    public int countAllStudents() {
        Session session = sessionFactory.openSession();
        try {
            String hql = "SELECT COUNT(u.id) FROM User u WHERE u.role = 'USER'";
            Query<Long> query = session.createQuery(hql, Long.class);
            Long count = query.getSingleResult();
            return count.intValue();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            session.close();
        }
    }

    @Override
    public int countAllConfirmedEnrollments() {
        Session session = sessionFactory.openSession();
        try {
            String hql = "SELECT COUNT(e.id) FROM Enrollment e WHERE e.status = :status";
            Query<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("status", CourseStatus.CONFIRM);
            Long count = query.getSingleResult();
            return count.intValue();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            session.close();
        }
    }

    @Override
    public int staticticsStudentsByCourseId(int courseId) {
        Session session = sessionFactory.openSession();
        try {
            String hql = "SELECT COUNT(e.id) FROM Enrollment e WHERE e.course.id = :courseId AND e.status = :status";
            Query<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("courseId", courseId);
            query.setParameter("status", CourseStatus.CONFIRM);
            Long count = query.getSingleResult();
            return count.intValue();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            session.close();
        }
    }

    @Override
    public List<Course> top5CourseWithHighestEnrollments() {
        Session session = sessionFactory.openSession();
        try {
            String hql = "SELECT c FROM Course c JOIN c.enrollments e GROUP BY c.id ORDER BY COUNT(e.id) DESC";
            Query<Course> query = session.createQuery(hql, Course.class);
            query.setMaxResults(5);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            session.close();
        }
    }

    @Override
    public List<Enrollment> paginateEnrollments(int page, int size, String sortBy, String order, String status, String keyword) {
        try (Session session = sessionFactory.openSession()) {
            StringBuilder hql = new StringBuilder("FROM Enrollment e WHERE 1=1");

            if (status != null && !status.trim().isEmpty()) {
                hql.append(" AND e.status = :status");
            }
            if (keyword != null && !keyword.trim().isEmpty()) {
                hql.append(" AND lower(e.course.name) LIKE :keyword");
            }

            hql.append(" ORDER BY e.").append(sortBy).append(" ").append(order);

            Query<Enrollment> query = session.createQuery(hql.toString(), Enrollment.class);

            if (status != null && !status.trim().isEmpty()) {
                try {
                    CourseStatus enumStatus = CourseStatus.valueOf(status.toUpperCase());
                    query.setParameter("status", enumStatus);
                } catch (IllegalArgumentException ex) {
                    System.err.println("Invalid status value: " + status);
                    return Collections.emptyList();
                }
            }

            if (keyword != null && !keyword.trim().isEmpty()) {
                query.setParameter("keyword", "%" + keyword.toLowerCase() + "%");
            }

            query.setFirstResult((page - 1) * size);
            query.setMaxResults(size);

            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public int countEnrollmentsByStatusAndKeyword(String status, String keyword) {
        try (Session session = sessionFactory.openSession()) {
            StringBuilder hql = new StringBuilder("SELECT COUNT(*) FROM Enrollment e WHERE 1=1");

            if (status != null && !status.trim().isEmpty()) {
                hql.append(" AND e.status = :status");
            }
            if (keyword != null && !keyword.trim().isEmpty()) {
                hql.append(" AND lower(e.course.name) LIKE :keyword");
            }

            Query<Long> query = session.createQuery(hql.toString(), Long.class);

            if (status != null && !status.trim().isEmpty()) {
                try {
                    CourseStatus enumStatus = CourseStatus.valueOf(status.toUpperCase());
                    query.setParameter("status", enumStatus);
                } catch (IllegalArgumentException ex) {
                    System.err.println("Invalid status value: " + status);
                    return 0;
                }
            }

            if (keyword != null && !keyword.trim().isEmpty()) {
                query.setParameter("keyword", "%" + keyword.toLowerCase() + "%");
            }

            return query.uniqueResult().intValue();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public boolean aproveEnrollment(Enrollment enrollment) {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            enrollment.setStatus(CourseStatus.CONFIRM);
            session.update(enrollment);
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
    public boolean rejectEnrollment(Enrollment enrollment) {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            enrollment.setStatus(CourseStatus.DENIED);
            session.update(enrollment);
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
    public Enrollment getEnrollmentById(int id) {
        Session session = sessionFactory.openSession();
        try {
            return session.get(Enrollment.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            session.close();
        }
    }


}
