package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;

@Getter
@Setter
@Entity
@Table(name = "profile")
@NoArgsConstructor
public class Profile extends AuditModel {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    private Timestamp date;

    @OneToMany(mappedBy = "profile", fetch = FetchType.EAGER)
    private List<Information> information;

    @JsonBackReference
    @OneToMany(mappedBy = "profile", fetch = FetchType.LAZY)
    private List<Lend> lends;

    public Instant getDate() {
        return date.toInstant();
    }

    public void setDate(Instant date) {
        this.date = Timestamp.from(date);
    }

    public String getDateAsString() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy hh:mm:ss a", Locale.ENGLISH);
        return formatter.format(date);
    }
}