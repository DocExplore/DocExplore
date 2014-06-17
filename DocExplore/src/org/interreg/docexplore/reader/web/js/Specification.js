var Spec = {};

Spec.path = {};
Spec.aspect = {};
Spec.pages = [];

Spec.emptyTex = {};
Spec.roiMaterial = {};
Spec.roiSelectedMaterial = {};

Spec.pageHeight = 2;

Spec.init = function(xml)
{
	Spec.roiMaterial = new THREE.LineBasicMaterial({color: 0xff0000});
	Spec.roiMaterial.depthTest = false;
	Spec.roiSelectedMaterial = new THREE.LineBasicMaterial({color: 0xffff00});
	Spec.roiSelectedMaterial.depthTest = false;
	Spec.emptyTex = THREE.ImageUtils.loadTexture("empty.png");
	
	var start = xml.indexOf("<Book path=\"");
	start += 12;
	var end = xml.indexOf("\"", start);
	Spec.path = xml.substring(start, end);
	
	start = xml.indexOf("aspectRatio=\"");
	start += 13;
	end = xml.indexOf("\"", start);
	Spec.aspect = xml.substring(start, end);
	
	while (true)
	{
		start = xml.indexOf("<Page");
		if (start == -1)
			break;
		end = xml.indexOf("</Page>")
		Spec.pages[Spec.pages.length] = Spec.buildPage(xml.substring(start, end+7), Spec.pages.length);
		xml = xml.substring(end+7, xml.length);
	}
	if (Spec.pages.length%2 == 1)
		Spec.pages[Spec.pages.length] = Spec.buildEmptyPage(Spec.pages.length);
}

Spec.buildPage = function(xml, index)
{
	var page = {};
	
	var start = xml.indexOf("src=\"");
	var end = xml.indexOf("\"", start+5);
	page.path = Spec.path+xml.substring(start+5, end);
	page.tex = null;
	page.regions = [];
	page.index = index;
	
	while (true)
	{
		start = xml.indexOf("<RegionOfInterest");
		if (start == -1)
			break;
		end = xml.indexOf("</RegionOfInterest>")
		page.regions[page.regions.length] = Spec.buildRegion(xml.substring(start, end+19), index);
		xml = xml.substring(end+19, xml.length);
	}
	
	return page;
}

Spec.buildEmptyPage = function(index)
{
	var page = {};
	
	page.path = null;
	page.tex = null;
	page.regions = [];
	page.index = index;
	return page;
}

Spec.buildRegion = function(xml, index)
{
	var region = {};
	region.coords = [];
	region.worldCoords = [];
	region.line = {};
	region.infos = [];
	region.bounds = [];
	
	var start = xml.indexOf("region=\"");
	var end = xml.indexOf("\"", start+8);
	var coords = xml.substring(start+8, end).split(", ");
	var minx = null, miny = null, maxx = null, maxy = null;
	for (i=0;i<coords.length/2;i++)
	{
		region.coords[i] = [];
		region.coords[i][0] = coords[2*i];
		region.coords[i][1] = coords[2*i+1];
		
		region.worldCoords[i] = index%2 == 1 ? Camera.fromLeftPageCoords(region.coords[i]) : Camera.fromRightPageCoords(region.coords[i]);
		
		if (minx == null || region.worldCoords[i][0] < minx) minx = region.worldCoords[i][0];
		if (maxx == null || region.worldCoords[i][0] > maxx) maxx = region.worldCoords[i][0];
		if (miny == null || region.worldCoords[i][1] < miny) miny = region.worldCoords[i][1];
		if (maxy == null || region.worldCoords[i][1] > maxy) maxy = region.worldCoords[i][1];
	}
	region.bounds[0] = [minx, miny];
	region.bounds[1] = [maxx, maxy];
	
	var geom = new THREE.Geometry();
	for (i=0;i<region.worldCoords.length;i++)
		geom.vertices.push(new THREE.Vector3(region.worldCoords[i][0], region.worldCoords[i][1], 0));
	geom.vertices.push(new THREE.Vector3(region.worldCoords[0][0], region.worldCoords[0][1], 0));
	region.line = new THREE.Line(geom, Spec.roiMaterial);
	region.line.renderDepth = 1;
	
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
			region.infos.push(Spec.buildInfo(xml.substring(start, end+7)));
			xml = xml.substring(end+7, xml.length);
		}
		else
		{
			end = end2;
			region.infos.push(Spec.buildInfo(xml.substring(start, end+2)));
			xml = xml.substring(end+2, xml.length);
		}
		//alert(region.infos[region.infos.length-1]);
	}
	
	return region;
}

Spec.buildInfo = function(infoXml)
{
	var start = infoXml.indexOf("type=\"");
	var end = infoXml.indexOf("\"", start+6);
	var type = infoXml.substring(start+6, end);
	if (type == "text")
		return infoXml.substring(infoXml.indexOf(">")+1, infoXml.indexOf("</Info>"));
	else if (type == "image")
	{
		start = infoXml.indexOf("src=\"");
		end = infoXml.indexOf("\"", start+5);
		return "&lt;img src=\""+Spec.path+infoXml.substring(start+5, end)+"\" width=100% />";
	}
	else if (type == "media")
	{
		start = infoXml.indexOf("src=\"");
		end = infoXml.indexOf("\"", start+5);
		return "&lt;video src=\""+Spec.path+infoXml.substring(start+5, end)+"\" width=100% controls />";
	}
	return "";
}

Spec.refreshTextures = function(midPage, spread)
{
	for (var i=0;i<Spec.pages.length;i++)
	{
		if (Math.abs(i-midPage) < spread)
		{
			if (!Spec.pages[i].tex)
			{
				if (Spec.pages[i].path != null)
					Spec.pages[i].tex = THREE.ImageUtils.loadTexture(Spec.pages[i].path);
				else Spec.pages[i].tex = Spec.emptyTex;
			}
		}
		else if (Spec.pages[i].tex != null)
		{
			if (Spec.pages[i].path != null)
				Spec.pages[i].tex.dispose();
			Spec.pages[i].tex = null;
		}
			
	}
}
