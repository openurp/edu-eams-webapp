<script src='${base}/static/scripts/common/SemesterWithProjectRefEduDepStdType.js'></script>
 <tr>
     <td class="title" width="13%">项目:</td>
     <td class="brightStyle">
	      <input type="hidden" id="semester.id" name="semester.id" value="${semester.id}"/>
	      <select id="project" name="project.id" style="width:120px;">
	        <option value="${project.id}"></option>
	      </select>
     </td>
     <td class="title" id="f_year">${b.text("attr.year2year")}:</td>
     <td>
          <select id="year" name="semester.schoolYear" style="width:120px;">
            <option value="${Parameters['courseGrade.semester.schoolYear']?if_exists}">${b.text("filed.choose")}</option>
          </select>
     </td>
     <td class="title" id="f_term">${b.text("attr.term")}:</td>
     <td>
         <select id="term" name="semester.name" style="width:120px;">
            <option value="${Parameters['courseGrade.semester.name']?if_exists}">${b.text("filed.choose")}</option>
         </select>
     </td>
 </tr>