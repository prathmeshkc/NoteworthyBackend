package com.pcandroiddev.noteworthybackend.util;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.pcandroiddev.noteworthybackend.model.note.ImgUrl;
import com.pcandroiddev.noteworthybackend.model.response.ImageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;


@RequiredArgsConstructor
@Component
public class CloudinaryUtil {


    @Autowired
    private Cloudinary cloudinary;

    public ImageResponse uploadFile(MultipartFile multipartFile) throws IOException {
        Map uploadResult = cloudinary.uploader()
                .upload(
                        multipartFile.getBytes(),
                        Map.of("public_id", UUID.randomUUID().toString())
                );
        String public_id = (String) uploadResult.get("public_id");
        String public_url = (String) uploadResult.get("secure_url");

        return new ImageResponse(public_id, public_url);

    }

    public void deleteFile(String public_id) throws IOException {
        cloudinary.uploader().destroy(public_id, ObjectUtils.emptyMap());
    }


}
