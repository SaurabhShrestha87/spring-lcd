package com.example.demo.repository;

import com.example.demo.model.setting.Setting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SettingRepository extends JpaRepository<Setting, Long> {
    Optional<Setting> findFirstByStatusTrue();
    List<Setting> findByStatusTrue();
    Optional<Setting> findFirstByName(String name);
    List<Setting> findByName(String name);
}
