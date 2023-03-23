const drop_down_div_load = document.querySelector('#drop-down-div-load');
const caret_load = document.querySelector('#caret-load');
const caretIcon_load = document.querySelector('#fa-caret-down-load');
const options_load = document.querySelector('#board-load');
const ps_load = document.querySelectorAll('#board-load p');
const selectedOption_load = document.querySelector('#selected-option-load p');

ps_load.forEach(p => {
  p.addEventListener('mouseover', () => {
    p.classList.add('p-color');
  });

  p.addEventListener('mouseleave', () => {
    p.classList.remove('p-color');
  });
  
  p.addEventListener('click', () => {
        var settingId = $(event.target).attr("settingid");
        console.log("settingId : " + settingId)
        $.ajax({
            type: 'POST',
            url: '/user/panel/load-setting',
            data: { value: settingId },
            success: function(response) {
                options_load.classList.toggle('show');
                caretIcon_load.classList.toggle('caret-down');
                selectedOption_load.textContent = p.textContent;
                window.location.reload();
            },
            error: function(xhr, status, error) {
                console.error('Error updating checkbox states:', error);
            }
        });
  });
});

drop_down_div_load.addEventListener('click', (e) => {
    options_load.classList.toggle('show');
    caretIcon_load.classList.toggle('caret-down');
});
