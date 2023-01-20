package com.example.demo.repository;

import com.example.demo.model.Information;
import com.example.demo.model.Lend;
import com.example.demo.model.LendStatus;
import com.example.demo.model.Panel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LendRepository extends JpaRepository<Lend, Long> {
    Optional<Lend> findByInformationAndStatus(Information information, LendStatus status);

    Page<Lend> findAllByPanelContains(Panel panel, Pageable pageable);
}
