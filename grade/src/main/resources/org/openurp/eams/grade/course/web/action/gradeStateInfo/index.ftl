[#ftl]
[#include "/template/macros.ftl"/]
[#assign gradeStatus={'0':'新添加','1':'已提交','2':'已发布'}/]
    <table id="myBar"></table>
    <table width="100%" class="infoTable">
        <tr>
            <td class="title">${b.text("attr.taskNo")}:</td>
            <td class="content">${lesson.no}</td>
            <td class="title">${b.text("attr.courseNo")}:</td>
            <td class="content">${lesson.course.code}</td>
            <td class="title">${b.text("attr.courseName")}:</td>
			<td class="content">[@i18nName lesson.course/]</td>
        </tr>
        <tr>
            <td class="title">授课教师:</td>
            <td class="content">[@getTeacherNames lesson.teachers/]</td>
            <td class="title">考核方式:</td>
            <td class="content">${(lesson.examMode.name)!}</td>
            <td class="title">上次录入:</td>
			<td class="content">
				[#if gradeState?exists]
					${gradeState.inputedAt!!}
				[/#if]
			</td>
        </tr>
        <tr>
            <td class="title">记录方式:</td>
            <td class="content">
				[#if gradeState?exists]
	            	[@i18nName gradeState.scoreMarkStyle/]
				[/#if]
            	</td>
            <td class="title">精确度:</td>
            <td class="content">
				[#if gradeState?exists]
				${gradeState.precision}
				[/#if]
            	位小数</td>
            <td class="title">状态:</td>
			<td class="content">
				[#if gradeState?exists]
					${gradeStatus[gradeState.status?string]!}
				[/#if]
				</td>
        </tr>
    </table>
    <table class="gridtable">
        <tr align="center" style="backGround-color:#C7DBFF">
        	<td width="20%">成绩类型</td>
        	<td width="15%">记录方式</td>
        	<td width="10%">百分比</td>
        	<td width="20%">精确度(保留小数位)</td>
        	<td width="10%">状态</td>
        	<td width="25%">上次录入</td>
        </tr>
       [#if gradeState?exists]
	        [#list gradeState.states?sort_by(["gradeType","code"])  as state]
	        <tr align="center">
	        	<td>${state.gradeType.name}</td>
	        	<td>[@i18nName state.scoreMarkStyle/]</td>
	        	<td>[#if state.percent??]${(state.percent)?string.percent}[/#if]</td>
	        	<td>${state.precision}位</td>
	        	<td>${gradeStatus[state.status?string]!}</td>
	        	<td>${state.inputedAt!!}</td>
	        </tr>
	        [/#list]
        [/#if]
    </table>
<div id="holder"></div>
<script>
 [#assign total=0/]
 [#assign seg90=0/]
 [#assign seg80=0/]
 [#assign seg70=0/]
 [#assign seg60=0/]
 [#assign seg00=0/]
 [#list grades as grade]
 [#if grade.score??]
 	[#assign total=total+1/]
 	[#if grade.score>90][#assign seg90=seg90+1/]
 	[#elseif grade.score>80][#assign seg80=seg80+1/]
 	[#elseif grade.score>70][#assign seg70=seg70+1/]
 	[#elseif grade.score>60][#assign seg60=seg60+1/]
 	[#else][#assign seg00=seg00+1/][/#if]
 [/#if]
 [/#list]
 [#if total>0]
     var r = Raphael("holder"),
     pie = r.piechart(120, 150, 100, [${seg90/total*100}, ${seg80/total*100}, ${seg70/total*100},  ${seg60/total*100},  ${seg00/total*100}],
 { legend: ["%%.%-优秀(90-100)  ", "良好(80-89)","中等(70-79)","及格(60-69)","不及格(0-59)"], legendpos: "east"});
    r.text(120, 20, "最终成绩分布图").attr({ font: "15px sans-serif" });
    /*pie.hover(function () {
        this.sector.stop();
        this.sector.scale(1.1, 1.1, this.cx, this.cy);
        if (this.label) {
            this.label[0].stop();
            this.label[0].attr({ r: 7.5 });
            this.label[1].attr({ "font-weight": 800 });
        }
    }, function () {
        this.sector.animate({ transform: 's1 1 ' + this.cx + ' ' + this.cy }, 500, "bounce");
        if (this.label) {
            this.label[0].animate({ r: 5 }, 500, "bounce");
            this.label[1].attr({ "font-weight": 400 });
        }
    });*/
[/#if]
</script>