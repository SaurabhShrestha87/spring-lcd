package com.example.demo.repository;

import com.example.demo.model.InfoType;
import com.example.demo.model.Information;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InformationRepository extends JpaRepository<Information, Long> {
    List<Information> findAllByProfileIsNull();

    Optional<Information> findByType(InfoType type);

    Page<Information> findAllByNameContains(String name, Pageable pageable);

    Page<Information> findAllByNameContainsAndProfileIsNull(String name, Pageable pageable);

    List<Information> findAllByNameContains(String name);

    Page<Information> findAllByProfileIsNull(Pageable pageable);
}
