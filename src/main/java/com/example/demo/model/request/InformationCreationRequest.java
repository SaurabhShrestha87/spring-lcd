package com.example.demo.model.request;

import com.example.demo.model.InfoType;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class InformationCreationRequest {
    private Long id = 0L;
    private String name;
    private InfoType type;
    private MultipartFile multipartFile;
    private String fileURL;
    private long profileId;

}
