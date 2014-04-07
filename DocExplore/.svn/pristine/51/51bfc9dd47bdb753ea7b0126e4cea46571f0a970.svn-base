Region = {};

Region.getRegionAt = function(page, x, y)
{
	for (var i=0;i<page.regions.length;i++)
		if (Region.contains(page.regions[i], x, y))
			return page.regions[i];
	return null;
}

Region.contains = function(region, x, y)
{
	var n = 0;
	for (var i=0;i<region.coords.length;i++)
		if (Region.intersects(0, -100, x, y, region.coords[i][0], region.coords[i][1], 
			region.coords[i==region.coords.length-1 ? 0 : i+1][0], region.coords[i==region.coords.length-1 ? 0 : i+1][1]))
				n++;
	return n%2 == 1;
}

Region.intersects = function(p1x1, p1y1, p1x2, p1y2, p2x1, p2y1, p2x2, p2y2)
{
	var ux = p1x2-p1x1;
	var uy = p1y2-p1y1;
	var vx = p2x2-p2x1;
	var vy = p2y2-p2y1;
	
	var k = ((p2x1*vy-p2y1*vx)-(p1x1*vy-p1y1*vx))/(ux*vy-uy*vx);
	//Reader.dbg.value += p1x2;
	if (k < 0 || k > 1)
		return false;
	
	k = ((p1x1*uy-p1y1*ux)-(p2x1*uy-p2y1*ux))/(vx*uy-vy*ux);
	return k>=0 && k<=1;
}

Region.scaleInfo = function(html, scale)
{
	//Reader.trace("before scale x"+scale+":");
	//Reader.trace(html);
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
			//Reader.trace("scaling: "+div);
					
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
	//Reader.trace("after:");
	//Reader.trace(html);
	return html;
}
