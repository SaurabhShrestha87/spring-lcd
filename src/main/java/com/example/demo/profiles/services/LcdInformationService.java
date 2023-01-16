package com.example.demo.profiles.services;

import com.example.demo.profiles.dao.LcdServiceDAO;
import com.example.demo.profiles.entity.Information;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LcdInformationService implements InformationService {
	
	@Autowired
	private LcdServiceDAO dao;

	@Override
	public List<Information> getAllInfo() {
		return dao.getInformations();
	}

	@Override
	public Information createInformation(Information information) {
		return dao.createInformation(information);
	}

	@Override
	public Information createInformation(int id, Information information) {
		return dao.updateInformation(id, information);
	}

	@Override
	public Information getInformation(int id) {
		return dao.getInformation(id);
	}

	@Override
	public boolean deleteInformation(int id) {
		return dao.deleteInformation(id);
	}

}
