package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
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
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class Panel extends AuditModel {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String name;
    private String resolution;
    @Enumerated(EnumType.STRING)
    private PanelStatus status;

    @JsonManagedReference
    @OneToMany(mappedBy = "panel")
    private List<Lend> lends;

    public Panel(Long id, String name, String resolution, PanelStatus status, List<Lend> lends) {
        this.id = id;
        this.name = name;
        this.resolution = resolution;
        this.status = status;
        this.lends = lends;
    }

    public String getDevice(){
        return "/dev/"+name;
    }
}
