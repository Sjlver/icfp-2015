
var configurations = [{
    "height":15,
    "width":15,
    "sourceSeeds":[0],
    "units":[{"members":[{"x":0,"y":0}],"pivot":{"x":0,"y":0}}],
    "id":1,
    "filled":[{"x":2,"y":4},{"x":3,"y":4},{"x":4,"y":4},{"x":5,"y":4},{"x":6,"y":4},{"x":11,"y":4},{"x":2,"y":5},{"x":8,"y":5},{"x":11,"y":5},{"x":2,"y":6},{"x":11,"y":6},{"x":2,"y":7},{"x":3,"y":7},{"x":4,"y":7},{"x":8,"y":7},{"x":11,"y":7},{"x":2,"y":8},{"x":9,"y":8},{"x":11,"y":8},{"x":2,"y":9},{"x":8,"y":9},{"x":2,"y":10},{"x":3,"y":10},{"x":4,"y":10},{"x":5,"y":10},{"x":6,"y":10},{"x":9,"y":10},{"x":11,"y":10}],
    "current":{"members":[{"x":8,"y":3}],"pivot":{"x":9,"y":2}},
    "sourceLength":100},{
    "height":15,
    "width":15,
    "sourceSeeds":[0],
    "units":[{"members":[{"x":0,"y":0}],"pivot":{"x":0,"y":0}}],
    "id":1,
    "filled":[{"x":2,"y":4},{"x":3,"y":4},{"x":4,"y":4},{"x":5,"y":4},{"x":6,"y":4},{"x":11,"y":4},{"x":2,"y":5},{"x":8,"y":5},{"x":11,"y":5},{"x":2,"y":6},{"x":11,"y":6},{"x":2,"y":7},{"x":3,"y":7},{"x":4,"y":7},{"x":8,"y":7},{"x":11,"y":7},{"x":2,"y":8},{"x":9,"y":8},{"x":11,"y":8},{"x":2,"y":9},{"x":8,"y":9},{"x":2,"y":10},{"x":3,"y":10},{"x":4,"y":10},{"x":5,"y":10},{"x":6,"y":10},{"x":9,"y":10},{"x":11,"y":10}],
    "current":{"members":[{"x":8,"y":2}],"pivot":{"x":8,"y":1}},
    "sourceLength":100},{
    "height":15,
    "width":15,
    "sourceSeeds":[0],
    "units":[{"members":[{"x":0,"y":0}],"pivot":{"x":0,"y":0}}],
    "id":1,
    "filled":[{"x":2,"y":4},{"x":3,"y":4},{"x":4,"y":4},{"x":5,"y":4},{"x":6,"y":4},{"x":11,"y":4},{"x":2,"y":5},{"x":8,"y":5},{"x":11,"y":5},{"x":2,"y":6},{"x":11,"y":6},{"x":2,"y":7},{"x":3,"y":7},{"x":4,"y":7},{"x":8,"y":7},{"x":11,"y":7},{"x":2,"y":8},{"x":9,"y":8},{"x":11,"y":8},{"x":2,"y":9},{"x":8,"y":9},{"x":2,"y":10},{"x":3,"y":10},{"x":4,"y":10},{"x":5,"y":10},{"x":6,"y":10},{"x":9,"y":10},{"x":11,"y":10}],
    "current":{"members":[{"x":9,"y":2}],"pivot":{"x":8,"y":1}},
    "sourceLength":100}];


function drawHexagon(Xcenter, Ycenter, size, ctx) {
    var numberOfSides = 6;
    ctx.beginPath();
    ctx.moveTo (Xcenter +  size * Math.cos(Math.PI / numberOfSides), Ycenter +  size *  Math.sin(Math.PI / numberOfSides));          

    for (var i = 1; i <= numberOfSides;i += 1) {
        ctx.lineTo (Xcenter + size * Math.cos((i * 2 + 1) * Math.PI / numberOfSides), Ycenter + size * Math.sin((i * 2 + 1) * Math.PI / numberOfSides));
    }
    ctx.save();
    ctx.fill();
    ctx.restore();
}

