[#ftl]
[@b.head/]
[@b.toolbar title="成绩记录方式设置"]
        function removeSetting(configItemId) {
            var form =document.editConfigForm;
            if (confirm("确定要删除所选择的分数设置吗？")) {
                bg.form.addInput(form, "configItemIds", configItemId, "hidden");
                bg.form.addInput(form, "params", "gradeRateConfig.id=${gradeRateConfig.id}", "hidden");
                bg.form.submit(form,"${b.url('!removeConfigSettng')}");
            }
        }
        bar.addItem("添加配置", "addSetting(document.addConfigForm)");
        bar.addItem("保存修改配置", "saveSetting(document.editConfigForm)");
        bar.addItem("返回","backward()");
[/@]
    <div style="display:none" id="addConfigSetting">
		[@b.form action="!saveConfigSettng" name="addConfigForm"]
        <table class="formTable" width="100%">
            <tr style="background-color: #c7dbff;">
                <td colspan="6" style="font-weight:bold;text-align:center">添加成绩记录方式的配置</td>
            </tr>
            <tr align="center" style="background-color: #c7dbff;">
            	 <td></td>
            	 <td>分数名称</td>
            	 <td>最小值(含)</td>
            	 <td>最大值(含)</td>
            	 <td>默认值</td>
            	 <td>绩点</td>
            </tr>
       
            <tr align="center">
                <td width="5%">+</td>
                <td><input type="text" name="configItem.grade" value="" maxlength="10" style="width:100px"/></td>
                <td><input type="text" name="configItem.minScore" value="" maxlength="5" style="width:100px"/></td>
                <td><input type="text" name="configItem.maxScore" value="" maxlength="5" style="width:100px"/></td>
                <td><input type="text" name="configItem.defaultScore" value="" maxlength="5" style="width:100px"/></td>
                <td><input type="text" name="configItem.gpExp" value="" maxlength="80" style="width:100px"/></td>
            </tr>
            <tr style="background-color: #c7dbff;">
                <td colspan="6" style="font-weight:bold;text-align:center">
                	[@b.submit value="保存" onsubmit="saveAddSetting()" /]
                	<input value="取消" onclick="cancleSetting();return false;" type="button">
                </td>
            </tr>
            <input type="hidden" name="addConfig" value=""/>
        </table>
        [/@]
        <hr/>
    </div>
    [#assign configItems = gradeRateConfig.items/]
    [@b.form action="!saveConfigSettng" name="editConfigForm"]
	    <input type="hidden" name="gradeRateConfig.id" value="${gradeRateConfig.id}"/>
		<b>修改已设置的成绩记录方式配置</b>
	    [@b.grid items=configItems var="configItem"]
	    	[@b.row]
	    		[@b.boxcol/]
	    		[@b.col width="15%" title="分数名称"]<input type="text" name="scoreName${configItem.id}" value="${(configItem.grade?html)?default("")}" maxlength="10" style="width:100px"/>[/@]
	    		[@b.col width="15%" title="最小值(含)"]<input type="text" name="minScore${configItem.id}" value="${configItem.minScore!}" maxlength="5" style="width:50px"/>[/@]
	    		[@b.col width="15%" title="最大值(含)"]<input type="text" name="maxScore${configItem.id}" value="${configItem.maxScore!}" maxlength="5" style="width:50px"/>[/@]
	    		[@b.col width="15%" title="默认值"]<input type="text" name="defaultScore${configItem.id}" value="${(configItem.defaultScore)?if_exists}" maxlength="5" style="width:100px"/>[/@]
	    		[@b.col width="20%" title="绩点"]<input type="text" name="gpExp${configItem.id}" value="${configItem.gpExp!}" maxlength="80" style="width:150px"/>[/@]
            	[@b.col width="15%" title="操作"]<a onclick="removeSetting('${configItem.id}')" href="#">删除</a>[/@]
	    	[/@]
	    	<tr style="background-color: #c7dbff;">
	            <td colspan="7" style="text-align:center">
	            	<input value="保存修改配置" onclick="saveSetting();return false;" type="button">
	                <input value="恢复修改配置" onclick="resetConfigItem();return false;" type="button">
	            </td>
	        </tr>
	    [/@]
        
    [/@]
    <script>
        var configItemList = {
        [#list configItems as configItem]
            ${"'" + (configItem.grade?html)?default("") + "'"}:{'max':${configItem.maxScore!},'min':${configItem.minScore!},'default':${(configItem.defaultScore)?default("null")},'gpExp':'${configItem.gpExp!}'}[#if configItem_has_next],[/#if]
        [/#list]
        };
        var configItemNames = [[#list configItems as configItem]${"'" + (configItem.grade?html)?default("") + "'"}[#if configItem_has_next], [/#if][/#list]];
        var configItemIds = [[#list configItems as configItem]${configItem.id}[#if configItem_has_next], [/#if][/#list]];
        function addSetting(form) {
            if (document.getElementById("addConfigSetting").style.display != "none") {
                return;
            }
            
            document.getElementById("addConfigSetting").style.display = "block";
            form["configItem.grade"].focus();
        }
        
        function cancleSetting() {
       		var form = document.addConfigForm;
            if (document.getElementById("addConfigSetting").style.display == "none") {
                return true;
            }
            var x1 = form["configItem.grade"].value;
            var x2 = form["configItem.maxScore"].value;
            var x3 = form["configItem.minScore"].value;
            var x4 = form["configItem.defaultScore"].value;
            var x5 = form["configItem.gpExp"].value;
            if ("" != x1 || "" != x2 || "" != x3 || "" != x4 || "" != x5) {
                if (confirm("真的要放弃添加的分数配置吗？")) {
                    document.getElementById("addConfigSetting").style.display = "none";
                    form["configItem.grade"].value = "";
                    form["configItem.maxScore"].value = "";
                    form["configItem.minScore"].value = "";
                    form["configItem.defaultScore"].value = "";
                    form["configItem.gpExp"].value = "";
                    document.getElementById("addConfigSetting").style.display = "none";
                    return true;
                }
            } else {
                document.getElementById("addConfigSetting").style.display = "none";
            }
            return false;
        }
        
        function saveAddSetting() {
            var form = document.addConfigForm;
            var gradeName = form["configItem.grade"].value;
            if (null != configItemList[gradeName] && "" != configItemList[gradeName]) {
                alert("分数名称已经存在，不能保存。");
                form["configItem.grade"].focus();
                return false;
            }
            var maxScore = parseFloat(form["configItem.maxScore"].value);
            var minScore = parseFloat(form["configItem.minScore"].value);
            if (isNaN(minScore)) {
                alert("请输入要新增分数的最小值(数字格式)。");
                form["configItem.minScore"].focus();
                return false;
            }
             if (isNaN(maxScore)) {
                alert("请输入要新增分数的最大值(数字格式)。");
                form["configItem.maxScore"].focus();
                return false;
            }
            if (minScore > maxScore) {
                alert("最小值不能超过最大值。");
                form["configItem.maxScore"].focus();
                return false;
            }
             for (var i = 0; i < configItemNames.length; i++) {
                if (configItemList[configItemNames[i]]['min'] >= minScore && configItemList[configItemNames[i]]['min'] <= maxScore
                || configItemList[configItemNames[i]]['max'] >= minScore && configItemList[configItemNames[i]]['max'] <= maxScore) {
                    alert("分数范围" + minScore + "～" + maxScore + "区间与已设范围有冲突。");
                    form["configItem.maxScore"].focus();
                    return false;
                }
            }
            var defaultScore = parseFloat(form["configItem.defaultScore"].value);
            if (isNaN(defaultScore)) {
                alert("请输入默认分值(数字格式)。");
                form["configItem.defaultScore"].focus();
                return false;
            }
            if (!(defaultScore >= minScore && defaultScore <= maxScore)) {
                alert("默认分值不能超出已设定的" + minScore + "～" + maxScore + "区间范围。");
                form["configItem.defaultScore"].focus();
                return false;
            }
            var gp = form["configItem.gpExp"].value;
            if (!(/^[\+\-]?\d*\.?\d*$/.test(gp)) && -1 == gp.indexOf('score')) {
                alert("输入的绩点不是数字，或者含有score的表达式");
                form["configItem.gpExp"].focus();
                return false;
            }
            if (confirm("确定要保存所添加的分数设置吗？\n\n提示:如果要继续保存所添加的分数设置，\n则在“修改已设置的成绩记录方式配置”中\n的修改内容将不会被保存。")) {
                bg.form.addInput(form, "gradeRateConfig.id", "${gradeRateConfig.id}", "hidden");
               	bg.form.addInput(form, "params", "gradeRateConfig.id=${gradeRateConfig.id}", "hidden");
                //bg.form.submit("addConfigForm");
                return true;
            }else{
            	return false;
            }
        }
        
        function saveSetting() {
        	var form = document.editConfigForm;
            if (!cancleSetting(form)) {
                resetConfigItem();
                return;
            }
            
            var isModified = false;
            for (var i = 0; i < configItemIds.length; i++) {
                var toCompareValue1 = form["scoreName" + configItemIds[i]].value;
                var toCompareValue2 = parseFloat(form["maxScore" + configItemIds[i]].value);
                var toCompareValue3 = parseFloat(form["minScore" + configItemIds[i]].value);
                var toCompareValue4 = parseFloat(form["defaultScore" + configItemIds[i]].value);
                var toCompareValue5 = parseFloat(form["gpExp" + configItemIds[i]].value);
                if (isNaN(toCompareValue2) || isNaN(toCompareValue3) || !(/^[\+\-]?\d*\.?\d*$/.test(form["maxScore" + configItemIds[i]].value)) || !(/^[\+\-]?\d*\.?\d*$/.test(form["minScore" + configItemIds[i]].value))) {
                    alert("第" + (i + 1) + "行分数范围输入了非法数字,或没有输入。");
                    return;
                }
                if (isNaN(toCompareValue4)|| !(/^[\+\-]?\d*\.?\d*$/.test(form["defaultScore" + configItemIds[i]].value))) {
                    alert("第" + (i + 1) + "行分数默认值输入了非法数字,或没有输入。");
                    return;
                }
                if (configItemNames[i] != form["scoreName" + configItemIds[i]].value
                    || configItemList[configItemNames[i]]['max'] != form["maxScore" + configItemIds[i]].value
                    || configItemList[configItemNames[i]]['min'] != form["minScore" + configItemIds[i]].value
                    || configItemList[configItemNames[i]]['default'] != form["defaultScore" + configItemIds[i]].value
                    ||configItemList[configItemNames[i]]['gpExp'] != form["gpExp" + configItemIds[i]].value) {
                    isModified = true;
                }
                for (var j = 0; j < configItemIds.length; j++) {
                    if (j == i) {
                        continue;
                    }
                    var otherRowValue1 = form["scoreName" + configItemIds[j]].value;
                    var otherRowValue2 = parseFloat(form["maxScore" + configItemIds[j]].value);
                    var otherRowValue3 = parseFloat(form["minScore" + configItemIds[j]].value);
                    var otherRowValue4 = parseFloat(form["defaultScore" + configItemIds[j]].value);
                    var otherRowValue5 = parseFloat(form["gpExp" + configItemIds[j]].value);
                    if ("" != otherRowValue1 && null != otherRowValue1 && null != toCompareValue1 && "" != toCompareValue1 && otherRowValue1 == toCompareValue1) {
                        alert("分数名称" + toCompareValue1 + "于第" + (i + 1) + "行、第" + (j + 1) + "行重复。");
                        return;
                    }
                    if (isNaN(otherRowValue2) || isNaN(otherRowValue3) || !(/^[\+\-]?\d*\.?\d*$/.test(form["maxScore" + configItemIds[j]].value)) || !(/^[\+\-]?\d*\.?\d*$/.test(form["minScore" + configItemIds[j]].value))) {
                        alert("第" + (j + 1) + "行分数范围输入了非法数字,或没有输入。");
                        return;
                    }
                    if (otherRowValue3 >= otherRowValue2) {
                        alert("第" + (j + 1) + "行分数范围设定不正确！\n即，最小值（" + otherRowValue3 + "）超过了最大值（" + otherRowValue2 + "）。");
                        return;
                    }
                    if (toCompareValue2 >= otherRowValue3 && toCompareValue2 <= otherRowValue2 || toCompareValue3 >= otherRowValue3 && toCompareValue3 <= otherRowValue2) {
                        alert("分数范围" + toCompareValue3 + "～" + toCompareValue2 + "（第" + (i + 1) + "行）和" + otherRowValue3 + "～" + otherRowValue2 + "(第" + (j + 1) + "行)之间冲突。");
                        return;
                    }
                    if (isNaN(toCompareValue4) || !(/^[\+\-]?\d*\.?\d*$/.test(form["defaultScore" + configItemIds[j]].value))) {
                        alert("第" + (j + 1) + "行分数默认值输入了非法数字,或没有输入。");
                        return;
                    }
                    if (!(toCompareValue4 >= toCompareValue3 && toCompareValue4 <= toCompareValue2)) {
                        alert("第" + (i + 1) + "行输入的分数默认值" + toCompareValue4 + "，不在该行\n的" + toCompareValue3 + "～" + toCompareValue2 + "分数范围之内。");
                        return;
                    }
                }
            }
            if (isModified == false) {
                if (confirm("当前分数配置信息没有被修改，无需保存。\n\n现在若想返回列表，单击“确定”；\n若想继续修改分数配置，单击“取消”。")) {
                    backward();
                }
                return;
            }
            if (confirm("确定要保存所修改的分数配置吗？")) {
				bg.form.addInput(form, "gradeRateConfig.id", "${gradeRateConfig.id}", "hidden");
				bg.form.addInput(form, "configItemIds", configItemIds.join(","), "hidden");
				bg.form.submit(form);
            }
        }
        
        function resetConfigItem() {
            document.editConfigForm.reset();
        }
        
        function backward() {
            bg.form.submit("gradeRateConfigForm");
        }
    </script>
[@b.foot/]
