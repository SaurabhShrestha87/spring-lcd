package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "lend")
@NoArgsConstructor
public class Lend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private LendStatus status;

    private Timestamp startOn;
    private Timestamp dueOn;

    @ManyToOne
    @JoinColumn(name = "information_id")
    @JsonManagedReference
    private Information information;

    @ManyToOne
    @JoinColumn(name = "profile_id")
    @JsonManagedReference
    private Profile profile;

    @ManyToOne
    @JoinColumn(name = "panel_id")
    @JsonManagedReference
    private Panel panel;

    public Instant getStartOn() {
        return startOn.toInstant();
    }

    public void setStartOn(Instant startOn) {
        this.startOn = Timestamp.from(startOn);
    }

    public Instant getDueOn() {
        return dueOn.toInstant();
    }

    public void setDueOn(Instant dueOn) {
        this.dueOn = Timestamp.from(dueOn);
    }
}
