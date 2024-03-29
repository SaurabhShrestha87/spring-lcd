package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.springframework.lang.Nullable;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "information")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Information extends AuditModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private InfoType type;

    @Column(name = "file_url")
    private String url;

    @ManyToOne
    @JoinColumn(name = "profile_id")
    @JsonBackReference
    private Profile profile;

    @Column(name = "duration")
    @Nullable
    private String duration;

    @Column(name = "count")
    @Nullable
    private String count;
}
