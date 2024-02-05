package app.world;

public class PlayerCollisionEngine {
    /*
     *
    positionBuffer = s.positionBuffer;
    texCoordBuffer = s.texCoordBuffer;

    var fieldOfViewRadians = 70*Math.PI/180;

    canvas.addEventListener('mousemove',function(e){
        if(events.mouseBtns[0] || events.mouseBtns[2])return;

        let dx = e.movementX, dy = e.movementY;
        rotation[0] = Math.max(Math.min(rotation[0]+dy/250,Math.PI/2),-Math.PI/2);
        rotation[1] = (rotation[1]-dx/250)%(Math.PI*2);
    })

    info.children[0].addEventListener('input',function(e){fieldOfViewRadians = e.target.value*Math.PI/180;});

    let playerSpeed = 0.125;    //speed of player. should be 1/8 to 1/2

    const playerSize = 1/2; //size of player(in blocks). 1/4 - 2/3
	const collide = true;
    let vely = null;
	let selectedblock = 0;

    // Draw the scene.
    let prevFrameTime = 0;

    function blockUnder(){
        return blockAt(0,-1.00000001,0)
    }
    function blockAt(dx=0,dy=0,dz=0){
        return  s.blockAt(cameraPos,dx+0.5-playerSize/2,dy,dz+0.5-playerSize/2) ||
                s.blockAt(cameraPos,dx+0.5+playerSize/2,dy,dz+0.5-playerSize/2) ||
                s.blockAt(cameraPos,dx+0.5-playerSize/2,dy,dz+0.5+playerSize/2) ||
                s.blockAt(cameraPos,dx+0.5+playerSize/2,dy,dz+0.5+playerSize/2);
    }

	window.addEventListener('wheel',e=>{
		let d = e.deltaY / 100;
		selectedblock = (selectedblock + d)%(atlasdata.size * atlasdata.size)
	})

	let evtqueue = [];

	window.addEventListener('mousedown',e=>{
		if(events.mouseBtns[0]){
			evtqueue.push(1);
			let ref = setInterval(_=>{if(events.mouseBtns[0])evtqueue.push(1);else clearInterval(ref)},250);
		}
		if(events.mouseBtns[2]){
			evtqueue.push(2);
			let ref = setInterval(_=>{if(events.mouseBtns[2])evtqueue.push(2);else clearInterval(ref)},250);
		}
	});

	gl.enable(gl.DEPTH_TEST);
	gl.enable(gl.BLEND);
	gl.enable(gl.CULL_FACE);


	gl.useProgram(program);

	let lastFrameTime = 0

    function drawScene(now) {

		let elapsed_time = now - lastFrameTime;
		lastFrameTime = now;
		while(evtqueue.length){
			let evt = evtqueue.shift();
			if(evt==2){
				let block = s.look(cameraPos,rotation);
				if(block){
					let pos = block[0];
					s.setblock(pos[0],pos[1],pos[2],0);
				}
			}
			if(evt==1){
				let block = s.place(cameraPos,rotation)
				if(block){
					let pos = block[0];
					s.setblock(...block[0],selectedblock,false);
					if(blockAt(0,-1)||blockAt()){
						s.setblock(...block[0],0,false);
					}
					else s.update();
				}
			}
		}


                {   //movement

let speed = playerSpeed*(events.keysdown[16]?0.25:events.keysdown['f']?2:1);

        let dx = 0,dz = 0;
            if(events.keysdown['s']){   //forward and backwards
dx -= speed*Math.sin(rotation[1]);
dz += speed*Math.cos(rotation[1]);
            }
                    if(events.keysdown['w']){
dx += speed*Math.sin(rotation[1]);
dz -= speed*Math.cos(rotation[1]);
            }

                    if(events.keysdown['a']){   //left and right
dz -= speed*Math.sin(rotation[1]);
dx -= speed*Math.cos(rotation[1]);
            }
                    if(events.keysdown['d']){
dz += speed*Math.sin(rotation[1]);
dx += speed*Math.cos(rotation[1]);
            }
                    if(events.keysdown[39]){   //looking angle
rotation[1]+=Math.PI/60;
rotation[1]%=Math.PI*2;
        }
        if(events.keysdown[37]){
rotation[1]+=119*Math.PI/60;
rotation[1]%=Math.PI*2;
        }

        if(events.keysdown[38]){   //same as above
rotation[0] = Math.max(Math.min(rotation[0]+Math.PI/60,Math.PI/2),-Math.PI/2);
        }
        if(events.keysdown[40]){
rotation[0] = Math.max(Math.min(rotation[0]-Math.PI/60,Math.PI/2),-Math.PI/2);
        }

let triggerJump = false;

cameraPos[0] += dx;

			if(collide){
        if(blockAt()){
cameraPos[0] = dx>0?Math.floor(cameraPos[0])-((playerSize-1)/2+Number.EPSILON*100):
        Math.ceil(cameraPos[0])+((playerSize-1)/2+Number.EPSILON*100)
        }
        else if(blockAt(0,-1)){
cameraPos[0] = dx>0?Math.floor(cameraPos[0])-((playerSize-1)/2+Number.EPSILON*100):
        Math.ceil(cameraPos[0])+((playerSize-1)/2+Number.EPSILON*100)
        if(!events.keysdown[16]) triggerJump = true;
        }
        else if(events.keysdown[16]&&!blockUnder()&&vely==null){
cameraPos[0] = dx>0?Math.floor(cameraPos[0])-((-playerSize-1)/2+Number.EPSILON*100):
        Math.ceil(cameraPos[0])+((-playerSize-1)/2+Number.EPSILON*100)
        }
        }

cameraPos[2] += dz;

            if(collide){
        if(blockAt()){
cameraPos[2] = dz>0?Math.floor(cameraPos[2])-((playerSize-1)/2+Number.EPSILON*100):
        Math.ceil(cameraPos[2])+((playerSize-1)/2+Number.EPSILON*100)
        }
        else if(blockAt(0,-1)){
cameraPos[2] = dz>0?Math.floor(cameraPos[2])-((playerSize-1)/2+Number.EPSILON*100):
        Math.ceil(cameraPos[2])+((playerSize-1)/2+Number.EPSILON*100)
        if(!events.keysdown[16]) triggerJump = true;
        }
        else if(events.keysdown[16]&&!blockUnder()&&vely==null){
cameraPos[2] = dz>0?Math.floor(cameraPos[2])-((-playerSize-1)/2+Number.EPSILON*100):
        Math.ceil(cameraPos[2])+((-playerSize-1)/2+Number.EPSILON*100)
        }
        if(events.keysdown[32]||triggerJump){    //jumping
        if(blockUnder(cameraPos,0,-2,0) && !blockAt(cameraPos,0,1,0))  vely = 0.36; //make sure there is a block below you
        }
        }
        }

        if(collide){   //gravity

        if(vely != null){   //not stationary
        if(vely > 0){   //moving
cameraPos[1] += vely;
                    if(!blockAt(0,1)){
vely -= 0.05;
        }
        else{
cameraPos[1] = Math.floor(cameraPos[1])
vely *= -0.5;
        }
        }
        else{   //falling
cameraPos[1] += vely;
                    if(!blockUnder()){
vely = Math.max(vely-0.1,-0.9);
                    }
                            else{
vely = null;
cameraPos[1] = Math.ceil(cameraPos[1]);
                    }
                            if( cameraPos[1] < 0){
vely = 2;
cameraPos[1] = 0;
        }
        }
        }
        else{   //stationary
        if(!blockUnder() && cameraPos[1] > 0){    //in midair
vely = 0;
        }
        if(blockAt(0,-1)){    //stuck in block
cameraPos[1]++;
        }
        }
        }
        else{
        if(events.keysdown[32]) cameraPos[1] += playerSpeed;
			if(events.keysdown[16]) cameraPos[1] -= playerSpeed;
		}

                */
}
