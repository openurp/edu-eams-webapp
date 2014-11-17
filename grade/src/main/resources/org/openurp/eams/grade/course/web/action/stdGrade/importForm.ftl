[#ftl/]
[@b.head/]
[@b.toolbar title=Parameters["display"]!("导入")]
    bar.addItem("模板下载","downloadTemplate()","${base}/static/images/action/download.gif");
    bar.addBackOrClose();
[/@]


[@b.form action="!importData" theme="list" enctype="multipart/form-data"]
    [@b.messages/]
    <label for="importFile" class="label"><em class="required">*</em>文件目录:</label><input type="file" name="importFile" value="" id="importFile"/>
    <br>
    <div>
    	[@b.submit value="system.button.submit" onsubmit="validateExtendName"/]
    	<input type="reset" value="${b.text("system.button.reset")}" class="buttonStyle"/>
    </div>
    [#list Parameters?keys as key]
          [#if key!='method']<input type="hidden" name="${key}" value="${Parameters[key]}" />[/#if]
     [/#list]
[/@]
    <div style="color:red;font-size:2">
		<ul>
			<li>本功能只能导入新的成绩，不能更新已有成绩，如果成绩导入错误，请删除成绩再进行导入</li>
			<li>导入考试成绩前请先确保学生已经有最终成绩，如果没有则可以先导入一个空的最终成绩</li>
			<li>导入考试成绩后，系统会自动重新计算最终成绩</li>
			<li>本功能不能同时导入最终成绩和考试成绩</li>
			<li>上传文件中的所有信息均要采用文本格式。对于日期和数字等信息也是一样。</li>
		</ul>
    </div>
    
 [@b.form name="downloadForm" action="/system/staticfile"]
     [#list Parameters?keys as key]
          [#if key!='method']<input type="hidden" name="${key}" value="${Parameters[key]}" />[/#if]
     [/#list]
 [/@]
<script type="text/javascript">
    function downloadTemplate(){
        [#if Parameters['templateDocumentId']??]
        self.location="dataTemplate!download.action?document.id=${Parameters['templateDocumentId']}";
        [#elseif Parameters['tempPath']??]
        	document.downloadForm.action = "${Parameters['tempPath']}";
        	document.downloadForm.submit();
        [#else]
        document.downloadForm.submit();
        [/#if]
    }
    function validateExtendName(form){
        var value = form.importFile.value;
        if(value == ""){
        	alert("请选择文件");
        	return false;
        }
        var index = value.indexOf(".xls");
        if((index < 1) || (index < (value.length - 4))){
            alert("${b.text("filed.file")}'.xls'");
            return false;
        }
        return true;
    }
</script>
[@b.foot/]