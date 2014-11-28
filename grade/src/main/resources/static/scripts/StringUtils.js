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

//计算字符串的字节数长度
function getStringLength(str) {
	if (str == null || str == "") {
		return 0;
	}
	var strLen = 0;
	for (var i = 0; i < str.length; i ++) {
		if (Math.abs(str.charCodeAt(i)) <= 255) {
			strLen ++;
		} else {
			strLen += 2;
		}
	}
	return strLen;
}

//去除字符串的所有空格
function cleanSpaces(str) {
	if (str == null || str == "") {
		return str;
	}
	
	return str.replace(" ", "");
}

//检查字符串是否为A-Z和0-9组成的字符串
function isStringAZ09(str) {
	if (str == null || str == "") {
		return false;
	}
	
	for (var i = 0; i < str.length; i++) {
		if ((str.charCodeAt(i) >= "0".charCodeAt(0) && str.charCodeAt(i) <= "9".charCodeAt(0) || str.charCodeAt(i) >= "A".charCodeAt(0) && str.charCodeAt(i) <= "Z" || str.charCodeAt(i) >= "a".charCodeAt(0) && str.charCodeAt(i) <= "z".charCodeAt(0)) == false) {
			return false;
		}
	}
	return true;
}

//查询TextArea字符长度，长度可自定义,默认200
function checkTextLength(textAreaContent, displayTitle, maxLength) {
	if (maxLength == null || maxLength == "") {
		maxLength = 200;
	}
    if (getStringLength(textAreaContent) > maxLength) {
    	alert(displayTitle + "不能超过" + maxLength + "个字符！");
    	return false;
    }
    return true;
}

// 填充字符
//      fillString 默认1个空格, 默认10个英文字符
function fillText(fromText, fillString, textLength) {
    var fromStr = fromText;
    if (isEmpty(fromStr)) {
        fromStr = "";
    }
    var maxLength = textLength;
    if (isEmpty(maxLength)) {
        maxLength = 10;
    }
    var fillStr = fillString;
    if (isEmpty(fillStr)) {
        fillStr = " ";
    }
    if (getStringLength(fromStr) >= maxLength) {
        return fromStr;
    } else {
        var fillResult = "";
        var fillStrLength = getStringLength(fillStr);
        for (var i = getStringLength(fromStr); i <= maxLength;) {
            if (i + fillStrLength > maxLength) {
                fillResult += fillStr.substr(0, (i + fillStrLength) - maxLength);
                break;
            } else {
                fillResult += fillStr;
            }
            i += fillStrLength;
        }
        fillResult = fillResult.replace(new RegExp(" ", "gm"), "&nbsp;");
        return fromStr + fillResult;
    }
}
