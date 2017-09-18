function Floor(w, y0)
{
	this.geom = new THREE.Geometry();
	this.geom.vertices.push(new THREE.Vector3(-.5*w, y0, -.5*w));
	this.geom.vertices.push(new THREE.Vector3(.5*w, y0, -.5*w));
	this.geom.vertices.push(new THREE.Vector3(.5*w, y0, .5*w));
	this.geom.vertices.push(new THREE.Vector3(-.5*w, y0, .5*w));
	
	this.geom.faces.push(new THREE.Face3(0, 1, 2));
	this.geom.faceVertexUvs[0].push([new THREE.Vector2(0, 0), new THREE.Vector2(1, 0), new THREE.Vector2(1, 1)]);
	this.geom.faces.push(new THREE.Face3(0, 2, 3));
	this.geom.faceVertexUvs[0].push([new THREE.Vector2(0, 0), new THREE.Vector2(1, 1), new THREE.Vector2(0, 1)]);
	
	this.geom.computeFaceNormals();
	this.geom.computeVertexNormals();
	
	this.mesh = new THREE.Mesh(this.geom, new THREE.MeshLambertMaterial({map: Spec.floorTex}));
	this.mesh.material.side = THREE.BackSide;
	this.mesh.material.shading = THREE.SmoothShading;
}