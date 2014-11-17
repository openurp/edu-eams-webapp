[#ftl]
		   [#if stds.size()>1]
		   		[#if lineCount>maxCount]
		   			[#assign allisprint = false/]
		   			[#assign isprint = false/]
		   		[#else]
		   			[#if stdSchoolYear.size()>4]
		   				[#assign allisprint = false/]
		   				[#assign isprint = false/]
		   			[/#if]
		   		[/#if]
		   [#else]
		   		[#assign lineCount = lineCount+2/]
		   [/#if]
		    [#if allisprint]
    	    <table width='95%' align='center' border='0' cellspacing="0" style="font-family:仿宋_GB2312;font-size:14px;"> 
	        <tr height="">
	        	<td width="15%">&nbsp;${b.text("std.name")}:[@i18nName std!/]</td>
	            <td width="15%">&nbsp;${b.text("attr.stdNo")}:${std.code!}</td>
	            <td width="30%">&nbsp;${b.text("common.college")}:[@i18nName std.department!/]</td>
	            <td width="25%">&nbsp;${b.text('major')}:[@i18nName std.major!/]</td>
	            <td width="10%">&nbsp;${b.text('education')}:[@i18nName std.education!/]</td>
	            <td width="5%" align="left">&nbsp;学制:${std.duration!}</td>
	        </tr>
		    </table>
		    <table width="95%" align="center"  style="font-family:仿宋_GB2312;border-width: 0px;border-style: none;font-size:14px;table-layout: fixed;text-align:center">
	    			<tr algin="center">
			    		[#list stdSchoolYear as key]
	    					[#if key_index>3 && key_index%4==0]
	    						</tr><tr>
	    					[/#if]
	    					<td>
			    				[#list stdSemesterName as ke]
										[#if semesters[key][ke]?exists]
											[#assign count = 0/]
				    						<table width="100%" class="reportTable">
				    							<tr><td colspan="4">${key}学年第${ke}学期</td></tr>
				    							<tr>
				    								<td width="55%">课程名称</td>
					    							<td width="15%">考试</td>
													<td width="15%">考查</td>
													<td width="15%">学分</td>
												</tr>
												[#list semesters[key][ke]?sort_by(["course","code"]) as courseGrade]
													[#if courseGrade.course.name?length>14]
														[#assign count = count+1/]
														<tr style="height:36px;">
															<td rowspan="1">${courseGrade.course.name!!}</td>
															[#if courseGrade.markStyle.numStyle]<td rowspan="1">${courseGrade.score!}</td><td rowspan="1"></td>
															[#else]<td rowspan="1"></td><td rowspan="1">${courseGrade.scoreText!}</td>[/#if]
															<td rowspan="1">${courseGrade.course.credits!}</td>
														</tr>
													[#else]
														<tr>
															<td>${courseGrade.course.name!!}</td>
															[#if courseGrade.markStyle.numStyle]<td>${courseGrade.score!}</td><td rowspan="1"></td>
															[#else]<td></td><td rowspan="1">${courseGrade.scoreText!}</td>[/#if]
															<td>${courseGrade.course.credits!}</td>
														</tr>
													[/#if]
												[/#list]
												[#if (lineCount-semesters[key][ke].size()-count)>0]
													[#list lineCount-semesters[key][ke].size()-count..1 as i]
														<tr>
														<td></td>
														<td></td>
														<td></td>
														<td></td>	
														</tr>
												    [/#list]
											    [/#if]
				    						</table>
				    						[@b.div style="margin-top:5px;"/]
				    					[#else]
				    						[@stdGradeTable key,ke/]
									    [/#if]
			    				[/#list]
	    					</td>
			    		[/#list]
			    		[#assign lineLengthb = 4-semesters.size()/]
			    		[#if lineLengthb >0]
				    		[#list lineLengthb..1 as i]
				    			<td>
			    				[#list semesterName as i]
									[@stdGradeTable '',i/]
			    				[/#list]
			    				</td>
				    		[/#list]
			    		[/#if]
	    			</tr>
		    	</table>
		    	[@b.div style="margin-top:5px;"/]
			    <table width='95%' align='center' border='0' cellspacing="0" style="font-family:仿宋_GB2312;font-size:14px;"> 
			        <tr>
			        	<td width="50%">&nbsp;总学分:[#if stdGpa?exists]${stdGpa.credits!}[/#if]</td>
			            <td width="40%">&nbsp;毕(结)业证书编号:</td>
			        </tr>
			        <tr >
			            <td width="50%">&nbsp;平均绩点:[#if stdGpa?exists]${stdGpa.gpa!}[/#if]</td>
			            <td width="40%">&nbsp;学位证书编号:</td>
			        </tr>
			        <tr>
			        	<td width="50%">&nbsp;毕结业结论:</td>
			        	<td>[@i18nName school/]教务处<td>
			        </tr>
			        <tr>
			        	<td colspan="2" align="right">经办人:<td>
			        </tr>
			        <tr>
			        	<td colspan="2" align="right">日期:${nowDate?if_exists?string('yyyy年MM月dd日')}<td>
			        </tr>
		    	</table>
		    	[#macro stdGradeTable(schoolYear,name)]
					<table width="100%" class="reportTable">
						<tr><td colSpan="4">${schoolYear!}学年第${name!}学期</td></tr>
						<tr>
							<td width="70%">课程名称</td>
							<td width="10%">考试</td>
							<td width="10%">考查</td>
							<td width="10%">学分</td>
						</tr>
						[#list 1..lineCount as j]
							<tr height="18px">
							<td></td>
							<td></td>
							<td></td>
							<td></td>
							</tr>
					   [/#list]
					 </table>  
					 [@b.div style="margin-top:5px;"/]
		    	[/#macro]
		    	[#else]
					[#if !isprint]
						${std.name!}该学生的成绩异常,请单独打印！
					[/#if]
		    	[/#if]
