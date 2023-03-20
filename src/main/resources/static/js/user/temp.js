window.addEventListener("DOMContentLoaded",() => {
	const thermostat = new Thermostat(".t");
});

class Thermostat {
	constructor(qs) {
		this.el = document.querySelector(qs);
		this.temp = 60;
		this.scale = "f";
		this.min = {
			f: 60,
			c: 16,
			hue: 10,
			angle: 0
		};
		this.max = {
			f: 90,
			c: 32,
			hue: 50,
			angle: 359
		};
		this.init();
	}
	init() {
		const dataAttr = "[data-drag]";
		const dragEl = this.el?.querySelector(dataAttr);
		const draggingClass = "t__drag--dragging";

		dragEl?.addEventListener("keydown",this.changeTemp.bind(this));
		this.el?.addEventListener("click",this.changeScale.bind(this));

		Draggable.create(dataAttr,{
			type: "rotation",
			bounds: {
				minRotation: this.min.angle, 
				maxRotation: this.max.angle
			},
			onDrag: () => {
				this.temp = this.tempFromDrag();
				this.updateDisplay();
				dragEl.classList.add(draggingClass);
			},
			onDragEnd: () => {
				dragEl.classList.remove(draggingClass);
			}
		});

		this.updateDisplay();
	}
	changeTemp(e) {
		const { key } = e;
		const step = 1;
		
		// value change
		if (key === "ArrowUp" || key === "ArrowRight")
			this.temp += step;
		else if (key === "ArrowDown" || key === "ArrowLeft")
			this.temp -= step;

		// keep within bounds
		if (this.temp < this.min[this.scale])
			this.temp = this.min[this.scale];
		else if (this.temp > this.max[this.scale])
			this.temp = this.max[this.scale];

		this.updateDisplay();
	}
	changeScale(e) {
		if (e.target.hasAttribute("data-scale") && this.scale !== e.target.value) {
			this.scale = e.target.value;
			const rawTemp = this.scale === "f" ? this.CToF(this.temp) : this.FToC(this.temp);

			this.temp = Math.round(rawTemp);

			this.updateDisplay();
		}
	}
	setAriaPressed() {
		const scale = this.el?.querySelectorAll("[data-scale]");

		if (scale) {
			Array.from(scale).forEach(s => {
				s.setAttribute("aria-pressed",s.value === this.scale);
			});
		}
	}
	setDigits() {
		// screen reader value
		const sr = this.el?.querySelector("[data-temp-sr]");

		if (sr)
			sr.textContent = `${this.temp}°${this.scale.toUpperCase()}`;

		// displayed value
		const tempDigits = this.el?.querySelectorAll("[data-temp]");

		if (tempDigits) {
			const digitString = String(this.temp).split("").reverse();

			Array.from(tempDigits).reverse().forEach((digit,i) => {
				digit.textContent = digitString[i];
			})
		}
	}
	setTone() {
		const minHue = this.min.hue;
		const maxHue = this.max.hue;
		const temp = this.temp;
		const minTemp = this.min[this.scale];
		const maxTemp = this.max[this.scale];
		const hueDiff = maxHue - minHue;
		const relativeHue = hueDiff * ((temp - minTemp) / (maxTemp - minTemp));
		const hue = Math.round(maxHue - relativeHue);

		this.el?.style.setProperty("--temp-hue",hue);
	}
	CToF(c) {
		return c * (9 / 5) + 32;
	}
	FToC(f) {
		return (f - 32) * (5 / 9);
	}
	angleFromMatrix(transVal) {
		const matrixVal = transVal.split("(")[1].split(")")[0].split(",");
		const [cos1,sin] = matrixVal.slice(0,2);
		let angle = Math.round(Math.atan2(sin,cos1) * (180 / Math.PI));

		if (angle < 0)
			angle += 360;

		return angle;
	}
	tempFromDrag() {
		const drag = this.el.querySelector(".t__drag")

		if (drag) {
			const dragCS = window.getComputedStyle(drag);
			const trans = dragCS.getPropertyValue("transform");
			const dragAngle = this.angleFromMatrix(trans);
			const relAngle = dragAngle - this.min.angle;
			const angleFrac = relAngle / (this.max.angle - this.min.angle);
			const tempRange = this.max[this.scale] - this.min[this.scale];
			const result = angleFrac * tempRange + this.min[this.scale];

			return Math.round(result);
		}
	}
	updateDisplay() {
		this.setDigits();
		this.setAriaPressed();
		this.setTone();
	}
}