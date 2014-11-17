[#ftl/]
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
			<td class="content">[@i18nName lesson.course/][#if lesson.subCourse??]<sup style="color:#29429E;">${lesson.subCourse.name}</sup>[/#if]</td>
        </tr>
        <tr>
            <td class="title">授课教师:</td>
            <td class="content">[@getTeacherNames lesson.teachers/]</td>
            <td class="title">考核方式:</td>
            <td class="content">${(lesson.examMode.name)!}</td>
            <td class="title">上次录入:</td>
			<td class="content">
				[#if gradeState?exists]
					${gradeState.operator!}&nbsp;${gradeState.inputedAt?string('yyyy-MM-dd HH:mm')!}
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
        	<td width="15%">保留小数位</td>
        	<td width="10%">状态</td>
        	<td width="30%">上次录入</td>
        </tr>
       [#if gradeState?exists]
	        [#list gradeState.states?sort_by(["gradeType","code"])  as state]
	        <tr align="center">
	        	<td>${state.gradeType.name}</td>
	        	<td>[@i18nName state.scoreMarkStyle/]</td>
	        	<td>[#if state.percent??]${(state.percent)?string.percent}[/#if]</td>
	        	<td>${state.precision}位</td>
	        	<td>${gradeStatus[state.status?string]!}</td>
	        	<td>${state.operator!}&nbsp;${state.inputedAt?string('yyyy-MM-dd HH:mm')!}</td>
	        </tr>
	        [/#list]
        [/#if]
    </table>
<div id="holder" style="width:850px"></div>
<script>
 [#assign gatotal=0/][#assign seg100=0/]
 [#assign seg95=0/][#assign seg90=0/]
 [#assign seg85=0/][#assign seg80=0/]
 [#assign seg75=0/][#assign seg70=0/]
 [#assign seg65=0/][#assign seg60=0/]
 [#assign seg55=0/][#assign seg50=0/]
 [#assign seg45=0/][#assign seg40=0/]
 [#assign seg35=0/][#assign seg30=0/]
 [#assign seg00=0/]
 [#list grades as grade]
 [#assign score=-2/]
 [#list grade.examGrades as eg][#if eg.gradeType.id=7][#assign score=eg.score!0/][/#if][/#list]
   [#if score>-2]
 	[#assign gatotal=gatotal+1/]
 	[#if score<30][#assign seg00=seg00+1/]
 	[#elseif score<35][#assign seg30=seg30+1/]
 	[#elseif score<40][#assign seg35=seg35+1/]
 	[#elseif score<45][#assign seg40=seg40+1/]
 	[#elseif score<50][#assign seg45=seg45+1/]
 	[#elseif score<55][#assign seg50=seg50+1/]
 	[#elseif score<60][#assign seg55=seg55+1/]
 	[#elseif score<65][#assign seg60=seg60+1/]
 	[#elseif score<70][#assign seg65=seg65+1/]
 	[#elseif score<75][#assign seg70=seg70+1/]
 	[#elseif score<80][#assign seg75=seg75+1/]
 	[#elseif score<85][#assign seg80=seg80+1/]
 	[#elseif score<90][#assign seg85=seg85+1/]
 	[#elseif score<95][#assign seg90=seg90+1/]
 	[#elseif score<100][#assign seg95=seg95+1/]
 	[#else][#assign seg100=seg100+1/][/#if]
   [/#if]
 [/#list]
 [#--期末成绩--]
[#assign etotal=0/][#assign eseg100=0/]
 [#assign eseg95=0/][#assign eseg90=0/]
 [#assign eseg85=0/][#assign eseg80=0/]
 [#assign eseg75=0/][#assign eseg70=0/]
 [#assign eseg65=0/][#assign eseg60=0/]
 [#assign eseg55=0/][#assign eseg50=0/]
 [#assign eseg45=0/][#assign eseg40=0/]
 [#assign eseg35=0/][#assign eseg30=0/]
 [#assign eseg00=0/]
 [#list grades as grade]
 [#assign score=-2/]
 [#list grade.examGrades as eg][#if eg.gradeType.id=2][#assign score=eg.score!0/][/#if][/#list]
   [#if score>-2]
 	[#assign etotal=etotal+1/]
 	[#if score<30][#assign eseg00=eseg00+1/]
 	[#elseif score<35][#assign eseg30=eseg30+1/]
 	[#elseif score<40][#assign eseg35=eseg35+1/]
 	[#elseif score<45][#assign eseg40=eseg40+1/]
 	[#elseif score<50][#assign eseg45=eseg45+1/]
 	[#elseif score<55][#assign eseg50=eseg50+1/]
 	[#elseif score<60][#assign eseg55=eseg55+1/]
 	[#elseif score<65][#assign eseg60=eseg60+1/]
 	[#elseif score<70][#assign eseg65=eseg65+1/]
 	[#elseif score<75][#assign eseg70=eseg70+1/]
 	[#elseif score<80][#assign eseg75=eseg75+1/]
 	[#elseif score<85][#assign eseg80=eseg80+1/]
 	[#elseif score<90][#assign eseg85=eseg85+1/]
 	[#elseif score<95][#assign eseg90=eseg90+1/]
 	[#elseif score<100][#assign eseg95=eseg95+1/]
 	[#else][#assign eseg100=eseg100+1/][/#if]
   [/#if]
 [/#list]
 [#if gatotal>0||etotal>0]
     var  txtattr = { font: "15px sans-serif","font-weight": 800 };
     [#if gatotal>0]
require(["raphael", "g_line", "g_pie"], function(Raphael) {
     var r = Raphael("holder"),
     pie = r.piechart(120, 150, 100, [${(seg100)/gatotal*100},${(seg90+seg95)/gatotal*100}, ${(seg80+seg85)/gatotal*100}, ${(seg70+seg75)/gatotal*100},  ${(seg60+seg65)/gatotal*100},  ${(seg50+seg55)/gatotal*100}, ${(seg40+seg45)/gatotal*100}, ${(seg30+seg35)/gatotal*100},${(seg00)/gatotal*100}],
 { legend: ["%%.%-优异(100) ${seg100}人","%%.%-优秀(90-99) ${seg90+seg95}人", "良好(80-89) ${seg80+seg85}人","中等(70-79) ${seg70+seg75}人","及格(60-69) ${seg60+seg65}人","不及格(50-59) ${seg50+seg55}人","不及格(40-49) ${seg40+seg45}人","不及格(30-39) ${seg30+seg35}人","不及格(<30) ${seg00}人"], legendpos: "east"});
    r.text(120, 20, "总评成绩百分比分布图").attr(txtattr);
    r.text(600, 20, "总评[#if etotal>0](蓝)和期末[/#if]成绩分布(5分间隔)").attr(txtattr);
    pie.hover(function () {
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
    });
    [#else]
    var r = Raphael("holder");
    [/#if]
    [#if gatotal>0 && etotal>0 ]
    var line = r.linechart(430, 35, 400, 220, [[0,30,35,40,45,50,55,60,65,70,75,80,85,90,95,100],[0,30,35,40,45,50,55,60,65,70,75,80,85,90,95,100]], [[${seg00},${seg30},${seg35},${seg40}, ${seg45}, ${seg50}, ${seg55}, ${seg60}, ${seg65}, ${seg70},${seg75},${seg80},${seg85},${seg90},${seg95},${seg100}],[${eseg00},${eseg30},${eseg35},${eseg40}, ${eseg45}, ${eseg50}, ${eseg55}, ${eseg60}, ${eseg65}, ${eseg70},${eseg75},${eseg80},${eseg85},${eseg90},${eseg95},${eseg100}]],
     { nostroke: false, axis: "0 0 1 1", symbol: "circle", smooth: true,legend:["总评成绩","期末成绩"],legendpos: "east" });
    [#else]
    var line = r.linechart(430, 35, 400, 220, [[0,30,35,40,45,50,55,60,65,70,75,80,85,90,95,100]], [[${seg00},${seg30},${seg35},${seg40}, ${seg45}, ${seg50}, ${seg55}, ${seg60}, ${seg65}, ${seg70},${seg75},${seg80},${seg85},${seg90},${seg95},${seg100}]],
     { nostroke: false, axis: "0 0 1 1", symbol: "circle", smooth: true });
    [/#if]
     line.hoverColumn(function () {
        this.tags = r.set();
        for (var i = 0, ii = this.y.length; i < ii; i++) {
            this.tags.push(r.tag(this.x, this.y[i],this.values[i]+"人", 160, 10).insertBefore(this).attr([{ fill: "#fff" }, { fill: this.symbols[i].attr("fill") }]));
        }
    }, function () {
        this.tags && this.tags.remove();
    });
});
[/#if]
</script>