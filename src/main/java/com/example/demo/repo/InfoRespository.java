package com.example.demo.repo;

import com.example.demo.model.Info;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InfoRespository extends JpaRepository<Info, Integer> {

    // custom query to search to blog post by title or content
    List<Info> findByTitleContainingOrContentContaining(String text, String textAgain);
}