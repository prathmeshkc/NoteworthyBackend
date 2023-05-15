package com.pcandroiddev.noteworthybackend.service.note;

import com.pcandroiddev.noteworthybackend.model.exception.MessageBody;
import com.pcandroiddev.noteworthybackend.model.note.ImgUrl;
import com.pcandroiddev.noteworthybackend.model.note.Note;
import com.pcandroiddev.noteworthybackend.model.note.Priority;
import com.pcandroiddev.noteworthybackend.model.response.DeleteImageResponse;
import com.pcandroiddev.noteworthybackend.model.response.DeletedNoteResponse;
import com.pcandroiddev.noteworthybackend.model.response.ImageResponse;
import com.pcandroiddev.noteworthybackend.model.response.NoteResponse;
import com.pcandroiddev.noteworthybackend.model.user.User;
import com.pcandroiddev.noteworthybackend.repository.NoteRepository;
import com.pcandroiddev.noteworthybackend.repository.UserRepository;
import com.pcandroiddev.noteworthybackend.util.CloudinaryUtil;
import com.pcandroiddev.noteworthybackend.util.Helper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.swing.text.html.Option;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.pcandroiddev.noteworthybackend.util.Helper.noteToEmailBody;

@Service
@RequiredArgsConstructor
public class NoteService {


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NoteRepository noteRepository;


    @Autowired
    CloudinaryUtil cloudinaryUtil;

    @Autowired
    private EmailSenderService emailSenderService;

    private User getById(Integer userId) {
        Optional<User> foundUser = userRepository.findById(userId);
        return foundUser.orElse(null);

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

        if (user == null) {
            return ResponseEntity.badRequest().body(new MessageBody("User Not Found!"));
        }

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

        try {
            Note savedNote = noteRepository.save(note);
            return ResponseEntity.ok(NoteResponse.builder()
                    .noteId(savedNote.getId())
                    .userId(userId)
                    .title(savedNote.getTitle())
                    .description(savedNote.getDescription())
                    .priority(priority.name())
                    .img_urls(savedNote.getImg_urls())
                    .build()
            );

        } catch (IllegalArgumentException | OptimisticLockingFailureException e) {
            return ResponseEntity.internalServerError().body(new MessageBody("Something Went Wrong!"));
        }
    }

    public ResponseEntity<?> updateNote(
            String id,
            String title,
            String description,
            String passedPriority,
            List<ImgUrl> imgUrls
    ) {
        Integer noteId = Integer.parseInt(id);

        Priority priority = Helper.fromString(passedPriority);


        try {

            Optional<Note> noteInDBOptional = noteRepository.findById(noteId);
            if (noteInDBOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageBody("Note Not Found To Update!"));
            }

            Note newNote = noteInDBOptional.get();
            newNote.setTitle(title);
            newNote.setDescription(description);
            newNote.setPriority(priority);
            newNote.setImg_urls(imgUrls);

            Note updatedNote = noteRepository.save(newNote);

            return ResponseEntity.ok(NoteResponse.builder()
                    .noteId(updatedNote.getId())
                    .userId(updatedNote.getUser().getId())
                    .title(updatedNote.getTitle())
                    .description(updatedNote.getDescription())
                    .priority(priority.name())
                    .img_urls(updatedNote.getImg_urls())
                    .build()
            );

        } catch (Exception e) {
            System.out.println("Exception -" + e.getMessage());
            return ResponseEntity.internalServerError().body(new MessageBody("Something Went Wrong!"));
        }
    }

    public ResponseEntity<?> deleteNote(String id) {
        Integer noteId = Integer.parseInt(id);

        try {
            Optional<Note> deletedNoteOptional = noteRepository.findById(noteId);

            if (deletedNoteOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageBody("Note Not Found!"));
            }

            Note deletedNote = deletedNoteOptional.get();
            noteRepository.deleteById(noteId);


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

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new MessageBody("Something Went Wrong!"));
        }


    }

    public ResponseEntity<?> getAllNotes(Integer userId) {

        try {

            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageBody("User Not Found!"));
            }
            if (userOptional.get().getRole().name().equals("ADMIN")) {

                List<Note> noteList = noteRepository.findAll();

                List<NoteResponse> noteResponses = noteList.stream().map(note -> new NoteResponse(
                        note.getId(),
                        note.getUser().getId(),
                        note.getTitle(),
                        note.getDescription(),
                        note.getPriority().name(),
                        note.getImg_urls()
                )).toList();

                return ResponseEntity.ok(noteResponses);

            }

            List<Note> notes = noteRepository.findAllByUserId(userId);
            List<NoteResponse> noteResponses = notes.stream().map(note -> new NoteResponse(
                    note.getId(),
                    note.getUser().getId(),
                    note.getTitle(),
                    note.getDescription(),
                    note.getPriority().name(),
                    note.getImg_urls()
            )).toList();

            return ResponseEntity.ok(noteResponses);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new MessageBody("Something Went Wrong!"));
        }
    }

    public ResponseEntity<?> sortNotesByPriority(Integer userId, String sortBy) {

        try {
            List<Note> notes;

            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageBody("User Not Found!"));
            }

            if (userOptional.get().getRole().name().equals("ADMIN")) {

                if (sortBy.equalsIgnoreCase("low")) {
                    notes = noteRepository.findAllOrderByPriorityAsc();
                } else if (sortBy.equalsIgnoreCase("high")) {
                    notes = noteRepository.findAllOrderByPriorityDesc();
                } else {
                    return getAllNotes(userId);
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

            if (sortBy.equalsIgnoreCase("low")) {
                notes = noteRepository.findAllByUserIdOrderByPriorityAsc(userId);
            } else if (sortBy.equalsIgnoreCase("high")) {
                notes = noteRepository.findAllByUserIdOrderByPriorityDesc(userId);
            } else {
                return getAllNotes(userId);
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

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new MessageBody("Something Went Wrong!"));
        }
    }

    public ResponseEntity<?> searchNotes(String searchText, Integer userId) {

        try {
            List<Note> searchedNotes;
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageBody("User Not Found!"));
            }

            if (userOptional.get().getRole().name().equals("ADMIN")) {
                searchedNotes = noteRepository.searchByTitleOrDescription(searchText);
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

            searchedNotes = noteRepository.searchByTitleOrDescriptionAndUserId(searchText, userId);

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
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new MessageBody("Something Went Wrong!"));
        }

    }

    public ResponseEntity<?> shareNoteByEmail(String id) {
        try {
            Integer noteId = Integer.parseInt(id);
            Optional<Note> noteOptional = noteRepository.findById(noteId);
            if (noteOptional.isEmpty()) {
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageBody("Note Not Found!"));
            }

            Note note = noteOptional.get();
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
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new MessageBody("Something Went Wrong!"));
        }
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
