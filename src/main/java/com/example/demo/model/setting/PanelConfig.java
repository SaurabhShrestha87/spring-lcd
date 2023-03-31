package com.example.demo.model.setting;

import com.example.demo.model.AuditModel;
import com.example.demo.model.Lend;
import com.example.demo.model.PanelStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
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
@Table(name = "panel_config")
@NoArgsConstructor
public class PanelConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private int sn;
    @Enumerated(EnumType.STRING)
    private PanelStatus status = PanelStatus.ACTIVE;
    @ManyToOne
    @JoinColumn(name = "setting_id")
    private Setting setting;
    private int brightness = 31;
    private int bc = 400;
    private int bw = 400;
}
