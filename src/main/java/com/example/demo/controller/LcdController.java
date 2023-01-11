package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.pi4j.component.lcd.LCDTextAlignment;
import com.pi4j.component.lcd.impl.GpioLcdDisplay;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.system.SystemInfo;

@RestController
public class LcdController {

	private GpioLcdDisplay lcd;
	private GpioLcdDisplay lcd2;

	private GpioController gpio;

	public static final int LCD_ROWS = 2;
	public static final int LCD_COLUMNS = 16;

	private static final int LCD_ROW_1 = 0;
	private static final int LCD_ROW_2 = 1;
	
	private String text1 = "";
	private String text2 = "";
	private String text3 = "";
	@RequestMapping("/")
	public ModelAndView index() {
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("index");
		try {
			// Initialize the GPIO controller
			gpio = GpioFactory.getInstance();
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
					RaspiPin.GPIO_10, // BCM 10: Strobe pin
					RaspiPin.GPIO_04, // BCM 23: D4
					RaspiPin.GPIO_00, // BCM 17: D5
					RaspiPin.GPIO_01, // BCM 18: D6
					RaspiPin.GPIO_03 // BCM 22: D7
			);
			lcd.clear();
			lcd2.clear();
			lcd.write(0, "Started...");
			Thread.sleep(2000);
			lcd.write(1, "Java " + SystemInfo.getJavaVersion());
			Thread.sleep(2000);
			lcd2.write(0, "Started...2");
			Thread.sleep(2000);
			lcd2.write(1, "Java 2" + SystemInfo.getJavaVersion());
			Thread.sleep(2000);
			// Initial output to check if the wiring is OK
		} catch (Exception ex) {
			System.err.println("Error: " + ex.getMessage());
		}
		return modelAndView;
	}

	@RequestMapping(value = "/toggle1", method = RequestMethod.POST, consumes = "text/plain")
	public void toggleScreen1(@RequestBody String payload) {
		text1 = payload;
		LcdOutput();
	}
	@RequestMapping(value = "/toggle2", method = RequestMethod.POST, consumes = "text/plain")
	public void toggleScreen2(@RequestBody String payload) {
		text2 = payload;
		LcdOutput();
	}
	@RequestMapping(value = "/toggle3", method = RequestMethod.POST, consumes = "text/plain")
	public void toggleScreen3(@RequestBody String payload) {
		text3 = payload;
		LcdOutput();
	}
//	TODO : run to dismiss gpio..  when getting off app/controller
//		gpio.shutdown();
	
	public void LcdOutput() {
		try {
			lcd.clear();
			lcd2.clear();
			lcd.write(LCD_ROW_1, text1 + text2 + text3, LCDTextAlignment.ALIGN_CENTER);
			lcd.write(LCD_ROW_2, text1 + text2 + text3, LCDTextAlignment.ALIGN_CENTER);
			lcd2.write(LCD_ROW_1, text1 + " " + text2  + " " + text3, LCDTextAlignment.ALIGN_CENTER);
			lcd2.write(LCD_ROW_2, text1 + " "  + text2  + " " + text3, LCDTextAlignment.ALIGN_CENTER);
		} catch (Exception ex) {
			System.err.println("Error: " + ex.getMessage());
		}
	}
}
