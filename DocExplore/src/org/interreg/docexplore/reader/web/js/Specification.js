var Spec = {};

Spec.path = {};
Spec.aspect = {};
Spec.pages = [];
Spec.cover = {};
Spec.innerCover = {};
Spec.leftSide = null;
Spec.rightSide = null;

Spec.emptyTex = {};
Spec.loadingTex = {};
Spec.roiMaterial = {};
Spec.roiSelectedMaterial = {};

Spec.pageHeight = 2;

Spec.init = function(basePath, xml)
{
	Spec.roiMaterial = new THREE.LineBasicMaterial({color: 0xff0000});
	Spec.roiMaterial.depthTest = false;
	Spec.roiMaterial.transparent = true;
	Spec.roiSelectedMaterial = new THREE.LineBasicMaterial({color: 0xffff00});
	Spec.roiSelectedMaterial.depthTest = false;
	Spec.roiSelectedMaterial.transparent = true;
	Spec.emptyTex = THREE.ImageUtils.loadTexture("empty.png");
	Spec.loadingTex = THREE.ImageUtils.loadTexture("loading.jpg");
	
	var start = xml.indexOf("<Book path=\"");
	start += 12;
	var end = xml.indexOf("\"", start);
	Spec.path = basePath+xml.substring(start, end);
	//document.getElementById('poweredBy').innerHTML = Spec.path;
	
	start = xml.indexOf("aspectRatio=\"");
	start += 13;
	end = xml.indexOf("\"", start);
	Spec.aspect = xml.substring(start, end);
	
	start = xml.indexOf("cover=\"");
	start += 7;
	end = xml.indexOf("\"", start);
	Spec.cover = Spec.path+xml.substring(start, end);
	
	start = xml.indexOf("innerCover=\"");
	start += 12;
	end = xml.indexOf("\"", start);
	Spec.innerCover = Spec.path+xml.substring(start, end);
	
	start = xml.indexOf("leftSide=\"");
	if (start >= 0)
	{
		start += 10;
		end = xml.indexOf("\"", start);
		Spec.leftSide = Spec.path+xml.substring(start, end);
	}
	start = xml.indexOf("rightSide=\"");
	if (start >= 0)
	{
		start += 11;
		end = xml.indexOf("\"", start);
		Spec.rightSide = Spec.path+xml.substring(start, end);
	}
	
	
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
	start = xml.indexOf("tsrc=\"");
	if (start > -1)
	{
		end = xml.indexOf("\"", start+6);
		page.tpath = Spec.path+xml.substring(start+6, end);
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
		page.regions[page.regions.length] = Spec.buildRegion(xml.substring(start, end+19), index);
		xml = xml.substring(end+19, xml.length);
	}
	
	return page;
}

Spec.buildEmptyPage = function(index)
{
	var page = {};
	
	page.path = null;
	page.regions = [];
	page.index = index;
	return page;
}

Spec.buildRegion = function(xml, index)
{
	var region = {};
	region.infos = [];
	region.bounds = [];
	region.index = index;
	
	var start = xml.indexOf("region=\"");
	var end = xml.indexOf("\"", start+8);
	var coords = xml.substring(start+8, end).split(", ");
	region.coords = [];
	for (i=0;i<coords.length/2;i++)
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

function replaceAll(find, replace, str)
{
    return str.replace(new RegExp(find, 'g'), replace);
}
Spec.buildInfo = function(infoXml)
{
	var start = infoXml.indexOf("type=\"");
	var end = infoXml.indexOf("\"", start+6);
	var type = infoXml.substring(start+6, end);
	if (type == "text")
		return replaceAll("\n", "&lt;br/>", infoXml.substring(infoXml.indexOf(">")+1, infoXml.indexOf("</Info>")));
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
