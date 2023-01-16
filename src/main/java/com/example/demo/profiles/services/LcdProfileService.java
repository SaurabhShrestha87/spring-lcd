package com.example.demo.profiles.services;

import java.util.List;

import com.example.demo.profiles.dao.ProfileServiceDAO;
import com.example.demo.profiles.entity.Profile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LcdProfileService implements ProfileService {
	
	@Autowired
	private ProfileServiceDAO dao;

	@Override
	public List<Profile> getProfiles() {
		return dao.getProfiles();
	}

	@Override
	public Profile createProfile(Profile profile) {
		return dao.createProfile(profile);
	}

	@Override
	public Profile createProfile(int profileId, Profile profile) {
		return dao.updateProfile(profileId, profile);
	}

	@Override
	public Profile getProfile(int profileId) {
		return dao.getProfile(profileId);
	}

	@Override
	public boolean deleteProfile(int profileId) {
		return dao.deleteProfile(profileId);
	}

}
