package com.example.demo.repo;

import com.example.demo.model.Information;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InfoRespository extends JpaRepository<Information, Integer> {

}