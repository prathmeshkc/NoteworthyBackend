package com.pcandroiddev.noteworthybackend.model.note;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class ImgUrl {
    @Column(name = "public_id")
    private String public_id;
    @Column(name = "public_url")
    private String public_url;
}
