var canvas = document.getElementById("canvas");
var context = canvas.getContext("2d");

function reset() {
    $.ajax({
        type: "GET",
        url: "/draw/reset",
        success: function(response) {
          clearCanvas();
        }
    });
}

function sendShape(panelId) {
    var shapeType = document.querySelector('#selectShape').value;
    var size = document.getElementById("size").value;
    var x = document.getElementById("x").value;
    var y = document.getElementById("y").value;
    $.ajax({
        type: "POST",
        url: "/draw/sendShape/",
        data: {
          panelId: panelId,
          shapeType: shapeType
        },
        success: function(response) {
          redrawShapes(response);
        }
    });
}

function drawShape() {
  var shapeType = document.querySelector('#selectShape').value;
  var size = document.getElementById("size").value;
  var x = document.getElementById("x").value;
  var y = document.getElementById("y").value;

  $.ajax({
    type: "POST",
    url: "/draw/drawShape",
    data: {
      shapeType: shapeType,
      size: size,
      x: x,
      y: y
    },
    success: function(response) {
      redrawShapes(response);
    }
  });
}

function redrawShapes(shapes) {
  clearCanvas();

  for (var i = 0; i < shapes.length; i++) {
    var shape = shapes[i];
    if (shape.type === "circle") {
      drawCircle(shape.x, shape.y, shape.size);
    } else if (shape.type === "square") {
      drawSquare(shape.x, shape.y, shape.size);
    }
  }
}

function clearCanvas() {
  context.clearRect(0, 0, canvas.width, canvas.height);
}

function drawCircle(x, y, size) {
  context.beginPath();
  context.arc(x, y, size / 2, 0, 2 * Math.PI);
  context.stroke();
}

function drawSquare(x, y, size) {
  context.beginPath();
  context.rect(x - size / 2, y - size / 2, size, size);
  context.stroke();
}