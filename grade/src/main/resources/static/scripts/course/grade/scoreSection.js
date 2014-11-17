var count = 0;
var tableObj = $("scoreSectionTable");

function setCount(count) {
    this.count = count;
}

function getCount() {
    return count;
}

function checkScore(form) {
    var a_fields = {
        'fromScore':{'l':"开始值", 'r':true, 't':'f_fromScore', 'f':'real'},
        'toScore':{'l':"结束值", 'r':true, 't':'f_toScore', 'f':'real'}
    };
    var v = new validator(form, a_fields, null);
    if (v.exec()) {
        if (parseFloat(form["fromScore"].value) <= parseFloat(form["toScore"].value)) {
            alert("结束值必须低于开始值。");
            return false;
        }
        for (var i = 1; i <= count; i++) {
            if (parseFloat(form["fromScore"].value) > parseFloat(tableObj.rows[i].cells[1].innerHTML) && parseFloat(form["toScore"].value) < parseFloat(tableObj.rows[i].cells[0].innerHTML) ) {
                alert("添加的值不能与已添加的值有重复或交叉。");
                return false;
            }
        }
        return true;
    }
    return false;
}

function addScoreSection() {
    if (checkScore(form)) {
        count++;
        var tdObj1 = document.createElement("td");
        tdObj1.innerHTML = $("fromScore").value;
        $("fromScore").value = "";
        tdObj1.style.color = "blue";
        var tdObj2 = document.createElement("td");
        tdObj2.innerHTML = $("toScore").value;
        $("toScore").value = "";
        tdObj2.style.color = "blue";
        var tdObj3 = document.createElement("td");
        var buttonObj = document.createElement("button");
        buttonObj.innerHTML = "删除";
        buttonObj.onclick = function() {
            removeThisRow(this.parentNode.parentNode.rowIndex);
        };
        tdObj3.appendChild(buttonObj);
        var inputObj1 = document.createElement("input");
        inputObj1.type = "hidden";
        inputObj1.name = "section" + count + ".fromScore";
        inputObj1.value = tdObj1.innerHTML;
        tdObj3.appendChild(inputObj1);
        var inputObj2 = document.createElement("input");
        inputObj2.type = "hidden";
        inputObj2.name = "section" + count + ".toScore";
        inputObj2.value = tdObj2.innerHTML;
        tdObj3.appendChild(inputObj2);
        var trObj = tableObj.insertRow(tableObj.rows.length - 1);
        trObj.id = buttonObj.id;
        trObj.appendChild(tdObj1);
        trObj.appendChild(tdObj2);
        trObj.appendChild(tdObj3);
    }
    parent.toResize(document.body);
}

function removeThisRow(trObjIndex) {
    if (isNotEmpty($("fromScore").value) || isNotEmpty($("toScore").value)) {
        if (confirm("确认要放弃当前正在的添加分数段吗？")) {
            $("fromScore").value = $("toScore").value = "";
        } else {
            return;
        }
    }
    if (count <= 0) {
        return;
    }
    tableObj.deleteRow(trObjIndex);
    count--;
    
    for (var i = 1; i <= count; i++) {
        if (tableObj.rows[i].cells[2].childNodes.length > 2) {
            tableObj.rows[i].cells[2].childNodes[1].name = "section" + i + ".fromScore";
            tableObj.rows[i].cells[2].childNodes[2].name = "section" + i + ".toScore";
        } else {
            tableObj.rows[i].cells[2].childNodes[1].name = "section" + i + ".id";
        }
    }
}
