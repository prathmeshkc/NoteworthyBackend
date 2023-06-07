package com.pcandroiddev.noteworthybackend.controller;

import com.pcandroiddev.noteworthybackend.model.exception.MessageBody;
import com.pcandroiddev.noteworthybackend.model.request.NoteRequest;
import com.pcandroiddev.noteworthybackend.model.response.DeleteImageResponse;
import com.pcandroiddev.noteworthybackend.model.response.DeletedNoteResponse;
import com.pcandroiddev.noteworthybackend.model.response.ImageResponse;
import com.pcandroiddev.noteworthybackend.model.response.NoteResponse;
import com.pcandroiddev.noteworthybackend.service.note.NoteService;
import com.pcandroiddev.noteworthybackend.util.BadRequestException;
import com.pcandroiddev.noteworthybackend.util.InternalServerErrorException;
import com.pcandroiddev.noteworthybackend.util.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

        try {
            NoteResponse noteResponse = noteService.createNote(
                    noteRequest.getTitle(),
                    noteRequest.getDescription(),
                    noteRequest.getPriority(),
                    noteRequest.getImages(),
                    mutableHttpServletRequest
            );
            return ResponseEntity.ok(noteResponse);
        } catch (BadRequestException e) {
            return ResponseEntity.badRequest().body(new MessageBody(e.getMessage()));
        } catch (InternalServerErrorException e) {
            return ResponseEntity.internalServerError().body(new MessageBody(e.getMessage()));
        }
    }

    @PutMapping("/{noteId}")
    public ResponseEntity<?> updateNote(
            @PathVariable String noteId,
            @RequestBody NoteRequest noteRequest,
            HttpServletRequest mutableHttpServletRequest
    ) {

        try {
            NoteResponse noteResponse = noteService.updateNote(
                    noteId,
                    noteRequest.getTitle(),
                    noteRequest.getDescription(),
                    noteRequest.getPriority(),
                    noteRequest.getImages(),
                    mutableHttpServletRequest
            );
            return ResponseEntity.ok(noteResponse);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageBody(e.getMessage()));
        } catch (InternalServerErrorException e) {
            return ResponseEntity.internalServerError().body(new MessageBody(e.getMessage()));
        }
    }

    @DeleteMapping("/{noteId}")
    public ResponseEntity<?> deleteNote(
            @PathVariable String noteId,
            HttpServletRequest mutableHttpServletRequest
    ) {
        try {
            DeletedNoteResponse deletedNoteResponse = noteService.deleteNote(noteId, mutableHttpServletRequest);
            return ResponseEntity.accepted().body(deletedNoteResponse);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageBody(e.getMessage()));
        } catch (InternalServerErrorException e) {
            return ResponseEntity.internalServerError().body(new MessageBody(e.getMessage()));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageBody(e.getMessage()));
        }
    }


    @GetMapping("/")
    public ResponseEntity<?> getAllNotes(HttpServletRequest httpServletRequest) {
        try {
            Integer userId = Integer.parseInt(httpServletRequest.getHeader("userId"));
            List<NoteResponse> noteResponses = noteService.getAllNotes(userId);
            return ResponseEntity.ok(noteResponses);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageBody(e.getMessage()));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageBody("Something Went Wrong!"));
        }
    }

    @GetMapping("/sortBy")
    public ResponseEntity<?> sortNotesByPriority(
            @RequestParam(name = "sortBy") String sortBy,
            HttpServletRequest httpServletRequest
    ) {
        try {
            Integer userId = Integer.parseInt(httpServletRequest.getHeader("userId"));
            List<NoteResponse> noteResponses = noteService.sortNotesByPriority(userId, sortBy);
            return ResponseEntity.ok(noteResponses);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageBody(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageBody("Something Went Wrong!"));
        }
    }

    @GetMapping("/{searchText}")
    public ResponseEntity<?> searchNotes(
            @PathVariable String searchText,
            HttpServletRequest httpServletRequest
    ) {
        try {
            Integer userId = Integer.parseInt(httpServletRequest.getHeader("userId"));
            List<NoteResponse> noteResponses = noteService.searchNotes(searchText, userId);
            return ResponseEntity.ok(noteResponses);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageBody(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new MessageBody("Something Went Wrong!"));
        }
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
        try {
            List<ImageResponse> imageResponses = noteService.uploadImage(multipartFileList);
            return ResponseEntity.ok(imageResponses);
        } catch (InternalServerErrorException e) {
            return ResponseEntity.internalServerError().body(new MessageBody(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new MessageBody("Something Went Wrong!"));
        }
    }

    @DeleteMapping("/image/delete-image/{public_id}")
    public ResponseEntity<?> deleteImage(
            @PathVariable String public_id
    ) {
        try {
            DeleteImageResponse deleteImageResponse = noteService.deleteImage(public_id);
            return ResponseEntity.accepted().body(deleteImageResponse);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(new MessageBody("Error Destroying Image!"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new MessageBody("Something Went Wrong!"));
        }
    }

}
