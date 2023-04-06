package com.pcandroiddev.noteworthybackend.dao.note;

import com.pcandroiddev.noteworthybackend.dao.Dao;
import com.pcandroiddev.noteworthybackend.model.note.Note;
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


    public Note updateNoteById(Integer noteId, Note newNote){
        try {
            begin();
            Note note = getEntityManager().find(Note.class, noteId);

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

}
