/* a Pen by Diaco m.lotfollahi  : https://diacodesign.com */

var D = document.createElement('div');
TweenMax.set('svg',{overflow:"visible"})
TweenMax.set('.knob',{x:10,y:80})

var tl = new TimelineMax({paused:true})
.from("#path2",1,{drawSVG:"0%",stroke:'orange',ease:Linear.easeNone})
.to('.knob',1,{bezier:{type:"quadratic",values:[{x:10,y:80},{x:150,y:0},{x:300,y:80}]},ease:Linear.easeNone},0);

Draggable.create(D,{trigger:".knob",
type:'x',
throwProps:true,
bounds:{minX:0,maxX:300},
onDrag:Update,
onThrowUpdate:Update});
function Update(){tl.progress(Math.abs(this.x/300))};

TweenMax.to('#path1',0.5,{strokeDashoffset:-10,repeat:-1,ease:Linear.easeNone})

/* a Pen by Diaco m.lotfollahi  : https://diacodesign.com */