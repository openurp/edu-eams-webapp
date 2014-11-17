[#ftl/]
[@b.head/]
[@b.messages slash="6"/]
[#assign stdCount=15]
[@b.toolbar title="学生成绩添加(按课程代码－批量)"]
	bar.addBack("${b.text("action.back")}");
[/@]
<div style="background-color: #d1dCFF;border-width:2px 1px 0 1px;border-style: solid;border-color: #006CB2;padding:2px 10px;">
	[@eams.semesterCalendar label="学年学期" id="lessonSemesterId" name="semesterId" value=semester empty="false"/]
</div>
[@b.form name="bachAddForm"]
 	<input type="hidden" name="stdCount" value="${stdCount}"/>
	<table class="gridtable" width="100%">
		<tr >
			<td class="griddata-odd" style="text-align:right;width:10%;" id="f_planType">成绩类别:</td>
		    <td class="" width="20%">
		    	[@b.select items=gradeTypes id="gradeTypeId" name="gradeTypeId" style="width:140px;" value=Parameters['gradeTypeId']?default("") /]
		    </td>
		    <td class="griddata-odd" style="text-align:right;width:10%;" id="f_markStyleId">${b.text("entity.markStyle")}:</td>
		    <td class="" width="20%">
		    	[@b.select items=markStyles id="markStyleId" name="markStyleId" style="width:100px" onChange="changeInputField();" value="${Parameters['markStyleId']?if_exists}"/]
		    </td>
			<td class="griddata-odd" style="text-align:right;width:10%;">${b.text("成绩状态")}:</td>
			<td width="30%">
				[@b.select items={'0':'未提交','1':'已提交未发布','2','已发布'} value="2" name="courseGradeStatus" style="width:100px"/]
			</td>
	     </tr>
		 <tr>
			<td class="griddata-odd" style="text-align:right;width:10%;"><font color="red">*</font>${b.text("attr.courseNo")}:</td>
			<td>
				[@b.textfield label="" id="courseCode"  name="courseCode" maxlength="32" value="" style="width:95px" onChange="getCourseInfo()"/]
				<input type="hidden" id="courseId" name="courseId" value="">
			</td>
			<td class="griddata-odd" style="text-align:right;width:10%;">${b.text("attr.courseName")}:</td>
			<td id="courseNameTD"></td>
			<td class="griddata-odd" style="text-align:right;width:10%;">${b.text("attr.credit")}:</td>
			<td id="courseCredit"></td>
		 </tr>
	</table>
	[@b.div style=""]
		    说明:<br>
		    　　输入课程代码，系统自动查找任务。<br>
		    　　说明:对于已经存在的成绩，再行输入保存将进行更改操作。
	[/@]
	<table class="gridtable">
		<thead class="gridhead">
	       <tr>
	         <td width='10%'>${b.text("attr.personName")}</td>
	         <td width='10%'>${b.text("attr.stdNo")}</td>
	         <td width='10%'>成绩</td>
	         <td width='10%'>${b.text("attr.taskNo")}</td>
	         <td width='15%'>${b.text("entity.courseType")}</td>
	         <td width='15%'>修读类别</td>
	         <td width='10%'>考试情况</td>
	         <td width='20%'>说明</td>
	       </tr>
	    </thead> 
	    <tbody>
       [#list 0..stdCount-1 as i]
       <tr class="griddata-odd" align="center">
            <td id="stdName${i}"></td>
            <td>
            	[@b.textfield label="" id="stdCode${i}" name="stdCode${i}" maxlength="32" style="width:90px" onchange="getStudentByCode('${i}')"/]
            </td>
            <td id="scores${i}">
              <div id="inputId${i}" style="display:block;">
              	[@b.textfield label="" id="scoreVal${i}" name="score${i}" style="width:90px" maxlength="3" onchange="checkScore('${i}')"/]
              </div>
              <div id="selectId${i}" style="display:none;">
                <select style="width:100px" class="text" id="optionScoreId${i}" name="optionScore${i}">
                   <option value="">请选择...</option>
                </select>
              </div>
            </td>
            <td id="taskSeqNo${i}"></td>
            <td>
	            [@b.select id="courseType${i}" items=courseTypes empty="..." name="courseType${i}" style="width:140px"/]
            </td>
            <td>
            	[@b.select items=courseTakeTypes empty="..." id="courseTakeType${i}" name="courseTakeType${i}" style="width:100px"/]
            </td>
            <td id="examStatus${i}"></td>
            <td id="comment${i}"></td>
       </tr>
       [/#list]
       <tr align="center" class="darkColumn">
        <td colspan='10'>
        	<input type="button" onclick="saveGrade(0)" value="提交"/>
        	<input type="button" onclick="saveGrade(1)" value="提交,并添加下一批"/>
        </td>
       </tr>
     </tbody>
     </table>
[/@]
<script type="text/javascript">
		function getCourseInfo(){
		if($('#courseCode').val()==""){
			$('#courseNameTD').html("<span style='color:red'>课程代码为空!</span>");
			$('#courseId').val('');
			$('#courseCredit').html('');
			$('#courseCode').val('');
			return false;
		}
		
		var res = jQuery.post("${b.url('std-grade!getCourseByCode')}",{courseCode:$('#courseCode').val()},function(){
			if (res.readyState == 4 && res.status == 200 && res.responseText!=""){
				var course = jQuery.parseJSON(res.responseText);
				var message = "";
				if(course.responseStr == "noData"){
					message="课程代码输入有误!";
				}else if (course.responseStr == "dataException"){
					message="该课程代码对应的课程数据异常,请联系管理员";
				}else if(course.responseStr == "reqIsNull"){
					message="课程代码为空!";
				}else{
					$('#courseNameTD').html(course.courseName);
					$('#courseId').val(course.courseId);
					$('#courseCredit').html(course.credit);
				}
				
				if(message != ""){
					$('#courseNameTD').html("<span style='color:red'>"+message+"</span>");
					$('#courseId').val('');
					$('#courseCredit').html('');
					$('#courseCode').val('');
				}
			}
		});
	}
	

	function getStudentByCode(index){
		if($('#courseId').val()==""){
			$('#taskSeqNo'+index).html("<span style='color:red'>课程代码为空!</span>");
			return false;
		}
		
		if($('#stdCode'+index).val()==""){
			$('#taskSeqNo'+index).html("<span style='color:red'>学号为空!</span>");
			return false;
		}
		
		var res = jQuery.post("${b.url('std-grade!getStudentByCode')}",{stdCode:$('#stdCode'+index).val(),
											semesterId:jQuery('input[name=semesterId]').val(),
																  courseCode:$('#courseCode').val(),
																  gradeTypeId:$('#gradeTypeId').val()},function(){
			if (res.readyState == 4 && res.status == 200 && res.responseText!=""){
				var student = jQuery.parseJSON(res.responseText);
				if(student.flag =="true"){
					 $('#stdName'+index).html(student.stdName);
					 $('#scoreVal'+index).val(student.examGradeScore);
					 $('#courseType'+index).val(student.examGradeStatus);
					 $('#taskSeqNo'+index).html(student.lessonNo);
					 $('#courseType'+index).val(student.courseTypeId);
					 $('#courseTakeType'+index).val(student.courseTakeTypeId);
					 $('#examStatus'+index).html(student.examGradeStatus);
					 
					 if(student.courseTakeTypeId =="" && student.gradeId==""){
					 	 $("#comment"+index).html("不在上课名单中");
					 }
				}else{
					$('#stdName'+index).html('');
					$('#taskSeqNo'+index).val('');
					$('#courseType'+index).val('');
					$('#examStatus'+index).val('');
					$('#comment'+index).val('');
					
					if($('#courseId').val()==""){
						$('#stdName'+index).html("<span style='color:red'>请先确定课程!</span>");
					}else{
						$('#stdName'+index).html("<span style='color:red'>该学号不存在!</span>");
					}
				}
			}	
		});
	}
	
	function checkScore(index){
       var score= $('#scoreVal'+index).val();
       if(""!=score){
         if(isNaN(score)){
            alert(score+" 不是数字");
            $('#scoreVal'+index).val('');
         } else if(!/^\d*\.?\d*$/.test(score)) {
            alert("请输入0或正实数");
            $('#scoreVal'+index).val('');
         } else if(parseInt(score)>100) {
            alert("百分制输入不允许超过100分");
            $('#scoreVal'+index).val('');
         }
       }
    }

    function changeInputField() {
      var markStyleId=$('#markStyleId').val();
      [#list configs as config]
	      if (markStyleId == ${config.scoreMarkStyle.id}) {
	        [#if config.scoreMarkStyle.numStyle]
	            for (var i = 0; i < ${stdCount}; i++) {
	                $('#scoreVal'+i).val('');
	                $('#selectId'+i).css('display','none');
	                $('#inputId'+i).css('display','block');
	            }
	        [#else]
	            for (var i = 0; i < ${stdCount}; i++) {
	            	$('#optionScoreId'+i).val('');
	            	$('#optionScoreId'+i).get(0).options.length=0;
	            	$('#optionScoreId'+i).prepend("<option value=''>...</option>");	
	            [#list config.items as item]
	                $('#optionScoreId'+i).append("<option value='${item.defaultScore}'>${item.grade}</option>");
	            [/#list]
	            	$('#inputId'+i).css('display','none');
	            	$('#selectId'+i).css('display','block');
	            }
	        [/#if]
	      }
      [/#list]
    }
    
    
    function saveGrade(addAnother){
	     var markStyleId=$('#markStyleId').val();
	     var errMsg="";
	     if($('#courseCode').val()==""){
	        errMsg+="课程信息填写不全\n";
	     }
	     if($('#gradeTypeId').val()==""){
	        errMsg+="成绩类型缺失\n";
	     }
	     for(var i=0;i<${stdCount};i++){
	       if(markStyleId==2){
		        if($('#stdCode'+i).val()!="" && $('#optionScore'+i).val()==""){
		              errMsg+="第"+(i+1)+"行没有成绩\n";
		        }
	       }else{
		        if($('#stdCode'+i).val()!=""&&$('#score'+i).val()==""){
		            errMsg+="第"+(i+1)+"行没有成绩\n";
		        }
	       }
	     }
	     if(''!=errMsg){alert(errMsg);return false;}
	     var form = document.bachAddForm;
	     if(confirm("是否提交填写的成绩?")){
	     	bg.form.addInput(form,"addAnother",addAnother);
	     	bg.form.submit(form,"${b.url('std-grade!batchSaveCourseGrade')}");
	     }
    }
</script>
[@b.foot/]
