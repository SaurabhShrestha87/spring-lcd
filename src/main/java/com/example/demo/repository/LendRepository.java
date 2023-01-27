package com.example.demo.repository;

import com.example.demo.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LendRepository extends JpaRepository<Lend, Long> {
    Optional<Lend> findByProfileAndStatus(Profile profile, LendStatus status);

    Page<Lend> findAllByPanelContains(Panel panel, Pageable pageable);
}
