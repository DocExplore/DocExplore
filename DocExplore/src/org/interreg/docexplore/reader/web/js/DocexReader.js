/* Mapbox earcut
 * https://github.com/mapbox/earcut
 * Copyright (c) 2016, Mapbox
 * Licensed under the ISC license
*/
'use strict';function earcut(data,holeIndices,dim){dim=dim||2;var hasHoles=holeIndices&&holeIndices.length,outerLen=hasHoles?holeIndices[0]*dim:data.length,outerNode=linkedList(data,0,outerLen,dim,true),triangles=[];if(!outerNode)return triangles;var minX,minY,maxX,maxY,x,y,size;if(hasHoles)outerNode=eliminateHoles(data,holeIndices,outerNode,dim);if(data.length>80*dim){minX=maxX=data[0];minY=maxY=data[1];for(var i=dim;i<outerLen;i+=dim){x=data[i];y=data[i+1];if(x<minX)minX=x;if(y<minY)minY=y;if(x>maxX)maxX=x;if(y>maxY)maxY=y}size=Math.max(maxX-minX,maxY-minY)}earcutLinked(outerNode,triangles,dim,minX,minY,size);return triangles}function linkedList(data,start,end,dim,clockwise){var i,last;if(clockwise===(signedArea(data,start,end,dim)>0)){for(i=start;i<end;i+=dim)last=insertNode(i,data[i],data[i+1],last)}else{for(i=end-dim;i>=start;i-=dim)last=insertNode(i,data[i],data[i+1],last)}if(last&&equals(last,last.next)){removeNode(last);last=last.next}return last}function filterPoints(start,end){if(!start)return start;if(!end)end=start;var p=start,again;do{again=false;if(!p.steiner&&(equals(p,p.next)||area(p.prev,p,p.next)===0)){removeNode(p);p=end=p.prev;if(p===p.next)return null;again=true}else{p=p.next}}while(again||p!==end);return end}function earcutLinked(ear,triangles,dim,minX,minY,size,pass){if(!ear)return;if(!pass&&size)indexCurve(ear,minX,minY,size);var stop=ear,prev,next;while(ear.prev!==ear.next){prev=ear.prev;next=ear.next;if(size?isEarHashed(ear,minX,minY,size):isEar(ear)){triangles.push(prev.i/dim);triangles.push(ear.i/dim);triangles.push(next.i/dim);removeNode(ear);ear=next.next;stop=next.next;continue}ear=next;if(ear===stop){if(!pass){earcutLinked(filterPoints(ear),triangles,dim,minX,minY,size,1)}else if(pass===1){ear=cureLocalIntersections(ear,triangles,dim);earcutLinked(ear,triangles,dim,minX,minY,size,2)}else if(pass===2){splitEarcut(ear,triangles,dim,minX,minY,size)}break}}}function isEar(ear){var a=ear.prev,b=ear,c=ear.next;if(area(a,b,c)>=0)return false;var p=ear.next.next;while(p!==ear.prev){if(pointInTriangle(a.x,a.y,b.x,b.y,c.x,c.y,p.x,p.y)&&area(p.prev,p,p.next)>=0)return false;p=p.next}return true}function isEarHashed(ear,minX,minY,size){var a=ear.prev,b=ear,c=ear.next;if(area(a,b,c)>=0)return false;var minTX=a.x<b.x?(a.x<c.x?a.x:c.x):(b.x<c.x?b.x:c.x),minTY=a.y<b.y?(a.y<c.y?a.y:c.y):(b.y<c.y?b.y:c.y),maxTX=a.x>b.x?(a.x>c.x?a.x:c.x):(b.x>c.x?b.x:c.x),maxTY=a.y>b.y?(a.y>c.y?a.y:c.y):(b.y>c.y?b.y:c.y);var minZ=zOrder(minTX,minTY,minX,minY,size),maxZ=zOrder(maxTX,maxTY,minX,minY,size);var p=ear.nextZ;while(p&&p.z<=maxZ){if(p!==ear.prev&&p!==ear.next&&pointInTriangle(a.x,a.y,b.x,b.y,c.x,c.y,p.x,p.y)&&area(p.prev,p,p.next)>=0)return false;p=p.nextZ}p=ear.prevZ;while(p&&p.z>=minZ){if(p!==ear.prev&&p!==ear.next&&pointInTriangle(a.x,a.y,b.x,b.y,c.x,c.y,p.x,p.y)&&area(p.prev,p,p.next)>=0)return false;p=p.prevZ}return true}function cureLocalIntersections(start,triangles,dim){var p=start;do{var a=p.prev,b=p.next.next;if(!equals(a,b)&&intersects(a,p,p.next,b)&&locallyInside(a,b)&&locallyInside(b,a)){triangles.push(a.i/dim);triangles.push(p.i/dim);triangles.push(b.i/dim);removeNode(p);removeNode(p.next);p=start=b}p=p.next}while(p!==start);return p}function splitEarcut(start,triangles,dim,minX,minY,size){var a=start;do{var b=a.next.next;while(b!==a.prev){if(a.i!==b.i&&isValidDiagonal(a,b)){var c=splitPolygon(a,b);a=filterPoints(a,a.next);c=filterPoints(c,c.next);earcutLinked(a,triangles,dim,minX,minY,size);earcutLinked(c,triangles,dim,minX,minY,size);return}b=b.next}a=a.next}while(a!==start)}function eliminateHoles(data,holeIndices,outerNode,dim){var queue=[],i,len,start,end,list;for(i=0,len=holeIndices.length;i<len;i++){start=holeIndices[i]*dim;end=i<len-1?holeIndices[i+1]*dim:data.length;list=linkedList(data,start,end,dim,false);if(list===list.next)list.steiner=true;queue.push(getLeftmost(list))}queue.sort(compareX);for(i=0;i<queue.length;i++){eliminateHole(queue[i],outerNode);outerNode=filterPoints(outerNode,outerNode.next)}return outerNode}function compareX(a,b){return a.x-b.x}function eliminateHole(hole,outerNode){outerNode=findHoleBridge(hole,outerNode);if(outerNode){var b=splitPolygon(outerNode,hole);filterPoints(b,b.next)}}function findHoleBridge(hole,outerNode){var p=outerNode,hx=hole.x,hy=hole.y,qx=-Infinity,m;do{if(hy<=p.y&&hy>=p.next.y&&p.next.y!==p.y){var x=p.x+(hy-p.y)*(p.next.x-p.x)/(p.next.y-p.y);if(x<=hx&&x>qx){qx=x;if(x===hx){if(hy===p.y)return p;if(hy===p.next.y)return p.next}m=p.x<p.next.x?p:p.next}}p=p.next}while(p!==outerNode);if(!m)return null;if(hx===qx)return m.prev;var stop=m,mx=m.x,my=m.y,tanMin=Infinity,tan;p=m.next;while(p!==stop){if(hx>=p.x&&p.x>=mx&&hx!==p.x&&pointInTriangle(hy<my?hx:qx,hy,mx,my,hy<my?qx:hx,hy,p.x,p.y)){tan=Math.abs(hy-p.y)/(hx-p.x);if((tan<tanMin||(tan===tanMin&&p.x>m.x))&&locallyInside(p,hole)){m=p;tanMin=tan}}p=p.next}return m}function indexCurve(start,minX,minY,size){var p=start;do{if(p.z===null)p.z=zOrder(p.x,p.y,minX,minY,size);p.prevZ=p.prev;p.nextZ=p.next;p=p.next}while(p!==start);p.prevZ.nextZ=null;p.prevZ=null;sortLinked(p)}function sortLinked(list){var i,p,q,e,tail,numMerges,pSize,qSize,inSize=1;do{p=list;list=null;tail=null;numMerges=0;while(p){numMerges++;q=p;pSize=0;for(i=0;i<inSize;i++){pSize++;q=q.nextZ;if(!q)break}qSize=inSize;while(pSize>0||(qSize>0&&q)){if(pSize!==0&&(qSize===0||!q||p.z<=q.z)){e=p;p=p.nextZ;pSize--}else{e=q;q=q.nextZ;qSize--}if(tail)tail.nextZ=e;else list=e;e.prevZ=tail;tail=e}p=q}tail.nextZ=null;inSize*=2}while(numMerges>1);return list}function zOrder(x,y,minX,minY,size){x=32767*(x-minX)/size;y=32767*(y-minY)/size;x=(x|(x<<8))&0x00FF00FF;x=(x|(x<<4))&0x0F0F0F0F;x=(x|(x<<2))&0x33333333;x=(x|(x<<1))&0x55555555;y=(y|(y<<8))&0x00FF00FF;y=(y|(y<<4))&0x0F0F0F0F;y=(y|(y<<2))&0x33333333;y=(y|(y<<1))&0x55555555;return x|(y<<1)}function getLeftmost(start){var p=start,leftmost=start;do{if(p.x<leftmost.x)leftmost=p;p=p.next}while(p!==start);return leftmost}function pointInTriangle(ax,ay,bx,by,cx,cy,px,py){return(cx-px)*(ay-py)-(ax-px)*(cy-py)>=0&&(ax-px)*(by-py)-(bx-px)*(ay-py)>=0&&(bx-px)*(cy-py)-(cx-px)*(by-py)>=0}function isValidDiagonal(a,b){return a.next.i!==b.i&&a.prev.i!==b.i&&!intersectsPolygon(a,b)&&locallyInside(a,b)&&locallyInside(b,a)&&middleInside(a,b)}function area(p,q,r){return(q.y-p.y)*(r.x-q.x)-(q.x-p.x)*(r.y-q.y)}function equals(p1,p2){return p1.x===p2.x&&p1.y===p2.y}function intersects(p1,q1,p2,q2){if((equals(p1,q1)&&equals(p2,q2))||(equals(p1,q2)&&equals(p2,q1)))return true;return area(p1,q1,p2)>0!==area(p1,q1,q2)>0&&area(p2,q2,p1)>0!==area(p2,q2,q1)>0}function intersectsPolygon(a,b){var p=a;do{if(p.i!==a.i&&p.next.i!==a.i&&p.i!==b.i&&p.next.i!==b.i&&intersects(p,p.next,a,b))return true;p=p.next}while(p!==a);return false}function locallyInside(a,b){return area(a.prev,a,a.next)<0?area(a,b,a.next)>=0&&area(a,a.prev,b)>=0:area(a,b,a.prev)<0||area(a,a.next,b)<0}function middleInside(a,b){var p=a,inside=false,px=(a.x+b.x)/2,py=(a.y+b.y)/2;do{if(((p.y>py)!==(p.next.y>py))&&p.next.y!==p.y&&(px<(p.next.x-p.x)*(py-p.y)/(p.next.y-p.y)+p.x))inside=!inside;p=p.next}while(p!==a);return inside}function splitPolygon(a,b){var a2=new Node(a.i,a.x,a.y),b2=new Node(b.i,b.x,b.y),an=a.next,bp=b.prev;a.next=b;b.prev=a;a2.next=an;an.prev=a2;b2.next=a2;a2.prev=b2;bp.next=b2;b2.prev=bp;return b2}function insertNode(i,x,y,last){var p=new Node(i,x,y);if(!last){p.prev=p;p.next=p}else{p.next=last.next;p.prev=last;last.next.prev=p;last.next=p}return p}function removeNode(p){p.next.prev=p.prev;p.prev.next=p.next;if(p.prevZ)p.prevZ.nextZ=p.nextZ;if(p.nextZ)p.nextZ.prevZ=p.prevZ}function Node(i,x,y){this.i=i;this.x=x;this.y=y;this.prev=null;this.next=null;this.z=null;this.prevZ=null;this.nextZ=null;this.steiner=false}earcut.deviation=function(data,holeIndices,dim,triangles){var hasHoles=holeIndices&&holeIndices.length;var outerLen=hasHoles?holeIndices[0]*dim:data.length;var polygonArea=Math.abs(signedArea(data,0,outerLen,dim));if(hasHoles){for(var i=0,len=holeIndices.length;i<len;i++){var start=holeIndices[i]*dim;var end=i<len-1?holeIndices[i+1]*dim:data.length;polygonArea-=Math.abs(signedArea(data,start,end,dim))}}var trianglesArea=0;for(i=0;i<triangles.length;i+=3){var a=triangles[i]*dim;var b=triangles[i+1]*dim;var c=triangles[i+2]*dim;trianglesArea+=Math.abs((data[a]-data[c])*(data[b+1]-data[a+1])-(data[a]-data[b])*(data[c+1]-data[a+1]))}return polygonArea===0&&trianglesArea===0?0:Math.abs((trianglesArea-polygonArea)/polygonArea)};function signedArea(data,start,end,dim){var sum=0;for(var i=start,j=end-dim;i<end;i+=dim){sum+=(data[j]-data[i])*(data[i+1]+data[j+1]);j=i}return sum}earcut.flatten=function(data){var dim=data[0][0].length,result={vertices:[],holes:[],dimensions:dim},holeIndex=0;for(var i=0;i<data.length;i++){for(var j=0;j<data[i].length;j++){for(var d=0;d<dim;d++)result.vertices.push(data[i][j][d])}if(i>0){holeIndex+=data[i-1].length;result.holes.push(holeIndex)}}return result};

/*! Hammer.JS - v2.0.8 - 2016-04-23
 * http://hammerjs.github.io/
 * Copyright (c) 2016 Jorik Tangelder;
 * Licensed under the MIT license */
