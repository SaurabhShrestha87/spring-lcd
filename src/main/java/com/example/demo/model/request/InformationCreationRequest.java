package com.example.demo.model.request;

import com.example.demo.utils.FileUtils;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class InformationCreationRequest {
    private Long id = 0L;
    private String name;
    private String infoType;
    private Long duration;
    private Long count;
    private MultipartFile multipartFile;
    private String fileURL;
    private String profileId;

    public String getFileURLFromMultipart() {
        return fileURL == null && multipartFile != null ? FileUtils.createFileDir(multipartFile.getOriginalFilename()) : fileURL;
    }
}
