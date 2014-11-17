[#ftl]
[@b.head/]
<script language="JavaScript" type="text/JavaScript" src="${base}/static/scripts/course/grade/gradeSeg.js"></script>
<script language="JavaScript" type="text/JavaScript" src="${base}/static/scripts/grade/course/grade.js"></script>
<script language="JavaScript" type="text/javascript" src="${base}/static/scripts/ckeditor/ckeditor.js"></script>
[#assign textAreaId = "content"/]
[@b.toolbar title="编辑打印内容"]
   	bar.addBackOrClose();
[/@]
	<form method="post" action="" name="actionForm">
		<table align="center">
			<input type="hidden" name="std.ids" value="${Parameters['std.ids']?default('')}"/>
			<input type="hidden" name="semester.id" value="${Parameters['semester.id']?default('')}"/>
			<tr>
				<td style="font-weight:bold">综合分析内容:</td>
			</tr>
			<tr>
				<td><textarea id="${textAreaId}" name="contentValue" rows="10" cols="50">${(Parameters["contentValue"]?html)?default("")}</textarea></td>
			</tr>
			<tr>
				<td align="center"><button onclick="finish()">提交</button></td>
			</tr>
		</table>
	</form>
	<script language="JavaScript">
		var form = document.actionForm;
	    function finish() {
	    	form.action = "${b.url('term-report!reportContent')}";
		    for (var i = 0; i < seg.length; i++) {
				var segAttr = "segStat.scoreSegments[" + i + "]";
		        bg.form.addInput(form, segAttr + ".min", seg[i].min, "hidden");
		        bg.form.addInput(form, segAttr + ".max", seg[i].max, "hidden");
		    }
		    bg.form.addInput(form,"scoreSegmentsLength",seg.length);
	    	form.submit();
	    }
	    
	    CKEDITOR.replace( "${textAreaId}",
	    	{
	    		toolbar : 
	    		[
	    			['Source','-','NewPage','Preview','-','Templates'],  
			        ['Cut','Copy','Paste','PasteText','PasteFromWord','-','Print', 'SpellChecker', 'Scayt'],  
			        ['Undo','Redo','-','Find','Replace','-','SelectAll','RemoveFormat'],  
			        ['BidiLtr', 'BidiRtl'],  
			       	'/',  
			       	['Bold','Italic','Underline','Strike','-','Subscript','Superscript'],  
			       	['NumberedList','BulletedList','-','Outdent','Indent','Blockquote','CreateDiv'],  
			       	['JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock'],  
			       	['Link','Unlink','Anchor'],  
			       	['Image','Table','HorizontalRule','Smiley','SpecialChar','PageBreak'],  
			       	'/',  
			       	['Styles','Format','Font','FontSize'],  
			       	['TextColor','BGColor'],  
			      	['Maximize', 'ShowBlocks','-','About'] 
	    		]
	    	});  
	</script>
[@b.foot/]
