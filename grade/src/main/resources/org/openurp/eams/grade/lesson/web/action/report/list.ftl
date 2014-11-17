[#ftl]
[@b.head/]
<script language="JavaScript" type="text/JavaScript" src="${base}/static/scripts/course/grade/gradeSeg.js"></script>
[@b.toolbar title="已发布成绩的教学任务列表"]
	bar.addItem("教学班成绩","printTeachClassGrade(document.actionForm)");
	bar.addItem('任务分段统计',"printStatReport(document.actionForm,'task')");
    bar.addItem('成绩分段统计',"printStatReport(document.actionForm,'course')");
    bar.addItem('试卷分析',"printExamReport(document.actionForm)");
    bar.addItem("导出","exportData()");
[/@]
[#include "taskListTable.ftl"/]
  [@b.form name="actionForm"]
  		<input type="hidden" name="scoreSegmentsLength" value="5"/>
  [/@]
  <script>
    var action="teachClassGradeReport.action";
   
    function exportData(){
       var form =document.gradeIndexForm;
       bg.form.addInput(form,"keys","no,course.code,course.name,courseType.name,teachDepart.name,teachClass.name,teachClass.stdCount,course.credits");
       bg.form.addInput(form,"titles","课程序号,课程代码,课程名称,课程类别,开课院系,教学班,人数,学分");
       bg.form.addInput(form,"fileName","教学任务信息");
       bg.form.submit(form,"${b.url('!export')}",null,null,false);
    }
    
    orderBy = function(what){
        parent.searchTask();
    }
    	 //打印分段统计
	 function printStatReport(form,kind){
	    form.target="_blank";
	    form.action="${b.url('!stat')}";
	    for(var i=0;i<seg.length;i++){
          	var segAttr="segStat.scoreSegments["+i+"]";
          	bg.form.addInput(form,segAttr+".min",seg[i].min);
          	bg.form.addInput(form,segAttr+".max",seg[i].max);
        }
        if(null==kind){
           kind="task";
        }
        bg.form.addInput(form,"kind",kind);
	    bg.form.submitId(form,"lesson.id",true);
	    form.target="_self";
	 }
     //打印试卷分析
     function printExamReport(form){
        form.target="_blank";
	    for(var i=0;i<seg.length;i++){
          	var segAttr="segStat.scoreSegments["+i+"]";
         	bg.form.addInput(form,segAttr+".min",seg[i].min);
         	bg.form.addInput(form,segAttr+".max",seg[i].max);
        }
        bg.form.submitId(form,"lesson.id",true,"${b.url('!reportForExam')}");
        form.target="_self";
     }
	 //打印教学班成绩
	 function printTeachClassGrade(form){
        form.target="_blank";
        bg.form.submitId(form,"lesson.id",true,"${b.url('!report')}");
        form.target="_self";
	 }
  </script>
[@b.foot/]
