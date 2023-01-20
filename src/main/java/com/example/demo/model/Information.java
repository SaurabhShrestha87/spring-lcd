package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "information")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Information {

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
    @JsonManagedReference
    private Profile profile;

    @JsonBackReference
    @OneToMany(mappedBy = "information", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Lend> lends;

    @Override
    public String toString() {
        return "Information{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", url='" + url + '\'' +
                ", profile=" + profile +
//                ", lends=" + getLendsValue() +
                '}';
    }

    private String getLendsValue() {
        String lendString = "";
        if (lends != null) {
            for (Lend lend : lends) {
                lendString = lendString + "\n" + lend.toString();
                System.out.println(lend);
            }
        }
        return lendString;
    }
}
