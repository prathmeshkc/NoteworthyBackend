package com.pcandroiddev.noteworthybackend.service.note;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.pcandroiddev.noteworthybackend.dao.note.NoteDao;
import com.pcandroiddev.noteworthybackend.dao.user.UserDao;
import com.pcandroiddev.noteworthybackend.model.exception.ExceptionBody;
import com.pcandroiddev.noteworthybackend.model.note.ImgUrl;
import com.pcandroiddev.noteworthybackend.model.note.Note;
import com.pcandroiddev.noteworthybackend.model.note.Priority;
import com.pcandroiddev.noteworthybackend.model.response.DeletedNoteResponse;
import com.pcandroiddev.noteworthybackend.model.response.NoteResponse;
import com.pcandroiddev.noteworthybackend.model.user.User;
import com.pcandroiddev.noteworthybackend.util.Helper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NoteService {

    @Autowired
    private NoteDao noteDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private Cloudinary cloudinary;

    private User getById(Integer userId) {
        return userDao.getUser(userId);
    }

    public ResponseEntity<?> createNote(
            String title,
            String description,
            String passedPriority,
            List<MultipartFile> multipartFileList,
            HttpServletRequest mutableHttpServletRequest
    ) {

        Integer userId = Integer.parseInt(mutableHttpServletRequest.getHeader("userId"));
        User user = getById(userId);
        List<ImgUrl> imgUrls = new ArrayList<>();

        if (multipartFileList != null && multipartFileList.size() > 0) {
            for (MultipartFile file : multipartFileList) {
                try {
                    ImgUrl imgUrl = uploadFile(file);
                    imgUrls.add(imgUrl);
                } catch (IOException e) {
                    return ResponseEntity.internalServerError().body(new ExceptionBody("Error Uploading Image!"));
                }
            }
        }
        Priority priority = Helper.fromString(passedPriority);
        Note note = Note.builder()
                .title(title)
                .description(description)
                .priority(priority)
                .img_urls(imgUrls)
                .user(user)
                .build();

        Note savedNote = noteDao.saveNote(note);

        if (savedNote == null) {
            return ResponseEntity.internalServerError().body(new ExceptionBody("Something Went Wrong!"));
        }

        return ResponseEntity.ok(NoteResponse.builder()
                .noteId(savedNote.getId())
                .userId(userId)
                .title(savedNote.getTitle())
                .description(savedNote.getDescription())
                .priority(priority.name())
                .img_urls(savedNote.getImg_urls())
                .build()
        );


    }

    public ResponseEntity<?> updateNote(
            String id,
            String title,
            String description,
            String passedPriority,
            List<MultipartFile> multipartFileList,
            HttpServletRequest mutableHttpServletRequest
    ) {
        Integer noteId = Integer.parseInt(id);
        Integer userId = Integer.parseInt(mutableHttpServletRequest.getHeader("userId"));

        /*
          Add New Images
         */
        List<ImgUrl> imgUrls = new ArrayList<>();
        if (multipartFileList != null && multipartFileList.size() > 0) {
            for (MultipartFile file : multipartFileList) {
                try {
                    ImgUrl imgUrl = uploadFile(file);
                    imgUrls.add(imgUrl);
                } catch (IOException e) {
                    return ResponseEntity.internalServerError().body(new ExceptionBody("Error Uploading Image!"));
                }
            }

        }


        /*
            Clean-up Old Resources
        */

        Note oldNote = noteDao.getNoteById(noteId);

        if (oldNote == null) {
            return ResponseEntity.internalServerError().body(new ExceptionBody("Something Went Wrong!"));
        }

        if (oldNote.getImg_urls() != null && oldNote.getImg_urls().size() > 0) {
            for (ImgUrl imgUrl : oldNote.getImg_urls()) {
                try {
                    String public_id = imgUrl.getPublic_id();
                    deleteFile(public_id);
                } catch (IOException e) {
                    return ResponseEntity.internalServerError().body(new ExceptionBody("Error Destroying Image!"));
                }
            }
        }

        Priority priority = Helper.fromString(passedPriority);

        Note newNote = Note.builder()
                .title(title)
                .description(description)
                .priority(priority)
                .img_urls(imgUrls)
                .build();

        Note updatedNote = noteDao.updateNoteById(noteId, newNote);

        if (updatedNote == null) {
            return ResponseEntity.internalServerError().body(new ExceptionBody("Something Went Wrong!"));
        }

        return ResponseEntity.ok(NoteResponse.builder()
                .noteId(updatedNote.getId())
                .userId(userId)
                .title(updatedNote.getTitle())
                .description(updatedNote.getDescription())
                .priority(priority.name())
                .img_urls(updatedNote.getImg_urls())
                .build()
        );

    }

    public ResponseEntity<?> deleteNote(String id) {
        Integer noteId = Integer.parseInt(id);
        Note deletedNote = noteDao.deleteNoteById(noteId);

        if (deletedNote == null) {
            return ResponseEntity.internalServerError().body(new ExceptionBody("Something Went Wrong!"));
        }

         /*
            Clean-up Old Resources
        */

        if (deletedNote.getImg_urls() != null && deletedNote.getImg_urls().size() > 0) {
            for (ImgUrl imgUrl : deletedNote.getImg_urls()) {
                try {
                    String public_id = imgUrl.getPublic_id();
                    deleteFile(public_id);
                } catch (IOException e) {
                    return ResponseEntity.internalServerError().body(new ExceptionBody("Error Destroying Image!"));
                }
            }
        }

        NoteResponse deletedNoteResponse = NoteResponse.builder()
                .noteId(deletedNote.getId())
                .userId(deletedNote.getUser().getId())
                .title(deletedNote.getTitle())
                .description(deletedNote.getDescription())
                .priority(deletedNote.getPriority().name())
                .img_urls(deletedNote.getImg_urls())
                .build();

        return ResponseEntity.accepted().body(DeletedNoteResponse.builder()
                .message("Note Deleted Successfully!")
                .deleted_Note(deletedNoteResponse)
                .build()
        );

    }

    public ResponseEntity<?> getAllNotes(Integer userId) {
        List<Note> notes = noteDao.getAllNotes(userId);

        if (notes == null) {
            return ResponseEntity.internalServerError().body(new ExceptionBody("Something Went Wrong!"));
        }

        List<NoteResponse> noteResponses = notes.stream().map(note -> new NoteResponse(
                note.getId(),
                note.getUser().getId(),
                note.getTitle(),
                note.getDescription(),
                note.getPriority().name(),
                note.getImg_urls()
        )).toList();


        return ResponseEntity.ok(noteResponses);
    }

    public ResponseEntity<?> sortNotesByPriority(Integer userId, String sortBy) {
        List<Note> notesLowToHigh = null;

        if (sortBy.equalsIgnoreCase("low")) {
            notesLowToHigh = noteDao.sortNotesByLowPriority(userId);
        } else if (sortBy.equalsIgnoreCase("high")) {
            notesLowToHigh = noteDao.sortNotesByHighPriority(userId);
        } else {
            return getAllNotes(userId);
        }

        if (notesLowToHigh == null) {
            return ResponseEntity.internalServerError().body(new ExceptionBody("Something Went Wrong!"));
        }

        List<NoteResponse> noteResponses = notesLowToHigh.stream().map(note -> new NoteResponse(
                note.getId(),
                note.getUser().getId(),
                note.getTitle(),
                note.getDescription(),
                note.getPriority().name(),
                note.getImg_urls()
        )).toList();


        return ResponseEntity.ok(noteResponses);
    }


    public ResponseEntity<?> searchNotes(String searchText, Integer userId) {
        List<Note> searchedNotes = noteDao.searchNote(searchText, userId);

        if (searchedNotes == null) {
            return ResponseEntity.internalServerError().body(new ExceptionBody("Something Went Wrong!"));
        }

        List<NoteResponse> noteResponses = searchedNotes.stream().map(searchedNote ->
                new NoteResponse(
                        searchedNote.getId(),
                        searchedNote.getUser().getId(),
                        searchedNote.getTitle(),
                        searchedNote.getDescription(),
                        searchedNote.getPriority().name(),
                        searchedNote.getImg_urls()
                )
        ).toList();

        return ResponseEntity.ok(noteResponses);

    }


    private ImgUrl uploadFile(MultipartFile multipartFile) throws IOException {
        Map uploadResult = cloudinary.uploader()
                .upload(
                        multipartFile.getBytes(),
                        Map.of("public_id", UUID.randomUUID().toString())
                );
        String public_id = (String) uploadResult.get("public_id");
        String public_url = (String) uploadResult.get("url");

        return new ImgUrl(public_id, public_url);

    }

    private void deleteFile(String public_id) throws IOException {
        cloudinary.uploader().destroy(public_id, ObjectUtils.emptyMap());
    }


}
