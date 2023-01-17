package com.example.demo.profiles.controller;

import com.example.demo.profiles.entity.Information;
import com.example.demo.profiles.services.InformationService;
import com.example.demo.utils.OSValidator;
import com.example.demo.utils.definition.SpiCommand;
import com.example.demo.utils.helper.AsciiCharacterMode;
import com.example.demo.utils.helper.DemoMode;
import com.example.demo.utils.helper.ImageMode;
import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.pi4j.component.lcd.LCDTextAlignment;
import com.pi4j.component.lcd.impl.GpioLcdDisplay;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.RaspiPin;

import java.io.File;
import java.util.List;

@RestController
@RequestMapping("infoservice")
public class InfoController {
	private GpioLcdDisplay lcd;
	private GpioLcdDisplay lcd2;
	public static final int LCD_ROWS = 2;
	public static final int LCD_COLUMNS = 16;
	private static final int LCD_ROW_1 = 0;
	private static final int LCD_ROW_2 = 1;

	@Autowired
	private InformationService informationService;

	@RequestMapping("/")
	public ModelAndView index() {
//		init();
		LedInit();
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("index");
		return modelAndView;
	}
	@RequestMapping("/led")
	public ModelAndView led() {
		LedInit();
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("index");
		return modelAndView;
	}

	@GetMapping("/input")
	public ModelAndView greetingForm(Model model) {
		init();
		model.addAttribute("information", new Information());
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("input");
		return modelAndView;
	}

	@PostMapping("/input")
	public ModelAndView greetingSubmit(@ModelAttribute Information info, Model model) {
		model.addAttribute("information", info);
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("result");
		lcdOutput(lcd, "ID : ", String.valueOf(info.getId()));
		lcdOutput(lcd2, "Value : ", info.getValue());
		return modelAndView;
	}

	@GetMapping("/upload")
	public ModelAndView imageUpload() {
//		init();
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("upload");
		return modelAndView;
	}

	@PostMapping("/upload")
	public ResponseEntity<?> handleFileUpload( @RequestParam("file") MultipartFile file ) {
		String fileName = file.getOriginalFilename();
		try {
			if(OSValidator.isWindows()){
				file.transferTo( new File("D:\\upload\\" + fileName));
			}
			else{
				file.transferTo( new File("root\\home\\pi\\Pictures\\" + fileName));
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		return ResponseEntity.ok("File uploaded successfully.");
	}
	private void LedInit() {
		try {
			System.out.println("Starting SPI example...");

			// Initialize the SpiFactory
			SpiDevice spi = SpiFactory.getInstance(SpiChannel.CS0,
					SpiDevice.DEFAULT_SPI_SPEED, // default spi speed 1 MHz
					SpiDevice.DEFAULT_SPI_MODE); // default spi mode 0

			spi.write(SpiCommand.TEST.getValue(), (byte) 0x01);
			System.out.println("Test mode all on");
			Thread.sleep(1000);

			spi.write(SpiCommand.TEST.getValue(), (byte) 0x00);
			System.out.println("Test mode all off");
			Thread.sleep(1000);

			spi.write(SpiCommand.DECODE_MODE.getValue(), (byte) 0x00);
			System.out.println("Use all bits");

			spi.write(SpiCommand.BRIGHTNESS.getValue(), (byte) 0x08);
			System.out.println("Changed brightness to medium level"
					+ " (0x00 lowest, 0x0F highest)");

			spi.write(SpiCommand.SCAN_LIMIT.getValue(), (byte) 0x0f);
			System.out.println("Configured to scan all digits");

			spi.write(SpiCommand.SHUTDOWN_MODE.getValue(), (byte) 0x01);
			System.out.println("Woke up the MAX7219, is off on startup");

			DemoMode.showRows(spi, 250);
			DemoMode.showCols(spi, 250);
			DemoMode.showRandomOutput(spi, 5, 500);

			ImageMode.showAllImages(spi, 2000);
			AsciiCharacterMode.showAllAsciiCharacters(spi, 750);
			AsciiCharacterMode.scrollAllAsciiCharacters(spi, 50);
		} catch (Exception ex) {
			System.err.println("Error in main function: " + ex.getMessage());
		}
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

	@GetMapping("info")
	public ResponseEntity<List<Information>> getInfo(){
		List<Information> info = informationService.getAllInfo();
		return new ResponseEntity<List<Information>>(info, HttpStatus.OK);
	}

	@GetMapping("info/{id}")
	public ResponseEntity<Information> getInformation(@PathVariable("id") Integer id){
		Information info = informationService.getInformation(id);
		return new ResponseEntity<Information>(info, HttpStatus.OK);
	}

	@PostMapping("info")
	public ResponseEntity<Information> createInformation(@RequestBody Information information){
		Information b = informationService.createInformation(information);
		return new ResponseEntity<Information>(b, HttpStatus.OK);
	}

	@PutMapping("info/{id}")
	public ResponseEntity<Information> updateInformation(@PathVariable("id") int id, @RequestBody Information information){
		Information b = informationService.createInformation(id, information);
		return new ResponseEntity<Information>(b, HttpStatus.OK);
	}

	@DeleteMapping("info/{id}")
	public ResponseEntity<String> deleteInformation(@PathVariable("id") int id){
		boolean isDeleted = informationService.deleteInformation(id);
		if(isDeleted){
			String responseContent = "Information has been deleted successfully";
			return new ResponseEntity<String>(responseContent,HttpStatus.OK);
		}
		String error = "Error while deleting info from database";
		return new ResponseEntity<String>(error,HttpStatus.INTERNAL_SERVER_ERROR);
	}


	@RequestMapping(value = "/toggle1", consumes = "text/plain")
	@PostMapping
	public void toggleScreen1(@RequestBody String payload) {
		if(payload.equalsIgnoreCase("true")){
			lcdOutput(lcd, "LCD 1", "Switch ON");
		}else{
			lcdOutput(lcd, "LCD 1", "Switch OFF");
		}
	}
	@RequestMapping(value = "/toggle2", consumes = "text/plain")
	@PostMapping
	public void toggleScreen2(@RequestBody String payload) {
		if(payload.equalsIgnoreCase("true")){
			lcdOutput(lcd2, "LCD 2", "Switch ON");
		}else{
			lcdOutput(lcd2, "LCD 2", "Switch OFF");
		}
	}
	@RequestMapping(value = "/toggle3", consumes = "text/plain")
	@PostMapping
	public void toggleScreen3(@RequestBody String payload) {
		lcd.clear();
		lcd2.clear();
	}

	/**
	 * TODO : run 'gpio.shutdown();' to dismiss gpio..  when getting off app/controller
	 */
	
	public void lcdOutput(GpioLcdDisplay lcd, String text, String text2) {
		try {
			lcd.clear();
			lcd.write(LCD_ROW_1                       , text, LCDTextAlignment.ALIGN_CENTER);
			lcd.write(LCD_ROW_2, text2, LCDTextAlignment.ALIGN_CENTER);
		} catch (Exception ex) {
			System.err.println("Error: " + ex.getMessage());
		}
	}
}
