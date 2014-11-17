<#include "/template/head.ftl"/>
<body>

	<table id="bar"></table>
	<table class="infoTable" width="100%" border="1">
        <tr>
          	<td class="title" width="20%">学号:</td>
		  	<td colspan="2" width="30%">${std.code?if_exists}</td>
		    <td class="title" width="20%">姓名:</td>
		  	<td colspan="2" width="30%"><@i18nName std/></td>
        </tr>
	 	<#assign kcxz=''/>
	 	<#list grades?sort_by("courseType") as grade>
	 	<#if !(kcxz==grade.courseType.name)>
	 	<#assign temp=0/>
	 	<#assign tempStr=''/>
	 	<#assign kcxz=grade.courseType.name/>
	 		<#list grades?sort_by("courseType") as grade2>
	 			<#if kcxz==grade2.courseType.name>
	 			<#assign temp=temp+grade2.course.credits/>
	 			</#if>
	 		</#list>
	 	<tr>
	 		<td class="title">课程性质:</td>
	 		<td colspan="2">${(grade.courseType.name)?if_exists}</td>
	 		<td class="title">学分小计</td>
	 		<td colspan="2">${temp}</td>
	 	</tr>
	 	</#if>
	 	 <tr>
	 	   	<td class="title">课程代码:</td>
	 		<td width="15%">${(grade.course.code)?if_exists}</td>
	 		<td class="title" width="15%">课程名称:</td>
	 		<td><@i18nName grade.course/></td>
	 		<td class="title" width="15%">课程学分:</td>
	 		<td width="15%">${(grade.course.credits)?if_exists}</td>
	 	 </tr>
	 	</#list>
	</table>
	<script>
		var bar = new ToolBar("bar", "详细查看", null, true, true);
		bar.setMessage('<@getMessage/>');
		bar.addBack();
	</script>
</body>
<#include "/template/foot.ftl"/>
