[#ftl]
[@b.head/]
[#include "/template/macros.ftl"/]
[#include "/template/print.ftl"/]
<style type="text/css">
	.tacss{
		border-top:thick solid;
		border-top-width:1px;
		border-bottom:thick solid;
		border-bottom-width:1px;
		border-left-style: none;
		border-right-style: none;
		border-left:0px;
		border-right:0px;
	}
	
	.reportTable{
	
	}
	
	table.reportTable td{
		font-size:11px;
	}
</style>
[@b.toolbar title="毕业生成绩单打印"]
	bar.addPrint();
	bar.addClose();
[/@]
[#list stds as std]
		<h3 style="text-align:center;">[@i18nName school/]—学生成绩表</h3>
		<table style="width:80%;" align="center">
			<tr>
				<td width="40%">${b.text("i18n_task_fake.departments")}:[@i18nName std.department!/]</td>
				<td width="35%">${b.text("entity.major")}:[@i18nName std.major!/]</td>
				<td width="25%">${b.text("i18n_student_std.education.name")}:[@i18nName std.education!/]</td>
			</tr>
			<tr>
				<td>${b.text("attr.personName")}:[@i18nName std!/]</td>
				<td>${b.text("attr.stdNo")}:${std.code!}</td>
				<td>${b.text("i18n_student_stdExaminee.graduateOn")}:${std.graduateOn!}</td>
			</tr>
		</table>
		[@b.div style="margin-top:5px;"/]
		<table style="width:80%;" cellspacing="0" align="center" class="reportTable">
				[#assign semesters = stdMap[std.code]/]
	    		[#if stdGpaMap?exists]
			   		 [#assign stdGpa = stdGpaMap[std.code]?if_exists/]
			    [/#if]
			    [#assign stdSchoolYear = stdSemesetrMap[std.code]/]
			    [#assign stdSemesterName = stdSemesterNameMap[std.code]/]  
		    	[#list stdSchoolYear as key]
			    		[#list stdSemesterName as ke]
							[#if semesters[key][ke]?exists]
								    	<tr>
											<td colspan="6" text-align="left">${key}第${ke}学期</td>
										</tr>
										<tr>
										[#list semesterName as i]
											<td class="tacss" width="30%">课程名称</td>
											<td class="tacss" width="10%">学分</td>
											<td class="tacss" width="10%">成绩</td>
										[/#list]
										</tr>
										[#assign courseGrades = semesters[key][ke]/]
										[#if (semesters[key][ke].size()/2-1)>0||(semesters[key][ke].size()/2-1)=0]
											[#list 0..(semesters[key][ke].size()/2-1) as len]
												[#assign courseGradeFirst = courseGrades[len*2]/]
												[#assign courseGradeSecond = courseGrades[len*2+1]/]
												<tr>
													<td>${courseGradeFirst.course.name!}</td>
													<td>${courseGradeFirst.course.credits!}</td>
													<td>${courseGradeFirst.scoreText!}</td>
													<td>${courseGradeSecond.course.name!}</td>
													<td>${courseGradeSecond.course.credits!}</td>
													<td>${courseGradeSecond.scoreText!}</td>
												</tr>
											[/#list]
										[/#if]
										[#if courseGrades.size()%2!=0]
											<tr>
												[#assign courseGrade = courseGrades[courseGrades.size()-1]/]
													<td>${courseGrade.course.name!}</td>
													<td>${courseGrade.course.credits!}</td>
													<td>${courseGrade.scoreText!}</td>
													<td></td>
													<td></td>
													<td></td>
											</tr>
										[/#if]
										<tr><td colspan="6"></td></tr>
										<tr><td colspan="6"></td></tr>
							[/#if]
						[/#list]
		    	[/#list]
		</table>	
		[@b.div style="margin-top:5px;"/]
		<table style="width:80%;" align="center">
			<tr>
				<td colspan="2" width="70%" align="left">合计学分:[#if stdGpa?exists]${stdGpa.credits!}[/#if]</td>
				<td colspan="4">[@i18nName school/]教务处</td>
			</tr>
			<tr>
				<td colspan="2"></td>
				<td>
					经办人:<br>
					${nowDate?if_exists?string('yyyy年MM月dd日')}
				</td>
			</tr>
		</table>
	[/#list]
[@b.foot/]
