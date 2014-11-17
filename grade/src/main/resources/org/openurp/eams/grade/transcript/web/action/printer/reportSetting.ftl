[#ftl/]
[@b.div id="reportSetting" style="display:none;width:650px;height:230px;position:absolute;top:28px;right:0px;border:solid;border-width:1px;background-color:white"]
 <table class="settingTable">
   <tr>
     <input type="hidden" name="orderBy" value="${Parameters['orderBy']?default('null')}"/>
     <td width="14%">&nbsp;${b.text("grade.printTemplate")}：</td>
     <td width="30%" colspan="2">
        <select name="reportSetting.template" style="width:140px">
            <option value="default" selected>默认模板（居中）</option>
            <option value="print">打印模板（归档）</option>
            <option value="single">缺省模板（无院系、专业）</option>
        </select>
       </td>
       <td width="18%">&nbsp;${b.text("grade.printScope")}：</td>
       <td><input type="radio" value="passGrade" name="reportSetting.gradeFilters" checked/>${b.text("grade.pass")}
           <input type="radio" value="makeupGrade" name="reportSetting.gradeFilters"/>${b.text("grade.all")}
           <input type="radio" value="bestGrade" name="reportSetting.gradeFilters"/>最好成绩
       </td>
   </tr>
   <tr>
     <td colspan="5" style="height:5px;font-size:0px;">
       <img src="static/images/action/keyline.gif" height="2" width="100%" align="top"/>
     </td>
   </tr>
   <tr>
       <td id="f_pageSize" colspan="2">&nbsp;${b.text("common.maxRecodesEachPage")}<font color="red">*</font>：</td>
       <td><input value="100" type="text" maxlength="5" style="width:75px" name="reportSetting.pageSize"/></td>
       <td id="f_fontSize">&nbsp;${b.text("common.foneSize")}<font color="red">*</font>：</td>
       <td><input type="text" name="reportSetting.fontSize" value="10" style="width:75px" maxlength="2"/>px</td>
   </tr>
   <tr>
     <td colspan="5" style="height:5px;font-size:0px;">
       <img src="static/images/action/keyline.gif" height="2" width="100%" align="top"/>
     </td>
   </tr>
   <tr>
       <td>&nbsp;打印绩点：</td>
       <td colspan="2">
         <input type="radio" value="1" name="reportSetting.printGpa" checked/>${b.text("yes")}
         <input type="radio" name="reportSetting.printGpa" value="0"/>${b.text("no")}
       </td>
       <td>&nbsp;${b.text("grade.deploy")}：</td>
       <td>
           <input type="radio" value="1" name="reportSetting.published" checked/>${b.text("grade.beenDeployed")}
           <input type="radio" value="0" name="reportSetting.published"/>${b.text("grade.all")}
       </td>
   </tr>
   <tr>
     <td colspan="5" style="height:5px;font-size:0px;">
       <img src="static/images/action/keyline.gif" height="2" width="100%" align="top"/>
     </td>
   </tr>
   <tr>
       <td>&nbsp;${b.text("grade.sort")}：</td>
       <td>
           <select name="reportSetting.order.property">
            <option value="semester.beginOn">${b.text("attr.yearTerm")}</option>
            <option value="course.name">${b.text("attr.courseName")}</option>
            <option value="credit">${b.text("attr.credit")}</option>
           </select>
       </td>
       <td>
           ${b.text("common.order")}:
           <select name="reportSetting.order.ascending">
            <option value="1">${b.text("action.asc")}</option>
            <option value="0">${b.text("action.desc")}</option>
           </select>
       </td>
       <td>&nbsp;${b.text("grade.printType")}：</td>
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
     <td colspan="5" style="height:5px;font-size:0px;">
       <img src="static/images/action/keyline.gif" height="2" width="100%" align="top"/>
     </td>
   </tr>
   <tr>
     <td>&nbsp;${b.text("common.printPerson")}：</td>
     <td colspan="2"><input name="reportSetting.printBy" value="" style="width:100px" maxlength="20"/></td>
     <td>&nbsp;打印日期：</td>
     <td><input name="reportSetting.printAt" value="${Parameters["reportSetting.printAt"]?default(printAt?string("yyyy-MM-dd"))}" onfocus="calendar()" style="width:100px"/>（默认当天）</td>
   </tr>
   <tr>
    <td colspan="5">&nbsp;${b.text("grade.stdGradeReport.tip")}</td>
   </tr>
   <tr align="center">
      <td colspan="5">
      <input type="button" value="${b.text("action.preview")}" onclick="printGrade();closeSetting()">
      &nbsp;
      <input type="button" value="${b.text("action.close")}"  onclick="closeSetting();">
   </tr>
 </table>
  <script>
    function displaySetting(){
       if ($('#reportSetting')[0].style.display == "block") {
           $('#reportSetting')[0].style.display = 'none';
       } else {
           $('#reportSetting')[0].style.display = "block";
           f_frameStyleResize(self);
       }
    }
    function closeSetting(){
       $('#reportSetting')[0].style.display = 'none';
    }
  </script>
 [/@]
