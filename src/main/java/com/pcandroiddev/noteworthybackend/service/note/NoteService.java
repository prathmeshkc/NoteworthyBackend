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
import com.pcandroiddev.noteworthybackend.util.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import redis.clients.jedis.Jedis;

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
    private Jedis jedis;

    @Autowired
    private JsonConverter jsonConverter;

    @Autowired
    private CloudinaryUtil cloudinaryUtil;

    @Autowired
    private EmailSenderService emailSenderService;

    private User getById(Integer userId) {
        Optional<User> foundUser = userRepository.findById(userId);
        return foundUser.orElse(null);

    }

    public NoteResponse createNote(
            String title,
            String description,
            String passedPriority,
            List<ImgUrl> imgUrls,
            HttpServletRequest mutableHttpServletRequest
    ) {

        Integer userId = Integer.parseInt(mutableHttpServletRequest.getHeader("userId"));
        String userIdAsKey = String.valueOf(userId);
        User user = getById(userId);

        if (user == null) {
            throw new BadRequestException("User Not Found!");
        }

        if ((title.isEmpty() && description.isEmpty() && imgUrls.isEmpty()) || (title.isBlank() && description.isBlank())) {
            return NoteResponse.builder()
                    .noteId(-1)
                    .userId(userId)
                    .title("")
                    .description("")
                    .priority("")
                    .img_urls(new ArrayList<>())
                    .build();
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
            if (jedis.hexists("notes", userIdAsKey)) {
                jedis.hdel("notes", userIdAsKey);
            }
            Note savedNote = noteRepository.save(note);
            return NoteResponse.builder()
                    .noteId(savedNote.getId())
                    .userId(userId)
                    .title(savedNote.getTitle())
                    .description(savedNote.getDescription())
                    .priority(priority.name())
                    .img_urls(savedNote.getImg_urls())
                    .build();
        } catch (IllegalArgumentException | OptimisticLockingFailureException e) {
            throw new InternalServerErrorException("Something Went Wrong!");
        }
    }

    public NoteResponse updateNote(
            String id,
            String title,
            String description,
            String passedPriority,
            List<ImgUrl> imgUrls,
            HttpServletRequest mutableHttpServletRequest
    ) {
        Integer noteId = Integer.parseInt(id);
        Integer userId = Integer.parseInt(mutableHttpServletRequest.getHeader("userId"));
        String userIdAsKey = String.valueOf(userId);

        Priority priority = Helper.fromString(passedPriority);

        Optional<Note> noteInDBOptional = noteRepository.findById(noteId);
        if (noteInDBOptional.isEmpty()) {
            throw new NotFoundException("No Such Note Exists!");
        }

        Note newNote = noteInDBOptional.get();
        newNote.setTitle(title);
        newNote.setDescription(description);
        newNote.setPriority(priority);
        newNote.setImg_urls(imgUrls);

        try {
            if (jedis.hexists("notes", userIdAsKey)) {
                jedis.hdel("notes", userIdAsKey);
            }
            Note updatedNote = noteRepository.save(newNote);
            return NoteResponse.builder()
                    .noteId(updatedNote.getId())
                    .userId(updatedNote.getUser().getId())
                    .title(updatedNote.getTitle())
                    .description(updatedNote.getDescription())
                    .priority(priority.name())
                    .img_urls(updatedNote.getImg_urls())
                    .build();
        } catch (Exception e) {
            throw new InternalServerErrorException("Something Went Wrong!");
        }
    }

    public DeletedNoteResponse deleteNote(String id, HttpServletRequest mutableHttpServletRequest) throws Exception {
        Integer noteId = Integer.parseInt(id);
        Integer userId = Integer.parseInt(mutableHttpServletRequest.getHeader("userId"));
        String userIdAsKey = String.valueOf(userId);

        Optional<Note> deletedNoteOptional = noteRepository.findById(noteId);
        if (deletedNoteOptional.isEmpty()) {
            throw new NotFoundException("Note Not Found!");
        }

        Note deletedNote = deletedNoteOptional.get();
        noteRepository.deleteById(noteId);

        /*
        Clean-up the Redis cache and Old Resources
        */

        if (jedis.hexists("notes", userIdAsKey)) {
            jedis.hdel("notes", userIdAsKey);
        }

        if (deletedNote.getImg_urls() != null && deletedNote.getImg_urls().size() > 0) {
            for (ImgUrl imgUrl : deletedNote.getImg_urls()) {
                try {
                    String public_id = imgUrl.getPublic_id();
                    cloudinaryUtil.deleteFile(public_id);
                } catch (IOException e) {
                    throw new InternalServerErrorException("Error Destroying Image!");
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

        return DeletedNoteResponse.builder()
                .message("Note Deleted Successfully!")
                .deleted_Note(deletedNoteResponse)
                .build();


    }


    public List<NoteResponse> getAllNotes(Integer userId) throws Exception {
        String userIdAsKey = String.valueOf(userId);
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new NotFoundException("User Not Found!");
        }
        if (userOptional.get().getRole().name().equals("ADMIN")) {
            List<Note> noteList = noteRepository.findAll();
            return Helper.getNoteResponse(noteList);
        }

        if (jedis.hexists("notes", userIdAsKey)) {
            return jsonConverter.convertJsonToNoteResponseList(jedis.hget("notes", userIdAsKey));
        }

        List<Note> notes = noteRepository.findAllByUserId(userId);
        List<NoteResponse> noteResponses = Helper.getNoteResponse(notes);
        String jsonNoteResponses = jsonConverter.convertNoteResponseListToJson(noteResponses);
        jedis.hset("notes", userIdAsKey, jsonNoteResponses);
        return noteResponses;
    }

    public List<NoteResponse> sortNotesByPriority(Integer userId, String sortBy) throws Exception {
        List<Note> notes;
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            throw new NotFoundException("User Not Found!");
        }

        if (userOptional.get().getRole().name().equals("ADMIN")) {
            if (sortBy.equalsIgnoreCase("low")) {
                notes = noteRepository.findAllOrderByPriorityAsc();
            } else if (sortBy.equalsIgnoreCase("high")) {
                notes = noteRepository.findAllOrderByPriorityDesc();
            } else {
                return getAllNotes(userId);
            }
        } else {
            if (sortBy.equalsIgnoreCase("low")) {
                notes = noteRepository.findAllByUserIdOrderByPriorityAsc(userId);
            } else if (sortBy.equalsIgnoreCase("high")) {
                notes = noteRepository.findAllByUserIdOrderByPriorityDesc(userId);
            } else {
                return getAllNotes(userId);
            }
        }

        return Helper.getNoteResponse(notes);
    }

    public List<NoteResponse> searchNotes(String searchText, Integer userId) throws Exception {

        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new NotFoundException("User Not Found!");
        }

        List<Note> searchedNotes;
        if (userOptional.get().getRole().name().equals("ADMIN")) {
            searchedNotes = noteRepository.searchByTitleOrDescription(searchText);
        } else {
            searchedNotes = noteRepository.searchByTitleOrDescriptionAndUserId(searchText, userId);
        }

        return Helper.getNoteResponse(searchedNotes);
    }


    public List<ImageResponse> uploadImage(
            List<MultipartFile> multipartFileList
    ) {

        List<ImageResponse> imgUrls = new ArrayList<>();

        if (multipartFileList != null && !multipartFileList.isEmpty()) {
            for (MultipartFile file : multipartFileList) {
                try {
                    // TODO: Remember to change the return type of uploadFile() to ImageResponse
                    ImageResponse imgUrl = cloudinaryUtil.uploadFile(file);
                    imgUrls.add(imgUrl);
                } catch (IOException e) {
                    throw new InternalServerErrorException("Error Uploading Image!");
                }
            }
        }

        return imgUrls;

    }


    public DeleteImageResponse deleteImage(
            String public_id
    ) throws IOException {
        cloudinaryUtil.deleteFile(public_id);
        return new DeleteImageResponse("Image Delete Successfully!", public_id);
    }


}
