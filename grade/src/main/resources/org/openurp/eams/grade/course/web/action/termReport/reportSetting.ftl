[#ftl/]
[#include "/template/macros.ftl"/]
[@b.div id="reportSetting" style="display:none;width:500px;height:235px;position:absolute;top:28px;right:0px;border:solid;border-width:1px;background-color:#E1ECFF "]
 <table class="settingTable" style="background-color: #E1ECFF">
   <tr >
	   <td  style="width:40%">&nbsp;&nbsp;是否打印绩点</td>
	   <td>
  	     <input type="hidden" name="orderBy" value="${Parameters['orderBy']?default('null')}"/>
	     <input type="radio" value="1" name="reportSetting.printGpa" checked/>是 
	     <input type="radio" name="reportSetting.printGpa" value="0"/>否
	   </td>
   </tr>
   <tr>
     <td colspan="6"  style="height:5px;font-size:0px;" >
       <img src="static/images/action/keyline.gif" height="2" width="100%" align="top"/>
     </td>
   </tr>
   <tr>
	   <td>&nbsp;&nbsp;打印成绩：</td>
	   <td>
	       <input type="radio" value="" name="reportSetting.gradeFilters" checked />所有成绩
	       <input type="radio" value="passGrade" name="reportSetting.gradeFilters" />及格成绩
	   </td>
   </tr>
   <tr>
     <td colspan="6" style="height:5px;font-size:0px;">
       <img src="static/images/action/keyline.gif" height="2" width="100%" align="top"/>
     </td>
   </tr>
   <tr>
	   <td>&nbsp;&nbsp;是否打印奖励学分：</td>
	   <td>
	   <input type="radio" value="1" name="reportSetting.printAwardCredit" />是
	   <input type="radio" value="0" name="reportSetting.printAwardCredit" checked/>否
	   </td>
   </tr>
   <tr>
     <td colspan="6" style="height:5px;font-size:0px;">
       <img src="static/images/action/keyline.gif" height="2" width="100%" align="top"/>
     </td>
   </tr>
   <tr>
	   <td>&nbsp;&nbsp;字体大小：</td>
	   <td><input type="text" name="reportSetting.fontSize" value="13" maxlength="3"/>px</td>
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
	        	[#if GA?exists]
	                [#if gradeType.id != GA.id]
	            			<option value="${gradeType.id}">[@i18nName gradeType/]</option>
	                [/#if]
                [/#if]
	        [/#list]
	       </select>
       </td>
   </tr>
   <tr>
    <td colspan="2">&nbsp;&nbsp;提示:可在查询点击列标题中进行排序,打印出的报表也将按照查询的顺序输出.</td>
   </tr>
   <tr align="center">
      <td colspan="2">
	      <input type="button" value="打印预览" onclick="printGrade();closeSetting()"/>
	      <input type="button" value="${b.text("action.close")}" onclick="closeSetting()"/>
      </td>
   	</tr>
  </table>
  <script>
    function displaySetting(){
       closeMultiReportSetting();
       $('#reportSetting')[0].style.display="block";
       f_frameStyleResize(self);
    }
    function closeSetting(){
       $('#reportSetting')[0].style.display='none';
    }
  </script>
[/@]
