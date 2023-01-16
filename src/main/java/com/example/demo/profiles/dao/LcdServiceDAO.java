package com.example.demo.profiles.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.example.demo.profiles.entity.Information;
import com.example.demo.profiles.entity.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Repository
public class LcdServiceDAO implements ProfileServiceDAO {
	
	@PersistenceContext
	private EntityManager entityManager;

	/**
	 * This method is responsible to get all Profile available in database and return it as List<Profile>
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Profile> getProfiles() {
		String hql = "FROM Profile as atcl ORDER BY atcl.id";
		return (List<Profile>) entityManager.createQuery(hql).getResultList();
	}

	/**
	 * This method is responsible to get a particular Profile detail by given profile id
	 */
	@Override
	public Profile getProfile(int profileId) {
		
		return entityManager.find(Profile.class, profileId);
	}

	/**
	 * This method is responsible to create new profile in database
	 */
	@Override
	public Profile createProfile(Profile profile) {
		entityManager.persist(profile);
		Profile b = getLastInsertedProfile();
		return b;
	}

	/**
	 * This method is responsible to update profile detail in database
	 */
	@Override
	public Profile updateProfile(int profileId, Profile profile) {
		
		//First We are taking Profile detail from database by given profile id and
		// then updating detail with provided profile object
		Profile profileFromDB = getProfile(profileId);
		profileFromDB.setName(profile.getName());
		profileFromDB.setCategory(profile.getCategory());
		
		entityManager.flush();
		
		//again i am taking updated result of profile and returning the profile object
		Profile updatedProfile = getProfile(profileId);
		
		return updatedProfile;
	}

	/**
	 * This method is responsible for deleting a particular(which id will be passed that record) 
	 * record from the database
	 */
	@Override
	public boolean deleteProfile(int profileId) {
		Profile profile = getProfile(profileId);
		entityManager.remove(profile);
		
		//we are checking here that whether entityManager contains earlier deleted profile or not
		// if contains then profile is not deleted from DB that's why returning false;
		boolean status = entityManager.contains(profile);
		if(status){
			return false;
		}
		return true;
	}
	
	/**
	 * This method will get the latest inserted record from the database and return the object of Profile class
	 * @return profile
	 */
	private Profile getLastInsertedProfile(){
		String hql = "from Profile order by id DESC";
		Query query = entityManager.createQuery(hql);
		query.setMaxResults(1);
		Profile profile = (Profile)query.getSingleResult();
		return profile;
	}


	/**
	 * This method is responsible to get all Information available in database and return it as List<Information>
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Information> getInformations() {
		String hql = "FROM Information as atcl ORDER BY atcl.id";
		return (List<Information>) entityManager.createQuery(hql).getResultList();
	}

	/**
	 * This method is responsible to get a particular Information detail by given Information id
	 */
	@Override
	public Information getInformation(int information_id) {

		return entityManager.find(Information.class, information_id);
	}

	/**
	 * This method is responsible to create new Information in database
	 */
	@Override
	public Information createInformation(Information Information) {
		entityManager.persist(Information);
		Information b = getLastInsertedInformation();
		return b;
	}

	/**
	 * This method is responsible to update Information detail in database
	 */
	@Override
	public Information updateInformation(int information_id, Information Information) {

		//First We are taking Information detail from database by given Information id and
		// then updating detail with provided Information object
		Information InformationFromDB = getInformation(information_id);
		InformationFromDB.setValue(Information.getValue());

		entityManager.flush();

		//again i am taking updated result of Information and returning the Information object
		Information updatedInformation = getInformation(information_id);

		return updatedInformation;
	}

	/**
	 * This method is responsible for deleting a particular(which id will be passed that record) 
	 * record from the database
	 */
	@Override
	public boolean deleteInformation(int information_id) {
		Information Information = getInformation(information_id);
		entityManager.remove(Information);
		//we are checking here that whether entityManager contains earlier deleted Information or not
		// if contains then Information is not deleted from DB that's why returning false;
		boolean status = entityManager.contains(Information);
		if(status){
			return false;
		}
		return true;
	}

	/**
	 * This method will get the latest inserted record from the database and return the object of Information class
	 * @return Information
	 */
	private Information getLastInsertedInformation(){
		String hql = "from Information order by id DESC";
		Query query = entityManager.createQuery(hql);
		query.setMaxResults(1);
		Information Information = (Information)query.getSingleResult();
		return Information;
	}

}
