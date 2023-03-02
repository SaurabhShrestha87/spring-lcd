package com.example.demo.repository;

import com.example.demo.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LendRepository extends JpaRepository<Lend, Long> {
    Optional<Lend> findByProfileAndStatus(Profile profile, LendStatus status);
    Page<Lend> findAllByPanelContains(Panel panel, Pageable pageable);
    List<Lend> findAllByPanelIdAndStatus(Long id, LendStatus status);
    List<Lend> findAllByTypeAndStatus(DisplayType displayType, LendStatus status);
    void deleteByProfile(Profile profile);
}
