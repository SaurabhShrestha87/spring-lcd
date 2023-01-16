package com.example.demo.profiles.entity;

import java.io.Serializable;

import javax.persistence.*;

@Entity
@Table(name="profile")
public class Profile implements Serializable {

	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="profile_id")
	private int profile_id;

	@Column(name="name")
	private String name;

	@Column(name="category")
	private String category;

	public int getProfile_id() {
		return profile_id;
	}

	public void setProfile_id(int profile_id) {
		this.profile_id = profile_id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

}
