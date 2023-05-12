package com.pcandroiddev.noteworthybackend.dao.user;

import com.pcandroiddev.noteworthybackend.dao.Dao;
import com.pcandroiddev.noteworthybackend.model.exception.MessageBody;
import com.pcandroiddev.noteworthybackend.model.user.User;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.TransactionRequiredException;
import jakarta.transaction.Transactional;
import org.hibernate.NonUniqueResultException;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Transactional
@Repository
public class UserDao extends Dao {


    public Object saveUser(User user) {
        try {
            begin();

            Query query = (Query) getEntityManager().createQuery("from User u where u.email = :email");
            query.setParameter("email", user.getEmail());
            List<User> usersInDB = query.list();
            if (usersInDB.size() > 0) {
                return new MessageBody("User already exists!");
            }


            getEntityManager().persist(user);
            commit();
            close();
            return user;
        } catch (EntityExistsException e) {
            System.out.println("EntityExistsException: " + e.getMessage());
            return null;
        } catch (IllegalArgumentException e) {
            System.out.println("IllegalArgumentException: " + e.getMessage());
            return null;
        } catch (TransactionRequiredException e) {
            System.out.println("TransactionRequiredException: " + e.getMessage());
            return null;
        }
    }

    public void deleteUser(User user) {
        begin();
        getEntityManager().remove(user);
        commit();
        close();
    }

    public void deleteUserById(int userId) {
        begin();
        getEntityManager().remove(getUser(userId));
        commit();
        close();
    }

    public User getUser(int userId) {
        System.out.println("userId: " + userId);
        begin();
        User user = getEntityManager().find(User.class, userId);
        commit();
        close();
        return user;
    }

    public User findById(int userId) {
        begin();
        Query query = (Query) getEntityManager().createQuery("from User u where u.id = :id");
        query.setParameter("id", userId);
        try {
            Object user = query.uniqueResult();
            commit();
            close();
            return (User) user;

        } catch (NonUniqueResultException nonUniqueResultException) {
            return null;
        }
    }

    public User findByEmail(String email) {
        begin();
        Query query = (Query) getEntityManager().createQuery("from User u where u.email = :email");
        query.setParameter("email", email);
        try {
            Object user = query.uniqueResult();
            commit();
            close();
            return (User) user;

        } catch (NonUniqueResultException nonUniqueResultException) {
            return null;
        }
    }
}