!function(a,b,c,d){"use strict";function e(a,b,c){return setTimeout(j(a,c),b)}function f(a,b,c){return Array.isArray(a)?(g(a,c[b],c),!0):!1}function g(a,b,c){var e;if(a)if(a.forEach)a.forEach(b,c);else if(a.length!==d)for(e=0;e<a.length;)b.call(c,a[e],e,a),e++;else for(e in a)a.hasOwnProperty(e)&&b.call(c,a[e],e,a)}function h(b,c,d){var e="DEPRECATED METHOD: "+c+"\n"+d+" AT \n";return function(){var c=new Error("get-stack-trace"),d=c&&c.stack?c.stack.replace(/^[^\(]+?[\n$]/gm,"").replace(/^\s+at\s+/gm,"").replace(/^Object.<anonymous>\s*\(/gm,"{anonymous}()@"):"Unknown Stack Trace",f=a.console&&(a.console.warn||a.console.log);return f&&f.call(a.console,e,d),b.apply(this,arguments)}}function i(a,b,c){var d,e=b.prototype;d=a.prototype=Object.create(e),d.constructor=a,d._super=e,c&&la(d,c)}function j(a,b){return function(){return a.apply(b,arguments)}}function k(a,b){return typeof a==oa?a.apply(b?b[0]||d:d,b):a}function l(a,b){return a===d?b:a}function m(a,b,c){g(q(b),function(b){a.addEventListener(b,c,!1)})}function n(a,b,c){g(q(b),function(b){a.removeEventListener(b,c,!1)})}function o(a,b){for(;a;){if(a==b)return!0;a=a.parentNode}return!1}function p(a,b){return a.indexOf(b)>-1}function q(a){return a.trim().split(/\s+/g)}function r(a,b,c){if(a.indexOf&&!c)return a.indexOf(b);for(var d=0;d<a.length;){if(c&&a[d][c]==b||!c&&a[d]===b)return d;d++}return-1}function s(a){return Array.prototype.slice.call(a,0)}function t(a,b,c){for(var d=[],e=[],f=0;f<a.length;){var g=b?a[f][b]:a[f];r(e,g)<0&&d.push(a[f]),e[f]=g,f++}return c&&(d=b?d.sort(function(a,c){return a[b]>c[b]}):d.sort()),d}function u(a,b){for(var c,e,f=b[0].toUpperCase()+b.slice(1),g=0;g<ma.length;){if(c=ma[g],e=c?c+f:b,e in a)return e;g++}return d}function v(){return ua++}function w(b){var c=b.ownerDocument||b;return c.defaultView||c.parentWindow||a}function x(a,b){var c=this;this.manager=a,this.callback=b,this.element=a.element,this.target=a.options.inputTarget,this.domHandler=function(b){k(a.options.enable,[a])&&c.handler(b)},this.init()}function y(a){var b,c=a.options.inputClass;return new(b=c?c:xa?M:ya?P:wa?R:L)(a,z)}function z(a,b,c){var d=c.pointers.length,e=c.changedPointers.length,f=b&Ea&&d-e===0,g=b&(Ga|Ha)&&d-e===0;c.isFirst=!!f,c.isFinal=!!g,f&&(a.session={}),c.eventType=b,A(a,c),a.emit("hammer.input",c),a.recognize(c),a.session.prevInput=c}function A(a,b){var c=a.session,d=b.pointers,e=d.length;c.firstInput||(c.firstInput=D(b)),e>1&&!c.firstMultiple?c.firstMultiple=D(b):1===e&&(c.firstMultiple=!1);var f=c.firstInput,g=c.firstMultiple,h=g?g.center:f.center,i=b.center=E(d);b.timeStamp=ra(),b.deltaTime=b.timeStamp-f.timeStamp,b.angle=I(h,i),b.distance=H(h,i),B(c,b),b.offsetDirection=G(b.deltaX,b.deltaY);var j=F(b.deltaTime,b.deltaX,b.deltaY);b.overallVelocityX=j.x,b.overallVelocityY=j.y,b.overallVelocity=qa(j.x)>qa(j.y)?j.x:j.y,b.scale=g?K(g.pointers,d):1,b.rotation=g?J(g.pointers,d):0,b.maxPointers=c.prevInput?b.pointers.length>c.prevInput.maxPointers?b.pointers.length:c.prevInput.maxPointers:b.pointers.length,C(c,b);var k=a.element;o(b.srcEvent.target,k)&&(k=b.srcEvent.target),b.target=k}function B(a,b){var c=b.center,d=a.offsetDelta||{},e=a.prevDelta||{},f=a.prevInput||{};b.eventType!==Ea&&f.eventType!==Ga||(e=a.prevDelta={x:f.deltaX||0,y:f.deltaY||0},d=a.offsetDelta={x:c.x,y:c.y}),b.deltaX=e.x+(c.x-d.x),b.deltaY=e.y+(c.y-d.y)}function C(a,b){var c,e,f,g,h=a.lastInterval||b,i=b.timeStamp-h.timeStamp;if(b.eventType!=Ha&&(i>Da||h.velocity===d)){var j=b.deltaX-h.deltaX,k=b.deltaY-h.deltaY,l=F(i,j,k);e=l.x,f=l.y,c=qa(l.x)>qa(l.y)?l.x:l.y,g=G(j,k),a.lastInterval=b}else c=h.velocity,e=h.velocityX,f=h.velocityY,g=h.direction;b.velocity=c,b.velocityX=e,b.velocityY=f,b.direction=g}function D(a){for(var b=[],c=0;c<a.pointers.length;)b[c]={clientX:pa(a.pointers[c].clientX),clientY:pa(a.pointers[c].clientY)},c++;return{timeStamp:ra(),pointers:b,center:E(b),deltaX:a.deltaX,deltaY:a.deltaY}}function E(a){var b=a.length;if(1===b)return{x:pa(a[0].clientX),y:pa(a[0].clientY)};for(var c=0,d=0,e=0;b>e;)c+=a[e].clientX,d+=a[e].clientY,e++;return{x:pa(c/b),y:pa(d/b)}}function F(a,b,c){return{x:b/a||0,y:c/a||0}}function G(a,b){return a===b?Ia:qa(a)>=qa(b)?0>a?Ja:Ka:0>b?La:Ma}function H(a,b,c){c||(c=Qa);var d=b[c[0]]-a[c[0]],e=b[c[1]]-a[c[1]];return Math.sqrt(d*d+e*e)}function I(a,b,c){c||(c=Qa);var d=b[c[0]]-a[c[0]],e=b[c[1]]-a[c[1]];return 180*Math.atan2(e,d)/Math.PI}function J(a,b){return I(b[1],b[0],Ra)+I(a[1],a[0],Ra)}function K(a,b){return H(b[0],b[1],Ra)/H(a[0],a[1],Ra)}function L(){this.evEl=Ta,this.evWin=Ua,this.pressed=!1,x.apply(this,arguments)}function M(){this.evEl=Xa,this.evWin=Ya,x.apply(this,arguments),this.store=this.manager.session.pointerEvents=[]}function N(){this.evTarget=$a,this.evWin=_a,this.started=!1,x.apply(this,arguments)}function O(a,b){var c=s(a.touches),d=s(a.changedTouches);return b&(Ga|Ha)&&(c=t(c.concat(d),"identifier",!0)),[c,d]}function P(){this.evTarget=bb,this.targetIds={},x.apply(this,arguments)}function Q(a,b){var c=s(a.touches),d=this.targetIds;if(b&(Ea|Fa)&&1===c.length)return d[c[0].identifier]=!0,[c,c];var e,f,g=s(a.changedTouches),h=[],i=this.target;if(f=c.filter(function(a){return o(a.target,i)}),b===Ea)for(e=0;e<f.length;)d[f[e].identifier]=!0,e++;for(e=0;e<g.length;)d[g[e].identifier]&&h.push(g[e]),b&(Ga|Ha)&&delete d[g[e].identifier],e++;return h.length?[t(f.concat(h),"identifier",!0),h]:void 0}function R(){x.apply(this,arguments);var a=j(this.handler,this);this.touch=new P(this.manager,a),this.mouse=new L(this.manager,a),this.primaryTouch=null,this.lastTouches=[]}function S(a,b){a&Ea?(this.primaryTouch=b.changedPointers[0].identifier,T.call(this,b)):a&(Ga|Ha)&&T.call(this,b)}function T(a){var b=a.changedPointers[0];if(b.identifier===this.primaryTouch){var c={x:b.clientX,y:b.clientY};this.lastTouches.push(c);var d=this.lastTouches,e=function(){var a=d.indexOf(c);a>-1&&d.splice(a,1)};setTimeout(e,cb)}}function U(a){for(var b=a.srcEvent.clientX,c=a.srcEvent.clientY,d=0;d<this.lastTouches.length;d++){var e=this.lastTouches[d],f=Math.abs(b-e.x),g=Math.abs(c-e.y);if(db>=f&&db>=g)return!0}return!1}function V(a,b){this.manager=a,this.set(b)}function W(a){if(p(a,jb))return jb;var b=p(a,kb),c=p(a,lb);return b&&c?jb:b||c?b?kb:lb:p(a,ib)?ib:hb}function X(){if(!fb)return!1;var b={},c=a.CSS&&a.CSS.supports;return["auto","manipulation","pan-y","pan-x","pan-x pan-y","none"].forEach(function(d){b[d]=c?a.CSS.supports("touch-action",d):!0}),b}function Y(a){this.options=la({},this.defaults,a||{}),this.id=v(),this.manager=null,this.options.enable=l(this.options.enable,!0),this.state=nb,this.simultaneous={},this.requireFail=[]}function Z(a){return a&sb?"cancel":a&qb?"end":a&pb?"move":a&ob?"start":""}function $(a){return a==Ma?"down":a==La?"up":a==Ja?"left":a==Ka?"right":""}function _(a,b){var c=b.manager;return c?c.get(a):a}function aa(){Y.apply(this,arguments)}function ba(){aa.apply(this,arguments),this.pX=null,this.pY=null}function ca(){aa.apply(this,arguments)}function da(){Y.apply(this,arguments),this._timer=null,this._input=null}function ea(){aa.apply(this,arguments)}function fa(){aa.apply(this,arguments)}function ga(){Y.apply(this,arguments),this.pTime=!1,this.pCenter=!1,this._timer=null,this._input=null,this.count=0}function ha(a,b){return b=b||{},b.recognizers=l(b.recognizers,ha.defaults.preset),new ia(a,b)}function ia(a,b){this.options=la({},ha.defaults,b||{}),this.options.inputTarget=this.options.inputTarget||a,this.handlers={},this.session={},this.recognizers=[],this.oldCssProps={},this.element=a,this.input=y(this),this.touchAction=new V(this,this.options.touchAction),ja(this,!0),g(this.options.recognizers,function(a){var b=this.add(new a[0](a[1]));a[2]&&b.recognizeWith(a[2]),a[3]&&b.requireFailure(a[3])},this)}function ja(a,b){var c=a.element;if(c.style){var d;g(a.options.cssProps,function(e,f){d=u(c.style,f),b?(a.oldCssProps[d]=c.style[d],c.style[d]=e):c.style[d]=a.oldCssProps[d]||""}),b||(a.oldCssProps={})}}function ka(a,c){var d=b.createEvent("Event");d.initEvent(a,!0,!0),d.gesture=c,c.target.dispatchEvent(d)}var la,ma=["","webkit","Moz","MS","ms","o"],na=b.createElement("div"),oa="function",pa=Math.round,qa=Math.abs,ra=Date.now;la="function"!=typeof Object.assign?function(a){if(a===d||null===a)throw new TypeError("Cannot convert undefined or null to object");for(var b=Object(a),c=1;c<arguments.length;c++){var e=arguments[c];if(e!==d&&null!==e)for(var f in e)e.hasOwnProperty(f)&&(b[f]=e[f])}return b}:Object.assign;var sa=h(function(a,b,c){for(var e=Object.keys(b),f=0;f<e.length;)(!c||c&&a[e[f]]===d)&&(a[e[f]]=b[e[f]]),f++;return a},"extend","Use `assign`."),ta=h(function(a,b){return sa(a,b,!0)},"merge","Use `assign`."),ua=1,va=/mobile|tablet|ip(ad|hone|od)|android/i,wa="ontouchstart"in a,xa=u(a,"PointerEvent")!==d,ya=wa&&va.test(navigator.userAgent),za="touch",Aa="pen",Ba="mouse",Ca="kinect",Da=25,Ea=1,Fa=2,Ga=4,Ha=8,Ia=1,Ja=2,Ka=4,La=8,Ma=16,Na=Ja|Ka,Oa=La|Ma,Pa=Na|Oa,Qa=["x","y"],Ra=["clientX","clientY"];x.prototype={handler:function(){},init:function(){this.evEl&&m(this.element,this.evEl,this.domHandler),this.evTarget&&m(this.target,this.evTarget,this.domHandler),this.evWin&&m(w(this.element),this.evWin,this.domHandler)},destroy:function(){this.evEl&&n(this.element,this.evEl,this.domHandler),this.evTarget&&n(this.target,this.evTarget,this.domHandler),this.evWin&&n(w(this.element),this.evWin,this.domHandler)}};var Sa={mousedown:Ea,mousemove:Fa,mouseup:Ga},Ta="mousedown",Ua="mousemove mouseup";i(L,x,{handler:function(a){var b=Sa[a.type];b&Ea&&0===a.button&&(this.pressed=!0),b&Fa&&1!==a.which&&(b=Ga),this.pressed&&(b&Ga&&(this.pressed=!1),this.callback(this.manager,b,{pointers:[a],changedPointers:[a],pointerType:Ba,srcEvent:a}))}});var Va={pointerdown:Ea,pointermove:Fa,pointerup:Ga,pointercancel:Ha,pointerout:Ha},Wa={2:za,3:Aa,4:Ba,5:Ca},Xa="pointerdown",Ya="pointermove pointerup pointercancel";a.MSPointerEvent&&!a.PointerEvent&&(Xa="MSPointerDown",Ya="MSPointerMove MSPointerUp MSPointerCancel"),i(M,x,{handler:function(a){var b=this.store,c=!1,d=a.type.toLowerCase().replace("ms",""),e=Va[d],f=Wa[a.pointerType]||a.pointerType,g=f==za,h=r(b,a.pointerId,"pointerId");e&Ea&&(0===a.button||g)?0>h&&(b.push(a),h=b.length-1):e&(Ga|Ha)&&(c=!0),0>h||(b[h]=a,this.callback(this.manager,e,{pointers:b,changedPointers:[a],pointerType:f,srcEvent:a}),c&&b.splice(h,1))}});var Za={touchstart:Ea,touchmove:Fa,touchend:Ga,touchcancel:Ha},$a="touchstart",_a="touchstart touchmove touchend touchcancel";i(N,x,{handler:function(a){var b=Za[a.type];if(b===Ea&&(this.started=!0),this.started){var c=O.call(this,a,b);b&(Ga|Ha)&&c[0].length-c[1].length===0&&(this.started=!1),this.callback(this.manager,b,{pointers:c[0],changedPointers:c[1],pointerType:za,srcEvent:a})}}});var ab={touchstart:Ea,touchmove:Fa,touchend:Ga,touchcancel:Ha},bb="touchstart touchmove touchend touchcancel";i(P,x,{handler:function(a){var b=ab[a.type],c=Q.call(this,a,b);c&&this.callback(this.manager,b,{pointers:c[0],changedPointers:c[1],pointerType:za,srcEvent:a})}});var cb=2500,db=25;i(R,x,{handler:function(a,b,c){var d=c.pointerType==za,e=c.pointerType==Ba;if(!(e&&c.sourceCapabilities&&c.sourceCapabilities.firesTouchEvents)){if(d)S.call(this,b,c);else if(e&&U.call(this,c))return;this.callback(a,b,c)}},destroy:function(){this.touch.destroy(),this.mouse.destroy()}});var eb=u(na.style,"touchAction"),fb=eb!==d,gb="compute",hb="auto",ib="manipulation",jb="none",kb="pan-x",lb="pan-y",mb=X();V.prototype={set:function(a){a==gb&&(a=this.compute()),fb&&this.manager.element.style&&mb[a]&&(this.manager.element.style[eb]=a),this.actions=a.toLowerCase().trim()},update:function(){this.set(this.manager.options.touchAction)},compute:function(){var a=[];return g(this.manager.recognizers,function(b){k(b.options.enable,[b])&&(a=a.concat(b.getTouchAction()))}),W(a.join(" "))},preventDefaults:function(a){var b=a.srcEvent,c=a.offsetDirection;if(this.manager.session.prevented)return void b.preventDefault();var d=this.actions,e=p(d,jb)&&!mb[jb],f=p(d,lb)&&!mb[lb],g=p(d,kb)&&!mb[kb];if(e){var h=1===a.pointers.length,i=a.distance<2,j=a.deltaTime<250;if(h&&i&&j)return}return g&&f?void 0:e||f&&c&Na||g&&c&Oa?this.preventSrc(b):void 0},preventSrc:function(a){this.manager.session.prevented=!0,a.preventDefault()}};var nb=1,ob=2,pb=4,qb=8,rb=qb,sb=16,tb=32;Y.prototype={defaults:{},set:function(a){return la(this.options,a),this.manager&&this.manager.touchAction.update(),this},recognizeWith:function(a){if(f(a,"recognizeWith",this))return this;var b=this.simultaneous;return a=_(a,this),b[a.id]||(b[a.id]=a,a.recognizeWith(this)),this},dropRecognizeWith:function(a){return f(a,"dropRecognizeWith",this)?this:(a=_(a,this),delete this.simultaneous[a.id],this)},requireFailure:function(a){if(f(a,"requireFailure",this))return this;var b=this.requireFail;return a=_(a,this),-1===r(b,a)&&(b.push(a),a.requireFailure(this)),this},dropRequireFailure:function(a){if(f(a,"dropRequireFailure",this))return this;a=_(a,this);var b=r(this.requireFail,a);return b>-1&&this.requireFail.splice(b,1),this},hasRequireFailures:function(){return this.requireFail.length>0},canRecognizeWith:function(a){return!!this.simultaneous[a.id]},emit:function(a){function b(b){c.manager.emit(b,a)}var c=this,d=this.state;qb>d&&b(c.options.event+Z(d)),b(c.options.event),a.additionalEvent&&b(a.additionalEvent),d>=qb&&b(c.options.event+Z(d))},tryEmit:function(a){return this.canEmit()?this.emit(a):void(this.state=tb)},canEmit:function(){for(var a=0;a<this.requireFail.length;){if(!(this.requireFail[a].state&(tb|nb)))return!1;a++}return!0},recognize:function(a){var b=la({},a);return k(this.options.enable,[this,b])?(this.state&(rb|sb|tb)&&(this.state=nb),this.state=this.process(b),void(this.state&(ob|pb|qb|sb)&&this.tryEmit(b))):(this.reset(),void(this.state=tb))},process:function(a){},getTouchAction:function(){},reset:function(){}},i(aa,Y,{defaults:{pointers:1},attrTest:function(a){var b=this.options.pointers;return 0===b||a.pointers.length===b},process:function(a){var b=this.state,c=a.eventType,d=b&(ob|pb),e=this.attrTest(a);return d&&(c&Ha||!e)?b|sb:d||e?c&Ga?b|qb:b&ob?b|pb:ob:tb}}),i(ba,aa,{defaults:{event:"pan",threshold:10,pointers:1,direction:Pa},getTouchAction:function(){var a=this.options.direction,b=[];return a&Na&&b.push(lb),a&Oa&&b.push(kb),b},directionTest:function(a){var b=this.options,c=!0,d=a.distance,e=a.direction,f=a.deltaX,g=a.deltaY;return e&b.direction||(b.direction&Na?(e=0===f?Ia:0>f?Ja:Ka,c=f!=this.pX,d=Math.abs(a.deltaX)):(e=0===g?Ia:0>g?La:Ma,c=g!=this.pY,d=Math.abs(a.deltaY))),a.direction=e,c&&d>b.threshold&&e&b.direction},attrTest:function(a){return aa.prototype.attrTest.call(this,a)&&(this.state&ob||!(this.state&ob)&&this.directionTest(a))},emit:function(a){this.pX=a.deltaX,this.pY=a.deltaY;var b=$(a.direction);b&&(a.additionalEvent=this.options.event+b),this._super.emit.call(this,a)}}),i(ca,aa,{defaults:{event:"pinch",threshold:0,pointers:2},getTouchAction:function(){return[jb]},attrTest:function(a){return this._super.attrTest.call(this,a)&&(Math.abs(a.scale-1)>this.options.threshold||this.state&ob)},emit:function(a){if(1!==a.scale){var b=a.scale<1?"in":"out";a.additionalEvent=this.options.event+b}this._super.emit.call(this,a)}}),i(da,Y,{defaults:{event:"press",pointers:1,time:251,threshold:9},getTouchAction:function(){return[hb]},process:function(a){var b=this.options,c=a.pointers.length===b.pointers,d=a.distance<b.threshold,f=a.deltaTime>b.time;if(this._input=a,!d||!c||a.eventType&(Ga|Ha)&&!f)this.reset();else if(a.eventType&Ea)this.reset(),this._timer=e(function(){this.state=rb,this.tryEmit()},b.time,this);else if(a.eventType&Ga)return rb;return tb},reset:function(){clearTimeout(this._timer)},emit:function(a){this.state===rb&&(a&&a.eventType&Ga?this.manager.emit(this.options.event+"up",a):(this._input.timeStamp=ra(),this.manager.emit(this.options.event,this._input)))}}),i(ea,aa,{defaults:{event:"rotate",threshold:0,pointers:2},getTouchAction:function(){return[jb]},attrTest:function(a){return this._super.attrTest.call(this,a)&&(Math.abs(a.rotation)>this.options.threshold||this.state&ob)}}),i(fa,aa,{defaults:{event:"swipe",threshold:10,velocity:.3,direction:Na|Oa,pointers:1},getTouchAction:function(){return ba.prototype.getTouchAction.call(this)},attrTest:function(a){var b,c=this.options.direction;return c&(Na|Oa)?b=a.overallVelocity:c&Na?b=a.overallVelocityX:c&Oa&&(b=a.overallVelocityY),this._super.attrTest.call(this,a)&&c&a.offsetDirection&&a.distance>this.options.threshold&&a.maxPointers==this.options.pointers&&qa(b)>this.options.velocity&&a.eventType&Ga},emit:function(a){var b=$(a.offsetDirection);b&&this.manager.emit(this.options.event+b,a),this.manager.emit(this.options.event,a)}}),i(ga,Y,{defaults:{event:"tap",pointers:1,taps:1,interval:300,time:250,threshold:9,posThreshold:10},getTouchAction:function(){return[ib]},process:function(a){var b=this.options,c=a.pointers.length===b.pointers,d=a.distance<b.threshold,f=a.deltaTime<b.time;if(this.reset(),a.eventType&Ea&&0===this.count)return this.failTimeout();if(d&&f&&c){if(a.eventType!=Ga)return this.failTimeout();var g=this.pTime?a.timeStamp-this.pTime<b.interval:!0,h=!this.pCenter||H(this.pCenter,a.center)<b.posThreshold;this.pTime=a.timeStamp,this.pCenter=a.center,h&&g?this.count+=1:this.count=1,this._input=a;var i=this.count%b.taps;if(0===i)return this.hasRequireFailures()?(this._timer=e(function(){this.state=rb,this.tryEmit()},b.interval,this),ob):rb}return tb},failTimeout:function(){return this._timer=e(function(){this.state=tb},this.options.interval,this),tb},reset:function(){clearTimeout(this._timer)},emit:function(){this.state==rb&&(this._input.tapCount=this.count,this.manager.emit(this.options.event,this._input))}}),ha.VERSION="2.0.8",ha.defaults={domEvents:!1,touchAction:gb,enable:!0,inputTarget:null,inputClass:null,preset:[[ea,{enable:!1}],[ca,{enable:!1},["rotate"]],[fa,{direction:Na}],[ba,{direction:Na},["swipe"]],[ga],[ga,{event:"doubletap",taps:2},["tap"]],[da]],cssProps:{userSelect:"none",touchSelect:"none",touchCallout:"none",contentZooming:"none",userDrag:"none",tapHighlightColor:"rgba(0,0,0,0)"}};var ub=1,vb=2;ia.prototype={set:function(a){return la(this.options,a),a.touchAction&&this.touchAction.update(),a.inputTarget&&(this.input.destroy(),this.input.target=a.inputTarget,this.input.init()),this},stop:function(a){this.session.stopped=a?vb:ub},recognize:function(a){var b=this.session;if(!b.stopped){this.touchAction.preventDefaults(a);var c,d=this.recognizers,e=b.curRecognizer;(!e||e&&e.state&rb)&&(e=b.curRecognizer=null);for(var f=0;f<d.length;)c=d[f],b.stopped===vb||e&&c!=e&&!c.canRecognizeWith(e)?c.reset():c.recognize(a),!e&&c.state&(ob|pb|qb)&&(e=b.curRecognizer=c),f++}},get:function(a){if(a instanceof Y)return a;for(var b=this.recognizers,c=0;c<b.length;c++)if(b[c].options.event==a)return b[c];return null},add:function(a){if(f(a,"add",this))return this;var b=this.get(a.options.event);return b&&this.remove(b),this.recognizers.push(a),a.manager=this,this.touchAction.update(),a},remove:function(a){if(f(a,"remove",this))return this;if(a=this.get(a)){var b=this.recognizers,c=r(b,a);-1!==c&&(b.splice(c,1),this.touchAction.update())}return this},on:function(a,b){if(a!==d&&b!==d){var c=this.handlers;return g(q(a),function(a){c[a]=c[a]||[],c[a].push(b)}),this}},off:function(a,b){if(a!==d){var c=this.handlers;return g(q(a),function(a){b?c[a]&&c[a].splice(r(c[a],b),1):delete c[a]}),this}},emit:function(a,b){this.options.domEvents&&ka(a,b);var c=this.handlers[a]&&this.handlers[a].slice();if(c&&c.length){b.type=a,b.preventDefault=function(){b.srcEvent.preventDefault()};for(var d=0;d<c.length;)c[d](b),d++}},destroy:function(){this.element&&ja(this,!1),this.handlers={},this.session={},this.input.destroy(),this.element=null}},la(ha,{INPUT_START:Ea,INPUT_MOVE:Fa,INPUT_END:Ga,INPUT_CANCEL:Ha,STATE_POSSIBLE:nb,STATE_BEGAN:ob,STATE_CHANGED:pb,STATE_ENDED:qb,STATE_RECOGNIZED:rb,STATE_CANCELLED:sb,STATE_FAILED:tb,DIRECTION_NONE:Ia,DIRECTION_LEFT:Ja,DIRECTION_RIGHT:Ka,DIRECTION_UP:La,DIRECTION_DOWN:Ma,DIRECTION_HORIZONTAL:Na,DIRECTION_VERTICAL:Oa,DIRECTION_ALL:Pa,Manager:ia,Input:x,TouchAction:V,TouchInput:P,MouseInput:L,PointerEventInput:M,TouchMouseInput:R,SingleTouchInput:N,Recognizer:Y,AttrRecognizer:aa,Tap:ga,Pan:ba,Swipe:fa,Pinch:ca,Rotate:ea,Press:da,on:m,off:n,each:g,merge:ta,extend:sa,assign:la,inherit:i,bindFn:j,prefixed:u});var wb="undefined"!=typeof a?a:"undefined"!=typeof self?self:{};wb.Hammer=ha,"function"==typeof define&&define.amd?define(function(){return ha}):"undefined"!=typeof module&&module.exports?module.exports=ha:a[c]=ha}(window,document,"Hammer");


function DocexInput(reader)
{
	this.reader = reader;
	this.down = [0, 0];
	this.isDown = false;
	this.isDrag = false;
	this.lastMove = [0, 0];
	this.listeners = [];
	this.lastScale = -1;
	this.waitForNextPinch = false;
}

DocexInput.prototype.htmlMouseDown = function(x, y)
{
	var pos = this.reader.layout.container.getBoundingClientRect();
	x -= pos.left;
	y -= pos.top;
	this.down = [x, y];
	this.isDown = true;
	if (this.isDrag)
		this.notifyDropped(x, y);
	this.isDrag = false;
}

DocexInput.prototype.htmlMouseUp = function()
{
	if (!this.isDown && !this.isDrag)
		return;
	var pos = this.reader.layout.container.getBoundingClientRect();
	var x = this.lastMove[0]-pos.left;
	var y = this.lastMove[1]-pos.top;
	if (this.isDrag)
		this.notifyDropped(x, y);
	else this.notifyClicked(this.down[0], this.down[1]);
	this.isDown = false;
	this.isDrag = false;
}

DocexInput.prototype.htmlMouseMove = function(x, y)
{
	if (x == this.lastMove[0] && y == this.lastMove[1])
		return;
	this.lastMove = [x, y];
	
	if (!this.isDown)
		return;
	var pos = this.reader.layout.container.getBoundingClientRect();
	x -= pos.left;
	y -= pos.top;
	if (!this.isDrag && (x-this.down[0])*(x-this.down[0])+(y-this.down[1])*(y-this.down[1]) > 3*3)
	{
		this.isDrag = true;
		this.notifyGrabbed(this.down[0], this.down[1]);
	}
	if (this.isDrag)
		this.notifyDragged(x, y);
}

DocexInput.prototype.htmlMouseOut = function(x, y)
{
	//if (DocexInput.isDown)
	//	DocexInput.htmlMouseUp();
}

DocexInput.prototype.notifyClicked = function(x, y)
{
	for (var i=0;i<this.listeners.length;i++)
		if (this.listeners[i].onClick != undefined)
			this.listeners[i].onClick(x, y);
}
DocexInput.prototype.notifyGrabbed = function(x, y)
{
	for (var i=0;i<this.listeners.length;i++)
		if (this.listeners[i].onGrab != undefined)
			this.listeners[i].onGrab(x, y);
}
DocexInput.prototype.notifyDragged = function(x, y)
{
	for (var i=0;i<this.listeners.length;i++)
		if (this.listeners[i].onDrag != undefined)
			this.listeners[i].onDrag(x, y);
}
DocexInput.prototype.notifyDropped = function(x, y)
{
	for (var i=0;i<this.listeners.length;i++)
		if (this.listeners[i].onDrop != undefined)
			this.listeners[i].onDrop(x, y);
}

DocexInput.prototype.pinch = function(scale)
{
	if (this.waitForNextPinch)
		return;
	if (this.lastScale < 0)
		this.lastScale = 1;
	var amount = this.lastScale-scale;
	if (amount > 0)
		amount *= 4;
	this.reader.zoomBy(amount);
	this.lastScale = scale;
}
DocexInput.prototype.pinchEnd = function()
{
	this.lastScale = -1;
	this.waitForNextPinch = false;
}

function DocexSpec(basePath, xml)
{
	this.pages = [];
	this.leftSide = null;
	this.rightSide = null;

	this.pageHeight = 2;
	
	this.roiMaterial = new THREE.MeshBasicMaterial({color: 0xffffff});
	this.roiMaterial.depthTest = false;
	this.roiMaterial.transparent = true;
	this.roiMaterial.opacity = 0;
	this.roiOutlineMaterial = new THREE.MeshBasicMaterial({color: 0xff0000});
	this.roiOutlineMaterial.depthTest = false;
	this.roiOutlineMaterial.transparent = true;
	this.roiOutlineMaterial.opacity = 0;
	this.roiSelectedMaterial = new THREE.MeshBasicMaterial({color: 0xffffff});
	this.roiSelectedMaterial.depthTest = false;
	this.roiSelectedMaterial.transparent = true;
	this.roiSelectedMaterial.opacity = 0;
	this.roiSelectedOutlineMaterial = new THREE.MeshBasicMaterial({color: 0x2E4EFD});
	this.roiSelectedOutlineMaterial.depthTest = false;
	this.roiSelectedOutlineMaterial.transparent = true;
	this.roiSelectedOutlineMaterial.opacity = 1;
	var canvas = document.createElement("CANVAS");
	canvas.width = 512;
	canvas.height = 512;
	var g = canvas.getContext("2d", {alpha: false});
	g.fillStyle = "#ffffff";
	g.fillRect(0, 0, 512, 512);
	g.font = "42px Arial";
	g.textAlign = "center";
	g.textBaseline = "middle";
	g.fillStyle = "#2E4EFD";
	g.fillText("LOADING", 128, 128);
	g.fillText("LOADING", 384, 128);
	g.fillText("LOADING", 128, 384);
	g.fillText("LOADING", 384, 384);
	this.loadingTex = new THREE.Texture(canvas);
	this.loadingTex.needsUpdate = true;
	
	var start = xml.indexOf("<Book path=\"");
	start += 12;
	var end = xml.indexOf("\"", start);
	this.path = basePath+xml.substring(start, end);
	//document.getElementById('poweredBy').innerHTML = this.path;
	
	start = xml.indexOf("aspectRatio=\"");
	start += 13;
	end = xml.indexOf("\"", start);
	this.aspect = xml.substring(start, end);
	
	start = xml.indexOf("cover=\"");
	start += 7;
	end = xml.indexOf("\"", start);
	this.cover = this.path+xml.substring(start, end);
	
	start = xml.indexOf("innerCover=\"");
	start += 12;
	end = xml.indexOf("\"", start);
	this.innerCover = this.path+xml.substring(start, end);
	
	start = xml.indexOf("leftSide=\"");
	if (start >= 0)
	{
		start += 10;
		end = xml.indexOf("\"", start);
		this.leftSide = this.path+xml.substring(start, end);
	}
	start = xml.indexOf("rightSide=\"");
	if (start >= 0)
	{
		start += 11;
		end = xml.indexOf("\"", start);
		this.rightSide = this.path+xml.substring(start, end);
	}
	
	this.name = "";
	start = xml.indexOf("<Name>");
	if (start > -1)
	{
		end = xml.indexOf("</Name>")
		if (end > start)
			this.name = xml.substring(start+6, end);
	}
	
	this.desc = "";
	start = xml.indexOf("<Description>");
	if (start > -1)
	{
		end = xml.indexOf("</Description>")
		if (end > start)
			this.desc = xml.substring(start+13, end);
	}
	
	while (true)
	{
		start = xml.indexOf("<Page");
		if (start == -1)
			break;
		end = xml.indexOf("</Page>")
		this.pages[this.pages.length] = this.buildPage(xml.substring(start, end+7), this.pages.length);
		xml = xml.substring(end+7, xml.length);
	}
	if (this.pages.length%2 == 1)
		this.pages[this.pages.length] = this.buildEmptyPage(this.pages.length);
}

DocexSpec.prototype.buildPage = function(xml, index)
{
	var page = {};
	
	var start = xml.indexOf("src=\"");
	var end = xml.indexOf("\"", start+5);
	page.path = this.path+xml.substring(start+5, end);
	start = xml.indexOf("tsrc=\"");
	if (start > -1)
	{
		end = xml.indexOf("\"", start+6);
		page.tpath = this.path+xml.substring(start+6, end);
	}
	else page.tpath = null;
	page.regions = [];
	page.index = index;
	
	while (true)
	{
		start = xml.indexOf("<RegionOfInterest");
		if (start == -1)
			break;
		end = xml.indexOf("</RegionOfInterest>")
		page.regions[page.regions.length] = this.buildRegion(xml.substring(start, end+19), index);
		xml = xml.substring(end+19, xml.length);
	}
	
	return page;
}

DocexSpec.prototype.buildEmptyPage = function(index)
{
	var page = {};
	
	page.path = null;
	page.regions = [];
	page.index = index;
	return page;
}

DocexSpec.prototype.buildRegion = function(xml, index)
{
	var region = {};
	region.infos = [];
	region.bounds = [];
	region.index = index;
	
	var start = xml.indexOf("region=\"");
	var end = xml.indexOf("\"", start+8);
	var coords = xml.substring(start+8, end).split(", ");
	region.coords = [];
	for (var i=0;i<coords.length/2;i++)
		region.coords[i] = [parseFloat(coords[2*i]), parseFloat(coords[2*i+1])];
	
	while (true)
	{
		start = xml.indexOf("<Info");
		if (start == -1)
			break;
		var end1 = xml.indexOf("</Info>");
		var end2 = xml.indexOf("/>");
		if (end1 >= 0 && (end2 < 0 || end1 < end2))
		{
			end = end1;
			region.infos.push(this.buildInfo(xml.substring(start, end+7)));
			xml = xml.substring(end+7, xml.length);
		}
		else
		{
			end = end2;
			region.infos.push(this.buildInfo(xml.substring(start, end+2)));
			xml = xml.substring(end+2, xml.length);
		}
		//alert(region.infos[region.infos.length-1]);
	}
	
	return region;
}

function docexReplaceAll(find, replace, str)
{
    return str.replace(new RegExp(find, 'g'), replace);
}
DocexSpec.prototype.buildInfo = function(infoXml)
{
	var start = infoXml.indexOf("type=\"");
	var end = infoXml.indexOf("\"", start+6);
	var type = infoXml.substring(start+6, end);
	if (type == "text")
		return docexReplaceAll("\n", "&lt;br/>", infoXml.substring(infoXml.indexOf(">")+1, infoXml.indexOf("</Info>")));
	else if (type == "image")
	{
		start = infoXml.indexOf("src=\"");
		end = infoXml.indexOf("\"", start+5);
		return "&lt;img src=\""+this.path+infoXml.substring(start+5, end)+"\" width=100% />";
	}
	else if (type == "media")
	{
		start = infoXml.indexOf("src=\"");
		end = infoXml.indexOf("\"", start+5);
		return "&lt;video src=\""+this.path+infoXml.substring(start+5, end)+"\" width=100% controls />";
	}
	return "";
}

var DocexRegion = {};

DocexRegion.getRegionAt = function(reader, x, y)
{
	var d = reader.camera.toWorldRay(x, y);
	var p = new THREE.Vector3();
	p.copy(reader.camera.camera.position);
	p.sub(reader.bookModel.model.position);
	
	reader.bookModel.model.worldToLocal(p);
	reader.bookModel.model.worldToLocal(d);
	
	var coords = reader.bookModel.toPage(p, d);
	if (coords === null)
		return null;
	var page = coords[0] && reader.leftPageIndex >= 0 ? reader.spec.pages[reader.leftPageIndex] : !coords[0] && reader.rightPageIndex >= 0 ? reader.spec.pages[reader.rightPageIndex] : null;
	if (page === null)
		return null;
	for (var i=0;i<page.regions.length;i++)
		if (DocexRegion.contains(page.regions[i], coords[1][0], coords[1][1]))
			return page.regions[i];
	return null;
}

DocexRegion.contains = function(region, x, y)
{
	var n = 0;
	for (var i=0;i<region.coords.length;i++)
		if (DocexRegion.intersects(0, -100, x, y, region.coords[i][0], region.coords[i][1], 
			region.coords[i==region.coords.length-1 ? 0 : i+1][0], region.coords[i==region.coords.length-1 ? 0 : i+1][1]))
				n++;
	return n%2 == 1;
}

DocexRegion.intersects = function(p1x1, p1y1, p1x2, p1y2, p2x1, p2y1, p2x2, p2y2)
{
	var ux = p1x2-p1x1;
	var uy = p1y2-p1y1;
	var vx = p2x2-p2x1;
	var vy = p2y2-p2y1;
	
	var k = ((p2x1*vy-p2y1*vx)-(p1x1*vy-p1y1*vx))/(ux*vy-uy*vx);
	if (k < 0 || k > 1)
		return false;
	
	k = ((p1x1*uy-p1y1*ux)-(p2x1*uy-p2y1*ux))/(vx*uy-vy*ux);
	return k>=0 && k<=1;
}

DocexRegion.scaleInfo = function(html, scale)
{
	if (scale > 1)
	{
		var index = 0;
		while (true)
		{
			index = html.indexOf("&lt;div style=\"", index);
			if (index < 0)
				break;
			
			var end = html.indexOf("\"", index+"&lt;div style=\"".length);
			var div = html.substring(index, end);
					
			var tok = div.indexOf("margin-left:");
			if (tok > 0)
			{
				var tokend = div.indexOf("px", tok);
				div = div.substring(0, tok+"margin-left:".length)+Math.floor(scale*parseInt(div.substring(tok+"margin-left:".length, tokend)))+div.substring(tokend);
			}
			tok = div.indexOf("margin-right:");
			if (tok > 0)
			{
				var tokend = div.indexOf("px", tok);
				div = div.substring(0, tok+"margin-right:".length)+Math.floor(scale*parseInt(div.substring(tok+"margin-right:".length, tokend)))+div.substring(tokend);
			}
			tok = div.indexOf("font-size:");
			if (tok > 0)
			{
				var tokend = div.indexOf("px", tok);
				div = div.substring(0, tok+"font-size:".length)+Math.floor(scale*parseInt(div.substring(tok+"font-size:".length, tokend)))+div.substring(tokend);
			}
			html = html.substring(0, index)+div+html.substring(end);
			index = end;
		}
	}
	return html;
}

DocexRegion.buildRegionMesh = function(reader, region)
{
	var coords = region.coords;
	region.worldCoords = [];
	var minx = null, miny = null, maxx = null, maxy = null;
	var outline = [];
	for (var i=0;i<region.coords.length;i++)
	{
		//region.worldCoords[i] = region.index%2 == 1 ? DocexCamera.fromLeftPageCoords(region.coords[i]) : DocexCamera.fromRightPageCoords(region.coords[i]);
		region.worldCoords[i] = new THREE.Vector3();
		reader.bookModel.fromPageToWorld(region.coords[i][0], region.coords[i][1], region.index%2 == 1, region.worldCoords[i]);
		if (minx == null || region.worldCoords[i].x < minx) minx = region.worldCoords[i].x;
		if (maxx == null || region.worldCoords[i].x > maxx) maxx = region.worldCoords[i].x;
		if (miny == null || region.worldCoords[i].y < miny) miny = region.worldCoords[i].y;
		if (maxy == null || region.worldCoords[i].y > maxy) maxy = region.worldCoords[i].y;
		outline.push(region.worldCoords[i].x);
		outline.push(region.worldCoords[i].y);
		outline.push(region.worldCoords[i].z);
	}
	region.bounds[0] = [minx, miny];
	region.bounds[1] = [maxx, maxy];
	
	var triangles = earcut(outline, null, 3);
	var geom = new THREE.Geometry();
	for (var i=0;i<region.worldCoords.length;i++)
		geom.vertices.push(new THREE.Vector3(region.worldCoords[i].x, region.worldCoords[i].y, region.worldCoords[i].z));
	for (var i=0;i<triangles.length/3;i++)
		geom.faces.push(new THREE.Face3(triangles[3*i], triangles[3*i+1], triangles[3*i+2]));
	region.mesh = new THREE.Mesh(geom, reader.spec.roiMaterial);
	region.mesh.renderOrder = 200;
	region.outline = new THREE.Line(geom, reader.spec.roiOutlineMaterial);
	region.outline.renderOrder = 200;
}

function DocexCamera(reader)
{
	this.reader = reader;
	this.camera = {};
	this.camTan = 0;
	this.aspect = 0;
	this.defaultPos = [0, 0, 0];
	this.attractorPos = [0, 0, 0];
	this.maxUnzoom = 6;
	this.defaultFov = 23;
	this.activity = 0;
}

DocexCamera.prototype.init = function(x, y, z)
{
	var near = 0.01, far = 100;
	
	this.aspect = this.reader.width/this.reader.height;
	this.camera = new THREE.PerspectiveCamera(this.defaultFov, this.aspect, near, far);
	
	this.defaultPos = [x, y, z];
	this.attractorPos = [x, y, z];
	this.camera.position.x = x;
	this.camera.position.y = y;
	this.camera.position.z = z;
	
	this.camTan = Math.tan(.5*Math.PI*this.camera.fov/180);
}

DocexCamera.prototype.refreshDimensions = function(z)
{
	this.aspect = this.reader.width/this.reader.height;
	this.camera.aspect = this.aspect;
	if (this.attractorPos[2] == this.defaultPos[2])
		this.attractorPos[2] = z;
	this.defaultPos[2] = z;
	this.camera.updateMatrix();
	this.camera.updateProjectionMatrix();
	this.camera.matrixWorldNeedsUpdate = true;
}

DocexCamera.prototype.update = function()
{
	var spring = .1;
	var ox = this.camera.position.x, oy = this.camera.position.y, oz = this.camera.position.z;
	this.camera.position.x += spring*(this.attractorPos[0]-this.camera.position.x);
	this.camera.position.y += spring*(this.attractorPos[1]-this.camera.position.y);
	this.camera.position.z += spring*(this.attractorPos[2]-this.camera.position.z);
	this.activity = (ox-this.camera.position.x)*(ox-this.camera.position.x)+
		(oy-this.camera.position.y)*(oy-this.camera.position.y)+
		(oz-this.camera.position.z)*(oz-this.camera.position.z);
}

DocexCamera.prototype.unzoomed = function()
{
	return this.camera.position.z > this.maxUnzoom-.02;
}

DocexCamera.prototype.translate = function(x, y)
{
	this.attractorPos[0] += x*this.camera.position.z;
	this.attractorPos[1] += y*this.camera.position.z;
}
DocexCamera.prototype.setPos = function(x, y, z)
{
	z = z > this.maxUnzoom ? this.maxUnzoom : z < 2 ? 2 : z;
	this.attractorPos = [x, y, z];
}
DocexCamera.prototype.setDiffPos = function(x, y, z)
{
	this.setPos(this.attractorPos[0]+x, this.attractorPos[1]+y, this.attractorPos[2]+z);
}
DocexCamera.prototype.setDefaultPos = function()
{
	this.attractorPos[0] = this.defaultPos[0];
	this.attractorPos[1] = this.defaultPos[1];
	this.attractorPos[2] = this.defaultPos[2];
}
DocexCamera.prototype.setPosToRegion = function(region)
{
	var xm = region.bounds[1][0];
	var ym = .5*(region.bounds[0][1]+region.bounds[1][1]);
	var l = Math.max(2*this.reader.height*(region.bounds[1][0]-region.bounds[0][0])/this.reader.width, region.bounds[1][1]-region.bounds[0][1]);
	var zm = l/(2*Math.sin(.5*Math.PI*this.camera.fov/180));
	this.setPos(xm, ym, zm);
}

DocexCamera.prototype.toWorldCoords = function(x, y)
{
	x = (x/this.reader.width*2-1)*this.reader.width/this.reader.height;
	y = y/this.reader.height*2-1;
	var k = this.camera.position.z*this.camTan;
	x *= k;
	y *= k;
	return [x+this.camera.position.x, -y+this.camera.position.y];
}
DocexCamera.prototype.toWorldRay = function(x, y)
{
	var p = this.toWorldCoords(x, y);
	return new THREE.Vector3(p[0]-this.camera.position.x, p[1]-this.camera.position.y, -this.camera.position.z);
}
DocexCamera.prototype.isLeftPage = function(p) {return p[0] < 0;}
DocexCamera.prototype.toRightPageCoords = function(p) {return [p[0]/(this.reader.spec.pageHeight*this.reader.spec.aspect), .5*(1-p[1]/(.5*this.reader.spec.pageHeight))];}
DocexCamera.prototype.toLeftPageCoords = function(p) {return [1+p[0]/(this.reader.spec.pageHeight*this.reader.spec.aspect), .5*(1-p[1]/(.5*this.reader.spec.pageHeight))];}
DocexCamera.prototype.fromRightPageCoords = function(p) {return [p[0]*this.reader.spec.pageHeight*this.reader.spec.aspect, this.reader.spec.pageHeight*(.5-p[1])];}
DocexCamera.prototype.fromLeftPageCoords = function(p) {return [(p[0]-1)*this.reader.spec.pageHeight*this.reader.spec.aspect, this.reader.spec.pageHeight*(.5-p[1])];}

function DocexHand(reader)
{
	this.reader = reader;
	this.active = false;
	this.grabbedNode = null;
	this.grabbedX = 0;
	this.grabPos = [0, 0, 0];
	this.goLeft = false;
	this.dropped = 0;

	this.zoomGrab = [];
	this.zoomOrigin = [];
	this.tmpx = null;
}

DocexHand.prototype.grab = function(x, y)
{
	var book = this.reader.bookModel;
	if (!this.reader.zoomed)
	{
		if (!this.reader.ecoMode)
		{
    		var p = this.reader.camera.toWorldRay(x, y);
    		if (!this.active && !this.reader.bookModel.isAnimating)
    		{
    			if (this.reader.currentPage < -1 || p.x >= 0 && this.reader.currentPage > this.reader.spec.pages.length-2)
    				this.reader.requestPage = this.reader.currentPage+2;
    			else if (p.x < 0)
    				this.reader.requestPage = this.reader.currentPage-2;
    			if (this.reader.currentPage == -3 || this.reader.currentPage == this.reader.spec.pages.length+1)
    				return;
    			if (p.x < 0 && this.reader.currentPage < 0)
    				return;
    			if (p.x >= 0 && this.reader.currentPage > this.reader.spec.pages.length-2)
    				return;
    			this.active = true;
    			if (p.x < 0)
    				this.reader.bookModel.page.setTo(this.reader.bookModel.leftStack);
    			else
    				this.reader.bookModel.page.setTo(this.reader.bookModel.rightStack);
    		}
    		this.grabbedNode = this.reader.bookModel.closestToLine(this.reader.camera.camera.position, p);
    		this.grabbedX = p.x;
    	}
	}
	else
	{
		this.zoomGrab = [x, y];
		//this.zoomOrigin = [DocexCamera.attractorPos[0], DocexCamera.attractorPos[1]];
	}
}

DocexHand.prototype.drag = function(x, y)
{
	if (!this.reader.zoomed)
	{
		if (this.grabbedNode == null)
			return;
		var p = this.reader.camera.toWorldCoords(x, y);
		var pageHeight = this.reader.spec.pageHeight;
		var pageWidth = pageHeight*this.reader.spec.aspect;
		
		if (this.tmpx == null)
			this.tmpx = -p[0];
		else this.tmpx += .1*(-p[0]-this.tmpx);
		this.goLeft = p[0] < this.grabbedX;
		this.grabbedX = p[0];
	}
	//don't move cam in XY plane if we're in a pinch zoom (very annoying otherwise!)
	else if (this.reader.input.lastScale < 0)
	{
		this.reader.camera.translate((this.zoomGrab[0]-x)*.5/this.reader.width, (y-this.zoomGrab[1])*.5/this.reader.width);
		this.zoomGrab[0] = x;
		this.zoomGrab[1] = y;
	}
}

DocexHand.prototype.drop = function(x, y)
{
	this.grabbedNode = null;
	this.dropped = 0;
	this.tmpx = null;
}

DocexHand.prototype.update = function()
{
	if (this.grabbedNode == null)
	{
		this.dropped++;
		if (this.dropped > 25)
		{
			this.active = false;
			if (this.goLeft)
				this.reader.requestPage = this.reader.currentPage+2;
		}
	}
}

var DocexSpringPaperConstants = {};
DocexSpringPaperConstants.nodeSpringSpread = 3;
DocexSpringPaperConstants.cork = .16;
DocexSpringPaperConstants.gravity = -.0006;
DocexSpringPaperConstants.vlim = .1;
DocexSpringPaperConstants.damping = .7;

function DocexSpring(a, b)
{
	this.a = a;
	this.b = b;
	this.desired = a.node.distanceTo(b.node);
	this.buf = new THREE.Vector3();
	this.buf2 = new THREE.Vector3();
}

DocexSpring.prototype.update = function()
{
	this.buf.copy(this.b.node); this.buf.sub(this.a.node);
	var dist = this.buf.length();
	this.buf.setLength(1);
	//console.log(dist+" "+this.desired);
	var cor = DocexSpringPaperConstants.cork*(dist-this.desired);
	this.buf2.copy(this.buf); this.buf2.multiplyScalar(cor);
	this.a.v.add(this.buf2);
	this.buf2.copy(this.buf); this.buf2.multiplyScalar(-cor);
	this.b.v.add(this.buf2);
}

function DocexSpringNode(node, i, j)
{
	this.isStatic = false;
	this.node = node;
	this.springs = [];
	this.v = new THREE.Vector3(0, 0, 0);
	this.buf = new THREE.Vector3(0, 0, 0);
	this.iindex = i;
	this.jindex = j;
}

DocexSpringNode.prototype.attract = function(x, y, z, cor)
{
	var dx = x-this.node.x, dy = y-this.node.y, dz = z-this.node.z;
	var dist = Math.sqrt(dx*dx+dy*dy+dz*dz);
	dx /= dist; dy /= dist; dz /= dist;
	this.v.x += cor*dx; this.v.y += cor*dy; this.v.z += cor*dz; 
}

DocexSpringNode.prototype.addSpring = function(snode)
{
	var spring;
	this.springs.push(spring = new DocexSpring(this, snode));
	return spring;
}

DocexSpringNode.prototype.adjust = function()
{
	if (!this.isStatic)
	{
		if (this.v.lengthSq() > DocexSpringPaperConstants.vlim*DocexSpringPaperConstants.vlim)
			this.v.setLength(DocexSpringPaperConstants.vlim);
	}
	else this.v.set(0, 0, 0);
}
DocexSpringNode.prototype.update = function()
{
	if (!this.isStatic)
		this.node.add(this.v);
	this.v.multiplyScalar(DocexSpringPaperConstants.damping);
}

DocexSpringNode.prototype.dist2FromLine = function(a, u)
{
	var k = (this.node.dot(u)-a.dot(u))/u.dot(u);
	this.buf.copy(u);
	this.buf.multiplyScalar(k);
	this.buf.add(a);
	this.buf.sub(this.node);
	return this.buf.lengthSq();
}

function DocexGrid(p, u, v, w, h)
{
	this.nodes = [];
	for (var i=0;i<w;i++)
	{
		this.nodes[i] = [];
		var ki = i*1./(w-1);
		for (var j=0;j<h;j++)
		{
			var kj = j*1./(h-1);
			this.nodes[i][j] = new THREE.Vector3(p.x+ki*u.x+kj*v.x, p.y+ki*u.y+kj*v.y, p.z+ki*u.z+kj*v.z);
		}
	}
}

DocexGrid.prototype.write = function(frontGeom, backGeom)
{
	var w = this.nodes.length;
	var h = this.nodes[0].length;
	
	for (var i=0;i<w;i++)
		for (var j=0;j<h;j++)
	{
		var index = j*w+i;
		frontGeom.vertices[index].copy(this.nodes[i][j]);
		backGeom.vertices[index].copy(this.nodes[i][j]);
	}
}

function DocexSpringPaper(p, u, v, w, h)
{
	this.grid = new DocexGrid(p, u, v, w, h);
	this.snodes = [];
	
	for (var i=0;i<w;i++)
	{
		this.snodes[i] = [];
		for (var j=0;j<h;j++)
			this.snodes[i][j] = new DocexSpringNode(this.grid.nodes[i][j], i, j);
	}
	
	this.springs = [];
	
	var spread = DocexSpringPaperConstants.nodeSpringSpread;
	for (var k=1;k<spread+1;k++)
		for (var i=0;i<w;i++)
			for (var j=0;j<h;j++)
	{
		var snode = this.snodes[i][j];
		
		if (i < w-k)
			this.springs.push(snode.addSpring(this.snodes[i+k][j]));
		if (j < h-k)
			this.springs.push(snode.addSpring(this.snodes[i][j+k]));
		if (i < w-k && j < h-k)
		{
			this.springs.push(snode.addSpring(this.snodes[i+k][j+k]));
			this.springs.push(this.snodes[i][j+k].addSpring(this.snodes[i+k][j]));
		}
	}

	//console.log("springs : "+this.springs.length);
	
	this.buf = new THREE.Vector3();
}

DocexSpringPaper.prototype.update = function()
{
	for (var spring of this.springs)
		spring.update();
	for (var snodeList of this.snodes)
		for (var snode of snodeList)
	{
		if (!snode.isStatic)
			snode.v[1] += DocexSpringPaperConstants.gravity;
		snode.adjust();
	}
	
	for (var snodeList of this.snodes)
		for (var snode of snodeList)
			snode.update();
	//this.grid.computeNormals();
}

DocexSpringPaper.prototype.closestToLine = function(a, v)
{
	var w = this.snodes.length;
	var h = this.snodes[0].length;
	
	var min = null;
	var minDist = 0;
	
	for (var i=0;i<w;i++)
		for (var j=0;j<h;j++)
	{
		var snode = this.snodes[i][j];
		var d2 = snode.dist2FromLine(a, v);
		if (min === null || d2 < minDist)
		{
			min = snode;
			minDist = d2;
		}
	}
	return min;
}

function DocexBookCover(book, coverLength, coverHeight, coverDepth, bindingWidth, spineWidth, nSpinePoints)
{
	this.book = book;
	this.coverLength = coverLength;
	this.coverHeight = coverHeight;
	this.coverDepth = coverDepth;
	this.bindingWidth = bindingWidth;
	this.spineWidth = spineWidth;
	this.nSpinePoints = nSpinePoints;
	
	this.outer = 0;
	this.inner = 1;
	
	this.path = [];
	this.path[0] = [-.5*this.coverDepth, this.coverLength+this.spineWidth, 0.];
	this.path[1] = [-.5*this.coverDepth, this.spineWidth, .45];
	var p0 = [-.5*this.coverDepth, this.spineWidth];
	var p1 = [.5*this.coverDepth, this.spineWidth];
	var n0 = [0., -2*this.spineWidth];
	var n1 = [0., 2*this.spineWidth];
	for (var i=0;i<this.nSpinePoints;i++)
	{
		var t = (i+1)*1./(this.nSpinePoints+1);
		this.path[2+i] = [ 
			(1-t)*(p0[0]+n0[0]*t)+t*(p1[0]-n1[0]+n1[0]*t),
			(1-t)*(p0[1]+n0[1]*t)+t*(p1[1]-n1[1]+n1[1]*t), 
			.55+.1*t];
	}
	this.path[2+this.nSpinePoints] = [.5*this.coverDepth, this.spineWidth, .55];
	this.path[3+this.nSpinePoints] = [.5*this.coverDepth, this.coverLength+this.spineWidth, 1.];
	
	this.geoms = [new THREE.Geometry(), new THREE.Geometry()];
	
	var vcnt = 0;
	this.topLeftEdge = vcnt++;
	this.bottomLeftEdge = vcnt++;
	this.topRightEdge = vcnt++;
	this.bottomRightEdge = vcnt++;
	this.topLeftHinge = vcnt++;
	this.bottomLeftHinge = vcnt++;
	this.topRightHinge = vcnt++;
	this.bottomRightHinge = vcnt++;
	
	this.indices = [];
	for (var j=0;j<vcnt;j++)
		this.indices[i] = -1;
	this.indices[this.topLeftEdge] = 0;
	this.indices[this.bottomLeftEdge] = 1;
	this.indices[this.topLeftHinge] = 2;
	this.indices[this.bottomLeftHinge] = 3;
	this.indices[this.topRightHinge] = 2*this.path.length-4;
	this.indices[this.bottomRightHinge] = 2*this.path.length-3;
	this.indices[this.topRightEdge] = 2*this.path.length-2;
	this.indices[this.bottomRightEdge] = 2*this.path.length-1;
	
	for (var i=0;i<this.path.length;i++)
	{
		this.geoms[this.outer].vertices.push(new THREE.Vector3(0, 0, 0));
		this.geoms[this.outer].vertices.push(new THREE.Vector3(0, 0, 0));
		this.geoms[this.inner].vertices.push(new THREE.Vector3(0, 0, 0));
		this.geoms[this.inner].vertices.push(new THREE.Vector3(0, 0, 0));
	}
	
	var normal = [0, 0];
	for (var i=0;i<this.path.length;i++)
	{
		if (i == 0) {normal[0] = -1; normal[1] = 0;}
		else if (i == this.path.length-1) {normal[0] = 1; normal[1] = 0;}
		else this.getPathNormal(i, normal);
		//float so = i == 0 ? 1 : i == path.length-1 ? 0 : .5f;
		
		this.geoms[this.outer].vertices[2*i].set(this.path[i][0]+this.bindingWidth*normal[0], .5*this.coverHeight, this.path[i][1]+this.bindingWidth*normal[1]);
		this.geoms[this.outer].vertices[2*i+1].set(this.path[i][0]+this.bindingWidth*normal[0], -.5*this.coverHeight, this.path[i][1]+this.bindingWidth*normal[1]);
		this.geoms[this.inner].vertices[2*i].set(this.path[i][0], .5*this.coverHeight, this.path[i][1]);
		this.geoms[this.inner].vertices[2*i+1].set(this.path[i][0], -.5*this.coverHeight, this.path[i][1]);
	}
	for (var i=0;i<this.path.length-1;i++)
	{
		var s0 = i == 0 ? 1 : i == this.path.length-1 ? 0 : .5;
		var s1 = i == this.path.length-2 ? 0 : .5;
		this.geoms[this.outer].faces.push(new THREE.Face3(2*i, 2*i+2, 2*i+1));
		this.geoms[this.outer].faceVertexUvs[0].push([new THREE.Vector2(s0, 1), new THREE.Vector2(s1, 1), new THREE.Vector2(s0, 0)]);
		this.geoms[this.outer].faces.push(new THREE.Face3(2*i+1, 2*i+2, 2*i+3));
		this.geoms[this.outer].faceVertexUvs[0].push([new THREE.Vector2(s0, 0), new THREE.Vector2(s1, 1), new THREE.Vector2(s1, 0)]);
		this.geoms[this.inner].faces.push(new THREE.Face3(2*i, 2*i+1, 2*i+2));
		this.geoms[this.inner].faceVertexUvs[0].push([new THREE.Vector2(1-s0, 1), new THREE.Vector2(1-s0, 0), new THREE.Vector2(1-s1, 1)]);
		this.geoms[this.inner].faces.push(new THREE.Face3(2*i+1, 2*i+3, 2*i+2));
		this.geoms[this.inner].faceVertexUvs[0].push([new THREE.Vector2(1-s0, 0), new THREE.Vector2(1-s1, 0), new THREE.Vector2(1-s1, 1)]);
	}
	
	this.meshes = [];
	this.meshes[this.outer] = new THREE.Mesh(this.geoms[this.outer], new THREE.MeshLambertMaterial({map: this.book.reader.spec.loadingTex}));
	this.meshes[this.inner] = new THREE.Mesh(this.geoms[this.inner], new THREE.MeshLambertMaterial({map: this.book.reader.spec.loadingTex}));
	
	this.book.reader.texLoader.loadCoverTex(this);
	
	this.model = new THREE.Group();
	for (var i=0;i<this.geoms.length;i++)
	{
		this.meshes[i].material.side = THREE.FrontSide;
		this.meshes[i].material.shading = THREE.SmoothShading;
		this.meshes[i].material.transparent = true;
		this.meshes[i].frustumCulled = false;
		this.geoms[i].computeFaceNormals();
		this.geoms[i].computeVertexNormals();
		this.model.add(this.meshes[i]);
	}
	this.model.frustumCulled = false;
	this.time = 0.;
}

DocexBookCover.prototype.getPathNormal = function(index, normal)
{
	var dx1 = this.path[index-1][0]-this.path[index][0], dy1 = this.path[index-1][1]-this.path[index][1],
		dx2 = this.path[index+1][0]-this.path[index][0], dy2 = this.path[index+1][1]-this.path[index][1];
	var l1 = Math.sqrt(dx1*dx1+dy1*dy1), l2 = Math.sqrt(dx2*dx2+dy2*dy2);
	dx1 /= l1; dy1 /= l1;
	dx2 /= l2; dy2 /= l2;
	var nx = -(dx1+dx2), ny = -(dy1+dy2);
	var ln = Math.sqrt(nx*nx+ny*ny);
	normal[0] = nx/ln;
	normal[1] = ny/ln;
}

DocexBookCover.prototype.setCoverVertex = function(geom, vertex, x, y, z)
{
	if (this.indices[vertex] > -1)
		this.geoms[geom].vertices[this.indices[vertex]].set(x, y, z);
}

DocexBookCover.prototype.setAngle = function(la, ra)
{
	var llx = -Math.sin(la), llz = Math.cos(la);
	var rlx = -Math.sin(ra), rlz = Math.cos(ra);
	var lnx = llz, lnz = -llx;
	var rnx = rlz, rnz = -rlx;
	
	this.setCoverVertex(this.inner, this.topLeftEdge, -this.coverDepth/2+this.coverLength*llx, .5*this.coverHeight, this.spineWidth+this.coverLength*llz);
	this.setCoverVertex(this.inner, this.bottomLeftEdge, -this.coverDepth/2+this.coverLength*llx, -.5*this.coverHeight, this.spineWidth+this.coverLength*llz);
	this.setCoverVertex(this.outer, this.topLeftEdge, -this.coverDepth/2+this.coverLength*llx-this.bindingWidth*lnx, .5*this.coverHeight, this.spineWidth+this.coverLength*llz-this.bindingWidth*lnz);
	this.setCoverVertex(this.outer, this.bottomLeftEdge, -this.coverDepth/2+this.coverLength*llx-this.bindingWidth*lnx, -.5*this.coverHeight, this.spineWidth+this.coverLength*llz-this.bindingWidth*lnz);
	
	this.setCoverVertex(this.inner, this.topRightEdge, this.coverDepth/2-this.coverLength*rlx, .5*this.coverHeight, this.spineWidth+this.coverLength*rlz);
	this.setCoverVertex(this.inner, this.bottomRightEdge, this.coverDepth/2-this.coverLength*rlx, -.5*this.coverHeight, this.spineWidth+this.coverLength*rlz);
	this.setCoverVertex(this.outer, this.topRightEdge, this.coverDepth/2-this.coverLength*rlx+this.bindingWidth*rnx, .5*this.coverHeight, this.spineWidth+this.coverLength*rlz-this.bindingWidth*rnz);
	this.setCoverVertex(this.outer, this.bottomRightEdge, this.coverDepth/2-this.coverLength*rlx+this.bindingWidth*rnx, -.5*this.coverHeight, this.spineWidth+this.coverLength*rlz-this.bindingWidth*rnz);
	
	for (var i=0;i<this.geoms.length;i++)
	{
		this.geoms[i].computeFaceNormals();
		this.geoms[i].computeVertexNormals();
		this.geoms[i].verticesNeedUpdate = true;
	}
}

function docexPaperCurveCompute(x0, y0, vx0, vy0, cdx, cdy, length, steps, weight, res)
{
	var cl = Math.sqrt(cdx*cdx+cdy*cdy);
	cdx /= cl; cdy /= cl;
	var cpx = -cdy, cpy = cdx;
	if (cpx*vx0+cpy*vy0 < 0)
		{cpx = -cpx; cpy = -cpy;}
	
	var x = res[0][0] = x0, y = res[0][1] = y0;
	
	var vx = vx0, vy = vy0;
	var vl = Math.sqrt(vx*vx+vy*vy);
	vx /= vl; vy /= vl;
	
	var flat = vy <= cdy;
	var g = -.015*(vy-cdy)*weight;
	if (flat)
	{
		vx = cdx;
		vy = cdy;
	}
	
	var stepLength = length/steps;
	for (var i=0;i<steps;i++)
	{
		if (!flat)
		{
			vy += g*stepLength;
			var cd = -(x0*cpx+y0*cpy-x*cpx-y*cpy)/(cpx*cpx+cpy*cpy);
			var cv = (vx*cpx+vy*cpy)/(cpx*cpx+cpy*cpy);
			if (cv >= 0) ;
			else if (cd <= 0)
			{
				vx = cdx;
				vy = cdy;
			}
			else if (cv < 0)
			{
				var k = -cd/cv;
				k = stepLength*1./k;
				vx += k*cdx;
				vy += k*cdy;
			}
			
		}
		
		vl = Math.sqrt(vx*vx+vy*vy);
		vx /= vl; vy /= vl;
		
		x += stepLength*vx;
		y += stepLength*vy;
		res[i+1][0] = x;
		res[i+1][1] = y;
	}
}

function docexPaperCurveNormalAt(i, path, seglength, n)
{
	var dx1 = path[i-1][0]-path[i][0], dy1 = path[i-1][1]-path[i][1];
	var dx2, dy2;
	if (i == path.length-1)
		{dx2 = -dx1; dy2 = -dy1;}
	else {dx2 = path[i+1][0]-path[i][0]; dy2 = path[i+1][1]-path[i][1];}
	
	n[0] = dy1-dy2; n[1] = dx2-dx1;
	var nl = Math.sqrt(n[0]*n[0]+n[1]*n[1]); 
	n[0] /= nl; n[1] /= nl;
}

function docexPaperCurveSlideToLength(px, py, cx, cy, vx, vy, length)
{
	var a = vx*vx+vy*vy;
	var b = 2*(cx*vx+cy*vy-px*vx-py*vy);
	var c = (cx-px)*(cx-px)+(cy-py)*(cy-py)-length*length;
	var d = Math.sqrt(b*b-4*a*c);
	return (d-b)/(2*a);
}

function docexPaperCurveProject(path, dx, dy, length, res)
{
	var dist = Math.sqrt(dx*dx+dy*dy);
	
	var normal = [0, 0];
	normal[0] = path[0][1]-path[1][1];
	normal[1] = path[1][0]-path[0][0];
	var nl = Math.sqrt(normal[0]*normal[0]+normal[1]*normal[1]);
	normal[0] /= nl; normal[1] /= nl;
	var reverse = dx*normal[0]+dy*normal[1] < 0;
	if (reverse)
		{normal[0] = -normal[0]; normal[1] = -normal[1];}
	
	var nSubPoints = Math.floor(dist*path.length/length)+1;
	
	var buf = [[path[0][0]+dx, path[0][1]+dy]];
	for (var i=0;i<nSubPoints;i++)
	{
		var k = (i+1)*1./(nSubPoints+1);
		var nx = (1-k)*dx/dist+k*normal[0];
		var ny = (1-k)*dy/dist+k*normal[1];
		nl = Math.sqrt(nx*nx+ny*ny);
		nx /= nl; ny /= nl;
		
		buf[i+1] = [path[0][0]+dist*nx, path[0][1]+dist*ny]; 
	}
	
	for (var i=1;i<path.length;i++)
	{
		docexPaperCurveNormalAt(i, path, length/(path.length-1), normal);
		if (reverse)
			{normal[0] = -normal[0]; normal[1] = -normal[1];}
		buf[i+nSubPoints] = [path[i][0]+dist*normal[0], path[i][1]+dist*normal[1]];
	}
	
	res[0][0] = buf[0][0];
	res[0][1] = buf[0][1];
	var pathIndex = 0;
	for (var i=1;i<path.length;i++)
	{
		var k = docexPaperCurveSlideToLength(res[i-1][0], res[i-1][1], res[i-1][0], res[i-1][1], 
			buf[pathIndex+1][0]-res[i-1][0], buf[pathIndex+1][1]-res[i-1][1], length/(path.length-1));
		if (k <= 1)
		{
			res[i][0] = res[i-1][0]+k*(buf[pathIndex+1][0]-res[i-1][0]);
			res[i][1] = res[i-1][1]+k*(buf[pathIndex+1][1]-res[i-1][1]);
		}
		else
		{
			while (k > 1)
			{
				if (pathIndex+1 == buf.length-1)
					break;
				pathIndex++;
				k = docexPaperCurveSlideToLength(res[i-1][0], res[i-1][1], buf[pathIndex][0], buf[pathIndex][1], 
					buf[pathIndex+1][0]-buf[pathIndex][0], buf[pathIndex+1][1]-buf[pathIndex][1], 
					length/(path.length-1));
			}
			res[i][0] = buf[pathIndex][0]+k*(buf[pathIndex+1][0]-buf[pathIndex][0]);
			res[i][1] = buf[pathIndex][1]+k*(buf[pathIndex+1][1]-buf[pathIndex][1]);
		}
	}
}

function DocexBookPageStack(book, cover, nPages, pageWidth, pageHeight, pathLength, left)
{
	this.book = book;
	this.cover = cover;
	this.nPages = nPages;
	this.pageWidth = pageWidth;
	this.pageHeight = pageHeight;
	this.pathLength = pathLength;
	this.left = left;
	this.vx0 = left ? -1 : 1;
	this.path = [];
	this.projection = [];
	this.normals = [];
	this.margin = .5*cover.coverHeight*(1-pageHeight);
	this.nStackPages = nPages/2+(!left && nPages%2==1 ? 1 : 0);
	this.sidef1 = null;
	this.sidef2 = null;
	this.flatness = 0;
	
	for (var i=0;i<pathLength;i++)
	{
		this.path[i] = [0, 0];
		this.projection[i] = [0, 0];
		this.normals[i] = [0, 0];
	}
	
	this.stackFrontGeom = new THREE.Geometry();
	for (var i=0;i<2*pathLength;i++)
		this.stackFrontGeom.vertices.push(new THREE.Vector3(0, 0, 0));
	this.stackSideGeom = new THREE.Geometry();
	for (var i=0;i<4*pathLength+4;i++)
		this.stackSideGeom.vertices.push(new THREE.Vector3(0, 0, 0));
	for (var i=0;i<pathLength-1;i++)
	{
		var va = left ? 2*i+1 : 2*i+3, 
			vb = left ? 2*i+3 : 2*i+1, 
			vc = left ? 2*i+3 : 2*i+2, 
			vd = left ? 2*i+2 : 2*i+3;
		var s0 = left ? 1-i*1./(pathLength-1) : i*1./(pathLength-1),
			s1 = left ? 1-(i+1)*1./(pathLength-1) : (i+1)*1./(pathLength-1);
		var sa = left ? s0 : s1, ta = 1,
			sb = left ? s1 : s0, tb = 1,
			sc = s1, tc = left ? 1 : 0,
			sd = s1, td = left ? 0 : 1;
		this.stackFrontGeom.faces.push(new THREE.Face3(2*i, va, vb));
		this.stackFrontGeom.faceVertexUvs[0].push([new THREE.Vector2(s0, 0), new THREE.Vector2(sa, ta), new THREE.Vector2(sb, tb)]);
		this.stackFrontGeom.faces.push(new THREE.Face3(2*i, vc, vd));
		this.stackFrontGeom.faceVertexUvs[0].push([new THREE.Vector2(s0, 0), new THREE.Vector2(sc, tc), new THREE.Vector2(sd, td)]);
		
		
		this.stackSideGeom.faces.push(new THREE.Face3(2*i, va, vb));
		this.stackSideGeom.faceVertexUvs[0].push([new THREE.Vector2(0, 0), new THREE.Vector2(0, 0), new THREE.Vector2(0, 0)]);
		this.stackSideGeom.faces.push(new THREE.Face3(2*i, vc, vd));
		this.stackSideGeom.faceVertexUvs[0].push([new THREE.Vector2(0, 0), new THREE.Vector2(0, 0), new THREE.Vector2(0, 0)]);
	}
	for (var i=pathLength-1;i<2*pathLength-2;i++)
	{
		var vi0 = i+1;
		this.stackSideGeom.faces.push(new THREE.Face3(2*vi0, left ? 2*vi0+3 : 2*vi0+1, left ? 2*vi0+1 : 2*vi0+3));
		this.stackSideGeom.faceVertexUvs[0].push([new THREE.Vector2(0, 0), new THREE.Vector2(0, 0), new THREE.Vector2(0, 0)]);
		this.stackSideGeom.faces.push(new THREE.Face3(2*vi0, left ? 2*vi0+2 : 2*vi0+3, left ? 2*vi0+3 : 2*vi0+2));
		this.stackSideGeom.faceVertexUvs[0].push([new THREE.Vector2(0, 0), new THREE.Vector2(0, 0), new THREE.Vector2(0, 0)]);
	}
	{
		var vi0 = 2*pathLength;
		this.stackSideGeom.faces.push(new THREE.Face3(2*vi0, left ? 2*vi0+1 : 2*vi0+3, left ? 2*vi0+3 : 2*vi0+1));
		this.sidef1 = [new THREE.Vector2(0, 0), new THREE.Vector2(0, 0), new THREE.Vector2(0, 0)];
		this.stackSideGeom.faceVertexUvs[0].push(this.sidef1);
		this.stackSideGeom.faces.push(new THREE.Face3(2*vi0, left ? 2*vi0+3 : 2*vi0+2, left ? 2*vi0+2 : 2*vi0+3));
		this.sidef2 = [new THREE.Vector2(0, 0), new THREE.Vector2(0, 0), new THREE.Vector2(0, 0)];
		this.stackSideGeom.faceVertexUvs[0].push(this.sidef2);
	}
	
	var coverPath = cover.path;
	this.spineGeom = new THREE.Geometry();
	for (var i=0;i<coverPath.length-2;i++)
		this.spineGeom.vertices.push(new THREE.Vector3(coverPath[i+1][0], -.5*cover.coverHeight+this.margin, coverPath[i+1][1]));
	for (var i=0;i<coverPath.length-2;i++)
		this.spineGeom.vertices.push(new THREE.Vector3(coverPath[i+1][0], .5*cover.coverHeight-this.margin, coverPath[i+1][1]));
	for (var i=0;i<coverPath.length-4;i++)
		this.spineGeom.faces.push(new THREE.Face3(0, i+1, i+2));
	for (var i=0;i<coverPath.length-4;i++)
		this.spineGeom.faces.push(new THREE.Face3(coverPath.length-2, coverPath.length+i, coverPath.length+i-1));
	
	this.stackFrontGeom.computeFaceNormals();
	this.stackFrontGeom.computeVertexNormals();
	this.stackSideGeom.computeFaceNormals();
	this.stackSideGeom.computeVertexNormals();
	this.spineGeom.computeFaceNormals();
	this.spineGeom.computeVertexNormals();
	
	this.stackFrontMesh = new THREE.Mesh(this.stackFrontGeom, new THREE.MeshLambertMaterial({map: this.book.reader.spec.loadingTex}));
	this.stackFrontMesh.material.transparent = true;
	this.stackFrontMesh.frustumCulled = false;
	this.stackFrontMesh.material.side = THREE.FrontSide;
	this.stackFrontMesh.material.shading = THREE.SmoothShading;
	this.stackSideMesh = new THREE.Mesh(this.stackSideGeom, new THREE.MeshLambertMaterial({color: 0xC0B0A0}));
	this.stackSideMesh.frustumCulled = false;
	this.stackSideMesh.material.side = THREE.FrontSide;
	this.stackSideMesh.material.shading = THREE.SmoothShading;
	this.spineMesh = new THREE.Mesh(this.spineGeom, new THREE.MeshLambertMaterial({}));
	this.spineMesh.frustumCulled = false;
	this.spineMesh.material.side = THREE.FrontSide;
	this.spineMesh.material.shading = THREE.SmoothShading;
	
	this.book.reader.texLoader.loadSideTex(this);
	
	this.model = new THREE.Group();
	this.model.add(this.stackFrontMesh);
	this.model.add(this.stackSideMesh);
	this.model.add(this.spineMesh);
	this.model.frustumCulled = false;
	
	this.bufv = [0, 0];
}

DocexBookPageStack.prototype.setTex = function(tex)
{
	this.stackFrontMesh.material.map = tex;
	this.stackFrontMesh.material.needsUpdate = true;
}

DocexBookPageStack.prototype.updateStack = function()
{
	this.model.visible = this.nStackPages != 0;
	
	var cover = this.cover;
	var hingex = cover.geoms[cover.inner].vertices[cover.indices[this.left ? cover.bottomLeftHinge : cover.bottomRightHinge]].x,
		hingey = cover.geoms[cover.inner].vertices[cover.indices[this.left ? cover.bottomLeftHinge : cover.bottomRightHinge]].z;
	var edgex = cover.geoms[cover.inner].vertices[cover.indices[this.left ? cover.bottomLeftEdge : cover.bottomRightEdge]].x,
		edgey = cover.geoms[cover.inner].vertices[cover.indices[this.left ? cover.bottomLeftEdge : cover.bottomRightEdge]].z;
	
	//recompute path and projection
	var sep = this.nStackPages*1./this.nPages;
	docexPaperCurveCompute(hingex, hingey, this.vx0, 1-this.flatness, edgex-hingex, edgey-hingey, this.pageWidth*cover.coverLength, this.pathLength-1, (1.+sep)*200, this.path);
	if (sep > 0)
		docexPaperCurveProject(this.path, cover.coverDepth*(this.left ? sep : -sep), 0, this.pageWidth*cover.coverLength, this.projection);
	else for (var i=0;i<this.path.length;i++)
		{this.projection[i][0] = this.path[i][0]; this.projection[i][1] = this.path[i][1];}
	
	//update the meshes
	for (var i=0;i<this.pathLength;i++)
	{
		var nx, ny;
		if (i == 0)
		{
			nx = -(this.projection[1][1]-this.projection[0][1]);
			ny = this.projection[1][0]-this.projection[0][0];
		}
		else if (i == this.pathLength-1)
		{
			nx = -(this.projection[this.pathLength-1][1]-this.projection[this.pathLength-2][1]);
			ny = this.projection[this.pathLength-1][0]-this.projection[this.pathLength-2][0];
		}
		else
		{
			nx = -(this.projection[i+1][1]-this.projection[i-1][1]);
			ny = this.projection[i+1][0]-this.projection[i-1][0];
		}
		var nl = Math.sqrt(nx*nx+ny*ny);
		nx /= nl; ny /= nl;
		if (this.left)
			{nx = -nx; ny = -ny;}
		
		this.normals[i][0] = nx; 
		this.normals[i][1] = ny;
		this.stackFrontGeom.vertices[2*i].set(this.projection[i][0], -.5*cover.coverHeight+this.margin, this.projection[i][1]);
		this.stackFrontGeom.vertices[2*i+1].set(this.projection[i][0], .5*cover.coverHeight-this.margin, this.projection[i][1]);
		
		this.stackSideGeom.vertices[2*i].set(this.path[i][0], -.5*cover.coverHeight+this.margin, this.path[i][1]);
		this.stackSideGeom.vertices[2*i+1].set(this.projection[i][0], -.5*cover.coverHeight+this.margin, this.projection[i][1]);
		this.stackSideGeom.vertices[2*(this.pathLength+i)].set(this.path[i][0], .5*cover.coverHeight-this.margin, this.path[i][1]);
		this.stackSideGeom.vertices[2*(this.pathLength+i)+1].set(this.projection[i][0], .5*cover.coverHeight-this.margin, this.projection[i][1]);
	}
	
	var dx = this.projection[this.pathLength-1][0]-this.path[this.pathLength-1][0],
		dy = this.projection[this.pathLength-1][1]-this.path[this.pathLength-1][1];
	var nx = -dy, ny = dx;
	var nl = Math.sqrt(nx*nx+ny*ny);
	nx /= nl; ny /= nl;
	if (!this.left)
		{nx = -nx; ny = -ny;}
	
	this.stackSideGeom.vertices[4*this.pathLength].set(this.path[this.pathLength-1][0], -.5*cover.coverHeight+this.margin, this.path[this.pathLength-1][1]);
	this.stackSideGeom.vertices[4*this.pathLength+1].set(this.projection[this.pathLength-1][0], -.5*cover.coverHeight+this.margin, this.projection[this.pathLength-1][1]);
	this.stackSideGeom.vertices[4*this.pathLength+2].set(this.path[this.pathLength-1][0], .5*cover.coverHeight-this.margin, this.path[this.pathLength-1][1]);
	this.stackSideGeom.vertices[4*this.pathLength+3].set(this.projection[this.pathLength-1][0], .5*cover.coverHeight-this.margin, this.projection[this.pathLength-1][1]);
	
	if (this.left)
	{
		this.sidef1[0].set(0, 0);
		this.sidef1[1].set(sep, 0);
		this.sidef1[2].set(sep, 1);
		this.sidef2[0].set(0, 0);
		this.sidef2[1].set(sep, 1);
		this.sidef2[2].set(0, 1);
	}
	else
	{
		this.sidef1[0].set(1, 0);
		this.sidef1[1].set(1-sep, 1);
		this.sidef1[2].set(1-sep, 0);
		this.sidef2[0].set(1, 0);
		this.sidef2[1].set(1, 1);
		this.sidef2[2].set(1-sep, 1);
	}
	
	this.stackFrontGeom.computeFaceNormals();
	this.stackFrontGeom.computeVertexNormals();
	this.stackFrontGeom.verticesNeedUpdate = true;
	this.stackSideGeom.computeFaceNormals();
	this.stackSideGeom.computeVertexNormals();
	this.stackSideGeom.verticesNeedUpdate = true;
	this.stackSideGeom.uvsNeedUpdate = true;
	this.spineGeom.computeFaceNormals();
	this.spineGeom.computeVertexNormals();
	this.spineGeom.verticesNeedUpdate = true;
}

DocexBookPageStack.prototype.toBookModel = function(x, y, res)
{
	this.projectionAt(x*this.cover.coverLength, this.bufv);
	res.x = this.bufv[0];
	res.y = -.5*this.cover.coverHeight+this.margin+(1-y)*(this.cover.coverHeight-2*this.margin);
	res.z = this.bufv[1];
}

DocexBookPageStack.prototype.projectionAt = function(l, res)
{
	var unitLength = (this.cover.coverLength/(this.pathLength-1));
	var proji = Math.floor(l/unitLength);
	if (proji > this.pathLength-2)
		proji = this.pathLength-2;
	
	var k = (l-proji*unitLength)/unitLength;
	res[0] = this.projection[proji][0]+k*(this.projection[proji+1][0]-this.projection[proji][0]);
	res[1] = this.projection[proji][1]+k*(this.projection[proji+1][1]-this.projection[proji][1]);
	return res;
}

var BookPageConstants = {};
BookPageConstants.springGridSize = 12;
BookPageConstants.collideCor = .0000001;
BookPageConstants.collideLim = .00001;
BookPageConstants.springUpdateLoops = 10;
BookPageConstants.pinchLength = .95;

function DocexBookPage(book)
{
	this.book = book;
	
	var a = -Math.PI/10.;
	this.paper = new DocexSpringPaper(
		new THREE.Vector3(book.leftStack.projection[0][0], .5*book.cover.coverHeight-book.leftStack.margin, book.leftStack.projection[0][1]), //p
		new THREE.Vector3(Math.sin(a)*book.leftStack.pageWidth*book.cover.coverLength, 0, Math.cos(a)*book.leftStack.pageWidth*book.cover.coverLength), //u
		new THREE.Vector3(0, -(book.cover.coverHeight-2*book.leftStack.margin), 0), //v
		BookModelConstants.gridSize, BookModelConstants.gridSize);
		//3, 3);
	for (var i=0;i<this.paper.snodes[0].length;i++)
		this.paper.snodes[0][i].isStatic = true;
	
	this.frontPageGeom = new THREE.Geometry();
	this.backPageGeom = new THREE.Geometry();
	
	var w = this.paper.snodes.length;
	var h = this.paper.snodes[0].length;
	
	for (var i=0;i<this.paper.snodes.length;i++)
		for (var j=0;j<this.paper.snodes[0].length;j++)
	{
		this.frontPageGeom.vertices.push(new THREE.Vector3());
		this.backPageGeom.vertices.push(new THREE.Vector3());
	}
	
	for (var i=0;i<w-1;i++)
		for (var j=0;j<h-1;j++)
	{
		this.frontPageGeom.faces.push(new THREE.Face3(j*w+i, (j+1)*w+i, (j+1)*w+(i+1)));
		this.frontPageGeom.faceVertexUvs[0].push([new THREE.Vector2(i*1./(w-1), 1-j*1./(w-1)), new THREE.Vector2(i*1./(w-1), 1-(j+1)*1./(w-1)), new THREE.Vector2((i+1)*1./(w-1), 1-(j+1)*1./(w-1))]);
		this.frontPageGeom.faces.push(new THREE.Face3(j*w+i, (j+1)*w+(i+1), j*w+(i+1)));
		this.frontPageGeom.faceVertexUvs[0].push([new THREE.Vector2(i*1./(w-1), 1-j*1./(w-1)), new THREE.Vector2((i+1)*1./(w-1), 1-(j+1)*1./(w-1)), new THREE.Vector2((i+1)*1./(w-1), 1-j*1./(w-1))]);
		this.backPageGeom.faces.push(new THREE.Face3(j*w+i, (j+1)*w+i, (j+1)*w+(i+1)));
		this.backPageGeom.faceVertexUvs[0].push([new THREE.Vector2(1-i*1./(w-1), 1-j*1./(w-1)), new THREE.Vector2(1-i*1./(w-1), 1-(j+1)*1./(w-1)), new THREE.Vector2(1-(i+1)*1./(w-1), 1-(j+1)*1./(w-1))]);
		this.backPageGeom.faces.push(new THREE.Face3(j*w+i, (j+1)*w+(i+1), j*w+(i+1)));
		this.backPageGeom.faceVertexUvs[0].push([new THREE.Vector2(1-i*1./(w-1), 1-j*1./(w-1)), new THREE.Vector2(1-(i+1)*1./(w-1), 1-(j+1)*1./(w-1)), new THREE.Vector2(1-(i+1)*1./(w-1), 1-j*1./(w-1))]);
	}
	
	this.frontPageMesh = new THREE.Mesh(this.frontPageGeom, new THREE.MeshLambertMaterial({map: this.book.reader.spec.loadingTex}));
	this.frontPageMesh.material.side = THREE.FrontSide;
	this.frontPageMesh.material.shading = THREE.SmoothShading;
	this.frontPageMesh.renderOrder = 1;
	this.frontPageMesh.onBeforeRender = function(renderer) {renderer.clearDepth();};
	this.backPageMesh = new THREE.Mesh(this.backPageGeom, new THREE.MeshLambertMaterial({map: this.book.reader.spec.loadingTex}));
	this.backPageMesh.material.side = THREE.BackSide;
	this.backPageMesh.material.shading = THREE.SmoothShading;
	this.backPageMesh.renderOrder = 2;
	
	this.model = new THREE.Group();
	this.model.add(this.frontPageMesh);
	this.model.add(this.backPageMesh);
	this.model.visible = false;
	this.model.renderOrder = 99;
	
	this.opacity = 0;
	
	this.buf1 = [0, 0];
	this.buf2 = [0, 0];
	this.bufv = new THREE.Vector3();
}

DocexBookPage.prototype.setPageOpacity = function(val)
{
	
	if (val == 0)
		this.model.visible = false;
	else
	{
		this.model.visible = true;
		this.frontPageMesh.material.transparent = true;
		this.backPageMesh.material.transparent = true;
		this.frontPageMesh.material.opacity = val;
		this.backPageMesh.material.opacity = val;
	}
	this.opacity = val;
}

DocexBookPage.prototype.writeGeoms = function()
{
	this.paper.grid.write(this.frontPageGeom, this.backPageGeom);
	this.frontPageGeom.computeFaceNormals();
	this.frontPageGeom.computeVertexNormals();
	this.frontPageGeom.verticesNeedUpdate = true;
	this.backPageGeom.computeFaceNormals();
	this.backPageGeom.computeVertexNormals();
	this.backPageGeom.verticesNeedUpdate = true;
}

DocexBookPage.prototype.setFrontTex = function(front) {this.frontPageMesh.material.map = front;}
DocexBookPage.prototype.setBackTex = function(back) {this.backPageMesh.material.map = back;}

DocexBookPage.prototype.setTo = function(stack)
{
	for (var i=0;i<this.paper.snodes.length;i++)
	{
		var l = i*this.book.leftStack.pageWidth*this.book.cover.coverLength/(this.paper.snodes.length-1);
		stack.projectionAt(l, this.buf1);
		for (var j=0;j<this.paper.snodes[0].length;j++)
			this.paper.snodes[i][j].node.set(this.buf1[0], this.paper.snodes[0][j].node.y, this.buf1[1]);
	}
	this.writeGeoms();
}

DocexBookPage.prototype.updateHeldNode = function(snode, x)
{
	var length = BookPageConstants.pinchLength*this.book.cover.coverLength;
	var ca = Math.max(-.95, Math.min(.95, x/this.book.cover.coverLength));
	var sa = Math.sqrt(1-ca*ca);
	this.bufv.set(this.paper.snodes[0][0].node.x+ca*length, this.paper.snodes[0][snode.jindex].node.y, this.paper.snodes[0][0].node.z+sa*length);
	snode.node.copy(this.bufv);
}

DocexBookPage.prototype.update = function()
{
	var hand = this.book.reader.hand;
	if (!hand.active)
	{
		this.setPageOpacity(Math.max(0, this.opacity-.1));
		return;
	}
	this.setPageOpacity(1.);
	
	this.book.leftStack.projectionAt(0, this.buf1);
	this.book.rightStack.projectionAt(0, this.buf2);
		
	for (var j=0;j<this.paper.snodes[0].length;j++)
	{
		this.paper.snodes[0][j].node.x = (this.buf1[0]+this.buf2[0])/2;
		this.paper.snodes[0][j].node.z = (this.buf1[1]+this.buf2[1])/2;
	}
	
	if (hand.grabbedNode !== null)
		this.updateHeldNode(hand.grabbedNode, hand.grabbedX);
	
	for (var k=0;k<BookPageConstants.springUpdateLoops;k++)
	{
		if (hand.grabbedNode === null)
			this.attractToStack(hand.goLeft);
		
		this.paper.update();
		
		for (var i=0;i<this.paper.snodes.length;i++)
			for (var j=0;j<this.paper.snodes[0].length;j++)
		{
			var snode = this.paper.snodes[i][j];
			if (snode.node.x < this.book.leftStack.projection[0][0])
				this.collide(snode, this.book.leftStack);
			if (snode.node.x > this.book.rightStack.projection[0][0])
				this.collide(snode, this.book.rightStack);
		}
	}
	this.writeGeoms();
}

DocexBookPage.prototype.collide = function(snode, stack)
{
	if (snode.isStatic)
		return;
	var dist = this.projectOnPath(snode.node.x, snode.node.z, 
		stack.projection, stack.normals, this.buf1, this.buf2);
	if (dist < BookPageConstants.collideLim)
		return;
	if ((snode.node.x-this.buf1[0])*this.buf2[0]+(snode.node.z-this.buf1[1])*this.buf2[1] > 0)
		return;
	
	dist = Math.sqrt(dist);
	var cx = (this.buf1[0]-snode.node.x)/dist, cy = (this.buf1[1]-snode.node.z)/dist;
	snode.node.x += cx*(dist+BookPageConstants.collideCor);
	snode.node.z += cy*(dist+BookPageConstants.collideCor);
}

DocexBookPage.prototype.projectOnPath = function(x, y, path, normals, proj, normal)
{
	var minDist = (x-path[0][0])*(x-path[0][0])+(y-path[0][1])*(y-path[0][1]);
	
	for (var i=0;i<path.length-1;i++)
	{
		var k = this.dist(x, y, path[i][0], path[i][1], path[i+1][0]-path[i][0], path[i+1][1]-path[i][1]);
		var dist = 99999999;
		if (k > 0 && k < 1)
		{
			var ix = path[i][0]+k*(path[i+1][0]-path[i][0]), iy = path[i][1]+k*(path[i+1][1]-path[i][1]);
			dist = (x-ix)*(x-ix)+(y-iy)*(y-iy);
		}
		if (dist < minDist)
		{
			minDist = dist;
			
			proj[0] = path[i][0]+k*(path[i+1][0]-path[i][0]);
			proj[1] = path[i][1]+k*(path[i+1][1]-path[i][1]);
			normal[0] = normals[i][0]+normals[i+1][0];
			normal[1] = normals[i][1]+normals[i+1][1];
			var nl = Math.sqrt(normal[0]*normal[0]+normal[1]*normal[1]);
			normal[0] /= nl; normal[1] /= nl;
		}
		
		dist = (x-path[i+1][0])*(x-path[i+1][0])+(y-path[i+1][1])*(y-path[i+1][1]);
		if (dist < minDist)
		{
			minDist = dist;
			
			proj[0] = path[i+1][0];
			proj[1] = path[i+1][1];
			normal[0] = normals[i+1][0];
			normal[1] = normals[i+1][1];
		}
	}
	return minDist;
}
DocexBookPage.prototype.dist = function(x, y, px, py, vx, vy) {return ((x*vx+y*vy)-(px*vx+py*vy))/(vx*vx+vy*vy);}

DocexBookPage.prototype.attractToStack = function(headedLeft)
{
	var stack = headedLeft ? this.book.leftStack : this.book.rightStack;
	for (var i=1;i<this.paper.snodes.length;i++)
	{
		var k = i*1./(this.paper.snodes.length-1);
		
		stack.projectionAt(.99*k*this.book.cover.coverLength, this.buf1);
		for (var j=0;j<this.paper.snodes[0].length;j++)
		{//console.log(""+.1*(this.buf1[0]-this.paper.snodes[i][j].node.x));
			this.paper.snodes[i][j].v.x += (.7+.3*(1-k))*.01*(this.buf1[0]-this.paper.snodes[i][j].node.x);
			this.paper.snodes[i][j].v.y += (.7+.3*(1-k))*.01*(this.paper.snodes[0][j].node.y-this.paper.snodes[i][j].node.y);
			this.paper.snodes[i][j].v.z += (.7+.3*(1-k))*.01*(this.buf1[1]-this.paper.snodes[i][j].node.z);
		}
	}
}

var BookModelConstants = {};
BookModelConstants.gridSize = 15;

function DocexBookModel(reader, length, height)
{
	this.reader = reader;
	this.length = length;
	this.height = height;
	
	var coverDepth = Math.min(.08, this.reader.spec.pages.length*.00125);
	this.cover = new DocexBookCover(this, length, height, coverDepth, .005, .05, 10);
	this.nPages = this.reader.spec.pages.length/2;
	this.leftStack = new DocexBookPageStack(this, this.cover, this.nPages, .95, .95, 2*BookModelConstants.gridSize, true);
	this.rightStack = new DocexBookPageStack(this, this.cover, this.nPages, .95, .95, 2*BookModelConstants.gridSize, false);
	this.maxCoverAngle = 1.*Math.PI/2;
	this.minCoverAngle = .001*Math.PI/2;
	this.modelRotation = 0;
	this.leftCoverAng = -.01;
	this.rightCoverAng = -.01;
	this.openCoverAng = .9;
	this.inc = 1./30.;
	this.isAnimating = false;
	this.page = new DocexBookPage(this);
	this.page.model.visible = false;
	
	this.model = new THREE.Group();
	this.model.add(this.cover.model);
	this.model.add(this.leftStack.model);
	this.model.add(this.rightStack.model);
	this.model.frustumCulled = false;
	
	this.buf1 = new THREE.Vector3();
	this.buf2 = new THREE.Vector3();
	this.bufv = [0, 0];
	
	this.update();
}

DocexBookModel.prototype.setLeftTex = function(tex) {this.leftStack.setTex(tex);}
DocexBookModel.prototype.setRightTex = function(tex) {this.rightStack.setTex(tex);}

function docexSmooth(x) {return 3*x*x-2*x*x*x;}
function docexSmoothCover(x, openCoverAng)
{
	if (x <= openCoverAng)
	{
		x /= openCoverAng;
		return docexSmooth(x)*openCoverAng;
	}
	return x;
}
function docexSudden(x) {return x*x*x;}

DocexBookModel.prototype.getCoverAngle = function(v)
{
	return this.minCoverAngle+v*(this.maxCoverAngle-this.minCoverAngle);
}

DocexBookModel.prototype.update = function()
{
	var coverNeedsUpdate = false, leftStackNeedsUpdate = false, rightStackNeedsUpdate = false;
	
	var leftPagesTarget = Math.max(0, Math.min(this.nPages, (this.reader.currentPage+1)/2));
	var rightPagesTarget = Math.max(0, this.nPages-leftPagesTarget-(this.reader.hand.active ? 1 : 0));
	var flatnessTarget = this.reader.zoomed ? 1 : 0;
	var stackNeedsUpdate = leftPagesTarget != this.leftStack.nStackPages || rightPagesTarget != this.rightStack.nStackPages || this.leftStack.flatness != flatnessTarget;
	
	var leftCoverClosed = this.reader.currentPage == -3, rightCoverClosed = this.reader.currentPage == this.reader.spec.pages.length+1;
	var leftCoverAngTarget = leftCoverClosed || rightCoverClosed ? 0 : this.reader.zoomed ? 1 : this.openCoverAng;
	var rightCoverAngTarget = leftCoverClosed || rightCoverClosed ? 0 : this.reader.zoomed ? 1 : this.openCoverAng;
	var modelRotationTarget = leftCoverClosed ? 1 : rightCoverClosed ? -1 : 0;
	var coverNeedsUpdate = leftCoverAngTarget != this.leftCoverAng || rightCoverAngTarget != this.rightCoverAng;
	var modelTransformNeedsUpdate = modelRotationTarget != this.modelRotation;
	//console.log(leftCoverClosed+" "+rightCoverClosed);
	if (coverNeedsUpdate)
	{
		if (Math.abs(this.leftCoverAng-leftCoverAngTarget) < this.inc)
			this.leftCoverAng = leftCoverAngTarget;
		else this.leftCoverAng = Math.max(0, Math.min(1, this.leftCoverAng+this.inc*(this.leftCoverAng < leftCoverAngTarget ? 1 : -1)));
		if (Math.abs(this.rightCoverAng-rightCoverAngTarget) < this.inc)
			this.rightCoverAng = rightCoverAngTarget;
		else this.rightCoverAng = Math.max(0, Math.min(1, this.rightCoverAng+this.inc*(this.rightCoverAng < rightCoverAngTarget ? 1 : -1)));
		this.cover.setAngle(this.getCoverAngle(docexSmoothCover(this.leftCoverAng, this.openCoverAng)), this.getCoverAngle(docexSmoothCover(this.rightCoverAng, this.openCoverAng)));
	}
	if (stackNeedsUpdate || coverNeedsUpdate)
	{
		if (Math.abs(this.leftStack.flatness-flatnessTarget) < this.inc)
			this.leftStack.flatness = flatnessTarget;
		else this.leftStack.flatness += this.leftStack.flatness > flatnessTarget ? -this.inc : this.inc;
		this.rightStack.flatness = this.leftStack.flatness;
		this.leftStack.nStackPages = leftPagesTarget;
		this.rightStack.nStackPages = rightPagesTarget;
		this.leftStack.updateStack();
		this.rightStack.updateStack();
	}
	if (modelTransformNeedsUpdate)
	{
		if (Math.abs(this.modelRotation-modelRotationTarget) < this.inc)
			this.modelRotation = modelRotationTarget;
		else this.modelRotation = Math.max(-1, Math.min(1, this.modelRotation+this.inc*(this.modelRotation < modelRotationTarget ? 1 : -1)));
		this.model.rotation.set(.05*Math.PI*(docexSudden(1-Math.abs(this.modelRotation))), .5*Math.PI*docexSudden(this.modelRotation), 0);
		this.model.position.set(-.5*this.length*this.modelRotation, 0, this.reader.heightBound ? 0 : Math.abs(this.modelRotation));
	}
	this.page.model.rotation.copy(this.model.rotation);
	this.page.model.position.copy(this.model.position);
	
	this.isAnimating = stackNeedsUpdate || coverNeedsUpdate || modelTransformNeedsUpdate;
	
	this.page.update();
}

DocexBookModel.prototype.closestToLine = function(a, v)
{
	this.buf1.copy(a);
	this.buf2.copy(v);
	this.model.worldToLocal(this.buf1);
	this.model.worldToLocal(this.buf2);
	var snode = this.page.paper.closestToLine(this.buf1, this.buf2);
	snode = this.page.paper.snodes[this.page.paper.snodes.length-1][snode.jindex];
	return snode;
}

DocexBookModel.prototype.fromPageToWorld = function(x, y, left, res)
{
	if (left) this.leftStack.toBookModel(1-x, y, res);
	else this.rightStack.toBookModel(x, y, res);
	this.model.localToWorld(res);
}

DocexBookModel.prototype.proj = function(px1, py1, vx1, vy1, px2, py2, vx2, vy2, res)
{
	res[0] = (px2*vy2-py2*vx2-px1*vy2+py1*vx2)/(vx1*vy2-vy1*vx2);
	res[1] = vx2*vx2 > vy2*vy2 ? (px1+res[0]*vx1-px2)/vx2 : (py1+res[0]*vy1-py2)/vy2;
}
DocexBookModel.prototype.toPage = function(p, d)
{
	var left = p.x-d.z*d.x < 0;
	var stack = left ? this.leftStack : this.rightStack;
	if (stack.nStackPages == 0)
		return [left, null];
	
	var mink = -1, cx = 0;
	for (var i=0;i<stack.projection.length-1;i++)
	{
		var vx = stack.projection[i+1][0]-stack.projection[i][0],
			vy = stack.projection[i+1][1]-stack.projection[i][1];
		this.proj(p.x, p.z, d.x, d.z, stack.projection[i][0], stack.projection[i][1], vx, vy, this.bufv);
		if (this.bufv[1] < 0 || this.bufv[1] > 1)
			continue;
		if (mink < 0 || this.bufv[0] < mink)
			{mink = this.bufv[0]; cx = (i+this.bufv[1])/stack.projection.length;}
	}
	if (mink < 0)
		return null;
	
	//TODO: ugly hack
	cx *= 1.03;
	if (left)
		cx = 1-cx;
	var cy = 1-(p.y+mink*d.y+.5*this.cover.coverHeight-stack.margin)/(this.cover.coverHeight-2*stack.margin);
	return [left, [cx, cy]];
}

function DocexTexLoader(reader)
{
	this.reader = reader;
	this.loader = new THREE.TextureLoader();
	this.spread = 3;
	this.max = 3;
	this.images = {};
	this.queued = 0;
}

var docexTexLoaderOnReq = function(loader)
{
	loader.queued++;
	loader.reader.layout.loadingSpan.innerHTML = "loading ("+loader.queued+")";
}
var docexTexLoaderOnRes = function(loader)
{
	loader.queued--;
	loader.reader.layout.loadingSpan.innerHTML = loader.queued == 0 ? "Ready" : "loading ("+loader.queued+")";
}

function docexTexLoaderBuildGenericOnError(loader) {return function(v) {docexTexLoaderOnRes(loader);};}

function docexTexLoaderBuildOnSuccess(pageImage) {return function(image) {docexTexLoaderOnRes(pageImage.loader); pageImage.tex = image; pageImage.loading = false; pageImage.loader.refresh();};}
function docexTexLoaderBuildOnTSuccess(pageImage) {return function(image) {docexTexLoaderOnRes(pageImage.loader); pageImage.ttex = image; pageImage.tloading = false; pageImage.loader.refresh();};}
function docexTexLoaderBuildOnError(pageImage) {return function(v) {docexTexLoaderOnRes(pageImage.loader); pageImage.tex = null; pageImage.loading = false; pageImage.loader.refresh();};}
function docexTexLoaderBuildOnTError(pageImage) {return function(v) {docexTexLoaderOnRes(pageImage.loader); pageImage.ttex = null; pageImage.tloading = false; pageImage.loader.refresh();};}

function DocexPageImage(loader, page, path, tpath)
{
	this.loader = loader;
	this.page = page;
	this.tex = null;
	this.ttex = null;
	this.loading = true;
	this.tloading = tpath !== null;

	loader.loader.load(path, 
		docexTexLoaderBuildOnSuccess(this),
		function(v) {},
		docexTexLoaderBuildOnError(this)
	);
	docexTexLoaderOnReq(loader);
	
	if (tpath !== null)
	{
		loader.loader.load(tpath, 
			docexTexLoaderBuildOnTSuccess(this),
			function(v) {},
			docexTexLoaderBuildOnTError(this)
		);
		docexTexLoaderOnReq(loader);
	}
}

DocexTexLoader.prototype.refresh = function()
{
	var hand = this.reader.hand;
	var from = Math.max(0, this.reader.currentPage-this.spread);
	var to = Math.min(this.reader.spec.pages.length-1, this.reader.currentPage+this.spread+(hand.active ? 2 : 0));
	
	for (var i=from;i<=to;i++)
	{
		if (!("p"+i in this.images) && this.queued < this.max)
			this.images["p"+i] = new DocexPageImage(this, i, this.reader.spec.pages[i].path, this.reader.spec.pages[i].tpath);
		else
		{
			var pi = "p"+i in this.images ? this.images["p"+i] : null;
			var tex = pi !== null && !pi.loading && pi.tex !== null ? pi.tex : this.reader.spec.loadingTex;
			var ttex = pi !== null && !pi.tloading && pi.ttex !== null ? pi.ttex : this.reader.spec.loadingTex;
			
			if (i == this.reader.leftPageIndex) this.reader.bookModel.setLeftTex(tex);
			else if (i == this.reader.rightPageIndex) this.reader.bookModel.setRightTex(tex);
			else if (i == this.reader.leftPageIndex+1 && hand.active) this.reader.bookModel.page.setFrontTex(this.reader.spec.pages[i].tpath !== null ? ttex : tex);
			else if (i == this.reader.leftPageIndex+2 && hand.active) this.reader.bookModel.page.setBackTex(this.reader.spec.pages[i].tpath !== null ? ttex : tex);
		}
	}
	
	for (var prop in this.images)
		if (!this.images[prop].loading && (this.images[prop].page < from || this.images[prop].page > to))
		{
			if (this.images[prop].tex != null)
				this.images[prop].tex.dispose();
			if (this.images[prop].ttex != null)
				this.images[prop].ttex.dispose();
			delete this.images[prop];
		}
	//console.log(Object.keys(this.images).length);
}

function docexTexLoaderBuildOnSideSuccess(loader, stack) {return function(tex) {docexTexLoaderOnRes(loader); stack.stackSideMesh.material.color = new THREE.Color(0xffffff); stack.stackSideMesh.material.map = tex; stack.stackSideMesh.material.needsUpdate = true;};}
DocexTexLoader.prototype.loadSideTex = function(stack)
{
	var path = stack.left ? this.reader.spec.leftSide : this.reader.spec.rightSide;
	if (path !== null)
	{
		this.loader.load(path, 
			docexTexLoaderBuildOnSideSuccess(this, stack),
			function(v) {},
			docexTexLoaderBuildGenericOnError(this)
		);
		docexTexLoaderOnReq(this);
	}
}

function docexTexLoaderBuildOnOuterCoverSuccess(loader, cover) {return function(tex) {docexTexLoaderOnRes(loader); cover.meshes[cover.outer].material.map = tex; cover.meshes[cover.outer].material.needsUpdate = true;};}
function docexTexLoaderBuildOnInnerCoverSuccess(loader, cover) {return function(tex) {docexTexLoaderOnRes(loader); cover.meshes[cover.inner].material.map = tex; cover.meshes[cover.inner].material.needsUpdate = true;};}
DocexTexLoader.prototype.loadCoverTex = function(cover)
{
	this.loader.load(this.reader.spec.cover, 
		docexTexLoaderBuildOnOuterCoverSuccess(this, cover),
		function(v) {},
		docexTexLoaderBuildGenericOnError(this)
	);
	docexTexLoaderOnReq(this);
	
	this.loader.load(this.reader.spec.innerCover, 
		docexTexLoaderBuildOnInnerCoverSuccess(this, cover),
		function(v) {},
		docexTexLoaderBuildGenericOnError(this)
	);
	docexTexLoaderOnReq(this);
}

/** @namespace */
var THREEx		= THREEx 		|| {};
THREEx.FullScreen	= THREEx.FullScreen	|| {};
THREEx.FullScreen.available	= function() {return this._hasWebkitFullScreen || this._hasMozFullScreen;}
THREEx.FullScreen.activated	= function()
{
	if (this._hasWebkitFullScreen) {return document.webkitIsFullScreen;}
	else if (this._hasMozFullScreen) {return document.mozFullScreen;}
	else {console.assert(false);}
}
THREEx.FullScreen.request	= function(element)
{
	element	= element	|| document.body;
	if (this._hasWebkitFullScreen) {element.webkitRequestFullScreen();}
	else if (this._hasMozFullScreen) {element.mozRequestFullScreen();}
	else {console.assert(false);}
}
THREEx.FullScreen.cancel	= function()
{
	if (this._hasWebkitFullScreen) {document.webkitCancelFullScreen();}
	else if (this._hasMozFullScreen) {document.mozCancelFullScreen();}
	else {console.assert(false);}
}
THREEx.FullScreen._hasWebkitFullScreen	= 'webkitCancelFullScreen' in document	? true : false;	
THREEx.FullScreen._hasMozFullScreen	= 'mozCancelFullScreen' in document	? true : false;

var docexSliderFormatVal = function(num, reader)
{
    num = 2*Math.floor(num/2);
    if (num == -2)
    	return "-";
    if (num > reader.spec.pages.length)
    	return "-";
    if (num == 0)
        return 1;
    if (num == reader.spec.pages.length)
        return reader.spec.pages.length;
    return num+"-"+(num+1);
}

var docexZoomSvg = `<svg version="1.1" xmlns="http://www.w3.org/2000/svg" x="0px" y="0px" viewBox="0 0 279.965 279.965">
<g>
	<path style="fill:#2E4EFD;" d="M272.698,272.712c-9.67,9.67-25.342,9.67-35.021,0l-99.243-99.235
		c-9.661-9.67-9.661-25.351,0-35.038c9.67-9.661,25.36-9.661,35.03,0l99.235,99.252
		C282.368,247.369,282.368,263.042,272.698,272.712z"/>
	<polygon style="fill:#2743CE;" points="201.274,166.257 148.786,166.257 148.786,183.829 218.723,253.757 218.793,253.757 
		218.793,183.777 	"/>
	<path style="fill:#2E4EFD;" d="M118.15,0c65.246,0,118.137,52.881,118.137,118.137c0,65.238-52.899,118.119-118.137,118.119
		c-65.246,0-118.137-52.881-118.137-118.119C0.013,52.881,52.903,0,118.15,0z"/>
	<path style="fill:#B3C0F9;" d="M118.15,26.253c50.746,0,91.884,41.129,91.884,91.884c0,50.746-41.138,91.884-91.884,91.884
		s-91.884-41.138-91.884-91.884S67.412,26.253,118.15,26.253z"/>
	<!-- path style="fill:#3DB39E;" d="M83.146,104.993h21.877v-21.86c0-7.263,5.881-13.144,13.126-13.144s13.126,5.881,13.126,13.144
		v21.86h21.877c7.246,0,13.126,5.881,13.126,13.126c0,7.263-5.881,13.135-13.126,13.135h-21.877v21.886
		c0,7.246-5.881,13.118-13.126,13.118c-7.246,0-13.126-5.872-13.126-13.118v-21.886H83.146c-7.246,0-13.126-5.872-13.126-13.135
		C70.02,110.873,75.901,104.993,83.146,104.993z"/ -->
</g>
</svg>`;
var docexZoomInSvg = `<svg version="1.1" xmlns="http://www.w3.org/2000/svg" x="0px" y="0px" viewBox="0 0 279.965 279.965">
<g>
	<path style="fill:#2E4EFD;" d="M272.698,272.712c-9.67,9.67-25.342,9.67-35.021,0l-99.243-99.235
		c-9.661-9.67-9.661-25.351,0-35.038c9.67-9.661,25.36-9.661,35.03,0l99.235,99.252
		C282.368,247.369,282.368,263.042,272.698,272.712z"/>
	<polygon style="fill:#2743CE;" points="201.274,166.257 148.786,166.257 148.786,183.829 218.723,253.757 218.793,253.757 
		218.793,183.777 	"/>
	<path style="fill:#2E4EFD;" d="M118.15,0c65.246,0,118.137,52.881,118.137,118.137c0,65.238-52.899,118.119-118.137,118.119
		c-65.246,0-118.137-52.881-118.137-118.119C0.013,52.881,52.903,0,118.15,0z"/>
	<path style="fill:#B3C0F9;" d="M118.15,26.253c50.746,0,91.884,41.129,91.884,91.884c0,50.746-41.138,91.884-91.884,91.884
		s-91.884-41.138-91.884-91.884S67.412,26.253,118.15,26.253z"/>
	<path style="fill:#2E4EFD;" d="M83.146,104.993h21.877v-21.86c0-7.263,5.881-13.144,13.126-13.144s13.126,5.881,13.126,13.144
		v21.86h21.877c7.246,0,13.126,5.881,13.126,13.126c0,7.263-5.881,13.135-13.126,13.135h-21.877v21.886
		c0,7.246-5.881,13.118-13.126,13.118c-7.246,0-13.126-5.872-13.126-13.118v-21.886H83.146c-7.246,0-13.126-5.872-13.126-13.135
		C70.02,110.873,75.901,104.993,83.146,104.993z"/>
</g>
</svg>`;
var docexZoomOutSvg = `<svg version="1.1" xmlns="http://www.w3.org/2000/svg" x="0px" y="0px" viewBox="0 0 279.965 279.965">
<g>
	<path style="fill:#2E4EFD;" d="M272.698,272.712c-9.67,9.67-25.342,9.67-35.021,0l-99.243-99.235
		c-9.661-9.67-9.661-25.351,0-35.038c9.67-9.661,25.36-9.661,35.03,0l99.235,99.252
		C282.368,247.369,282.368,263.042,272.698,272.712z"/>
	<polygon style="fill:#2743CE;" points="201.274,166.257 148.786,166.257 148.786,183.829 218.723,253.757 218.793,253.757 
		218.793,183.777 	"/>
	<path style="fill:#2E4EFD;" d="M118.15,0c65.246,0,118.137,52.881,118.137,118.137c0,65.238-52.899,118.119-118.137,118.119
		c-65.246,0-118.137-52.881-118.137-118.119C0.013,52.881,52.903,0,118.15,0z"/>
	<path style="fill:#B3C0F9;" d="M118.15,26.253c50.746,0,91.884,41.129,91.884,91.884c0,50.746-41.138,91.884-91.884,91.884
		s-91.884-41.138-91.884-91.884S67.412,26.253,118.15,26.253z"/>
	<path style="fill:#2E4EFD;" d="M83.143,104.993h70.007c7.246,0,13.126,5.881,13.126,13.126c0,7.263-5.881,13.135-13.126,13.135
		H83.143c-7.246,0-13.126-5.872-13.126-13.135C70.017,110.873,75.897,104.993,83.143,104.993z"/>
</g>
</svg>`;
var docexBackSvg = `<svg version="1.1" xmlns="http://www.w3.org/2000/svg" x="0px" y="0px" viewBox="0 0 52.502 52.502">
<g>
	<path style="fill:#2E4EFD;" d="M21.524,16.094V4.046L1.416,23.998l20.108,20.143V32.094c0,0,17.598-4.355,29.712,16
		c0,0,3.02-15.536-10.51-26.794C40.727,21.299,34.735,15.696,21.524,16.094z"/>
	<path style="fill:#2E4EFD;" d="M51.718,50.857l-1.341-2.252C40.163,31.441,25.976,32.402,22.524,32.925v13.634L0,23.995
		L22.524,1.644v13.431c12.728-0.103,18.644,5.268,18.886,5.494c13.781,11.465,10.839,27.554,10.808,27.715L51.718,50.857z
		 M25.645,30.702c5.761,0,16.344,1.938,24.854,14.376c0.128-4.873-0.896-15.094-10.41-23.01c-0.099-0.088-5.982-5.373-18.533-4.975
		l-1.03,0.03V6.447L2.832,24.001l17.692,17.724V31.311l0.76-0.188C21.354,31.105,23.014,30.702,25.645,30.702z"/>
</g>
</svg>`;
var docexLeftSvg = `<svg version="1.1" xmlns="http://www.w3.org/2000/svg" x="0px" y="0px" viewBox="0 0 492 492">
<g>
	<path d="M198.608,246.104L382.664,62.04c5.068-5.056,7.856-11.816,7.856-19.024c0-7.212-2.788-13.968-7.856-19.032l-16.128-16.12    
		C361.476,2.792,354.712,0,347.504,0s-13.964,2.792-19.028,7.864L109.328,227.008c-5.084,5.08-7.868,11.868-7.848,19.084    
		c-0.02,7.248,2.76,14.028,7.848,19.112l218.944,218.932c5.064,5.072,11.82,7.864,19.032,7.864c7.208,0,13.964-2.792,19.032-7.864    
		l16.124-16.12c10.492-10.492,10.492-27.572,0-38.06L198.608,246.104z" fill="#2E4EFD"/>
</g>
</svg>`;
var docexRightSvg = `<svg version="1.1" xmlns="http://www.w3.org/2000/svg" x="0px" y="0px" viewBox="0 0 492.004 492.004">
<g>
	<path d="M382.678,226.804L163.73,7.86C158.666,2.792,151.906,0,144.698,0s-13.968,2.792-19.032,7.86l-16.124,16.12    
		c-10.492,10.504-10.492,27.576,0,38.064L293.398,245.9l-184.06,184.06c-5.064,5.068-7.86,11.824-7.86,19.028    
		c0,7.212,2.796,13.968,7.86,19.04l16.124,16.116c5.068,5.068,11.824,7.86,19.032,7.86s13.968-2.792,19.032-7.86L382.678,265    
		c5.076-5.084,7.864-11.872,7.848-19.088C390.542,238.668,387.754,231.884,382.678,226.804z" fill="#2E4EFD"/>
</g>
</svg>`;
var docexFullscreenSvg = `<svg version="1.1" xmlns="http://www.w3.org/2000/svg" x="0px" y="0px" viewBox="0 0 223.453 223.453">
<g>
	<path style="fill:#2E4EFD;" d="M55.391,29.571l11.585-10.968c7.741-7.616,5.739-16.207-4.461-18.594H13.448
		C5.313,0.304-0.131,2.762,0.138,13.516L0.066,62.637c2.065,10.128,9.136,12.104,15.733,4.398l13.024-12.309l41.165,41.147
		c3.576,3.567,9.377,3.576,12.944,0.018l12.935-12.935c3.567-3.558,3.558-9.368-0.009-12.935
		C95.858,70.021,55.391,29.571,55.391,29.571z M82.682,127.884c-3.549-3.558-9.341-3.558-12.899,0.018l-41.04,41.004l-12.989-12.291
		c-6.57-7.688-13.623-5.712-15.688,4.38L0.146,210c-0.259,10.691,5.149,13.176,13.275,13.453h48.951
		c10.155-2.405,12.157-10.959,4.443-18.531l-11.549-10.933l40.316-40.334c3.558-3.558,3.567-9.35,0.009-12.89
		C95.59,140.765,82.682,127.884,82.682,127.884z M140.76,95.882c3.549,3.558,9.35,3.549,12.908-0.018l41.049-41.147l12.989,12.309
		c6.552,7.706,13.605,5.73,15.67-4.398l-0.072-49.121C223.564,2.753,218.137,0.295,210.031,0h-48.987
		c-10.164,2.396-12.113,10.986-4.398,18.594l11.567,10.977L127.87,70.003c-3.558,3.567-3.558,9.377,0,12.935
		C127.87,82.938,140.76,95.882,140.76,95.882z M207.742,156.606l-12.953,12.291l-40.906-41.004
		c-3.558-3.576-9.341-3.576-12.872-0.018l-12.863,12.881c-3.54,3.54-3.531,9.333,0.018,12.89l40.199,40.316l-11.54,10.951
		c-7.679,7.589-5.712,16.153,4.416,18.531h48.835c8.09-0.277,13.525-2.762,13.239-13.453l0.072-49.005
		C221.32,150.902,214.285,148.927,207.742,156.606z"/>
</g>
</svg>`;
var docexUnfullscreenSvg = `<svg version="1.1" xmlns="http://www.w3.org/2000/svg" x="0px" y="0px" viewBox="0 0 223.453 223.453">
<g>
	<path d="m 180.32314,154.39074 11.585,-10.968 c 7.741,-7.616 5.739,-16.207 -4.461,-18.594 h -49.067 c -8.135,0.295 -13.579,2.753 -13.31,13.507 l -0.072,49.121 c 2.065,10.128 9.136,12.104 15.733,4.398 l 13.024,-12.309 41.165,41.147 c 3.576,3.567 9.377,3.576 12.944,0.018 l 12.935,-12.935 c 3.567,-3.558 3.558,-9.368 -0.009,-12.935 0,0 -40.467,-40.45 -40.467,-40.45 z" style="fill:#2E4EFD" />
	<path d="m 207.81721,2.7344333 c -3.549,-3.5579998 -9.341,-3.5579998 -12.899,0.018 l -41.04,41.0040007 -12.989,-12.291 c -6.57,-7.688 -13.623,-5.712 -15.688,4.38 l 0.08,49.005 c -0.259,10.690999 5.149,13.175999 13.275,13.452999 h 48.951 c 10.155,-2.405 12.157,-10.958999 4.443,-18.530999 l -11.549,-10.933 40.316,-40.334 c 3.558,-3.558 3.567,-9.35 0.009,-12.89 -0.001,0 -12.909,-12.8810007 -12.909,-12.8810007 z" style="fill:#2E4EFD" />
	<path d="m 15.608393,220.7325 c 3.549,3.558 9.35,3.549 12.908,-0.018 l 41.049,-41.147 12.989,12.309 c 6.552,7.706 13.605,5.73 15.67,-4.398 l -0.072,-49.121 c 0.26,-10.754 -5.167,-13.212 -13.273,-13.507 h -48.987 c -10.164,2.396 -12.113,10.986 -4.398,18.594 l 11.567,10.977 -40.3430004,40.432 c -3.55800001,3.567 -3.55800001,9.377 0,12.935 0,0 12.8900004,12.944 12.8900004,12.944 z" style="fill:#2E4EFD" />
	<path d="m 82.324544,31.508941 -12.953,12.291 -40.906,-41.0039995 c -3.558,-3.57600004 -9.341,-3.57600004 -12.872,-0.018 L 2.7305436,15.658941 c -3.54,3.54 -3.531,9.333 0.018,12.89 l 40.1990004,40.316 -11.54,10.951 c -7.679,7.589 -5.712,16.153 4.416,18.530999 h 48.835 c 8.09,-0.277 13.524997,-2.761999 13.239,-13.452999 l 0.072,-49.005 c -2.066997,-10.084 -9.101997,-12.059 -15.644997,-4.38 z" style="fill:#2E4EFD" />
</g>
</svg>`;
var docexInfoSvg = `<svg version="1.1" xmlns="http://www.w3.org/2000/svg" x="0px" y="0px" viewBox="0 0 330 330">
<g>
	<path d="M165,0C74.019,0,0,74.02,0,165.001C0,255.982,74.019,330,165,330s165-74.018,165-164.999C330,74.02,255.981,0,165,0z    M165,300c-74.44,0-135-60.56-135-134.999C30,90.562,90.56,30,165,30s135,60.562,135,135.001C300,239.44,239.439,300,165,300z" fill="#2E4EFD"/>
	<path d="M164.998,70c-11.026,0-19.996,8.976-19.996,20.009c0,11.023,8.97,19.991,19.996,19.991   c11.026,0,19.996-8.968,19.996-19.991C184.994,78.976,176.024,70,164.998,70z" fill="#2E4EFD"/>
	<path d="M165,140c-8.284,0-15,6.716-15,15v90c0,8.284,6.716,15,15,15c8.284,0,15-6.716,15-15v-90C180,146.716,173.284,140,165,140z   " fill="#2E4EFD"/>
</g>
</svg>`;
var docexPlaySvg = `<svg version="1.1" xmlns="http://www.w3.org/2000/svg" x="0px" y="0px" viewBox="0 0 438.533 438.533">
<g>
	<path d="M409.133,109.203c-19.608-33.592-46.205-60.189-79.798-79.796C295.736,9.801,259.058,0,219.273,0    
		c-39.781,0-76.47,9.801-110.063,29.407c-33.595,19.604-60.192,46.201-79.8,79.796C9.801,142.8,0,179.489,0,219.267    
		c0,39.78,9.804,76.463,29.407,110.062c19.607,33.592,46.204,60.189,79.799,79.798c33.597,19.605,70.283,29.407,110.063,29.407    
		s76.47-9.802,110.065-29.407c33.593-19.602,60.189-46.206,79.795-79.798c19.603-33.596,29.403-70.284,29.403-110.062    
		C438.533,179.485,428.732,142.795,409.133,109.203z M353.742,297.208c-13.894,23.791-32.736,42.64-56.527,56.534    
		c-23.791,13.894-49.771,20.834-77.945,20.834c-28.167,0-54.149-6.94-77.943-20.834c-23.791-13.895-42.633-32.743-56.527-56.534    
		c-13.897-23.791-20.843-49.772-20.843-77.941c0-28.171,6.949-54.152,20.843-77.943c13.891-23.791,32.738-42.637,56.527-56.53    
		c23.791-13.895,49.772-20.84,77.943-20.84c28.173,0,54.154,6.945,77.945,20.84c23.791,13.894,42.634,32.739,56.527,56.53    
		c13.895,23.791,20.838,49.772,20.838,77.943C374.58,247.436,367.637,273.417,353.742,297.208z" fill="#2E4EFD"/>
	<path d="M328.911,203.561l-155.32-91.36c-5.896-3.617-11.991-3.711-18.271-0.284c-6.09,3.615-9.136,8.942-9.136,15.984v182.725    
		c0,7.04,3.046,12.371,9.136,15.985c3.046,1.522,6.09,2.286,9.135,2.286c3.234,0,6.283-0.856,9.136-2.567l155.32-91.361    
		c6.088-3.426,9.134-8.661,9.134-15.699C338.045,212.23,334.992,206.988,328.911,203.561z" fill="#2E4EFD"/>
</g>
</svg>`;
var docexLogo = `<svg version="1.1" xmlns="http://www.w3.org/2000/svg" x="0px" y="0px" style="background: #2E4EFD" viewBox="0 0 940.000000 198.000000">
<g transform="translate(0.000000,198.000000) scale(0.100000,-0.100000)" fill="#FFFFFF" stroke="none">
	<path d="M0 990 l0 -990 4700 0 4700 0 0 990 0 990 -4700 0 -4700 0 0 -990z m2195 855 c117 -35 228 -150 266 -277 17 -56 21 -88 17 -176 -3 -95 -7 -115 -35 -175 -42 -89 -119 -169 -202 -210 -62 -30 -71 -32 -181 -32 -104 0 -120 3 -170 26 -108 52 -184 141 -220 257 -27 87 -27 227 0 314 39 126 147 234 273 273 57 18 193 18 252 0z m969 -10 l68 -24 25 25 c46 46 49 38 58 -138 6 -104 5 -128 -7 -128 -7 0 -23 26 -35 58 -47 120 -140 186 -259 185 -181 -2 -288 -148 -288 -393 -1 -185 49 -289 166 -349 59 -30 77 -34 140 -34 97 0 153 25 228 103 55 57 80 73 80 49 0 -18 -57 -94 -100 -133 -75 -67 -118 -81 -255 -81
		-116 0 -123 1 -187 33 -208 102 -288 375 -178 609 31 67 102 150 156 184 105 64 263 78 388 34z m-1905 -11 c130 -29 223 -109 273 -234 32 -81 32 -260 -1 -343 -47 -119 -142 -203 -269 -239 -56 -15 -104 -18 -314 -18 -159 0 -248 4 -248 10 0 6 8 10 19 10 42 0 80 22 90 52 15 42 15 664 0 706 -10 30 -48 52 -90 52 -11 0 -19 5 -19 10 0 18 479 13 559 -6z m2341 -79 c16 -8 31 -24 35 -35 6 -20 10 -20 2559 -20 l2552 0 237 -237 237 -238 0 -372 c0 -302 3 -374 14 -380 40 -24 47 -93 11 -128 -48 -49 -130 -22 -141 46 -5 28 -1 41 20 66 l26 31 0 353 0 354 -217 217 -218 218 -2532 0 -2531 0 -30 -30 c-65 -64 -172
		-18 -172 73 0 29 34 75 64 87 31 13 54 12 86 -5z m710 -565 l0 -40 -305 0 c-168 0 -305 3 -305 6 0 16 24 45 48 58 22 12 78 15 295 16 l267 0 0 -40z m308 7 c12 -18 22 -36 22 -40 0 -4 -49 -7 -108 -7 -107 0 -109 0 -128 28 -40 54 -44 52 78 52 l113 0 23 -33z m542 26 c0 -4 -10 -22 -22 -40 l-23 -33 -113 0 -112 0 26 40 27 40 108 0 c60 0 109 -3 109 -7z m700 -18 c12 -11 26 -28 31 -37 9 -17 -9 -18 -331 -18 l-340 0 0 40 0 41 309 -3 c292 -3 310 -4 331 -23z m280 -15 l0 -40 -90 0 -90 0 0 40 0 40 90 0 90 0 0 -40z m1118 29 c26 -9 62 -46 62 -64 0 -3 -148 -5 -330 -5 -267 0 -330 3 -330 13 0 15 37 51 60 60 29
		11 506 7 538 -4z m740 0 c26 -9 62 -46 62 -64 0 -3 -151 -5 -335 -5 l-335 0 0 40 0 40 289 0 c179 0 300 -4 319 -11z m752 -29 l0 -40 -310 0 -310 0 12 24 c23 51 50 56 341 56 l267 0 0 -40z m-4440 -90 l0 -40 -310 0 -310 0 0 40 0 40 310 0 310 0 0 -40z m374 6 c14 -19 26 -37 26 -40 0 -3 -49 -6 -109 -6 l-110 0 -25 33 c-15 19 -26 37 -26 40 0 4 49 7 109 7 l109 0 26 -34z m416 28 c0 -3 -12 -21 -26 -40 l-26 -34 -109 0 c-60 0 -109 3 -109 7 0 3 11 21 26 40 l25 33 110 0 c60 0 109 -3 109 -6z m800 -34 l0 -40 -340 0 -340 0 0 40 0 40 340 0 340 0 0 -40z m240 0 l0 -40 -90 0 -90 0 0 40 0 40 90 0 90 0 0 -40z m1188 -2
		l3 -38 -340 0 -341 0 0 40 0 40 338 -2 337 -3 3 -37z m740 0 l3 -38 -340 0 -341 0 0 40 0 40 338 -2 337 -3 3 -37z m682 2 l0 -40 -315 0 -315 0 0 33 c0 19 3 37 7 40 3 4 145 7 315 7 l308 0 0 -40z m-4880 -90 l0 -40 -90 0 -90 0 0 40 0 40 90 0 90 0 0 -40z m876 9 l22 -31 23 31 23 31 108 0 c59 0 108 -3 108 -7 0 -4 -10 -22 -22 -40 l-23 -33 -217 0 -216 0 -21 34 c-12 18 -21 36 -21 40 0 3 48 6 108 6 l107 0 21 -31z m654 -9 l0 -40 -90 0 -90 0 0 40 0 40 90 0 90 0 0 -40z m500 0 l0 -40 -85 0 -85 0 0 40 0 40 85 0 85 0 0 -40z m240 0 l0 -40 -90 0 -90 0 0 40 0 40 90 0 90 0 0 -40z m680 0 l0 -40 -85 0 -85 0 0 40 0 40
		85 0 85 0 0 -40z m510 0 l0 -40 -90 0 -90 0 0 40 0 40 90 0 90 0 0 -40z m230 0 l0 -40 -85 0 -85 0 0 40 0 40 85 0 85 0 0 -40z m508 3 l-3 -38 -87 -3 -88 -3 0 41 0 40 91 0 90 0 -3 -37z m232 -3 l0 -40 -90 0 -90 0 0 40 0 40 90 0 90 0 0 -40z m-3990 -90 l0 -40 -310 0 -310 0 0 40 0 40 310 0 310 0 0 -40z m654 28 c-4 -7 -16 -25 -26 -40 l-19 -28 -148 0 -149 0 -26 34 c-14 19 -26 37 -26 40 0 3 90 6 201 6 157 0 199 -3 193 -12z m936 -28 l0 -40 -340 0 -340 0 0 40 0 40 340 0 340 0 0 -40z m240 0 l0 -40 -90 0 -90 0 0 40 0 40 90 0 90 0 0 -40z m680 0 l0 -40 -85 0 -85 0 0 40 0 40 85 0 85 0 0 -40z m510 0 l0 -40 -90 0
		-90 0 0 40 0 40 90 0 90 0 0 -40z m721 22 c-5 -9 -19 -26 -31 -37 -21 -19 -39 -20 -326 -23 l-304 -3 0 40 0 41 335 0 c317 0 335 -1 326 -18z m699 -22 l0 -40 -315 0 -315 0 0 40 0 40 315 0 315 0 0 -40z m-8313 -75 l22 -25 1288 0 1289 0 249 -250 249 -250 594 0 593 0 23 27 c34 39 98 39 132 0 74 -86 -38 -195 -124 -122 l-30 25 -609 0 -608 0 -250 250 -250 250 -1273 0 -1274 0 -30 -25 c-42 -35 -91 -34 -129 4 -31 31 -35 53 -19 95 22 58 114 71 157 21z m3873 -15 l0 -40 -310 0 -310 0 0 40 0 40 310 0 310 0 0 -40z m590 31 c0 -6 -5 -13 -10 -16 -7 -4 -7 -12 0 -25 5 -10 10 -21 10 -24 0 -3 -61 -6 -135 -6 l-135 0
		12 25 c8 19 8 28 -2 40 -11 13 3 15 124 15 78 0 136 -4 136 -9z m988 -13 c-8 -13 -30 -31 -49 -40 -30 -16 -68 -18 -326 -18 l-293 0 0 40 0 40 341 0 341 0 -14 -22z m252 -18 l0 -40 -90 0 -90 0 0 40 0 40 90 0 90 0 0 -40z m680 0 l0 -40 -85 0 -85 0 0 40 0 40 85 0 85 0 0 -40z m510 0 l0 -40 -90 0 -90 0 0 40 0 40 90 0 90 0 0 -40z m680 25 c26 -13 50 -42 50 -59 0 -3 -151 -6 -335 -6 l-335 0 0 40 0 40 295 0 c245 0 301 -3 325 -15z m740 -25 l0 -40 -315 0 -315 0 0 40 0 40 315 0 315 0 0 -40z m-4880 -90 l0 -40 -90 0 -90 0 0 40 0 40 90 0 90 0 0 -40z m1074 7 c15 -19 26 -37 26 -40 0 -4 -90 -7 -200 -7 -110 0 -200 2
		-200 5 0 3 10 21 23 40 l22 35 152 0 152 0 25 -33z m456 -7 l0 -40 -90 0 -90 0 0 40 0 40 90 0 90 0 0 -40z m740 0 l0 -40 -90 0 -90 0 0 40 0 40 90 0 90 0 0 -40z m680 0 l0 -40 -85 0 -85 0 0 40 0 40 85 0 85 0 0 -40z m510 0 l0 -40 -90 0 -90 0 0 40 0 40 90 0 90 0 0 -40z m230 0 l0 -40 -85 0 -85 0 0 40 0 40 85 0 85 0 0 -40z m510 0 l0 -40 -90 0 -90 0 0 40 0 40 90 0 90 0 0 -40z m230 0 l0 -40 -90 0 -90 0 0 40 0 40 90 0 90 0 0 -40z m-8005 -50 c21 -20 38 -20 1331 -20 l1309 0 245 -245 245 -245 1088 0 1088 0 23 27 c34 39 98 39 132 0 74 -86 -38 -195 -124 -122 l-30 25 -1104 0 -1103 0 -245 245 -245 245 -1292 0
		-1292 0 -27 -25 c-75 -70 -196 21 -138 103 32 46 97 51 139 12z m3575 -35 l0 -35 -90 0 -90 0 0 35 0 35 90 0 90 0 0 -35z m1138 5 c12 -16 22 -32 22 -35 0 -3 -49 -5 -109 -5 -108 0 -110 1 -131 27 l-22 28 -20 -28 c-19 -27 -21 -27 -129 -27 -60 0 -109 3 -109 6 0 3 9 19 21 35 l20 29 218 0 217 0 22 -30z m392 -5 l0 -35 -90 0 -90 0 0 35 0 35 90 0 90 0 0 -35z m740 0 l0 -35 -90 0 -90 0 0 35 0 35 90 0 90 0 0 -35z m680 0 l0 -35 -85 0 -85 0 0 35 0 35 85 0 85 0 0 -35z m510 0 l0 -35 -90 0 -90 0 0 35 0 35 90 0 90 0 0 -35z m230 0 l0 -35 -85 0 -85 0 0 35 0 35 85 0 85 0 0 -35z m510 0 l0 -35 -90 0 -90 0 0 35 0 35 90 0
		90 0 0 -35z m230 0 l0 -35 -90 0 -90 0 0 35 0 35 90 0 90 0 0 -35z m-3990 -90 l0 -35 -310 0 -310 0 0 35 0 35 310 0 310 0 0 -35z m378 0 l-23 -35 -113 0 c-88 0 -111 3 -105 13 39 61 29 57 150 57 l113 0 -22 -35z m390 0 l23 -36 -116 3 -116 3 -20 33 -21 32 114 0 113 0 23 -35z m322 0 l0 -35 -90 0 -90 0 0 35 0 35 90 0 90 0 0 -35z m1190 0 l0 -35 -315 0 -315 0 0 35 0 35 315 0 315 0 0 -35z m740 0 l0 -35 -340 0 -340 0 0 35 0 35 340 0 340 0 0 -35z m230 0 l0 -35 -85 0 -85 0 0 35 0 35 85 0 85 0 0 -35z m510 0 l0 -35 -90 0 -90 0 0 35 0 35 90 0 90 0 0 -35z m680 0 l0 -35 -312 2 -313 3 -3 33 -3 32 315 0 316 0 0
		-35z m-4440 -90 l0 -35 -279 0 -278 0 -27 25 c-14 13 -26 29 -26 35 0 7 99 10 305 10 l305 0 0 -35z m330 29 c0 -3 -9 -19 -21 -35 l-20 -29 -115 0 -115 0 27 35 26 35 109 0 c60 0 109 -3 109 -6z m499 -23 c12 -16 21 -32 21 -35 0 -3 -49 -6 -109 -6 l-109 0 -26 35 -27 35 115 0 115 0 20 -29z m261 -6 l0 -35 -90 0 -90 0 0 35 0 35 90 0 90 0 0 -35z m1190 0 l0 -35 -278 0 c-296 0 -306 2 -332 51 -11 19 -5 19 300 19 l310 0 0 -35z m720 16 c-26 -49 -37 -51 -326 -51 -259 0 -272 1 -298 21 -14 11 -26 27 -26 35 0 12 51 14 330 14 325 0 331 0 320 -19z m250 -16 l0 -35 -85 0 -85 0 0 35 0 35 85 0 85 0 0 -35z m510 0 l0 -35 -90 0
		-90 0 0 35 0 35 90 0 90 0 0 -35z m680 0 l0 -35 -278 0 c-296 0 -306 2 -332 51 -11 19 -5 19 300 19 l310 0 0 -35z"/>
	<path d="M2003 1809 c-50 -12 -126 -76 -153 -130 -66 -128 -66 -389 -1 -518 65 -127 186 -176 309 -126 59 24 124 100 148 172 30 90 27 324 -4 414 -50 144 -166 218 -299 188z"/>
	<path d="M1040 1795 c-8 -2 -32 -6 -52 -10 l-38 -6 0 -364 0 -364 38 -8 c114 -22 224 4 301 73 92 83 137 234 113 376 -16 92 -40 146 -90 200 -48 51 -108 84 -182 98 -53 10 -62 10 -90 5z"/>
	<path d="M3515 1690 c-22 -24 -13 -66 15 -75 52 -16 90 36 55 75 -10 11 -26 20 -35 20 -9 0 -25 -9 -35 -20z"/>
	<path d="M336 804 c-29 -28 -12 -64 29 -64 43 0 62 50 26 70 -27 14 -37 13 -55 -6z"/>
	<path d="M4785 304 c-19 -20 -16 -43 8 -58 34 -22 73 27 47 59 -16 19 -35 19 -55 -1z"/>
	<path d="M199 644 c-17 -21 0 -49 31 -49 31 0 48 28 31 49 -8 9 -21 16 -31 16 -10 0 -23 -7 -31 -16z"/>
	<path d="M5665 164 c-19 -20 -16 -43 8 -58 34 -22 73 27 47 59 -16 19 -35 19 -55 -1z"/>
</g>
</svg>`;

function DocexLayout(reader)
{
	this.reader = reader;
	
	this.top = document.createElement("DIV");
	reader.topContainer.appendChild(this.top);
	this.top.style.cssText = "width: 100%; height: 100%; border: 0px; padding: 0px; margin: 0px; position: relative";
	
	this.container = document.createElement("DIV");
	this.top.appendChild(this.container);
	this.container.style.cssText = "z-index: 0; background-color: #f0f0f0; opacity: 1; border: 0px; padding: 0px; position: absolute";
	this.container.onmousedown = function(event) {reader.input.htmlMouseDown(event.clientX+document.body.scrollLeft+document.documentElement.scrollLeft, event.clientY);};
	this.container.onmouseup = function(event) {reader.input.htmlMouseUp();};
	this.container.onmousemove = function(event) {reader.input.htmlMouseMove(event.clientX+document.body.scrollLeft+document.documentElement.scrollLeft, event.clientY);};
	this.container.onmouseout = function(event) {reader.input.htmlMouseOut(event.clientX+document.body.scrollLeft+document.documentElement.scrollLeft, event.clientY);};
	
	this.roiContainer = document.createElement("DIV");
	this.top.appendChild(this.roiContainer);
	this.roiContainer.style.cssText = "background: black; overflow-y: scroll; position: absolute; opacity: 1; z-index: -1; border: 0px; padding: 0px; margin: 0px";
	
	var infoDiv = document.createElement("DIV");
	this.top.appendChild(infoDiv);
	infoDiv.style.cssText = "position: absolute; z-index: 1; background: white; opacity: .75; vertical-align: middle; padding: 2px";
	var infoIconSpan = document.createElement("SPAN");
	infoIconSpan.innerHTML = docexInfoSvg;
	this.infoIcon = infoIconSpan.children[0];
	infoIconSpan.removeChild(this.infoIcon);
	infoDiv.appendChild(this.infoIcon);
	infoDiv.appendChild(document.createTextNode(" "));
	this.infoIcon.style.cssText = "vertical-align: middle";
	this.infoSpan = document.createElement("SPAN");
	infoDiv.appendChild(this.infoSpan);
	this.infoSpan.style.cssText = "font-family: Arial; color: #2E4EFD; vertical-align: middle";
	
	this.toolbar = document.createElement("TABLE");
	this.top.appendChild(this.toolbar);
	this.toolbar.style.cssText = "line-height: 1; background: #ffffff; position: absolute; width: 100%; border: 0px; padding: 0px; margin: 0px";
	var tbr = document.createElement("TR");
	this.toolbar.appendChild(tbr);
	tbr.style.cssText = "border: 0px; padding: 0px";
	var tdc = document.createElement("TD");
	tbr.appendChild(tdc);
	tdc.style.cssText = "width: 36%; border: 0px; padding: 0px; margin: 0px";
	this.poweredBy = document.createElement("DIV");
	tdc.appendChild(this.poweredBy);
	this.poweredBy.style.cssText = "text-align: left";
	this.loadingSpan = document.createElement("SPAN");
	this.poweredBy.appendChild(this.loadingSpan);
	this.loadingSpan.style.cssText = "color: blue";
	this.poweredBy.appendChild(document.createElement("BR"));
	var it = document.createElement("I");
	this.poweredBy.appendChild(it);
	it.innerHTML = "Powered by <a href='http://www.docexplore.eu'>DocExplore</a>";
	//it.innerHTML = "Powered by <a href='http://www.docexplore.eu'>DocExplore</a>, an <a href='http://www.interreg4a-manche.eu/index.php?lang=en'>Interreg IV A</a> project";
	var buttons = document.createElement("TD");
	tbr.appendChild(buttons);
	buttons.style.cssText = "text-align: center; vertical-align: middle; width: 28%; border: 0px; padding: 0px; margin: 0px";
	
	this.prevBut = document.createElement("BUTTON");
	buttons.appendChild(this.prevBut);
	this.prevBut.style.cssText = "background: white; padding: 0px; border: 1px solid lightgray";
	this.prevBut.onclick = function() {reader.quickPrev();};
	this.prevBut.innerHTML = docexLeftSvg;
	this.zoomoutBut = document.createElement("BUTTON");
	buttons.appendChild(this.zoomoutBut);
	this.zoomoutBut.style.cssText = "display: none; background: white; padding: 0px; border: 1px solid lightgray";
	this.zoomoutBut.onmousedown = function() {reader.zoomOut = true;};
	this.zoomoutBut.onmouseup = function() {reader.zoomOut = false;};
	this.zoomoutBut.onmouseout = function() {reader.zoomOut = false;};
	this.zoomoutBut.innerHTML = docexZoomOutSvg;
	this.zoomBut = document.createElement("BUTTON");
	buttons.appendChild(this.zoomBut);
	this.zoomBut.style.cssText = "background: white; padding: 0px; border: 1px solid lightgray";
	this.zoomBut.onclick = function() {reader.zoom();};
	this.zoomBut.innerHTML = docexZoomSvg;
	this.backBut = document.createElement("BUTTON");
	buttons.appendChild(this.backBut);
	this.backBut.style.cssText = "display: none; background: white; padding: 0px; border: 1px solid lightgray";
	this.backBut.onclick = function() {reader.back();};
	this.backBut.innerHTML = docexBackSvg;
	this.zoominBut = document.createElement("BUTTON");
	buttons.appendChild(this.zoominBut);
	this.zoominBut.style.cssText = "display: none; background: white; padding: 0px; border: 1px solid lightgray";
	this.zoominBut.onmousedown = function() {reader.zoomIn = true;};
	this.zoominBut.onmouseup = function() {reader.zoomIn = false;};
	this.zoominBut.onmouseout = function() {reader.zoomIn = false;};
	this.zoominBut.innerHTML = docexZoomInSvg;
	this.nextBut = document.createElement("BUTTON");
	buttons.appendChild(this.nextBut);
	this.nextBut.style.cssText = "background: white; padding: 0px; border: 1px solid lightgray";
	this.nextBut.onclick = function() {reader.quickNext();};
	this.nextBut.innerHTML = docexRightSvg;
	
	tdc = document.createElement("TD");
	tbr.appendChild(tdc);
	tdc.style.cssText = "width: 18%; text-align: center; vertical-align: middle; border: 0px; padding: 0px; margin: 0px; margin: 0px";
	tdc.innerHTML = "Page&nbsp;";
	this.sliderDiv = tdc;
	this.sliderVal = document.createElement("INPUT");
	tdc.appendChild(this.sliderVal);
	this.sliderVal.style.cssText = "display: inline; border: 0px; padding: 0px; margin: 0px; width: initial; vertical-align: middle; text-align: center; background: lightgray";
	this.sliderVal.type = "text";
	this.sliderVal.disabled = true;
	this.sliderVal.value = "1";
	this.sliderVal.size = "7";
	tdc = document.createElement("TD");
	tbr.appendChild(tdc);
	tdc.style.cssText = "width: 18%; text-align: center; vertical-align: middle; border: 0px; padding: 0px; margin: 0px; margin: 0px";
	this.slider = document.createElement("INPUT");
	tdc.appendChild(this.slider);
	this.slider.type = "range";
	this.slider.value = "0";
	this.slider.min = "0";
	this.slider.max = "100";
	this.slider.style.cssText = "display: initial; width: 90%; border: 0px; padding: 0px; margin: 0px; margin: 0px";
	var sliderVal = this.sliderVal;
	var slider = this.slider;
	this.slider.onchange = function() {sliderVal.value = docexSliderFormatVal(slider.value, reader); reader.quickJump(2*Math.floor(slider.value/2)-1);};
	this.slider.oninput = function() {sliderVal.value = docexSliderFormatVal(slider.value, reader);};
	
	tdc = document.createElement("TD");
	tbr.appendChild(tdc);
	tdc.style.cssText = "text-align: center; vertical-align: middle; border: 0px; padding: 0px; margin: 0px; margin: 0px";
	this.fullscreen = document.createElement("BUTTON");
	tdc.appendChild(this.fullscreen);
	this.fullscreen.style.cssText = "background: white; padding: 0px; border: 1px solid lightgray";
	var top = this.top;
	this.fullscreen.onclick = function fullscreen()
	{
		if (!THREEx.FullScreen.activated())
		{
			if (THREEx.FullScreen.available())
				THREEx.FullScreen.request(top);
		}
		else THREEx.FullScreen.cancel();
	}
	this.fullscreen.innerHTML = docexFullscreenSvg;
	
	var startDiv = document.createElement("DIV");
	this.top.appendChild(startDiv);
	startDiv.style.cssText = "position: absolute; z-index: 2; width: 100%; height: 100%; text-align: center; vertical-align: middle; background: white; opacity: 1";
	var logo = document.createElement("DIV");
	startDiv.appendChild(logo);
	logo.style.cssText = "width: 75%; margin: auto; text-align: center; border: 0px; padding: 0px";
	logo.innerHTML = docexLogo;
	var titleSpan = document.createElement("SPAN");
	startDiv.appendChild(titleSpan);
	titleSpan.innerHTML = "<h1>"+reader.spec.name+"</h1>";
	var playDiv = document.createElement("BUTTON");
	startDiv.appendChild(playDiv);
	playDiv.style.cssText = "width: 20%; text-align: center; background: none; border: none";
	playDiv.innerHTML = docexPlaySvg;
	var fadeFunc = function()
	{
		if (startDiv.style.opacity == 0) 
			top.removeChild(startDiv);
		else
		{
			startDiv.style.opacity = Math.max(0, startDiv.style.opacity-.05);
			setTimeout(fadeFunc, 30);
		}
	};
	playDiv.onclick = function() {reader.start(); fadeFunc();};
	var descSpan = document.createElement("DIV");
	startDiv.appendChild(descSpan);
	descSpan.style.cssText = "text-align: center"
	descSpan.innerHTML = "<br/>"+reader.spec.desc;
	
	var ht = new Hammer(this.container);
	ht.get('pinch').set({ enable: true});
	ht.on('pinch', function(ev) {reader.input.pinch(ev.scale);})
	ht.on('pinchend', function(ev) {reader.input.pinchEnd();})
}

function docexLimitButtonSize(elem, size)
{
	if (elem.nodeName == "BUTTON")
	{
		var elemStyle = window.getComputedStyle(elem, null);
		var paddingv = parseInt(elemStyle.getPropertyValue("padding-top"))+
		parseInt(elemStyle.getPropertyValue("padding-bottom"))+
		parseInt(elemStyle.getPropertyValue("border-top-width"))+
		parseInt(elemStyle.getPropertyValue("border-bottom-width"));
		var paddingh = parseInt(elemStyle.getPropertyValue("padding-left"))+
		parseInt(elemStyle.getPropertyValue("padding-right"))+
		parseInt(elemStyle.getPropertyValue("border-left-width"))+
		parseInt(elemStyle.getPropertyValue("border-right-width"));
		
		elem.style.width = (size-paddingv+paddingh)+"px";
		elem.style.height = size+"px";
		
		var image = elem.children[0];
		image.style.width = (size-paddingv)+"px";
		image.style.height = (size-paddingv)+"px";
	}
	else
	{
		var children = elem.children;
		for (var i=0;i<children.length;i++)
			docexLimitButtonSize(children[i], size)
	}
}

var DocexDetector = {

    canvas: !! window.CanvasRenderingContext2D,
    webgl: ( function () { try { var canvas = document.createElement( 'canvas' ); return !! window.WebGLRenderingContext && ( canvas.getContext( 'webgl' ) || canvas.getContext( 'experimental-webgl' ) ); } catch( e ) { return false; } } )(),
    workers: !! window.Worker,
    fileapi: window.File && window.FileReader && window.FileList && window.Blob,

    getWebGLErrorMessage: function () {

        var element = document.createElement( 'div' );
        element.id = 'webgl-error-message';
        element.style.fontFamily = 'monospace';
        element.style.fontSize = '13px';
        element.style.fontWeight = 'normal';
        element.style.textAlign = 'center';
        element.style.background = '#fff';
        element.style.color = '#000';
        element.style.padding = '1.5em';
        element.style.width = '400px';
        element.style.margin = '5em auto 0';

        if ( ! this.webgl ) {

            element.innerHTML = window.WebGLRenderingContext ? [
                'Your graphics card does not seem to support <a href="http://khronos.org/webgl/wiki/Getting_a_WebGL_Implementation" style="color:#000">WebGL</a>.<br />',
                'Find out how to get it <a href="http://get.webgl.org/" style="color:#000">here</a>.'
            ].join( '\n' ) : [
                'Your browser does not seem to support <a href="http://khronos.org/webgl/wiki/Getting_a_WebGL_Implementation" style="color:#000">WebGL</a>.<br/>',
                'Find out how to get it <a href="http://get.webgl.org/" style="color:#000">here</a>.'
            ].join( '\n' );

        }

        return element;

    },

    addGetWebGLMessage: function ( parameters ) {

        var parent, id, element;

        parameters = parameters || {};

        parent = parameters.parent !== undefined ? parameters.parent : document.body;
        id = parameters.id !== undefined ? parameters.id : 'oldie';

        element = DocexDetector.getWebGLErrorMessage();
        element.id = id;

        parent.appendChild( element );

    }

};

if (!window.requestAnimationFrame)
{
	window.requestAnimationFrame = (function()
	{
		return window.webkitRequestAnimationFrame ||
			window.mozRequestAnimationFrame ||
			window.oRequestAnimationFrame ||
			window.msRequestAnimationFrame ||
			function(callback, element) {window.setTimeout(callback, 1000/60);};
	})();
}

function docexBuildRenderCallback(reader) {return function() {reader.render();};}

function DocexReader(xml, bookBasePath, topContainer)
{
	if (!bookBasePath.endsWith("/"))
		bookBasePath = bookBasePath+"/";
	this.spec = new DocexSpec(bookBasePath, xml);
	
	this.topContainer = topContainer;
	this.tcw = -1;
	this.tch = -1;
	this.currentPage = -4;
	this.requestPage = -3;
	this.dbg = {};
	this.width = 0;
	this.height = 0;
	this.roiAlpha = 0;
	this.ecoMode = false;
	this.pageDepth = .025;
	this.useShadows = false;
	this.bookModel = null;
	this.layout = new DocexLayout(this);
	this.hand = new DocexHand(this);
	this.texLoader = new DocexTexLoader(this);
	this.camera = new DocexCamera(this);
	this.input = new DocexInput(this);
	this.glw = -1;
	this.glh = -1;
	this.pulse = 0;
	
	this.width = Math.max(10, this.layout.container.clientWidth);
	this.height = Math.max(10, this.layout.container.clientHeight);
	this.layout.container.addEventListener("touchstart", function(event) {event.preventDefault(); this.input.htmlMouseDown(reader, event.targetTouches[0].pageX, event.targetTouches[0].pageY);}, false);
	this.layout.container.addEventListener("touchmove", function(event) {event.preventDefault(); this.input.htmlMouseMove(reader, event.targetTouches[0].pageX, event.targetTouches[0].pageY);}, false);
	this.layout.container.addEventListener("touchend", function(event) {event.preventDefault(); this.input.htmlMouseUp(reader);}, false);
	
	if (DocexDetector.webgl)
	{
        this.renderer = new THREE.WebGLRenderer();
        if (this.useShadows)
        {
        	this.renderer.shadowMap.enabled = true;
        	this.renderer.shadowMap.type = THREE.PCFSoftShadowMap;
        }
	}
    else
    {
        this.renderer = new THREE.CanvasRenderer();
        this.renderer.sortObjects = false;
        this.renderer.sortElements = false;
        this.ecoMode = true;
    }
	this.renderer.setSize(this.width, this.height);
	this.renderer.autoClear = true;
    this.renderer.autoClearColor = true;
    this.renderer.setClearColor(new THREE.Color(0xf0f0f0));
    this.layout.container.appendChild(this.renderer.domElement);
	
	var distFactor = 2*this.spec.aspect*this.height/this.width;
	this.camera.init(0, 0, 5.2*Math.max(1, distFactor));
    this.scene = new THREE.Scene();
    this.heightBound = this.width*1./this.height > 2*this.spec.aspect;
    
    var pageHeight = this.spec.pageHeight;
    var pageWidth = pageHeight*this.spec.aspect;
	
	// and the camera
	this.scene.add(this.camera.camera);
	
	// create a point light
	var pointLight = new THREE.DirectionalLight(0xDFDFDF);
	pointLight.castShadow = this.useShadows;
	if (this.useShadows)
	{
		//pointLight.shadow.bias = .6;
		pointLight.shadow.mapSize.width = 1024;
		pointLight.shadow.mapSize.height = 1024;
		pointLight.shadow.camera.left = -3;
		pointLight.shadow.camera.right = 3;
		pointLight.shadow.camera.top = 3;
		pointLight.shadow.camera.bottom = -3;
		pointLight.shadow.camera.near = 1;
		pointLight.shadow.camera.far = 30;
		//this.scene.add(new THREE.AmbientLight(0x404040));
	}
	// set its position
	pointLight.position.x = 1;
	pointLight.position.y = 2;
	pointLight.position.z = 13;
	
	// add to the scene
	this.scene.add(pointLight);
	this.scene.add(new THREE.AmbientLight(0x202020));
	
	this.fcnt = 0;
	
	this.input.listeners.push(this);
	
	this.bookModel = new DocexBookModel(this, pageWidth, pageHeight);
	
	this.scene.add(this.bookModel.model);
	this.scene.add(this.bookModel.page.model);
	
	this.layout.slider.max = this.spec.pages.length;
	
	this.texLoader.refresh();
	//this.trace("init OK");
	this.doRender = docexBuildRenderCallback(this);
	
	this.tempModels = [];
	this.leftPageIndex = 0;
	this.rightPageIndex = 0;
	this.handWasActive = false;
	this.forceModelSynchro = false;
	this.regionsAreShown = true;
	this.selectedRegion = null;
	this.regionsAreLoaded = false;
	
	this.zoomed = false;
	this.zoomIn = false;
	this.zoomOut = false;
	this.infoMode = -1;
	this.started = false;
	
	this.refreshLayout();
	this.refreshCanvasSize();
	//this.render();
}

DocexReader.prototype.start = function()
{
	if (this.started)
		return;
	this.started = true;
	this.render();
}

DocexReader.prototype.updateInfoSpan = function()
{
	var mode = -1;
	if (this.zoomed) mode = 2;
	else if (this.selectedRegion != null) mode = 1;
	else mode = 0;
	
	if (mode != this.infoMode)
	{
		var fr = navigator.language.indexOf("fr") >= 0;
		this.infoMode = mode;
		var text = "";
		if (mode == 0) text = fr ? "Glissez les pages pour les tourner. Tapez une zone en surbrillance pour plus d'informations." : "Drag the pages to turn them. Hit highlighted areas for more information.";
		else if (mode == 1) text = fr ? "Tapez la partie gauche pour sortir." : "Hit the left side to exit.";
		else if (mode == 2) text = fr ? "Glissez l'image pour vous déplacer." : "Drag the image to move around.";
		this.layout.infoSpan.innerHTML = text;
	}
}

DocexReader.prototype.refreshLayout = function()
{
	var fse = document.fullscreenElement !== undefined ? document.fullscreenElement : (document.webkitFullscreenElement !== undefined ? document.webkitFullscreenElement : document.mozFullScreenElement);
	var isFullscreen = fse == this.layout.top;
	var w = isFullscreen ? window.innerWidth : this.layout.top.clientWidth;
	var h = isFullscreen ? window.innerHeight : this.layout.top.clientHeight-1;
	var toolbarHeight = Math.max(32, Math.floor(.06*h));
	
	var container = this.layout.container;
	container.style.left = "0px";
	container.style.top = "0px";
	container.style.width = w+"px";
	container.style.height = (h-toolbarHeight)+"px";
	
	var roi = this.layout.roiContainer;
	roi.style.left = Math.floor(.5*w)+"px";
	roi.style.top = "0px";
	roi.style.width = Math.floor(.5*w)+"px";
	roi.style.height = (h-toolbarHeight)+"px";
	
	var toolbar = this.layout.toolbar;
	toolbar.style.left = "0px";
	toolbar.style.top = (h-toolbarHeight)+"px";
	toolbar.style.width = w+"px";
	toolbar.style.height = toolbarHeight+"px";
	
	this.layout.poweredBy.style.fontSize = Math.floor(.36*toolbarHeight)+"px";
	this.layout.sliderDiv.style.fontSize = Math.floor(.4*toolbarHeight)+"px";
	this.layout.sliderVal.style.fontSize = Math.floor(.4*toolbarHeight)+"px";
	//this.layout.slider.style.height = Math.floor(.5*toolbarHeight)+"px";
	
	this.layout.fullscreen.innerHTML = isFullscreen ? docexUnfullscreenSvg : docexFullscreenSvg;
	
	docexLimitButtonSize(toolbar, toolbarHeight-6);
}
DocexReader.prototype.refreshCanvasSize = function()
{
	this.width = this.layout.container.clientWidth;
	this.height = this.layout.container.clientHeight;
	this.renderer.setSize(this.width, this.height);
	var distFactor = 2*this.spec.aspect*this.height/this.width;
	this.heightBound = this.width*1./this.height > 2*this.spec.aspect;
    this.camera.refreshDimensions(5.2*Math.max(1, distFactor));
    var infoSize = Math.floor(.8*Math.min(this.width*.025, this.height*.036));
    this.layout.infoSpan.style.fontSize = infoSize+"px";
    this.layout.infoIcon.style.width = infoSize+"px";
    this.layout.infoIcon.style.height = infoSize+"px";
    //console.log("DocexReader size: "+this.width+" "+this.height);
}

DocexReader.prototype.render = function()
{
	var isFullscreen = document.fullscreenElement == this.layout.top;
	var tcw = isFullscreen ? window.innerWidth : this.layout.top.clientWidth;
	var tch = isFullscreen ? window.innerHeight : this.layout.top.clientHeight;
	if (this.tcw != tcw || this.tch != tch)
	{
		this.tcw = tcw;
		this.tch = tch;
		//console.log("tc "+tch);
		this.refreshLayout();
	}
	var glw = this.layout.container.clientWidth;
	var glh = this.layout.container.clientHeight;
	if (this.glw != glw || this.glh != glh)
	{
		this.glw = glw;
		this.glh = glh;
		//console.log("gl "+glh);
		this.refreshCanvasSize();
	}
	
	this.bookModel.update();
	
	if (this.zoomed)
	{
		if (this.zoomIn) this.camera.setDiffPos(0, 0, -.1);
		if (this.zoomOut) this.camera.setDiffPos(0, 0, .1);
	}
	if (this.hand.active)
		this.hand.update();
	this.camera.update();
	
	this.fcnt += .03;
	var curOpacity = parseFloat(this.layout.roiContainer.style.opacity);
	var opacity = curOpacity+.1*(this.roiAlpha-curOpacity);
	this.layout.roiContainer.style.opacity = opacity;
	if (opacity > .05)
		this.layout.roiContainer.style.zIndex = 1;
	else this.layout.roiContainer.style.zIndex = -1;
	
	if (!this.regionsAreLoaded && !this.bookModel.isAnimating && this.camera.activity < .0001)
	{
		if (this.leftPageIndex >= 0)
		{
			var page = this.spec.pages[this.leftPageIndex];
			for (var i=0;i<page.regions.length;i++)
			{
				var region = page.regions[i];
				DocexRegion.buildRegionMesh(this, region);
				this.tempModels[this.tempModels.length] = region.mesh;
				this.tempModels[this.tempModels.length] = region.outline;
				this.scene.add(region.mesh);
				this.scene.add(region.outline);
				region.mesh.visible = this.regionsAreShown;
				region.outline.visible = this.regionsAreShown;
			}
		}
		if (this.rightPageIndex >= 0)
		{
			var page = this.spec.pages[this.rightPageIndex];
			for (var i=0;i<page.regions.length;i++)
			{
				var region = page.regions[i];
				DocexRegion.buildRegionMesh(this, region);
				this.tempModels[this.tempModels.length] = region.mesh;
				this.tempModels[this.tempModels.length] = region.outline;
				this.scene.add(region.mesh);
				this.scene.add(region.outline);
				region.mesh.visible = this.regionsAreShown;
				region.outline.visible = this.regionsAreShown;
			}
		}
		this.regionsAreLoaded = true;
		this.pulse = 0;
	}
	this.pulse = this.pulse+.006;
	if (this.pulse >= 1)
		this.pulse = this.pulse-1;
	var roiAlpha = .33*(.5*(-Math.cos(2*Math.PI*this.pulse)+1));
	this.spec.roiMaterial.opacity = roiAlpha;
	
	var nextPage = this.requestPage;
	if (nextPage != this.currentPage || this.handWasActive != this.hand.active || this.forceModelSynchro)
	{
		this.setSelectedRegion(null);
		this.clearModels();
		this.regionsAreLoaded = false;
		
		this.currentPage = nextPage;
		if (this.currentPage < -3)
			this.currentPage = -3;
		if (this.currentPage > this.spec.pages.length+1)
			this.currentPage = this.spec.pages.length+1;
		this.layout.slider.value=this.currentPage+1;
		this.layout.sliderVal.value = docexSliderFormatVal(this.currentPage+1, this);
		this.leftPageIndex = this.currentPage;
		this.rightPageIndex = this.currentPage+1+(this.hand.active ? 2 : 0);
		if (this.leftPageIndex < 0 || this.leftPageIndex > this.spec.pages.length-1)
			this.leftPageIndex = -1;
		if (this.rightPageIndex < 0 || this.rightPageIndex >= this.spec.pages.length-1)
			this.rightPageIndex = -1;
		
		this.texLoader.refresh();
	}
	
	var shouldRegionsBeShown = !this.hand.active && this.currentPage > -3 && this.currentPage < this.spec.pages.length+1;
	if (shouldRegionsBeShown != this.regionsAreShown)
	{
		if (shouldRegionsBeShown) this.showRegions();
		else this.hideRegions();
	}
	
	this.handWasActive = this.hand.active;
	this.forceModelSynchro = false;
	this.updateInfoSpan();
//	console.log(this.camera.activity);
//	console.log(this.bookModel.isAnimating);
	this.renderer.render(this.scene, this.camera.camera);
	window.requestAnimationFrame(this.doRender);
}
DocexReader.prototype.clearModels = function()
{
	for (var i=0;i<this.tempModels.length;i++)
		this.scene.remove(this.tempModels[i]);
	this.tempModels = [];
}
DocexReader.prototype.showRegions = function()
{
	for (var i=0;i<this.tempModels.length;i++)
		this.tempModels[i].visible = true;
	this.regionsAreShown = true;
}
DocexReader.prototype.hideRegions = function()
{
	for (var i=0;i<this.tempModels.length;i++)
		this.tempModels[i].visible = false;
	this.regionsAreShown = false;
}

DocexReader.prototype.onClick = function(x, y)
{
	if (!this.zoomed)
	{
		var region = null;
		if (this.selectedRegion == null)
			region = DocexRegion.getRegionAt(this, x, y);
		this.setSelectedRegion(region);
	}
}
DocexReader.prototype.setSelectedRegion = function(region)
{
	if (region != this.selectedRegion)
	{
		if (this.selectedRegion != null)
		{
			this.selectedRegion.mesh.material = this.spec.roiMaterial;
			this.selectedRegion.outline.material = this.spec.roiOutlineMaterial;
		}
		if (region != null)
		{
			region.mesh.material = this.spec.roiSelectedMaterial;
			region.outline.material = this.spec.roiSelectedOutlineMaterial;
		}
		this.selectedRegion = region;
		
		if (this.selectedRegion != null)
		{
			var info = "";
			for (var i=0;i<this.selectedRegion.infos.length;i++)
				info += DocexRegion.scaleInfo(this.selectedRegion.infos[i], this.layout.roiContainer.clientWidth/512.);
			this.roiAlpha = 1;
			var d = document.createElement("div");
			d.innerHTML = info;
			this.layout.roiContainer.innerHTML = d.textContent;
			this.camera.setPosToRegion(this.selectedRegion);
			this.layout.zoomBut.style.display = 'none';
			this.layout.backBut.style.display = 'inline';
			this.layout.prevBut.style.display = 'none';
			this.layout.nextBut.style.display = 'none';
			this.layout.sliderDiv.style.visibility = 'hidden';
			this.layout.slider.style.visibility = 'hidden';
		}
		else
		{
			this.camera.setDefaultPos();
			this.roiAlpha = 0;
			this.layout.zoomBut.style.display = 'inline';
			this.layout.backBut.style.display = 'none';
			this.layout.prevBut.style.display = 'inline';
			this.layout.nextBut.style.display = 'inline';
			this.layout.sliderDiv.style.visibility = 'visible';
			this.layout.slider.style.visibility = 'visible';
			var videos = document.getElementsByTagName("video");
			for (var i=0;i<videos.length;i++)
				videos[i].pause();
		}
	}
}
DocexReader.prototype.back = function()
{
	if (this.zoomed)
		this.zoom();
	else this.setSelectedRegion(null);
}

DocexReader.prototype.zoom = function()
{
	if (this.selectedRegion != null)
		return;
	
	if (this.zoomed)
	{
		this.zoomed = false;
		this.forceModelSynchro = true;
		this.camera.setDefaultPos();
		this.layout.zoominBut.style.display = 'none';
		this.layout.zoomoutBut.style.display = 'none';
		this.layout.backBut.style.display = 'none';
		this.layout.prevBut.style.display = 'inline';
		this.layout.nextBut.style.display = 'inline';
		this.layout.zoomBut.style.display = 'inline';
	}
	else
	{
		this.zoomed = true;
		this.clearModels();
		this.camera.setDiffPos(0, 0, -2);
		this.layout.zoominBut.style.display = 'inline';
		this.layout.zoomoutBut.style.display = 'inline';
		this.layout.backBut.style.display = 'inline';
		this.layout.prevBut.style.display = 'none';
		this.layout.nextBut.style.display = 'none';
		this.layout.zoomBut.style.display = 'none';
	}
}
DocexReader.prototype.zoomBy = function(amount)
{
	if (!this.zoomed)
		this.zoom();
	else if (this.zoomed && this.camera.unzoomed() && amount > .1)
	{
		this.zoom();
		this.input.waitForNextPinch = true;
	}
	
	if (this.zoomed)
	{
		this.camera.setDiffPos(0, 0, amount);
	}
}

DocexReader.prototype.quickNext = function() {this.requestPage = this.currentPage+2;}
DocexReader.prototype.quickPrev = function() {this.requestPage = this.currentPage-2;}
DocexReader.prototype.quickJump = function(page) {this.requestPage = page;}

DocexReader.prototype.onGrab = function(x, y) {this.hand.grab(x, y);}
DocexReader.prototype.onDrag = function(x, y) {this.hand.drag(x, y);}
DocexReader.prototype.onDrop = function(x, y) {this.hand.drop(x, y);}

function createDocexReader(element, bookBasePath, onload, spec)
{
	if (!bookBasePath.endsWith("/"))
		bookBasePath = bookBasePath+"/";
	var url = bookBasePath+(spec !== undefined && spec != null ? spec : 'book0.xml');
	
	var req = new XMLHttpRequest();
	req.onreadystatechange = function()
	{
		if (req.readyState !== XMLHttpRequest.DONE)
			return;
		if (req.status === 200)
		{
			var xml = req.responseText;
			if (!xml.startsWith("<Book"))
			{
				element.innerHTML = xml;
			}
			else
			{
				var reader = new DocexReader(xml, bookBasePath, element); 
				if (onload !== undefined && onload != null)
					onload(reader);
			}
			return;
		}

		element.innerHTML = "<center>Could not load book data from "+url+"</center>";
	}
	req.open("GET", url, true);
	req.send();
}
