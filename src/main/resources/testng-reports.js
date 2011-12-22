$(document).ready(function() {
//    $('.panel').hide();
    $('a.navigator-link').click(function() {
	// Extract the panel for this link
	var panel = getPanelName($(this));

	// Mark this link as currently selected
	$('.navigator-link').parent().removeClass('navigator-selected');
	$(this).parent().addClass('navigator-selected');

	showPanel(panel);
    });
    $('a.method').click(function() {
	showMethod($(this));
	return false;
    });
	
});

function getHashForMethod(element) {
    return element.attr('hash-for-method');
}

function getPanelName(element) {
    return element.attr('panel-name');
}

function showPanel(panelName) {
    $('.panel').hide();
    $('.panel.' + panelName).show();
}

function showMethod(element) {
    var hashTag = getHashForMethod(element);
    var panelName = getPanelName(element);
    showPanel(panelName);
    var current = document.location.href;
    var base = current.substring(0, current.indexOf('#'))
    document.location.href = base + '#' + hashTag;
}
