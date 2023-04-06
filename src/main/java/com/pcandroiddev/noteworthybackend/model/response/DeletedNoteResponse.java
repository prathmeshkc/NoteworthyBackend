package com.pcandroiddev.noteworthybackend.model.response;

import com.pcandroiddev.noteworthybackend.model.note.Note;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeletedNoteResponse {

    private String message;
    private NoteResponse deleted_Note;

}
