package com.example.demo.profiles.controller;

import java.util.List;

import com.example.demo.profiles.entity.Profile;
import com.example.demo.profiles.services.LcdProfileService;
import com.example.demo.utils.OSValidator;
import com.pi4j.component.lcd.impl.GpioLcdDisplay;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.RaspiPin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("profileservice")
public class ProfileController {
	private GpioLcdDisplay lcd;
	private GpioLcdDisplay lcd2;
	public static final int LCD_ROWS = 2;
	public static final int LCD_COLUMNS = 16;
	private static final int LCD_ROW_1 = 0;
	private static final int LCD_ROW_2 = 1;

	@Autowired
	private LcdProfileService service;

	@GetMapping("profile")
	public ResponseEntity<List<Profile>> getProfile(){
		List<Profile> Profile = service.getProfiles();
		return new ResponseEntity<List<Profile>>(Profile, HttpStatus.OK);
	}

	@GetMapping("profile/{id}")
	public ResponseEntity<Profile> getProfile(@PathVariable("id") Integer id){
		Profile profile = service.getProfile(id);
		return new ResponseEntity<Profile>(profile, HttpStatus.OK);
	}
	
	@PostMapping("profile")
	public ResponseEntity<Profile> createProfile(@RequestBody Profile profile){
		Profile b = service.createProfile(profile);
		return new ResponseEntity<Profile>(b, HttpStatus.OK);
	}
	
	@PutMapping("profile/{id}")
	public ResponseEntity<Profile> updateProfile(@PathVariable("id") int id, @RequestBody Profile profile){
		
		Profile b = service.createProfile(id, profile);
		return new ResponseEntity<Profile>(b, HttpStatus.OK);
	}
	
	@DeleteMapping("profile/{id}")
	public ResponseEntity<String> deleteProfile(@PathVariable("id") int id){
		boolean isDeleted = service.deleteProfile(id);
		if(isDeleted){
			String responseContent = "Profile has been deleted successfully";
			return new ResponseEntity<String>(responseContent,HttpStatus.OK);
		}
		String error = "Error while deleting profile from database";
		return new ResponseEntity<String>(error,HttpStatus.INTERNAL_SERVER_ERROR);
	}

	private void init() {
		try {
			// Initialize the GPIO controller if not running in Windows OS
			if(!OSValidator.isWindows()){
				GpioController gpio = GpioFactory.getInstance();
				// Initialize the LCD
				lcd = new GpioLcdDisplay(LCD_ROWS, // Nr of rows
						LCD_COLUMNS, // Nr of columns
						RaspiPin.GPIO_06, // BCM 25: RS pin
						RaspiPin.GPIO_05, // BCM 24: Strobe pin
						RaspiPin.GPIO_04, // BCM 23: D4
						RaspiPin.GPIO_00, // BCM 17: D5
						RaspiPin.GPIO_01, // BCM 18: D6
						RaspiPin.GPIO_03 // BCM 22: D7
				);

				lcd2 = new GpioLcdDisplay(LCD_ROWS, // Nr of rows
						LCD_COLUMNS, // Nr of columns
						RaspiPin.GPIO_06, // BCM 25: RS pin
						RaspiPin.GPIO_21, // BCM 5: Strobe pin
						RaspiPin.GPIO_04, // BCM 23: D4
						RaspiPin.GPIO_00, // BCM 17: D5
						RaspiPin.GPIO_01, // BCM 18: D6
						RaspiPin.GPIO_03 // BCM 22: D7
				);
				lcd.clear();
				lcd2.clear();
				lcd.write(0, "Loading LCD 1");
				lcd2.write(0, "Loading LCD 2");
				for (int i = 0; i <= 100; i++) {
					lcd.write(1, "..." + i + "%");
					lcd2.write(1, "..." + i + "%");
					Thread.sleep(5);
				}
				// Initial output to check if the wiring is OK
			}
		} catch (Exception ex) {
			System.err.println("Error: " + ex.getMessage());
		}
	}
}
