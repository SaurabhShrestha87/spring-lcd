package com.example.demo.profiles.services;


import com.example.demo.profiles.entity.Profile;

import java.util.List;

public interface ProfileService {
	
	List<Profile> getProfiles();
	Profile createProfile(Profile profile);
	Profile createProfile(int profileId, Profile profile);
	Profile getProfile(int profileId);
	boolean deleteProfile(int profileId);

}
