function countersToHeading(counters) {
    var result = "";
    for (var i = 0; i < counters.length; i++) {
	if (i > 0) result = result + ".";
	result = result + counters[i];
    }
    return result;
}

function indentSection(count) {
    var result = "";
    for (var i = 0; i < count; i++) {
	result += "&nbsp;&nbsp;";
    }
    return result;
}

function rewriteSection(section, counters) {
    var result = "";
    result = result + countersToHeading(counters) + " - " + section.innerHTML;
    return result;
}

function generateToc() {
    var sections = document.getElementsByTagName("a");

    var toc = "";
    var counters = new Array();
    for (i = 0; i < sections.length; i++)
    {
	var section = sections[i];
	if (section.className == "section") {
	    var nameNode = section.attributes["name"];
	    var name = nameNode ? nameNode.nodeValue : i;
	    var indentNode = section.attributes["indent"];
            var indent = indentNode ? indentNode.nodeValue : ".";
            var currentCounter = 0;
            var ind = indent.length;
	    if (ind < counters.length) {
		var p = ind;
		while (p++ <= counters.length) {
                    counters.pop();
		}
	    }
            if (counters[ind - 1]) {
		var n = counters.pop();
		counters.push(n + 1);
            } else {
		counters.push(1);
            }
            toc = toc
		+ indentSection(ind) + '<a class="table-of-contents" href=#' + name + '>'
		+ rewriteSection(section, counters)
		+ "</a><br>";
	    section.innerHTML = rewriteSection(section, counters);
	}
    }
    
    var tocId = "table-of-contents";
    var tocTag = document.getElementById(tocId);
    
    if (tocTag) {
	tocTag.innerHTML = toc;
    } else {
	alert("Couldn't find an id " + tocId);
    }

}

