[#ftl]
[@b.head/]
<script type="text/javascript" src="${base}/static/scripts/jsdifflib/difflib.js"></script> 
<script type="text/javascript" src="${base}/static/scripts/jsdifflib/diffview.js"></script> 
<link rel="stylesheet" type="text/css" href="${base}/static/scripts/jsdifflib/diffview.css"/>

[#function detail_part operation]
	[#local mod_oper = operation?trim /]
	[#local idx1 = mod_oper?index_of('DETAIL=') + 7/]
	[#local mod_oper = mod_oper?substring(idx1)?js_string /]
	[#return mod_oper /]
[/#function]

[@b.toolbar title="日志详细信息"]
	bar.addBack();
[/@]
<table class="infoTable" style="word-break: break-all">
	<tr>
		<td class="title" width="20%">操作人员:</td>
		<td width="30%">${(log.operator)!}&nbsp;&nbsp;IP:${log.ip!}</td>
		<td class="title" width="20%">操作日期:</td>
		<td width="30%">${log.operateAt?string('yyyy-MM-dd HH:mm:ss')}</td>
	</tr>
	<tr>
		<td class="title" width="20%">访问资源路径(URI):</td>
		<td width="30%" colspan="3">${log.entry!}</td>
	</tr>
	<tr valign="top">
		<td colspan="4" width="100%">
			<div id="diffoutput" style="width:100%"></div> 
        </td>
	</tr>
</table>
	
<script>
	function diffUsingJS () {
  [#if log_prev??]
     var bDetail=eval("${log_prev.gradeDetail!}");
     var befores=
                '课程代码='+'${log_prev.courseCode!}'+'\n'
               +'课程名称='+'${log_prev.courseName!}'+'\n'
               +'课程序号='+'${log_prev.lessonNo}'+'\n'
               +'学期='+'${log_prev.semester.schoolYear!}'+' ${log_prev.semester.name!}'+'学期'+'\n'
               +'修读类别='+'${log_prev.courseTakeType.name}'+'\n'
               +'学生姓名[学号]='+'${log_prev.student.name!}'+'['+'${log_prev.student.code!}'+']'+'\n'
               +'分数='+'${log_prev.score!}'+'\n'
               +'得分等级='+'${log_prev.scoreText!}'+'\n'
               +'是否通过='+'${log_prev.passed}'+'\n'
               +'成绩记录方式='+'${log_prev.markStyle.name!}'+'\n'
               +'绩点='+'${log_prev.gp!}'+'\n'
               +'成绩状态='+'${log_prev.status!}'+'\n'
               +'成绩详情：'+'\n'
               ;
               for(var k=0; k<=bDetail.length-1; k++){
                  befores +='成绩类型:' + bDetail[k].gradeType + ' 记录方式:' + bDetail[k].markStyle + ' 是否通过:'+bDetail[k].passed 
                    +' 百分比:'+bDetail[k].percent +' 得分:'+ bDetail[k].score + ' 分数等级:' + bDetail[k].scoreText + ' 状态:' + bDetail[k].status +'\n';
               }
  [#else]
     var befores='';
  [/#if]
   var aDetail=eval("${log.gradeDetail!('({})')}");
     var afters=
                '课程代码='+'${log.courseCode!}'+'\n'
               +'课程名称='+'${log.courseName!}'+'\n'
               +'课程序号='+'${log.lessonNo!}'+'\n'
               +'学期='+'${log.semester.schoolYear!}'+' ${log.semester.name!}'+'学期'+'\n'
               +'修读类别='+'${log.courseTakeType.name}'+'\n'
               +'学生姓名[学号]='+'${log.student.name!}'+'['+'${log.student.code!}'+']'+'\n'
               +'分数='+'${log.score!}'+'\n'
               +'得分等级='+'${log.scoreText!}'+'\n'
               +'是否通过='+'${log.passed!}'+'\n'
               +'成绩记录方式='+'${log.markStyle.name!}'+'\n'
               +'绩点='+'${log.gp!}'+'\n'
               +'成绩状态='+'${log.status!}'+'\n'
               +'成绩详情：'+'\n'
               ;
             for(var i=0; i<=aDetail.length-1; i++){
                   afters +='成绩类型:' + aDetail[i].gradeType + ' 记录方式:' + aDetail[i].markStyle + ' 是否通过:'+aDetail[i].passed 
                    +' 百分比:'+aDetail[i].percent +' 得分:'+ aDetail[i].score + ' 分数等级:' + aDetail[i].scoreText + ' 状态:' + aDetail[i].status +'\n';
               }
		var before = difflib.stringAsLines(befores);
		var after = difflib.stringAsLines(afters);
		var sm = new difflib.SequenceMatcher(before, after);
		var opcodes = sm.get_opcodes();
		var diffoutputdiv = document.getElementById("diffoutput");
		while (diffoutputdiv.firstChild) diffoutputdiv.removeChild(diffoutputdiv.firstChild);
		var contextSize = null;
		var viewType = 0;
		diffoutputdiv.appendChild(diffview.buildView({ baseTextLines:before,
													   newTextLines:after,
													   opcodes:opcodes,
													   baseTextName:"变更前",
													   newTextName:"变更后",
													   contextSize:contextSize,
													   viewType:viewType}));
	}
	jQuery(function() {
	  diffUsingJS();
	});
</script>
[@b.foot/]
