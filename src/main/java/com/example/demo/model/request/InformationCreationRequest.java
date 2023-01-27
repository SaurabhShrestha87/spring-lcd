package com.example.demo.model.request;

import com.example.demo.model.InfoType;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class InformationCreationRequest {
    private String name;
    private InfoType type;
    private MultipartFile multipartFile;
    private String fileURL;
    private long profileId;

    public InformationCreationRequest(String name, String type, MultipartFile multipartFile, String fileUrl, String profileId) {
        this.name = name;
        this.type = InfoType.valueOf(type);
        this.multipartFile = multipartFile;
        this.fileURL = fileURL;
        this.profileId = Long.parseLong(profileId);
    }

    public InformationCreationRequest() {

    }

    public void setType(String type){
        this.type =  InfoType.valueOf(type);
    }
    @Override
    public String toString() {
        return "InformationCreationRequest{" +
                "\nname='" + name + '\'' +
                ",\n type=" + type +
                ",\n multipartFile=" + multipartFile.getName() +
                ",\n fileURL='" + fileURL + '\'' +
                ",\n profileId=" + profileId +
                '}';
    }
}
