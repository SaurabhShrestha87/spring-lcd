package com.example.demo.profiles.services;


import com.example.demo.profiles.entity.Information;

import java.util.List;

public interface InformationService {

	List<Information> getAllInfo();
	Information createInformation(Information Information);
	Information createInformation(int InformationId, Information Information);
	Information getInformation(int InformationId);
	boolean deleteInformation(int InformationId);

}
