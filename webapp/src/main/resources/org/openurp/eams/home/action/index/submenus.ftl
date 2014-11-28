[#ftl]
[#assign base = request.getContextPath()/]
[@b.head]
<link href="${base}/static/themes/default/homePage.css?10" rel="stylesheet" type="text/css" />
<script src="${base}/static/scripts/js/view_menus.js" type="text/javascript"></script>
[/@]
[#macro i18nNameTitle(entity)][#if locale.language?index_of("en")!=-1][#if entity.engTitle!?trim==""]${entity.title!}[#else]${entity.engTitle!}[/#if][#else][#if entity.title!?trim!=""]${entity.title!}[#else]${entity.engTitle!}[/#if][/#if][/#macro]

[#assign displayed={} /]
[#macro displayMenu menu]
[#if !(displayed[menu.id?string]??)][#assign displayed=displayed+{menu.id?string:menu}/][#else][#return/][/#if]
<li>
[#if menu.entry??]
[@b.a href="${(menu.entry)!}" target="main"][@i18nNameTitle menu/][/@]
[#else]
	[@i18nNameTitle menu/]
	<ul style="padding-left: 20px;">
	[#list menu.children?sort_by("code") as child]
		[#if submenus?seq_contains(child)][@displayMenu child/][/#if]
	[/#list]
	</ul>
[/#if]
</li>
[/#macro]

[@b.div id="MLeft"]
<div class="list_box_1">
[#if submenus?size>0]
	<ul>
	[#list submenus! as module]
		<li class="li_1">
			[@b.a href="!childmenus?menu.id=${(module.id)!}" myTitle="${(module.title)!}" parentId="${(module.parent.id)!}" parentName="${(module.parent.title)!}" target="main" class="subMenu"]
				[#if (module_index + 1) % 2 == 0 && (module_index + 1) % 4 !=0]
				<div class="ico3_2">&nbsp;</div>
				[#elseif (module_index + 1) % 3 == 0]
				<div class="ico3_3">&nbsp;</div>
				[#elseif (module_index + 1) % 4 == 0]
				<div class="ico3_4">&nbsp;</div>
				[#elseif (module_index + 1) % 5 == 0]
				<div class="ico3_5">&nbsp;</div>
				[#elseif (module_index + 1) % 6 == 0]
				<div class="ico3_6">&nbsp;</div>
				[#elseif (module_index + 1) % 7 == 0]
				<div class="ico3_7">&nbsp;</div>
				[#else]
				<div class="ico3_1">&nbsp;</div>
				[/#if]
				<h3>[@i18nNameTitle module/]</h3>
			[/@]
		</li>
		[#if (module_index + 1) % 4 ==0]
			<li class="li_2"></li>
		[/#if]
	[/#list]
	</ul>
[#else]
without any menu!
[/#if]
</div>
[/@]
[@b.div id="MRight"]
	<div class="BlankLine2"></div>
	<div class="list_box_2">
		<h3>新闻公告</h3>
		<ul>
			<li class="li_1">
			1.使用IE8浏览器访问系统不流畅的用户可以点击下面链接下载相应浏览器进行访问。<br/>
			<a href="http://www.eoffice.ecnu.edu.cn/newjw/Firefox17.0.1.rar">火狐浏览器</a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			<a href="http://www.eoffice.ecnu.edu.cn/newjw/chrome_installer_Stable12.zip">谷歌浏览器</a>
			</li>
		</ul>
		[#if isStd??]
		<ul>
			<li class="li_1">
			2.2013届本科毕业生“最喜爱的教师”网络评选<br/>
			&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;>>><a href="http://202.120.85.110:8100/index.asp" target="_blank">进入投票</a><<<
			</li>
		</ul>
		[/#if]
	</div>
[/@]
<script type="text/javaScript">
	$(".subMenu").click(function(e){
		$("#position_bar").find("span").remove();
		if ($(this).attr("parentId") != ""){
			var firstSpan = $("<span class='firstMenu'>&nbsp;>");
			var firstA = $("<a href='${base}/home!submenus.action?menu.id="+$(this).attr("parentId")+"'>"+$(this).attr("parentName")+"</a>");
			firstA.click(function(e){
				$("#position_bar").find("span:gt(0)").remove();
				return bg.Go(this,'main',true);
			});
			firstSpan.append("&nbsp;").append(firstA);
			$("#position_bar").append(firstSpan);
		}
		var secondSpan = $("<span class='fontMenu'>&nbsp;>&nbsp;"+$(this).attr("myTitle")+"</span>");
		$("#position_bar").append(secondSpan);
	});
</script>
[@b.foot/]
