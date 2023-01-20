package com.example.demo.repository;

import com.example.demo.model.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

    Page<Profile> findAllByName(String name, Pageable pageable);
}
