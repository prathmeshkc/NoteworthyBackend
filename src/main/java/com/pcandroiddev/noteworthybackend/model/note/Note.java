package com.pcandroiddev.noteworthybackend.model.note;

import com.pcandroiddev.noteworthybackend.model.user.User;
import jakarta.persistence.*;
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
@Table(name = "notes")
@Entity
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "title", nullable = false, length = 1024)
    private String title;

    @Column(name = "description", nullable = false, length = 1024)
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "note_img_urls")
    private List<ImgUrl> img_urls = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "user_Id", referencedColumnName = "id", nullable = false)
    private User user;
}
