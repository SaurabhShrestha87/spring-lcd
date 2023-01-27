package com.example.demo.model.request;

import com.example.demo.model.InfoType;
import com.example.demo.utils.FileUtils;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class InformationCreationRequest {
    private Long id = 0L;
    private String name;
    private String type;
    private MultipartFile multipartFile;
    private String fileURL;
    private String profileId;

    public Long getProfileIdAsLong() {
        return Long.parseLong(profileId);
    }

    public InfoType getTypeAsInfoType() {
        return InfoType.valueOf(type);
    }

    public String getFileURL() {
        return fileURL == null && multipartFile != null ? FileUtils.createFileDir(multipartFile.getOriginalFilename()) : fileURL;
    }

    @Override
    public String toString() {
        return "InformationCreationRequest{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", multipartFile=" + multipartFile +
                ", fileURL='" + fileURL + '\'' +
                ", profileId='" + profileId + '\'' +
                '}';
    }
}
