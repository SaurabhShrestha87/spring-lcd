package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "panel")
@NoArgsConstructor
public class Panel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String resolution;
    @Enumerated(EnumType.STRING)
    private PanelStatus status;

    @JsonBackReference
    @OneToMany(mappedBy = "panel", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Lend> lends;

    public Panel(Long id, String name, String resolution, PanelStatus status, List<Lend> lends) {
        this.id = id;
        this.name = name;
        this.resolution = resolution;
        this.status = status;
        this.lends = lends;
    }
}
