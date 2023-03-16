const pause = document.querySelector('.pause')
const play = document.querySelector('.play')
const btnCircle = document.querySelector('.circle-btn')
const wave1 = document.querySelector('.wave-1')
const wave2 = document.querySelector('.wave-2')

btnCircle.addEventListener('click', (event) => {
  event.preventDefault()
  const isPaused = wave1.classList.contains('paused');
    if (isPaused) {
      console.log('Circle is paused');
    } else {
      console.log('Circle is playing');
    }
  $.ajax({
      type: "POST",
      url: "/user/togglePanel",
      data: {toggleState: isPaused},
      success: function() {
          console.log("Toggle state sent to controller");
          pause.classList.toggle('visibility')
          play.classList.toggle('visibility')
          wave1.classList.toggle('paused')
          wave2.classList.toggle('paused')
          btnCircle.classList.toggle('shadow')
      },
      error: function() {
        console.log("Error sending toggle state to controller");
      }
    });
})