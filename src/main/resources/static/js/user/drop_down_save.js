const drop_down_div = document.querySelector('#drop-down-div-save');
const caret = document.querySelector('#caret-save');
const caretIcon = document.querySelector('#fa-caret-down-save');
const options = document.querySelector('#board-save');
const ps = document.querySelectorAll('#board-save p');
const selectedOption = document.querySelector('#selected-option-save p');

ps.forEach(p => {
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
        url: '/user/panel/save-custom-to-setting',
        data: { value: settingId },
        success: function(response) {
            options.classList.toggle('show');
            caretIcon.classList.toggle('caret-down');
            selectedOption.textContent = p.textContent;
            document.querySelector('#selected-option-load p').textContent = p.textContent;
        },
        error: function(xhr, status, error) {
            console.error('Error updating checkbox states:', error);
        }
    });
  });
});

drop_down_div.addEventListener('click', (e) => {
    options.classList.toggle('show');
    caretIcon.classList.toggle('caret-down');
});
