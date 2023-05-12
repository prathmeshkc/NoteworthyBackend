package com.pcandroiddev.noteworthybackend.controller;

import com.pcandroiddev.noteworthybackend.model.request.NoteRequest;
import com.pcandroiddev.noteworthybackend.service.note.NoteService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/notes")
@RequiredArgsConstructor
public class NoteController {

    @Autowired
    private final NoteService noteService;


    @PostMapping("/")
    public ResponseEntity<?> createNote(
            @RequestBody NoteRequest noteRequest,
            HttpServletRequest mutableHttpServletRequest
    ) {

        return noteService.createNote(
                noteRequest.getTitle(),
                noteRequest.getDescription(),
                noteRequest.getPriority(),
                noteRequest.getImages(),
                mutableHttpServletRequest
        );
    }

    @PutMapping("/{noteId}")
    public ResponseEntity<?> updateNote(
            @PathVariable String noteId,
            @RequestBody NoteRequest noteRequest,
            HttpServletRequest mutableHttpServletRequest
    ) {

        return noteService.updateNote(
                noteId,
                noteRequest.getTitle(),
                noteRequest.getDescription(),
                noteRequest.getPriority(),
                noteRequest.getImages(),
                mutableHttpServletRequest
        );
    }

    @DeleteMapping("/{noteId}")
    public ResponseEntity<?> deleteNote(
            @PathVariable String noteId
    ) {
        return noteService.deleteNote(noteId);
    }


    @GetMapping("/")
    public ResponseEntity<?> getAllNotes(HttpServletRequest httpServletRequest) {
        Integer userId = Integer.parseInt(httpServletRequest.getHeader("userId"));
        return noteService.getAllNotes(userId);
    }

    @GetMapping("/{searchText}")
    public ResponseEntity<?> searchNotes(
            @PathVariable String searchText,
            HttpServletRequest httpServletRequest
    ) {
        Integer userId = Integer.parseInt(httpServletRequest.getHeader("userId"));
        return noteService.searchNotes(searchText, userId);
    }

    @GetMapping("/sortBy")
    public ResponseEntity<?> sortNotesByPriority(
            @RequestParam(name = "sortBy") String sortBy,
            HttpServletRequest httpServletRequest
    ) {
        Integer userId = Integer.parseInt(httpServletRequest.getHeader("userId"));

        return noteService.sortNotesByPriority(userId, sortBy);

    }

    @PostMapping("/share/{noteId}")
    public ResponseEntity<?> shareNoteByEmail(
            @PathVariable String noteId
    ) {

        return noteService.shareNoteByEmail(noteId);
    }


    @PostMapping("/image/upload-image")
    public ResponseEntity<?> uploadImage(
            @RequestParam(name = "img_urls", required = false) List<MultipartFile> multipartFileList
    ) {
        return noteService.uploadImage(multipartFileList);
    }

    @DeleteMapping("/image/delete-image/{public_id}")
    public ResponseEntity<?> deleteImage(
            @PathVariable String public_id
    ) {
        return noteService.deleteImage(public_id);
    }

}
