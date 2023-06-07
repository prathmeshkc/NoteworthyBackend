package com.pcandroiddev.noteworthybackend.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pcandroiddev.noteworthybackend.model.response.NoteResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.List;

@RequiredArgsConstructor
@Component
public class JsonConverter {


    @Autowired
    private Gson gson;

    public String convertNoteResponseListToJson(List<NoteResponse> noteResponses) {
        return gson.toJson(noteResponses);
    }

    public List<NoteResponse> convertJsonToNoteResponseList(String jsonString) {
        Type noteResponseType = new TypeToken<List<NoteResponse>>() {
        }.getType();
        return gson.fromJson(jsonString, noteResponseType);
    }
}