function Board(nrows, ncols, cellSize, filledCells, currentUnit) {
    this.nrows = nrows;
    this.ncols = ncols;
    this.cellSize = cellSize;
    this.filled = filledCells;
    this.current = currentUnit;

    this.getCellX = function (row, col) {
        if (row%2 == 0)
            return col*this.cellSize + this.cellSize/1.21;
        else
            return col*this.cellSize + this.cellSize/0.74;
    }

    this.getCellY = function (row, col) {
        return row*this.cellSize/1.21 + this.cellSize/1.21;
    }

    this.getCellColor = function(row, col) {
        for (var i = 0; i < this.current.members.length; ++i) {
            if (this.current.members[i].x == col &&
                this.current.members[i].y == row) {
                return '#cc9900';
            }
        }
        for (var i = 0; i < this.filled.length; ++i) {
            if (this.filled[i].x == col &&
                this.filled[i].y == row)
                return '#dd2211';
        }
        return '#13c3d3';
    }

    this.isPivot = function(row, col) {
        return (this.current.pivot.x == col &&
                this.current.pivot.y == row);
    }

    this.labelCell = function(row, col, x, y, ctx) {
        ctx.font = Math.floor(this.cellSize/4)+"px Arial";
        ctx.fillStyle = '#000000';
        var name = "(" + col + "," + row + ")";
        ctx.textAlign = "center";
        ctx.textBaseline = "middle";
        ctx.fillText(name, x, y);
    }

    this.drawPivot = function(x, y, ctx) {
        ctx.fillStyle = '#9933ff';
        ctx.beginPath();
        ctx.arc(x, y, this.cellSize/3, 0, 2*Math.PI, true);
        ctx.fill();
    }

    this.drawCell = function(row, col, ctx) {
        ctx.lineWidth = 1;
        ctx.strokeStyle = '#005555';
        ctx.fillStyle = this.getCellColor(row, col);
        var x = this.getCellX(row, col);
        var y = this.getCellY(row, col);
        drawHexagon(x, y, this.cellSize/1.93, ctx);
        if (this.isPivot(row, col)) {
            this.drawPivot(x, y, ctx);
        }
        this.labelCell(row, col, x, y, ctx);
        ctx.stroke();
    }

    this.draw = function(ctx) {
        for (var i = 0; i < this.nrows; ++i) {
            for (var j = 0; j < this.ncols; ++j) {
                this.drawCell(i, j, ctx);
            }
        }
    }
}

function drawConfig(configuration) {
    var canvas = document.getElementById('hex-board');

    if(canvas.getContext) {
        var nrows = configuration.height, ncols = configuration.width;
        var ctx = canvas.getContext('2d');
        var cellSize = Math.min(canvas.width/(ncols+1), canvas.height/nrows);
        var b = new Board(nrows, ncols, cellSize, configuration.filled, configuration.current);
        b.draw(ctx);
    } else {
        alert("Canvas not supported.");
    }
}

var currentConfigIndex = 0;

function draw() {
    drawConfig(configurations[currentConfigIndex]);
}

function drawNextConfig() {
    if (currentConfigIndex < configurations.length - 1) {
        ++currentConfigIndex;
        drawConfig(configurations[currentConfigIndex]);
    }
}

function drawPrevConfig() {
    if (currentConfigIndex > 0) {
        --currentConfigIndex;
        drawConfig(configurations[currentConfigIndex]);
    }
}

function getChar(event) {
  if (event.which == null) { // IE
    if (event.keyCode < 32) return null; // a special symbol
    return String.fromCharCode(event.keyCode)
  }

  if (event.which != 0 && event.charCode != 0) { // !IE
    if (event.which < 32) return null; // a specifal symbol
    return String.fromCharCode(event.which); // others
  }

  return null; // special symbol
}

function aKeyPressed(event) {
    if (getChar(event) == 'j')
        drawNextConfig();
    else if (getChar(event) == 'k')
        drawPrevConfig();
}
