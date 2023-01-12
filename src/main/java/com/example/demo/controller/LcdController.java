package com.example.demo.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import com.pi4j.component.lcd.LCDTextAlignment;
import com.pi4j.component.lcd.impl.GpioLcdDisplay;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.RaspiPin;

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
					RaspiPin.GPIO_21, // BCM 5: Strobe pin
					RaspiPin.GPIO_04, // BCM 23: D4
					RaspiPin.GPIO_00, // BCM 17: D5
					RaspiPin.GPIO_01, // BCM 18: D6
					RaspiPin.GPIO_03 // BCM 22: D7
			);
			lcd.clear();
			lcd2.clear();
			lcd.write(0, "Loading LCD #1");
			lcd2.write(0, "Loading LCD #2");
			for (int i = 0; i <= 100; i++) {
				lcd.write(1, "..." + i + "%");
				lcd2.write(1, "..." + i + "%");
				Thread.sleep(40);
			}
			Thread.sleep(2000);

			lcd.write(0, "Ready #1");
			lcd.write(1, "ʕ•́ᴥ•̀ʔっ♡");
			lcd2.write(0, "Ready #2");
			lcd2.write(1, "(ง︡'-'︠)ง");
			// Initial output to check if the wiring is OK
		} catch (Exception ex) {
			System.err.println("Error: " + ex.getMessage());
		}
		return modelAndView;
	}

	@RequestMapping(value = "/toggle1", consumes = "text/plain")
	@PostMapping
	public void toggleScreen1(@RequestBody String payload) {
		text1 = payload;
		lcdOutput();
	}
	@RequestMapping(value = "/toggle2", consumes = "text/plain")
	@PostMapping
	public void toggleScreen2(@RequestBody String payload) {
		text2 = payload;
		lcdOutput();
	}
	@RequestMapping(value = "/toggle3", consumes = "text/plain")
	@PostMapping
	public void toggleScreen3(@RequestBody String payload) {
		text3 = payload;
		lcdOutput();
	}
//	TODO : run to dismiss gpio..  when getting off app/controller
//		gpio.shutdown();
	
	public void lcdOutput() {
		try {
			lcd.clear();
			lcd2.clear();
			lcd.write(LCD_ROW_2, text1 + text2 + text3, LCDTextAlignment.ALIGN_CENTER);
			lcd2.write(LCD_ROW_2, text1 + " "  + text2  + " " + text3, LCDTextAlignment.ALIGN_CENTER);
		} catch (Exception ex) {
			System.err.println("Error: " + ex.getMessage());
		}
	}
}
