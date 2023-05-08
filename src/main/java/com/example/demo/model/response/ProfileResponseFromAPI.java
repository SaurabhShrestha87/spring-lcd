package com.example.demo.model.response;

import com.example.demo.model.InfoType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileResponseFromAPI {
    private Integer pageCount;
    @Enumerated(EnumType.STRING)
    private InfoType type;
    private String title;
    private String fileUrl;
    private String status;
    private List<String> profiles;
}
