package com.pcandroiddev.noteworthybackend.model.request;

import com.pcandroiddev.noteworthybackend.model.note.ImgUrl;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NoteRequest {

    private String title;
    private String description;
    private String priority;
    private List<ImgUrl> images;
}
