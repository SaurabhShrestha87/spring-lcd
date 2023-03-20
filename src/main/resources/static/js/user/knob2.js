      var knobPositionX2;
      var knobPositionY2;
      var mouseX2;
      var mouseY2;
      var knobCenterX2;
      var knobCenterY2;
      var adjacentSide2;
      var oppositeSide2;
      var currentRadiansAngle2;
      var getRadiansInDegrees2;
      var finalAngleInDegrees2;
      var volumeSetting2;
      var tickHighlightPosition2;
      var startingTickAngle2 = -135;
    //2nd
      var tickContainer2 = document.getElementById("tickContainer2");
      var volumeKnob2 = document.getElementById("knob2");
      var boundingRectangle2 = volumeKnob2.getBoundingClientRect(); //get rectangular geometric data of knob (x, y, width, height)

      function main()
      {
          volumeKnob2.addEventListener(getMouseDown(), onMouseDown2); //listen for mouse button click
          document.addEventListener(getMouseUp(), onMouseUp2); //listen for mouse button release
          createTicks2(27, 0);
      }

      //on mouse button down
      function onMouseDown2()
      {
          document.addEventListener(getMouseMove(), onMouseMove2); //start drag
      }

      //on mouse button release
      function onMouseUp2()
      {
          document.removeEventListener(getMouseMove(), onMouseMove2); //stop drag
      }

      //compute mouse angle relative to center of volume knob
      //For clarification, see my basic trig explanation at:
      //https://www.quora.com/What-is-the-significance-of-the-number-pi-to-the-universe/answer/Kevin-Lam-15
      function onMouseMove2(event)
      {
          knobPositionX2 = boundingRectangle2.left; //get knob's global x position
          knobPositionY2 = boundingRectangle2.top; //get knob's global y position

          if(detectMobile() == "desktop")
          {
              mouseX2 = event.pageX; //get mouse's x global position
              mouseY2 = event.pageY; //get mouse's y global position
          } else {
              mouseX2 = event.touches[0].pageX; //get finger's x global position
              mouseY2 = event.touches[0].pageY; //get finger's y global position
          }

          knobCenterX2 = boundingRectangle2.width / 2 + knobPositionX2; //get global horizontal center position of knob relative to mouse position
          knobCenterY2 = boundingRectangle2.height / 2 + knobPositionY2; //get global vertical center position of knob relative to mouse position

          adjacentSide2 = knobCenterX2 - mouseX2; //compute adjacent value of imaginary right angle triangle
          oppositeSide2 = knobCenterY2 - mouseY2; //compute opposite value of imaginary right angle triangle

          //arc-tangent function returns circular angle in radians
          //use atan2() instead of atan() because atan() returns only 180 degree max (PI radians) but atan2() returns four quadrant's 360 degree max (2PI radians)
          currentRadiansAngle2 = Math.atan2(adjacentSide2, oppositeSide2);

          getRadiansInDegrees2 = currentRadiansAngle2 * 180 / Math.PI; //convert radians into degrees

          finalAngleInDegrees2 = -(getRadiansInDegrees2 - 135); //knob is already starting at -135 degrees due to visual design so 135 degrees needs to be subtracted to compensate for the angle offset, negative value represents clockwise direction

          //only allow rotate if greater than zero degrees or lesser than 270 degrees
          console.log(finalAngleInDegrees2);
          if(finalAngleInDegrees2 >= 0 && finalAngleInDegrees2 <= 270)
          {
              volumeKnob2.style.transform = "rotate(" + finalAngleInDegrees2 + "deg)"; //use dynamic CSS transform to rotate volume knob

              //270 degrees maximum freedom of rotation / 100% volume = 1% of volume difference per 2.7 degrees of rotation
              volumeSetting2 = Math.floor(finalAngleInDegrees2 / (270 / 100));

              tickHighlightPosition2 = Math.round((volumeSetting2 * 2.7) / 10); //interpolate how many ticks need to be highlighted

              createTicks2(27, tickHighlightPosition2); //highlight ticks

              audio.volume = volumeSetting2 / 100; //set audio volume

               var states = [];
                       $('.cb2').each(function() {
                           states.push($(this).prop('checked'));
                       });
              $.ajax({
                      type: "POST",
                      url: "/user/panel/sliderDataWarm",
                      data: { value: volumeSetting2, states : JSON.stringify(states)},
                      success: function(data) {
                        console.error('Success updating sliderData:', data);
                        document.getElementById("volumeValue2").innerHTML = volumeSetting2 + "%"; //update volume text
                      },
                      error: function(error) {
                        console.error('Error updating sliderData:', error);
                      }
                });
          }
      }

      //dynamically create volume knob "ticks"
      function createTicks2(numTicks, highlightNumTicks)
      {
          //reset first by deleting all existing ticks
          while(tickContainer2.firstChild)
          {
              tickContainer2.removeChild(tickContainer2.firstChild);
          }

          //create ticks
          for(var i=0;i<numTicks;i++)
          {
              var tick = document.createElement("div");
              //highlight only the appropriate ticks using dynamic CSS
              if(i < highlightNumTicks)
              {
                  tick.className = "tick2 activetick2";
              } else {
                  tick.className = "tick2";
              }

              tickContainer2.appendChild(tick);
              tick.style.transform = "rotate(" + startingTickAngle2 + "deg)";
              startingTickAngle2 += 10;
          }

          startingTickAngle2 = -135; //reset
      }

      //detect for mobile devices from https://www.sitepoint.com/navigator-useragent-mobiles-including-ipad/
      function detectMobile()
      {
          var result = (navigator.userAgent.match(/(iphone)|(ipod)|(ipad)|(android)|(blackberry)|(windows phone)|(symbian)/i));

          if(result !== null)
          {
              return "mobile";
          } else {
              return "desktop";
          }
      }

      function getMouseDown()
      {
          if(detectMobile() == "desktop")
          {
              return "mousedown";
          } else {
              return "touchstart";
          }
      }

      function getMouseUp()
      {
          if(detectMobile() == "desktop")
          {
              return "mouseup";
          } else {
              return "touchend";
          }
      }

      function getMouseMove()
      {
          if(detectMobile() == "desktop")
          {
              return "mousemove";
          } else {
              return "touchmove";
          }
      }

      main();

