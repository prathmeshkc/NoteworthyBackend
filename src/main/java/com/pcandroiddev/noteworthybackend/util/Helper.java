package com.pcandroiddev.noteworthybackend.util;

import com.pcandroiddev.noteworthybackend.model.note.Note;
import com.pcandroiddev.noteworthybackend.model.note.Priority;

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

}
