      var knobPositionX3;
      var knobPositionY3;
      var mouseX3;
      var mouseY3;
      var knobCenterX3;
      var knobCenterY3;
      var adjacentSide3;
      var oppositeSide3;
      var currentRadiansAngle3;
      var getRadiansInDegrees3;
      var finalAngleInDegrees3;
      var volumeSetting3;
      var tickHighlightPosition3;
      var startingTickAngle3 = -135;
      var tickContainer3 = document.getElementById("tickContainer3");
      var volumeKnob3 = document.getElementById("knob3");
      var boundingRectangle3 = volumeKnob3.getBoundingClientRect(); //get rectangular geometric data of knob (x, y, width, height)

      function main()
      {
          volumeKnob3.addEventListener(getMouseDown(), onMouseDown3); //listen for mouse button click
          document.addEventListener(getMouseUp(), onMouseUp3); //listen for mouse button release
          createTicks3(27, 0);
      }

      //on mouse button down
      function onMouseDown3()
      {
          document.addEventListener(getMouseMove(), onMouseMove3); //start drag
      }

      //on mouse button release
      function onMouseUp3()
      {
          document.removeEventListener(getMouseMove(), onMouseMove3); //stop drag
      }

      //compute mouse angle relative to center of volume knob
      //For clarification, see my basic trig explanation at:
      //https://www.quora.com/What-is-the-significance-of-the-number-pi-to-the-universe/answer/Kevin-Lam-15
      function onMouseMove3(event)
      {
          knobPositionX3 = boundingRectangle3.left; //get knob's global x position
          knobPositionY3 = boundingRectangle3.top; //get knob's global y position
          if(detectMobile() == "desktop")
          {
              mouseX3 = event.pageX; //get mouse's x global position
              mouseY3 = event.pageY; //get mouse's y global position
          } else {
              mouseX3 = event.touches[0].pageX; //get finger's x global position
              mouseY3 = event.touches[0].pageY; //get finger's y global position
          }

          knobCenterX3 = boundingRectangle3.width / 2 + knobPositionX3; //get global horizontal center position of knob relative to mouse position
          knobCenterY3 = boundingRectangle3.height / 2 + knobPositionY3; //get global vertical center position of knob relative to mouse position

          adjacentSide3 = knobCenterX3 - mouseX3; //compute adjacent value of imaginary right angle triangle
          oppositeSide3 = knobCenterY3 - mouseY3; //compute opposite value of imaginary right angle triangle

          //arc-tangent function returns circular angle in radians
          //use atan2() instead of atan() because atan() returns only 180 degree max (PI radians) but atan2() returns four quadrant's 360 degree max (2PI radians)
          currentRadiansAngle3 = Math.atan2(adjacentSide3, oppositeSide3);

          getRadiansInDegrees3 = currentRadiansAngle3 * 180 / Math.PI; //convert radians into degrees

          finalAngleInDegrees3 = -(getRadiansInDegrees3 - 135); //knob is already starting at -135 degrees due to visual design so 135 degrees needs to be subtracted to compensate for the angle offset, negative value represents clockwise direction

          //only allow rotate if greater than zero degrees or lesser than 270 degrees
          console.log(finalAngleInDegrees3);
          if(finalAngleInDegrees3 >= 0 && finalAngleInDegrees3 <= 270)
          {
              volumeKnob3.style.transform = "rotate(" + finalAngleInDegrees3 + "deg)"; //use dynamic CSS transform to rotate volume knob
              //270 degrees maximum freedom of rotation / 100% volume = 1% of volume difference per 2.7 degrees of rotation
              volumeSetting3 = Math.floor(finalAngleInDegrees3 / (270 / 100));
              tickHighlightPosition3 = Math.round((volumeSetting3 * 2.7) / 10); //interpolate how many ticks need to be highlighted
              createTicks3(27, tickHighlightPosition3); //highlight ticks
              audio.volume = volumeSetting3 / 100; //set audio volume
               var states = [];
                       $('.cb2').each(function() {
                           states.push($(this).prop('checked'));
                       });
              $.ajax({
                      type: "POST",
                      url: "/user/panel/sliderDataCool",
                      data: { value: volumeSetting3, states : JSON.stringify(states)},
                      success: function(data) {
                        console.error('Success updating sliderData:', data);
                        document.getElementById("volumeValue3").innerHTML = volumeSetting3 + "%"; //update volume text
                      },
                      error: function(error) {
                        console.error('Error updating sliderData:', error);
                      }
                });
          }
      }

      //dynamically create volume knob "ticks"
      function createTicks3(numTicks, highlightNumTicks)
      {
          //reset first by deleting all existing ticks
          while(tickContainer3.firstChild)
          {
              tickContainer3.removeChild(tickContainer3.firstChild);
          }

          //create ticks
          for(var i=0;i<numTicks;i++)
          {
              var tick = document.createElement("div");

              //highlight only the appropriate ticks using dynamic CSS
              if(i < highlightNumTicks)
              {
                  tick.className = "tick3 activetick3";
              } else {
                  tick.className = "tick3";
              }

              tickContainer3.appendChild(tick);
              tick.style.transform = "rotate(" + startingTickAngle3 + "deg)";
              startingTickAngle3 += 10;
          }

          startingTickAngle3 = -135; //reset
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

