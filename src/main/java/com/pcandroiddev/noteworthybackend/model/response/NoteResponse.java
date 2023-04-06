package com.pcandroiddev.noteworthybackend.model.response;


import com.pcandroiddev.noteworthybackend.model.note.ImgUrl;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NoteResponse {

    private Integer noteId;
    private Integer userId;
    private String title;
    private String description;
    private List<ImgUrl> img_urls = new ArrayList<>();

}
