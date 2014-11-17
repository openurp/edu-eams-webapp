[#ftl/]
[@b.head /]
[#include "/template/macros.ftl"/]
<div  style='display:none;'>
<div id="multiStdReportSetting">
[@b.form name="printSettingForm" action="!classGradeReport"]
<input type="hidden" name="semester.id" value="${(semester.id)!}"/>
 <table class="settingTable">
   <tr >
	   <td style="width:40%">&nbsp;&nbsp;是否打印绩点</td>
	   <td>
   	     <input type="hidden" name="orderBy" value="${Parameters['orderBy']?default('null')}"/>
	     <input type="radio" value="1" name="reportSetting.printGpa" checked/>是 
	     <input type="radio" name="reportSetting.printGpa" value="0"/>否
	   </td>
   </tr>
   <tr>
     <td colspan="6" style="height:5px;font-size:0px;">
       <img src="static/images/action/keyline.gif" height="2" width="100%" align="top"/>
     </td>
   </tr>
   <tr>
	   <td>&nbsp;&nbsp;每页最大记录数：</td>
	   <td style="width:50px"><input value="40" type="text" maxlength="7" name="reportSetting.pageSize"/></td>
   </tr>
   <tr>
     <td colspan="6" style="height:5px;font-size:0px;">
       <img src="static/images/action/keyline.gif" height="2" width="100%" align="top"/>
     </td>
   </tr>
   <tr>
	   <td>&nbsp;&nbsp;成绩排序：</td>
	   <td><select name="reportSetting.order.property">
	        <option value="stdGpa.gpa">平均绩点</option>
	        <option value="std.code">${b.text("attr.stdNo")}</option>
	       </select>
	       顺序:
	        <select name="reportSetting.order.ascending">
            <option value="1">${b.text("action.asc")}</option>
            <option value="0">${b.text("action.desc")}</option>
           </select>
       </td>
   </tr>
   <tr>
     <td colspan="6" style="height:5px;font-size:0px;">
       <img src="static/images/action/keyline.gif" height="2" width="100%" align="top"/>
     </td>
   </tr>
   <tr>
	   <td>&nbsp;&nbsp;字体大小：</td>
	   <td><input type="text" name="reportSetting.fontSize" value="10" maxlength="3"/>px</td>
   </tr>
   <tr>
     <td colspan="6" style="height:5px;font-size:0px;">
       <img src="static/images/action/keyline.gif" height="2" width="100%" align="top"/>
     </td>
   </tr>
   <tr>
	   <td>&nbsp;&nbsp;打印共同成绩时,最低比例：</td>
	   <td><input value="0.15" maxlength="10" type="text" name="ratio" style="width:40px"/></td>
   </tr>
   <tr>
	   <td colspan="2">&nbsp;&nbsp;0.15表示:在100个学生中,如果一门课有15个或更少的学生,则单独显示该课程成绩.</td>
   </tr>
   <tr>
     <td colspan="6" style="height:5px;font-size:0px;">
       <img src="static/images/action/keyline.gif" height="2" width="100%" align="top"/>
     </td>
   </tr>
   <tr>
	   <td>&nbsp;&nbsp;打印成绩类型：</td>
	   <td>
	      <select name="reportSetting.gradeType.id">
	        [#list gradeTypes?sort_by("code") as gradeType]
		       	<option value="${gradeType.id}">[@i18nName gradeType/]</option>
	        [/#list]
	       </select>
       </td>
   </tr>
   <tr>
    <td colspan="2">&nbsp;&nbsp;提示:可在查询点击列标题中进行排序,打印出的报表也将按照查询的顺序输出.</td>
   </tr>
   <tr align="center">
      <td colspan="2">
      <input type="button" value="打印预览" onclick="printMultiStdGrade()"/>&nbsp;
      <input type="button" value="${b.text("action.close")}" onclick="jQuery.colorbox.close()">
   </tr>
  </table>
[/@]
  </div>
</div>

<script>
    function displayMultiStdSetting(){
    	var ids=bg.input.getCheckBoxValues('adminclass.id');
    	if(""==ids){
			alert("请选择一个或多个进行操作");
			return;
		}
		jQuery.colorbox({
		 overClose : false,
		 transition:'none',
		 inline:true,
		 title:'行政班成绩打印设置',
		 href:'#multiStdReportSetting'
		});
    }
	function printMultiStdGrade(){
    	form = document.printSettingForm;
    	jQuery.colorbox.close();
    	form.target="_blank";
    	var ids=bg.input.getCheckBoxValues('adminclass.id');
    	if(""==ids){
			alert("请选择一个或多个进行操作");
			return;
		}
		bg.form.addInput(form,"adminclassIds",ids,"hidden");
		bg.form.submit(form);
	}
  </script>
 [@b.foot/]
