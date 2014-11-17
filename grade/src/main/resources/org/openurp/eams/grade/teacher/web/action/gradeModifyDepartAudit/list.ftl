[#ftl]
[@b.head/]
[#include "/template/macros.ftl"/]
[@b.form name="gradeModifyAuditUpdateFrom" action="!updateStatus" target="contentDiv"]
	<input type="hidden" name="params" value="[@htm.queryStr/]" />
[/@]
[@b.grid items=gradeModifyApplys var="gradeModifyApply"]
	[@b.gridbar]
		[#if status.name() == 'NOT_AUDIT']
			var menu = bar.addMenu("${b.text('attr.graduate.outsideExam.auditPass')}","updateStatus(1)");
			menu.addItem("${b.text('attr.graduate.outsideExam.noAuditPass')}","updateStatus(0)");
		[#elseif status.name()=='DEPART_AUDIT_PASSED']
			bar.addItem("${b.text('attr.graduate.outsideExam.noAuditPass')}","updateStatus(0)");
		[#elseif status.name()=='DEPART_AUDIT_UNPASSED']	
			bar.addItem("${b.text('attr.graduate.outsideExam.auditPass')}","updateStatus(1)");
		[/#if]
		function updateStatus(passed){
			var form = document.gradeModifyAuditUpdateFrom;
			var ids = bg.input.getCheckBoxValues("gradeModifyApply.id");
			if(ids == null || ids == ""){
				alert("请至少选择一条");
				return;
			}
			var msg = "";
			if(passed){
				msg = "是否确认审核通过?";
			}else{
				msg = "是否确认审核不通过?";
			}
			if(confirm(msg)){
				bg.form.addInput(form,"passed",passed);
				bg.form.addInput(form,"gradeModifyApplyIds",ids);
				bg.form.submit(form);
			}
		}
	[/@]
	[@b.row]
		[@b.boxcol/]
		[@b.col property="course.code" title="attr.courseNo" width="5%"/]
		[@b.col property="course.name" title="attr.courseName" width="15%"/]
		[@b.col property="std.code" title="attr.stdNo" width="6%"/]
		[@b.col property="std.name" title="attr.personName" width="6%"/]
		[@b.col property="gradeType.name" title="grade.scoreType" width="6%"/]
		[@b.col property="origScoreText" title="field.exam.exam" width="6%"/]
		[@b.col property="scoreText" title="修改后成绩" width="6%"/]
		[@b.col property="examStatusBefore.name" title="exam.situation" width="6%"/]
		[@b.col property="examStatus.name" title="修改后考试情况" width="8%"/]
		[@b.col property="status" title="attr.graduate.auditStatus" width="7%"]${gradeModifyApply.status.fullName}[/@]
		[@b.col property="createdAt" title="attr.graduate.degreeApplication.degreeApplyTime" width="14%"]${gradeModifyApply.createdAt?string('yyyy-MM-dd HH:mm')}[/@]
		[@b.col property="applyer" title="申请人" width="6%"/]
		[@b.col property="applyReason" title="申请理由" width="9%"/]
	[/@]
[/@]
[@b.foot/]
