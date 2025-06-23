package edu.repository;

import edu.entity.Course;
import edu.entity.Enrollment;
import edu.entity.User;
import edu.utils.CourseStatus;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public class ClientRepositoryImp implements ClientRepository{

    @Autowired
    private SessionFactory sessionFactory;


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
    public int countEnrollmentsByUserIdAndKeyword(int userId, String keyword, String status) {
        Session session = sessionFactory.openSession();
        try {
            StringBuilder hql = new StringBuilder("SELECT count(e.id) FROM Enrollment e WHERE e.user.id = :userId");

            if (status != null && !status.trim().isEmpty()) {
                hql.append(" AND e.status = :status");
            }
            if (keyword != null && !keyword.trim().isEmpty()) {
                hql.append(" AND lower(e.course.name) LIKE :keyword");
            }

            Query<Long> query = session.createQuery(hql.toString(), Long.class);
            query.setParameter("userId", userId);

            if (status != null && !status.trim().isEmpty()) {
                try {
                    // PHẢI convert String thành Enum trước khi set parameter
                    CourseStatus enumStatus = CourseStatus.valueOf(status.toUpperCase());
                    query.setParameter("status", enumStatus);
                } catch (IllegalArgumentException ex) {
                    System.err.println("Invalid status value: " + status);
                    // Trả về 0 nếu status không hợp lệ
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
        } finally {
            session.close();
        }
    }

    @Override
    public boolean cancelEnrollment(int userId, Enrollment enrollment) {
        Session session = sessionFactory.openSession();
        try {
            session.beginTransaction();
            Enrollment existingEnrollment = session.get(Enrollment.class, enrollment.getId());
            if (existingEnrollment != null && existingEnrollment.getUser().getId() == userId) {
                existingEnrollment.setStatus(CourseStatus.CANCEL);
                session.update(existingEnrollment);
                session.getTransaction().commit();
                return true;
            } else {
                return false; // Enrollment not found or does not belong to the user
            }
        } catch (Exception e) {
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
    public boolean updateProfile(User user) {
        Session session = sessionFactory.openSession();
        try {
            session.beginTransaction();
            User existingUser = session.get(User.class, user.getId());
            if (existingUser != null) {
                existingUser.setUsername(user.getUsername());
                existingUser.setEmail(user.getEmail());
                existingUser.setPhone(user.getPhone());
                existingUser.setAvatar(user.getAvatar());
                existingUser.setDob(user.getDob());
                existingUser.setGender(user.isGender());
                session.update(existingUser);
                session.getTransaction().commit();
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
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
    public User findUserById(int userId) {
        Session session = sessionFactory.openSession();
        try {
            return session.get(User.class, userId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            session.close();
        }
    }

    @Override
    public boolean updatePassword(int userId, String newPassword) {
        Session session = sessionFactory.openSession();
        try {
            session.beginTransaction();
            User user = session.get(User.class, userId);
            if (user != null) {
                user.setPassword(newPassword);
                session.update(user);
                session.getTransaction().commit();
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            if (session.getTransaction() != null) {
                session.getTransaction().rollback();
            }
            e.printStackTrace();
            return false;
        } finally {
            session.close();
        }
    }


}
