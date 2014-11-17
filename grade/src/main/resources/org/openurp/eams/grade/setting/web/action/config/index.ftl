[#ftl/]
[@b.head/]
[#include "nav.ftl" /]
[@b.toolbar title="成绩全局设置"]
	bar.addBackOrClose();
[/@]
<style>
form.listform label.title{
	width:140px
}
</style>
[#function subtract first,second]
	[#assign rs= []]
	[#list first as g]
		[#assign contains=false]
		[#list second as s][#if s.id==g.id][#assign contains=true][#break/][/#if][/#list]
		[#if !contains][#assign rs= rs+[g]][/#if]
	[/#list]
	[#return rs/]
[/#function]

[@b.form theme="list" title="成绩全局设置" action="!save"]
	[@b.select2 style="width:150px;height:150px" require=true label="最终成绩来源" name1st="allFinalCandinateTypes" name2nd="setting.finalCandinateTypes" items1st=subtract(gradeTypes,setting.finalCandinateTypes) items2nd = setting.finalCandinateTypes  /]
	[@b.select2 style="width:150px;height:150px" label="总评成绩成分" name1st="allGaElementTypes" name2nd="setting.gaElementTypes"  items2nd = setting.gaElementTypes items1st=subtract(gradeTypes,setting.gaElementTypes)/]
	[@b.select2 style="width:150px;height:150px" label="可发布成绩类型" name1st="allPublishableTypes" name2nd="setting.publishableTypes" items1st=subtract(gradeTypes,setting.publishableTypes) items2nd =setting.publishableTypes /]
	[@b.select2 style="width:150px;height:150px" label="不给成绩的考试情况" name1st="emptyScoreStatuses" name2nd="setting.emptyScoreStatuses" items1st=subtract(examStatues,setting.emptyScoreStatuses) items2nd = setting.emptyScoreStatuses /]
	[@b.select2 style="width:150px;height:150px" label="允许补考的考试情况" name1st="allowExamStatuses" name2nd="setting.allowExamStatuses"   items1st=subtract(examStatues,setting.allowExamStatuses) items2nd = setting.allowExamStatuses/]
	[@b.radios label="计算总评考试情况" name="setting.calcGaExamStatus"  value=setting.calcGaExamStatus/]
	[@b.radios label="提交即发布" name="setting.submitIsPublish"  value=setting.submitIsPublish/]
	[@b.formfoot][@b.submit value="提交"/][/@]
[/@]
[@b.foot/]