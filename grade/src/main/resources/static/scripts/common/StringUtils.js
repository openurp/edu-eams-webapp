function isEmpty(obj) {
    return "undefined" == typeof(obj) || null == obj || "" == obj;
}

function isNotEmpty(obj) {
    return !isEmpty(obj);
}

function autoLineFeed(strValue, strLen) {
    if (null == strValue || "" == strValue) {
        return "";
    }
    if (isNotEmpty(strValue.match(new RegExp("\n", "gi")))) {
        return strValue;
    }
    var strLength = null == strLen || isNaN(parseInt(strLen)) ? 50 : strLen;
    var content = "";
    for (var i = 0; ;) {
        for (var j = 0; j < strLength;) {
            if (strValue.charAt(i) >= 0 && strValue.charAt(i) <= 255) {
                j++;
            } else{
                j+=2;
            }
            content += strValue.substr(i++, 1);
            if (i >= strValue.length) {
                return content;
            }
        }
        content += "\n";
    }
    return content;
}