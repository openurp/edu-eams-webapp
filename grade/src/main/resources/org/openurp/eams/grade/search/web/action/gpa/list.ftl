[#ftl/]
[@b.head/]
[@b.toolbar title="学生绩点列表"/]
[@b.div id ="statStdGpaMessage" style="display:none"]由于统计的数据较多,请耐心等待...[/@]
[@b.form name="stdGpaListForm" action="!search" tareget="contentDiv"]
	[@b.grid items=stdGpas var="stdGpa" filterable="true"]
		[@b.gridbar]
			bar.addItem("查看学生绩点",action.info());
		    bar.addItem("重新统计","restat(document.stdGpaListForm)");
		    bar.addItem("${b.text("action.delete")}", action.remove());
		    bar.addItem("导出","exportList(document.stdGpaListForm)");
		    bar.addItem("统计学生绩点","statStdGpa(document.stdSearchForm)");
		    		    
	        function restat(form){
	            var stdGpaIds = bg.input.getCheckBoxValues("stdGpa.id");
	            if("" != stdGpaIds){
	                if(!confirm("是否要更新所选择学生的统计结果?"))return false;
	                bg.form.addInput(form,"stdGpaIds",stdGpaIds);
	                bg.form.submit(form,"${b.url('!reStatGp')}");
	            }else{
	                if(!confirm("是否要更新统计查询范围内学生的统计结果?"))return false;
	                //addHiddens(form,actionQueryStr);
	                bg.form.submit(form,"${b.url('!reStatGp')}"+jQuery(document.stdSearchForm).serialize());
	            }
	        } 
	       
	           function exportList(form){
	           var stdGpaIds = bg.input.getCheckBoxValues("stdGpa.id");
	    	var form=document.stdGpaListForm;
	       	form.target="_self";
	       	if(confirm("确认导出查询出来的结果？")){
	          	bg.form.addInput(form,"keys","std.code,std.name,std.grade,std.department.name,std.major.name,std.adminclass.name,gpa,updatedAt");
	        	bg.form.addInput(form,"titles","学号,姓名,年级,院系所,专业,班级,平均绩点,统计时间");
	        	bg.form.addInput(form,"fileName","学生绩点信息");
	        	 bg.form.addInput(form,"stdGpaIds",stdGpaIds);
	          	bg.form.submit(form,"${b.url('gpa!export')}");
	       }
      		form.target="contentDiv";
          	form.action="${b.url('gpa!search')}";
	    }
	        
	     [#--
	        function exportList(form){
	        	bg.form.addInput(form,"keys","std.code,std.name,std.grade,std.department.name,std.major.name,std.adminclass.name,gpa,updatedAt");
	        	bg.form.addInput(form,"titles","学号,姓名,年级,院系所,专业,班级,平均绩点,统计时间");
	        	bg.form.addInput(form,"fileName","学生绩点信息");
	        	bg.form.submit(form,"${b.url('!export')}",null,null,false);
	        }
	           --] 
	        function statStdGpa(form){
	        	if(!confirm("你确定统计所有未统计学生的绩点?"))return false;
	        	bg.form.submit(form,"${b.url('!statStdGpa')}");
	        	jQuery('#statStdGpaMessage').css("display","");
	        	form.action="!search";
	        }
		[/@]
		[@b.row]
			[@b.boxcol/]
			[@b.col title="attr.stdNo" property="std.code" width="10%"/]
			[@b.col title="attr.personName" property="std.name" width="15%"/]
			[@b.col title="entity.gender" property="std.gender.name" width="10%"/]
			[@b.col title="std.grade" property="std.grade" width="15%"/]
			[@b.col title="部门" property="std.department.name" width="20%"/]
			[@b.col title="entity.major" property="std.major.name" width="15%"/]
			[@b.col title="平均绩点" property="gpa" width="15%"/]
		[/@]
	[/@]
[/@]
[@b.foot/]