package com.example.demo.repository;

import com.example.demo.model.Information;
import com.example.demo.model.Panel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PanelRepository extends JpaRepository<Panel, Long> {
    Page<Panel> findAllByNameContains(String name, Pageable pageable);
}
