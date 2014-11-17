[#ftl/]
[@b.head/]
[@b.toolbar title="显示分数段设置"]
	bar.addItem("保存", "scoreSectionSave(document.scoreSectionSettingForm)");
	bar.addBack("${b.text('action.back')}");
	
    function scoreSectionSave(form) {
        if (0 == $('#count').val()) {
            alert("当前没有添加任何分数段，无需保存。");
            return;
        }
        if (confirm("确认要保存当前的设置吗?")) {
        	bg.form.submit(form,"${b.url('stat!scoreSectionSetting')}");
        }
    }
    var tableObj = document.getElementById('scoreSectionTable');
    
    function checkScore(form) {
    
    	if(!(checkScoreText($('#fromScore').val()) && checkScoreText($('#toScore').val()))){
    		return false;
    	}

    	
		var count = $('#count').val();
        if (parseFloat($('#fromScore').val()) <= parseFloat($('#toScore').val())) {
            alert("结束值必须低于开始值。");
            return false;
        }
        for (var i = 1; i <= count; i++) {
            if (parseFloat($('#fromScore').val()) > parseFloat(tableObj.rows[i].cells[1].innerHTML) && parseFloat($('#toScore').val()) < parseFloat(tableObj.rows[i].cells[0].innerHTML) ) {
                alert("添加的值不能与已添加的值有重复或交叉。");
                return false;
            }
        }
        return true;
	}
	
	function checkScoreText(score){
       if(""!=score){
         if(isNaN(score)){
            alert(score+" 不是数字");
            return false;
         } else if(!/^\d*\.?\d*$/.test(score)) {
            alert("请输入0或正实数");
            return false;
         } else if(parseInt(score)>100) {
            alert("百分制输入不允许超过100分");
            return false;
         }else{
         	return true;
         }
       }
	}
    
    function addScoreSection(){
    	if(checkScore(document.scoreSectionSettingForm)){
			var count = $('#count').val();
	    	count++;
	        var tdObj1 = document.createElement("td");
	        tdObj1.innerHTML = $("#fromScore").val();
	        $("#fromScore").val("");
	        tdObj1.style.color = "blue";
	        var tdObj2 = document.createElement("td");
	        tdObj2.innerHTML = $("#toScore").val();
	        $("#toScore").val("");
	        tdObj2.style.color = "blue";
	        var tdObj3 = document.createElement("td");
	        var buttonObj = document.createElement("input");
	        buttonObj.type="button"
	        $(buttonObj).val("删除");
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
	        $('#count').val(count);
        }
    }
    
    function removeThisRow(trObjIndex) {
	    var count = $('#count').val();
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
	    $('#count').val(count);
	}
    
[/@]
[@b.form name="scoreSectionSettingForm"]
	<table id="scoreSectionTable" class="gridtable" width="100%" style="text-align:center">
		<thead class="gridhead">
	        <tr class="darkColumn">
	            <td width="40%" id="f_fromScore">开始值（最高值含/不含）</td>
	            <td width="40%" id="f_toScore">结束值（含）</td>
	            <td>操作</td>
	        </tr>
        </thead>
        [#list sections as section]
	        <tr>
	            <td>${section.fromScore}</td>
	            <td>${section.toScore}</td>
	            <td>
		            <input type="button" value="删除" onclick="removeThisRow(this.parentNode.parentNode.rowIndex)">
		            <input type="hidden" name="section${section_index + 1}.id" value="${section.id}"/>
	           	</td>
	        </tr>
        [/#list]
        <tr>
            <td><input type="text" id="fromScore" name="fromScore" value="" maxlength="20" style="width:200px"/></td>
            <td><input type="text" id="toScore" name="toScore" value="" maxlength="20" style="width:200px"/></td>
            <td><input type="button" value="添加" onclick="addScoreSection()"></td>
        </tr>
    </table>
    <input type="hidden" id="count" name="count" value="${sections?size}"/>
[/@]
[@b.foot/]