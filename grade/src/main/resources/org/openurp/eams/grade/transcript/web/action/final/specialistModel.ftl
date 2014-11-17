[#ftl]
    [#if stds.size()>1]
   		[#if lineCount>maxCount]
   			[#assign allisprint = false/]
   			[#assign isprint = false/]
   		[#else]
   			[#if stdSchoolYear.size()>3]
   				[#assign allisprint = false/]
   				[#assign isprint = false/]
   			[/#if]
   		[/#if]
    [/#if]
	[#if allisprint]
	    <table width='95%' align='center' border='0' cellspacing="0" style="font-family:仿宋_GB2312;font-size:14px;"> 
	        <tr height="">
	        	<td width="35%">&nbsp;${b.text("std.name")}:[@i18nName std!/]</td>
	            <td width="30%">&nbsp;${b.text("attr.stdNo")}:${std.code!}</td>
	            <td>&nbsp;${b.text("common.college")}:[@i18nName std.department!/]</td>
	        </tr>
	        <tr height="">
	            <td width="25%">&nbsp;${b.text('major')}:[@i18nName std.major!/]</td>
	            <td width="30%">&nbsp;${b.text('education')}:[@i18nName std.education!/]</td>
	            <td align="left">&nbsp;学制:${std.duration!}</td>
	        </tr>
	    </table>
    	<table id="fatherTable" width='95%' align="center"  style="font-family:仿宋_GB2312;border-width: 0px;border-style: none;font-size:14px;table-layout: fixed;text-align:center">
				[#list stdSchoolYear as key]
					<tr align="center">
							[#assign myLinecount=0/]
							[#list stdSemesterName as ke]
								[#if semesters[key][ke]?? && semesters[key][ke]?size>myLinecount]
								[#assign myLinecount=semesters[key][ke]?size/]
								[/#if]
							[/#list]
							[#list stdSemesterName as ke]
							[#if semesters[key][ke]?exists]
								[#assign count = 0/]
									<td>
										<table width="100%" class="reportTable">
										  <tr>
											<td rowspan="${myLinecount+1}" width="5%" id="${std.code}${key}${ke}">${key}学年第${ke}学期</td>
											<td width="65%">课程名称</td>
											<td width="10%">考试</td>
						 					<td width="10%">考查</td>
											<td width="10%">学分</td>
										   </tr>
										   [#list semesters[key][ke] as courseGrade]
											[#assign course = courseGrade.course/]
										   	[#if courseGrade.course.name?length>30]
										   		[#assign count = count +1/]
												<tr style="height:36px;">
													<td rowspan="1">[@i18nName course/]</td>
													[#if (!course.examMode??)||(course.examMode?? && course.examMode.id = examMode.id)]
													<td rowspan="1">${courseGrade.score!}</td>
													[#else]
														<td rowspan="1"><td>
													[/#if]
													
													[#if course.examMode?? && course.examMode.id !=examMode.id]
													<td rowspan="1">${courseGrade.scoreText!}</td>
													[#else]
														<td rowspan="1"><td>
													[/#if]
													<td rowspan="1">${courseGrade.course.credits!}</td>
												</tr>
											[#else] 
												<tr>
													<td>${courseGrade.course.name!}</td>
													[#if (!course.examMode??)||(course.examMode?? && course.examMode.id = examMode.id)]
													<td>${courseGrade.score!}</td>
													[#else]
													<td></td>
													[/#if]
													
													[#if course.examMode?? && course.examMode.id !=examMode.id]
													<td>${courseGrade.scoreText!}</td>
													[#else]
													<td></td>
													[/#if]
													<td>${courseGrade.course.credits!}</td>
												</tr>
											[/#if]
										   [/#list]
										   [#if myLinecount-semesters[key][ke].size()-count>0]
											   [#list (myLinecount-semesters[key][ke].size())-count..1 as i]
													<tr>
													<td></td>
													<td></td>
													<td></td>
													<td></td>
													</tr>
											   [/#list]
										   [/#if]
										</table>
										<script>
											var count = ${count};
											var maxCount = ${myLinecount};
											if(count>0){
												var count = maxCount+1-count;
												jQuery('#${std.code}${key}${ke}').prop("rowSpan",count);
											}
										</script>
									</td>
								[#else]
									[@stdGradeTable key,ke,myLinecount/]
								[/#if]
							[/#list]
					</tr>
						[@b.div style="margin-top:2px;"/]
				[/#list]
				[#assign lineLengthz = 3-semesters.size()/]
				[#if lineLengthz > 0]
				[#list lineLengthz..1 as i]
					<tr align="center">
						[#list semesterName as a]
							[@stdGradeTable '',a,myLinecount/]
						[/#list]
					</tr>
					[@b.div style="margin-top:5px;"/]
				[/#list]
				[/#if]
       	</table>
	    <table width='95%' align='center' border='0' cellspacing="0" style="font-family:仿宋_GB2312;font-size:14px;"> 
	        <tr>
	        	<td width="20%">&nbsp;总学分:[#if stdGpa?exists]${stdGpa.credits!}[/#if]</td>
	            <td width="40%">&nbsp;毕(结)业证书编号:</td>
	            <td width="30%">&nbsp;平均绩点:[#if stdGpa?exists]${stdGpa.gpa!}[/#if]</td>
	        </tr>
	        <tr >
	            
	            <td colspan="2">&nbsp;毕(结)业结论:</td>
	            <td>经办人:</td>
	        </tr>
	        <tr>
	        	<td colspan="2">[@i18nName school/]教务处</td>
	        	<td align="right">${nowDate?if_exists?string('yyyy年MM月dd日')}<td>
	        </tr>
    	</table>
	    	<script>
				var tds = jQuery('#fatherTable tr td table tr:first-child td:first-child');
				tds.each(function(){
					var tdText = jQuery(this).text();
					var tdTextFor = '';
					for(var i = 0 ;i<tdText.length;i++){
						tdTextFor = tdTextFor + tdText.charAt(i)+"<br>";
					}
					jQuery(this).html(tdTextFor);
				});
			</script>
			[#macro stdGradeTable(schoolYear,name,myLinecount)]
				<td>
					<table width="100%" class="reportTable">
					  <tr>
							<td rowspan="${myLinecount+1}" width="5%">${schoolYear!}学年第${name!}学期</td>
							<td width="65%">课程名称</td>
							<td width="10%">考试</td>
							<td width="10%">考查</td>
							<td width="10%">学分</td>
					   </tr>
					   [#list 1 ..myLinecount as c]
							<tr>
							<td></td>
							<td></td>
							<td></td>
							<td></td>
							</tr>
					   [/#list]
					</table>
				</td>
			[/#macro]
	[#else]
		[#if !isprint]
		${std.name!}该学生的成绩异常,请单独打印！
		[/#if]
	[/#if]