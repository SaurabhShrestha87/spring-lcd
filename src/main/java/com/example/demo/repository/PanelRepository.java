package com.example.demo.repository;

import com.example.demo.model.Panel;
import com.example.demo.model.PanelStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PanelRepository extends JpaRepository<Panel, Long> {
    Page<Panel> findAllByNameContains(String name, Pageable pageable);
    Optional<Panel> findByStatus(PanelStatus status);
    Panel findByName(String name);
    List<Panel> findAllByStatus(PanelStatus status);

}
