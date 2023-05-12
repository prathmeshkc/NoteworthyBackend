package com.pcandroiddev.noteworthybackend.service.note;

import com.pcandroiddev.noteworthybackend.dao.note.NoteDao;
import com.pcandroiddev.noteworthybackend.dao.user.UserDao;
import com.pcandroiddev.noteworthybackend.model.exception.MessageBody;
import com.pcandroiddev.noteworthybackend.model.note.ImgUrl;
import com.pcandroiddev.noteworthybackend.model.note.Note;
import com.pcandroiddev.noteworthybackend.model.note.Priority;
import com.pcandroiddev.noteworthybackend.model.response.DeleteImageResponse;
import com.pcandroiddev.noteworthybackend.model.response.DeletedNoteResponse;
import com.pcandroiddev.noteworthybackend.model.response.ImageResponse;
import com.pcandroiddev.noteworthybackend.model.response.NoteResponse;
import com.pcandroiddev.noteworthybackend.model.user.User;
import com.pcandroiddev.noteworthybackend.util.CloudinaryUtil;
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

import static com.pcandroiddev.noteworthybackend.util.Helper.noteToEmailBody;

@Service
@RequiredArgsConstructor
public class NoteService {

    @Autowired
    private NoteDao noteDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    CloudinaryUtil cloudinaryUtil;

    @Autowired
    private EmailSenderService emailSenderService;

    private User getById(Integer userId) {
        return userDao.getUser(userId);
    }

    public ResponseEntity<?> createNote(
            String title,
            String description,
            String passedPriority,
            List<ImgUrl> imgUrls,
            HttpServletRequest mutableHttpServletRequest
    ) {

        Integer userId = Integer.parseInt(mutableHttpServletRequest.getHeader("userId"));
        User user = getById(userId);

        if (
                (title.isEmpty() && description.isEmpty() && imgUrls.isEmpty())
                        || (title.isBlank() && description.isBlank())

        ) {
            return ResponseEntity.ok(NoteResponse.builder()
                    .noteId(-1)
                    .userId(userId)
                    .title("")
                    .description("")
                    .priority("")
                    .img_urls(new ArrayList<>())
                    .build()
            );

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
            return ResponseEntity.internalServerError().body(new MessageBody("Something Went Wrong!"));
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
            List<ImgUrl> imgUrls,
            HttpServletRequest mutableHttpServletRequest
    ) {
        Integer noteId = Integer.parseInt(id);

        Priority priority = Helper.fromString(passedPriority);

        Note newNote = Note.builder()
                .title(title)
                .description(description)
                .priority(priority)
                .img_urls(imgUrls)
                .build();

        Note updatedNote = noteDao.updateNoteById(noteId, newNote);

        if (updatedNote == null) {
            return ResponseEntity.internalServerError().body(new MessageBody("Something Went Wrong!"));
        }

        return ResponseEntity.ok(NoteResponse.builder()
                .noteId(updatedNote.getId())
                .userId(updatedNote.getUser().getId())
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
            return ResponseEntity.internalServerError().body(new MessageBody("Something Went Wrong!"));
        }

         /*
            Clean-up Old Resources
        */

        if (deletedNote.getImg_urls() != null && deletedNote.getImg_urls().size() > 0) {
            for (ImgUrl imgUrl : deletedNote.getImg_urls()) {
                try {
                    String public_id = imgUrl.getPublic_id();
                    cloudinaryUtil.deleteFile(public_id);
                } catch (IOException e) {
                    return ResponseEntity.internalServerError().body(new MessageBody("Error Destroying Image!"));
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
            return ResponseEntity.internalServerError().body(new MessageBody("Something Went Wrong!"));
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
        List<Note> notesLowToHigh;

        if (sortBy.equalsIgnoreCase("low")) {
            notesLowToHigh = noteDao.sortNotesByLowPriority(userId);
        } else if (sortBy.equalsIgnoreCase("high")) {
            notesLowToHigh = noteDao.sortNotesByHighPriority(userId);
        } else {
            return getAllNotes(userId);
        }

        if (notesLowToHigh == null) {
            return ResponseEntity.internalServerError().body(new MessageBody("Something Went Wrong!"));
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
            return ResponseEntity.internalServerError().body(new MessageBody("Something Went Wrong!"));
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

    public ResponseEntity<?> shareNoteByEmail(String id) {
        Integer noteId = Integer.parseInt(id);
        Note note = noteDao.getNoteById(noteId);
        System.out.println("shareNoteByEmail: " + note);
        String emailBody = noteToEmailBody(note);

        String emailStatus = emailSenderService.sendEmailWithAttachments(
                "prathmesh.kiranchaudhari2001@gmail.com",
                emailBody,
                note.getImg_urls()
        );

        if (emailStatus == null) {
            return ResponseEntity.internalServerError().body(new MessageBody("Something Went Wrong!"));
        }

        return ResponseEntity.ok(new NoteResponse(
                note.getId(),
                note.getUser().getId(),
                note.getTitle(),
                note.getDescription(),
                note.getPriority().name(),
                note.getImg_urls()
        ));
    }

    public ResponseEntity<?> uploadImage(
            List<MultipartFile> multipartFileList
    ) {

        List<ImageResponse> imgUrls = new ArrayList<>();

        if (multipartFileList != null && multipartFileList.size() > 0) {
            for (MultipartFile file : multipartFileList) {
                try {
                    //TODO: Remember to change the return type of uploadFile() to ImageResponse
                    ImageResponse imgUrl = cloudinaryUtil.uploadFile(file);
                    imgUrls.add(imgUrl);
                } catch (IOException e) {
                    return ResponseEntity.internalServerError().body(new MessageBody("Error Uploading Image!"));
                }
            }
        }

        return ResponseEntity.ok(imgUrls);

    }


    public ResponseEntity<?> deleteImage(
            String public_id
    ) {
        try {
            cloudinaryUtil.deleteFile(public_id);
            return ResponseEntity.accepted().body(new DeleteImageResponse(
                    "Image Delete Successfully!",
                    public_id
            ));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(new MessageBody("Error Destroying Image!"));
        }
    }


}
