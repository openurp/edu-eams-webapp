[#ftl]
[@b.head/]
[@b.toolbar title="学生成绩添加(按课程代码－批量)"]
	bar.addClose();
[/@]
[@b.form name="actionForm"]
	[#assign stdCount=examTakeList?size]
	<input type="hidden" name="stdCount" value="${stdCount}"/>
	<table class="infoTable" width="100%">
	   <tr>
		   <td class="title">${b.text("entity.studentType")}:</td>
		   <td>${semester.studentType.name?if_exists}</td>
		   <td class="title">${b.text("attr.year2year")}:</td>
		   <td>${semester.schoolYear?if_exists}</td>
		   <td class="title">${b.text("attr.term")}:</td>
		   <td>${semester.name?if_exists}</td>
	   </tr>
	   <tr>
	   		<td class="title">成绩类别:</td>
	   		<td>${examType.name}</td>
	   		<td class="title">专业类别:</td>
	   		<td>
	   			<select name="majorType.id" style="width:100px">
	   				<option value="1" [#if Parameters['courseGrade.majorType.id']?if_exists="1"]selected[/#if]>第一专业</option>
   	      			<option value="2" [#if Parameters['courseGrade.majorType.id']?if_exists="2"]selected[/#if]>第二专业</option>
	   			</select>
	   		</td>
	   		<td class="title">${b.text("entity.markStyle")}:</td>
	   		<td>
	   			[@b.select items=markStyles value="${Parameters['markStyleId']?if_exists}" name="markStyleId" style="width:100%"/]
	   		</td>
	   </tr>
	   <tr>
	   		<td class="title">${b.text("attr.courseNo")}<font color="red">*</font>:</td>
	   		<td><input type='hidden' name='courseId' id='courseId' value='${examCourse.id}'/> ${examCourse.code}</td>
	   		<td class="title">${b.text("attr.courseName")}:</td>
	   		<td id="courseNameTD">${examCourse.name}</td>
	   		<td class="title">${b.text("attr.teachDepart")}</td>
	   		<td>${teachDepart.name}</td>
	   </tr>
	</table>
	[@b.grid items=0..stdCount-1 var="i"]
		[@b.row]
			[@b.col width="10%" title="${b.text('attr.personName')}"][/@]
			[@b.col width="10%" title="${b.text('attr.stdNo')}"][/@]
			[@b.col width="10%" title="成绩"][/@]
			[@b.col width="10%" title="${b.text('成绩')}"][/@]
			[@b.col width="10%" title="${b.text('attr.taskNo')}"][/@]
			[@b.col width="10%" title="${b.text('entity.courseType')}"][/@]
			[@b.col width="10%" title="修读类别"][/@]
			[@b.col width="10%" title="考试情况"][/@]
			[@b.col width="20%" title="说明"][/@]
		[/@]
	[/@]
	
[/@]

  <#assign stdCount=examTakeList?size>
  <input type="hidden" name="stdCount" value="${stdCount}"/>
  <table id="myBar"></table>
  <table align="center" width="90%" class="listTable"> 
   <tr>
     <td class="grayStyle" width="10%" id="f_studentType">
       &nbsp;<@text name="entity.studentType"/>:
     </td>
     <td class="brightStyle">${semester.studentType.name?if_exists}</td>
     <td class="grayStyle" id="f_year">&nbsp;<@text name="attr.year2year"/>:</td>
     <td class="brightStyle">${semester.schoolYear?if_exists}</td>
     <td class="grayStyle" width="10%" id="f_term">
       &nbsp;<@text name="attr.term"/>:
     </td>
     <td class="brightStyle">${semester.name?if_exists}</td>
   </tr>
   <tr>
     <td class="grayStyle" id="f_planType">
        &nbsp;成绩类别:
     </td>     
   	 <td class="brightStyle" >${examType.name}</td>
     <td class="grayStyle" width="13%">
       &nbsp;专业类别:
     </td>
   	 <td class="brightStyle">
   	    <select name="majorType.id" style="width:100px">
   	      <option value="1" <#if Parameters['courseGrade.majorType.id']?if_exists="1">selected</#if>>第一专业</option>
   	      <option value="2" <#if Parameters['courseGrade.majorType.id']?if_exists="2">selected</#if>>第二专业</option>
   	    </select>
   	 </td>
     <td class="grayStyle" width="13%" id="f_markStyleId">
       &nbsp;<@text name="entity.markStyle"/>:
     </td>
   	 <td class="brightStyle">
   	 	<@htm.i18nSelect datas=markStyles name="markStyleId" selected="${Parameters['markStyleId']?if_exists}" style="width:100px"/>
   	 </td>
   </tr>
   <tr>
     <td class="grayStyle" id="n_courseId">
        &nbsp;<@text name="attr.courseNo"/><font color="red">*</font>:
     </td>
   	 <td class="brightStyle" >
   	    <input type='hidden' name='courseId' id='courseId' value='${examCourse.id}'/> ${examCourse.code}
   	 </td>
   	 <td class="grayStyle"><@text name="attr.courseName"/>:</td>
   	 <td id="courseNameTD">${examCourse.name}</td>
   	 <td class="grayStyle"><@text name="attr.teachDepart"/>:</td>
	 <td> &nbsp;${teachDepart.name}</td>
   </tr>   	
  </table>
  <table width="90%" align="center" class="listTable" >
      <tbody id='addGradeTable'>
	   <tr align="center" class="darkColumn" >
  	     <td width='10%'><@text name="attr.personName"/></td>
	     <td width='10%'><@text name="attr.stdNo"/></td>
	     <td width='10%'>成绩</td>
	     <td width='10%'><@text name="attr.credit"/></td>
	     <td width='10%'><@text name="attr.taskNo"/></td>
	     <td width='10%'><@text name="entity.courseType"/></td>
	     <td width='10%'>修读类别</td>
	     <td width='10%'>考试情况</td>
  	     <td width='25%'>说明</td>
	   </tr>
	   <#list 0..stdCount-1 as i>
	   <tr class="grayStyle" align="center">
  			<td id="stdName${i}">${examTakeList[i].std.name}</td>
			<td ><input type='text' class="text" value="${examTakeList[i].std.code}" name='stdCode${i}' maxlength="32" style="width:100px" onChange='getStdName(event);'/></td>
		    <td ><input name="score${i}" class="text" onchange="checkScore(event)" style="width:100px" maxlength="3"/></td>
            <td id="credit${i}">${examTakeList[i].task.course.credits}</td>
            <td id="taskSeqNo${i}">${examTakeList[i].task.seqNo}</td>
            <td id="courseType${i}">${examTakeList[i].task.courseType.name}</td>
            <td id="courseTakeType${i}">${examTakeList[i].courseTake.courseTakeType.name}</td>
			<td id="examStatus${i}">${examTakeList[i].examStatus.name}</td>
       	    <td id="comment${i}">${examTakeList[i].examType.name}</td>
	   </tr>
	   </#list>
	   <tr align="center" class="darkColumn" >
	   	<td colspan='10'>
	   		<button  onClick="saveGrade(0)" class="buttonStyle">提交</button>
	   	</td>
	   </tr>
	 </tbody>
     </table>
    <pre>
    说明:对于已经存在的成绩，再行输入保存将进行更改操作。
    </pre>
  <script src='${base}/dwr/interface/courseDao.js'></script>
  <script>
     var bar = new ToolBar("myBar","学生成绩添加（按课程代码－批量）",null,true,true);
     bar.setMessage('<@getMessage/>')
     bar.addClose();
     var form =document.actionForm;
     function getCourseInfo(event){
        var input = getEventTarget(event);
        if(input.value!=""){
	        courseDao.getCourseByCode(form['courseCode'].value,setCourseData);
         }
     }
     function setCourseData(data){
        if(null==data){
           $("courseNameTD").innerHTML="<font color='red'>该学期没有该课程或者不唯一</font>";
           form['courseId'].value="";
           form['courseCode'].value="";
        } else {
	        form['courseId'].value=data.id;
	        form['courseCode'].value=data.code;
	        $("courseNameTD").innerHTML=data.name;
        }
     }
     var stdIndexArray= new Array();     
     function getStdName(event){
       var input = getEventTarget(event);
       var stdCode= input.value;
       if(stdCode==""){
          clearGradeInfo(input.name.substring("stdCode".length));
       }else{
          stdIndexArray.push(input.name.substring("stdCode".length));
          stdGrade.getCourseGradeInfo(
          		stdCode,
          		form['semester.studentType.id'].value,
          		form['semester.schoolYear'].value,
          		form['semester.name'].value,
          		form['courseCode'].value,
          		form['gradeTypeId'].value,
          		setGradeData);
       }
     }
     
     function setGradeData(data){
       index = stdIndexArray.shift();
       if(null!=data[1]){
          $("stdName"+index).innerHTML=data[1];
          if(null!=data[2]){
             $("comment"+index).innerHTML="数据已存在";
             form["score"+index].value=data[2];
          }
          $("examStatus"+index).innerHTML=data[3];
          $("credit"+index).innerHTML=data[4];
          $("taskSeqNo"+index).innerHTML=(data[5] == null ? "" : data[5]);
          $("courseType"+index).innerHTML=data[6];
          $("courseTakeType"+index).innerHTML=data[7];
          if(null==data[8]&&null==data[0]){
            $("comment"+index).innerHTML+="不在上课名单中";
          }
       }else{
          clearGradeInfo(index);
          if(form['courseCode'].value==""){
            $("stdName"+index).innerHTML="请先确定课程";
          }else{
            $("stdName"+index).innerHTML="该学号不存在";
          }
       }
     }
     //清除某一行中学生的成绩信息
     function clearGradeInfo(index){
          $("stdName"+index).innerHTML="";
          form["stdCode"+index].value="";
          form["score"+index].value="";
          $("credit"+index).innerHTML="";
          $("taskSeqNo"+index).innerHTML="";
          $("courseType"+index).innerHTML="";
          $("courseTakeType"+index).innerHTML="";
          $("examStatus"+index).innerHTML="";
          $("comment"+index).innerHTML="";
     }
     function checkScore(event){
       input = getEventTarget(event);
       var score= input.value;
       if(""!=score){
         if(isNaN(score)){
            alterErrorScore(input,score+" 不是数字");
         }
         else if(!/^\d*\.?\d*$/.test(score)){
            alterErrorScore(input,"请输入0或正实数");
         }
         else if(parseInt(score)>100){
            alterErrorScore(input,"百分制输入不允许超过100分");
         }
       }
    }
    //检查分数的合法性
    function alterErrorScore(input,msg){
      input.value="";      
      alert(msg);
    }
    function saveGrade(addAnother){
     var errMsg="";
     if(form['courseCode'].value==""){
        errMsg+="课程信息填写不全\n";
     }
     if(form['gradeTypeId'].value==""){
        errMsg+="成绩类型缺失\n";
     }
     for(var i=0;i<${stdCount};i++){
        if(form['stdCode'+i].value!=""&&isEmpty(form['score'+i].value)){
            errMsg+="第"+(i+1)+"行没有成绩\n";
        }
     }
     if(''!=errMsg){alert(errMsg);return;}
     
     form.action ="${b.url('stdGrade!batchSaveCourseGrade')}?addAnother="+addAnother;
     if(confirm("是否提交填写的成绩?")){
        form.submit();
     }
    }
   
  </script>
