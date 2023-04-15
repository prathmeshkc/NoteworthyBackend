package com.pcandroiddev.noteworthybackend.controller;

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
            @RequestParam(name = "title") String title,
            @RequestParam(name = "description") String description,
            @RequestParam(name = "priority", required = false) String priority,
            @RequestParam(name = "img_urls", required = false) List<MultipartFile> multipartFileList,
            HttpServletRequest mutableHttpServletRequest
    ) {

        return noteService.createNote(
                title,
                description,
                priority,
                multipartFileList,
                mutableHttpServletRequest
        );
    }

    @PutMapping("/{noteId}")
    public ResponseEntity<?> updateNote(
            @PathVariable String noteId,
            @RequestParam(name = "title") String title,
            @RequestParam(name = "description") String description,
            @RequestParam(name = "priority", required = false) String priority,
            @RequestParam(name = "img_urls", required = false) List<MultipartFile> multipartFileList,
            HttpServletRequest mutableHttpServletRequest
    ) {

        return noteService.updateNote(
                noteId,
                title,
                description,
                priority,
                multipartFileList,
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


}
