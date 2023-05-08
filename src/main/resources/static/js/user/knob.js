var knobPositionX;
var knobPositionY;
var mouseX;
var mouseY;
var knobCenterX;
var knobCenterY;
var adjacentSide;
var oppositeSide;
var currentRadiansAngle;
var getRadiansInDegrees;
var finalAngleInDegrees;
var volumeSetting;
var tickHighlightPosition;
var audio = new Audio(""); //Celine Dion's "Ashes"
var startingTickAngle = -135;
//1st
var tickContainer = document.getElementById("tickContainer");
var volumeKnob = document.getElementById("knob");
var boundingRectangle = volumeKnob.getBoundingClientRect(); //get rectangular geometric data of knob (x, y, width, height)

function main() {
    audio.volume = 0; //start at zero volume

    volumeKnob.addEventListener(getMouseDown(), onMouseDown); //listen for mouse button click
    document.addEventListener(getMouseUp(), onMouseUp); //listen for mouse button release

    createTicks(27, 0);
}

//on mouse button down
function onMouseDown() {
    //start audio if not already playing
    if (audio.paused == true) {
        //mobile users must tap anywhere to start audio
        //https://developers.google.com/web/updates/2017/09/autoplay-policy-changes
        var promise = audio.play();
        if (promise !== undefined) {
            promise.then(_ => {
                audio.play();
            }).catch(error => {});
        }
    }
    document.addEventListener(getMouseMove(), onMouseMove); //start drag
}

//on mouse button release
function onMouseUp() {
    document.removeEventListener(getMouseMove(), onMouseMove); //stop drag
}

//compute mouse angle relative to center of volume knob
//For clarification, see my basic trig explanation at:
//https://www.quora.com/What-is-the-significance-of-the-number-pi-to-the-universe/answer/Kevin-Lam-15
function onMouseMove(event)

{

    knobPositionX = boundingRectangle.left; //get knob's global x position
    knobPositionY = boundingRectangle.top; //get knob's global y position

    if (detectMobile() == "desktop") {
        mouseX = event.pageX; //get mouse's x global position
        mouseY = event.pageY; //get mouse's y global position
    } else {
        mouseX = event.touches[0].pageX; //get finger's x global position
        mouseY = event.touches[0].pageY; //get finger's y global position
    }

    knobCenterX = boundingRectangle.width / 2 + knobPositionX; //get global horizontal center position of knob relative to mouse position
    knobCenterY = boundingRectangle.height / 2 + knobPositionY; //get global vertical center position of knob relative to mouse position

    adjacentSide = knobCenterX - mouseX; //compute adjacent value of imaginary right angle triangle
    oppositeSide = knobCenterY - mouseY; //compute opposite value of imaginary right angle triangle

    //arc-tangent function returns circular angle in radians
    //use atan2() instead of atan() because atan() returns only 180 degree max (PI radians) but atan2() returns four quadrant's 360 degree max (2PI radians)
    currentRadiansAngle = Math.atan2(adjacentSide, oppositeSide);

    getRadiansInDegrees = currentRadiansAngle * 180 / Math.PI; //convert radians into degrees

    finalAngleInDegrees = -(getRadiansInDegrees - 135); //knob is already starting at -135 degrees due to visual design so 135 degrees needs to be subtracted to compensate for the angle offset, negative value represents clockwise direction

    //only allow rotate if greater than zero degrees or lesser than 270 degrees
    console.log(finalAngleInDegrees);
    if (finalAngleInDegrees >= 0 && finalAngleInDegrees <= 270) {
        volumeKnob.style.transform = "rotate(" + finalAngleInDegrees + "deg)"; //use dynamic CSS transform to rotate volume knob

        //270 degrees maximum freedom of rotation / 100% volume = 1% of volume difference per 2.7 degrees of rotation
        volumeSetting = Math.floor(finalAngleInDegrees / (270 / 100));

        tickHighlightPosition = Math.round((volumeSetting * 2.7) / 10); //interpolate how many ticks need to be highlighted

        createTicks(27, tickHighlightPosition); //highlight ticks

        var states = [];
        $('.cb2').each(function() {
            states.push($(this).prop('checked'));
        });
        $.ajax({
            type: "POST",
            url: "/user/panel/sliderData",
            data: {
                value: volumeSetting,
                states: JSON.stringify(states)
            },
            success: function(respList) {
                console.log('Success updating sliderData:', respList);
                document.getElementById("volumeValue").innerHTML = volumeSetting + "%"; //update brightness text
                respList.forEach(function(panelId) {
                    if (panelId != null) {
                        console.log(panelId);
                        document.getElementById("peek-brightness-" + panelId).innerHTML = volumeSetting + ""; //update brightness peek text
                        document.querySelector('#selected-option-save p').textContent = "SELECT";
                        document.querySelector('#selected-option-load p').textContent = "CUSTOM";
                    }
                });
            },
            error: function(error) {
                console.error('Error updating sliderData:', error);
            }
        });
    }
}

//dynamically create volume knob "ticks"
function createTicks(numTicks, highlightNumTicks) {
    //reset first by deleting all existing ticks
    while (tickContainer.firstChild) {
        tickContainer.removeChild(tickContainer.firstChild);
    }

    //create ticks
    for (var i = 0; i < numTicks; i++) {
        var tick = document.createElement("div");

        //highlight only the appropriate ticks using dynamic CSS
        if (i < highlightNumTicks) {
            tick.className = "tick activetick";
        } else {
            tick.className = "tick";
        }

        tickContainer.appendChild(tick);
        tick.style.transform = "rotate(" + startingTickAngle + "deg)";
        startingTickAngle += 10;
    }

    startingTickAngle = -135; //reset
}

//detect for mobile devices from https://www.sitepoint.com/navigator-useragent-mobiles-including-ipad/
function detectMobile() {
    var result = (navigator.userAgent.match(/(iphone)|(ipod)|(ipad)|(android)|(blackberry)|(windows phone)|(symbian)/i));

    if (result !== null) {
        return "mobile";
    } else {
        return "desktop";
    }
}

function getMouseDown() {
    if (detectMobile() == "desktop") {
        return "mousedown";
    } else {
        return "touchstart";
    }
}

function getMouseUp() {
    if (detectMobile() == "desktop") {
        return "mouseup";
    } else {
        return "touchend";
    }
}

function getMouseMove() {
    if (detectMobile() == "desktop") {
        return "mousemove";
    } else {
        return "touchmove";
    }
}

main();