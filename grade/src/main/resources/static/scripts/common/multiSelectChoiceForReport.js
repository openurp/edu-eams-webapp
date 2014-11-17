function selectMoveAll(fromSelect, toSelect) {
	while (fromSelect.options.length > 0) {
		toSelect.options.add(new Option(fromSelect.options[0].text, fromSelect.options[0].value));
		fromSelect.options[0] = null;
	}
	customFunction();
}

function selectMoveAnyOne(fromSelect, toSelect) {
	for (var i = 0; i < fromSelect.options.length;) {
		if (fromSelect.options[i].selected) {
			toSelect.options.add(new Option(fromSelect.options[i].text, fromSelect.options[i].value));
			fromSelect.options[i] = null;
		} else {
			i++;
		}
	}
	customFunction();
}

function customFunction() {
    ;
}
