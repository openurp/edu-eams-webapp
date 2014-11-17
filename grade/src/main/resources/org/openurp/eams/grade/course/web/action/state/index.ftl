[#ftl]
[@b.head/]
	[@b.toolbar title="成绩状态管理"]
		   bar.addHelp("${b.text("action.help")}");
		   bar.addClose();
		
	       function MouseOver(e){
				var o=bg.event.getTarget(e);
				while (o&&o.tagName.toLowerCase()!="td"){o=o.parentNode;}
				if(o)o.className="toolbar-item-transfer";
			}
			function MouseOut(e){
				var o=bg.event.getTarget(e);
				while (o&&o.tagName.toLowerCase()!="td"){o=o.parentNode;}
				if(o)o.className="toolbar-item";
			}
			
			function selectFrame(form){
				bg.form.addInput(form,"gradeState.lesson.semester.schoolYear",${(semester.schoolYear)!});
				bg.form.addInput(form,"gradeState.lesson.semester.name",${(semester.name)!});
				bg.form.submit(form,"${b.url('state!statusStat')}");
			}
	[/@]
    <table class="indexpanel">
        <tr valign="top">
            <td width="50%" class="index_view">
            	<table width="100%" id="menuTable" style="font-size:10pt">
					<tr class="infoTitle" align="left" valign="bottom">
						<td><img src="static/images/action/info.gif" align="top"/><b>统计选项</b></td>
					</tr>
					<tr>
						<td style="font-size:0px"><img src="static/images/action/keyline.gif" height="2" width="100%" align="top"></td>
					</tr>
					<tr height="25">
						<td id="gradeStateOption" onclick="selectFrame(document.actionForm)" onmouseover="MouseOver(event)" onmouseout="MouseOut(event)">
							&nbsp;&nbsp;<image src="static/images/action/list.gif" align="absmiddle"/>&nbsp;成绩状态统计
						</td>
					</tr>
				</table>
            </td>
            <td class="index_content">
                [@b.div id="contentDiv" href="!statusStat"/]
            </td>
        </tr>
    </table>
[@b.foot/]	
