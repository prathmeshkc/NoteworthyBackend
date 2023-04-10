package com.pcandroiddev.noteworthybackend.dao.note;

import com.pcandroiddev.noteworthybackend.dao.Dao;
import com.pcandroiddev.noteworthybackend.model.note.Note;
import com.pcandroiddev.noteworthybackend.model.user.User;
import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Transactional
@Repository
public class NoteDao extends Dao {


    public Note saveNote(Note note) {
        try {
            begin();
            getEntityManager().persist(note);
            commit();
            return note;
        } catch (EntityExistsException e) {
            System.out.println("EntityExistsException: " + e.getMessage());
            return null;
        } catch (IllegalArgumentException e) {
            System.out.println("IllegalArgumentException: " + e.getMessage());
            return null;
        } catch (TransactionRequiredException e) {
            System.out.println("TransactionRequiredException: " + e.getMessage());
            return null;
        } catch (Exception exception) {
            System.out.println("Some other Exception occurred: " + exception.getMessage());
            return null;
        }
    }


    public Note updateNoteById(Integer noteId, Note newNote) {
        try {
            begin();
            Note note = getEntityManager().find(Note.class, noteId);

            if (note == null) {
                System.out.println("Cannot find Note with Id: " + noteId);
                return null;
            }

            note.setTitle(newNote.getTitle());
            note.setDescription(newNote.getDescription());
            note.setImg_urls(newNote.getImg_urls());

            Note updatedNote = getEntityManager().merge(note);
            commit();
            return updatedNote;
        } catch (IllegalArgumentException e) {
            System.out.println("IllegalArgumentException: " + e.getMessage());
            return null;
        } catch (TransactionRequiredException e) {
            System.out.println("TransactionRequiredException: " + e.getMessage());
            return null;
        } catch (Exception exception) {
            System.out.println("Some other Exception occurred: " + exception.getMessage());
            return null;
        }
    }


    public Note deleteNoteById(Integer noteId) {
        try {
            begin();
            Note noteToRemove = getEntityManager().find(Note.class, noteId);
            if (noteToRemove == null) {
                System.out.println("Cannot find Note with Id: " + noteId);
                return null;
            }
            getEntityManager().remove(noteToRemove);
            commit();
            return noteToRemove;
        } catch (IllegalArgumentException e) {
            System.out.println("IllegalArgumentException: " + e.getMessage());
            return null;
        } catch (TransactionRequiredException e) {
            System.out.println("TransactionRequiredException: " + e.getMessage());
            return null;
        } catch (Exception exception) {
            System.out.println("Some other Exception occurred: " + exception.getMessage());
            return null;
        }
    }


    public Note getNoteById(Integer noteId) {
        begin();
        try {
            Note note = getEntityManager().find(Note.class, noteId);
            commit();
            return note;
        } catch (IllegalArgumentException e) {
            System.out.println("IllegalArgumentException: " + e.getMessage());
            return null;
        }
    }


    public List<Note> getAllNotes(Integer userId) {
        begin();
        Query query = (Query) getEntityManager().createQuery("from Note n where n.user.id = :userId");
        query.setParameter("userId", userId);
        try {
            List<Note> notes = query.list();
            commit();
            return notes;

        } catch (Exception exception) {
            return null;
        }
    }

    public List<Note> sortNotesByLowPriority(Integer userId) {
        begin();
//        Query query = (Query) getEntityManager().createQuery("FROM Note n WHERE n.user.id = :userId ORDER BY CASE WHEN ENUM(n.priority) = 'LOW' THEN 1 WHEN ENUM(n.priority) = 'MEDIUM' THEN 2 WHEN ENUM(n.priority) = 'HIGH' THEN 3 END");
        Query query = (Query) getEntityManager().createQuery("from Note n where n.user.id = :userId order by case n.priority when 'LOW' then 1 when 'MEDIUM' then 2 when 'HIGH' then 3 end");
        query.setParameter("userId", userId);
        try {
            List<Note> notes = query.list();
            commit();
            return notes;

        } catch (Exception exception) {
            return null;
        }
    }

    public List<Note> sortNotesByHighPriority(Integer userId) {
        begin();
        Query query = (Query) getEntityManager().createQuery("FROM Note n WHERE n.user.id = :userId ORDER BY CASE n.priority WHEN 'HIGH' THEN 1 WHEN 'MEDIUM' THEN 2 WHEN 'LOW' THEN 3 END");
        query.setParameter("userId", userId);
        try {
            List<Note> notes = query.list();
            commit();
            return notes;

        } catch (Exception exception) {
            return null;
        }
    }

    public List<Note> searchNote(String searchText, Integer userId) {
        begin();
        CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Note> query = criteriaBuilder.createQuery(Note.class);
        Root<Note> root = query.from(Note.class);
        Join<Note, User> userJoin = root.join("user");
        try {
            query.select(root).where(
                    criteriaBuilder.or(
                            criteriaBuilder.like(root.get("title"), "%" + searchText + "%"),
                            criteriaBuilder.like(root.get("description"), "%" + searchText + "%")
                    ),
                    criteriaBuilder.equal(userJoin.get("id"), userId)
            );

            List<Note> searchedNotes = getEntityManager().createQuery(query).getResultList();
            if (searchedNotes.isEmpty()) {
                System.out.println("No Matching Notes!");
            }
            commit();
            return searchedNotes;
        } catch (IllegalArgumentException exception) {
            System.out.println("IllegalArgumentException: " + exception.getMessage());
            return null;
        } catch (QueryTimeoutException exception) {
            System.out.println("QueryTimeoutException : " + exception.getMessage());
            return null;
        } catch (TransactionRequiredException e) {
            System.out.println("TransactionRequiredException: " + e.getMessage());
            return null;
        } catch (PessimisticLockException exception) {
            System.out.println("PessimisticLockException: " + exception.getMessage());
            return null;
        } catch (PersistenceException exception) {
            System.out.println("PersistenceException: " + exception.getMessage());
            return null;
        }

    }

}
