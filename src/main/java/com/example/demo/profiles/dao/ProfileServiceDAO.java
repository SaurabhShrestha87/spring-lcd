package com.example.demo.profiles.dao;

import com.example.demo.profiles.entity.Information;
import com.example.demo.profiles.entity.Profile;

import java.util.List;

public interface ProfileServiceDAO {

	//PROFILE
	List<Profile> getProfiles();
	Profile getProfile(int profileId);
	Profile createProfile(Profile profile);
	Profile updateProfile(int profileId, Profile profile);
	boolean deleteProfile(int profileId);

	//INFORMATION
	List<Information> getInformations();
	Information getInformation(int id);
	Information createInformation(Information information);
	Information updateInformation(int id, Information information);
	boolean deleteInformation(int id);

}
