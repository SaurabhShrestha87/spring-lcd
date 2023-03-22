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
    options_load.classList.toggle('show');
    caretIcon_load.classList.toggle('caret-down');
    selectedOption_load.textContent = p.textContent;
  });
});

drop_down_div_load.addEventListener('click', (e) => {
    options_load.classList.toggle('show');
    caretIcon_load.classList.toggle('caret-down');
});
