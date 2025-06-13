package edu.repository;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import edu.entity.User;

@Repository
public class AuthRepositoryImp implements AuthRepository{
    @Autowired
    private SessionFactory sessionFactory;


    @Override
    public boolean register(User user) {
        Session session = sessionFactory.openSession();
        try {
            session.beginTransaction();
            session.save(user);
            session.getTransaction().commit();
            return true;
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

    public User login(String email, String password) {
        String hql = "FROM User WHERE email = :email";
        Session session = sessionFactory.openSession();
        try {
            User user = session.createQuery(hql, User.class)
                    .setParameter("email", email)
                    .uniqueResult();

            if (user != null && BCrypt.checkpw(password, user.getPassword())) {
                return user;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            session.close();
        }
    }


}
