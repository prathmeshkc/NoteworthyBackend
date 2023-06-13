package com.pcandroiddev.noteworthybackend.util;

import com.pcandroiddev.noteworthybackend.model.note.Note;
import com.pcandroiddev.noteworthybackend.model.note.Priority;
import com.pcandroiddev.noteworthybackend.model.response.NoteResponse;

import java.util.List;

public class Helper {

    public static Priority fromString(String priorityString) {

        if (priorityString == null || priorityString.isEmpty()) {
            return Priority.LOW;
        }

        return switch (priorityString.toLowerCase()) {
            case "low" -> Priority.LOW;
            case "medium" -> Priority.MEDIUM;
            case "high" -> Priority.HIGH;
            default -> throw new IllegalArgumentException("Invalid priority: " + priorityString);
        };
    }


    public static String noteToEmailBody(Note note) {
        return "Title - " +
                note.getTitle() +
                "\n" +
                "Description - " +
                note.getDescription() +
                "\n";

    }

    public static List<NoteResponse> getNoteResponse(List<Note> notes) {
        return notes.stream().map(note -> new NoteResponse(
                note.getId(),
                note.getUser().getId(),
                note.getTitle(),
                note.getDescription(),
                note.getPriority().name(),
                note.getImg_urls()
        )).toList();
    }

}
