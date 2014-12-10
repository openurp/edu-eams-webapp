[#ftl]
[@b.head/]
[@b.toolbar title="修改专业"]bar.addBack();[/@]
[@b.tabs]
  [@b.tab label="基本信息"]
  [@b.form action="!update?id=${major.id}" theme="list"]
    [@b.textfield name="major.code" label="代码" value="${major.code!}" required="true" maxlength="20"/]
    [@b.textfield name="major.name" label="名称" value="${major.name!}" required="true" maxlength="20"/]
    [@b.textfield name="major.enName" label="英文名" value="${major.enName!}" maxlength="100"/]
    [@b.startend label="生效失效时间" 
      name="major.beginOn,major.endOn" required="false,false" 
      start=major.beginOn end=major.endOn format="date"/]
    [@b.textfield name="major.remark" label="备注" value="${major.remark!}" maxlength="30"/]
    [@b.select name="major.project.id" label="项目名称" value="${(major.project.id)!}" required="true" 
               style="width:200px;" items=projects option="id,name" empty="..."/]
 [#--
    [@b.select2 label="部门" name1st="journalsId1st" name2nd="journalsId2nd" 
      items1st=journals items2nd= major.journals
      option="id,name"/]  
     
    [@b.select2 label="培养层次" name1st="educationsId1st" name2nd="educationsId2nd" 
      items1st=educations items2nd= major.educations
      option="id,name"/]    
    [@b.select2 label="专业方向" name1st="directionsId1st" name2nd="directionsId2nd" 
      items1st=directions items2nd= major.directions
      option="id,name"/]   
  --]          
    [@b.select name="major.category.id" label="学科门类" value="${(major.category.id)!}" required="true" 
               style="width:200px;" items=categories option="id,name" empty="..."/]
    [@b.textfield name="major.duration" label="修读年限" value="${major.duration!}" required="true"/]
    [@b.textfield name="major.abbreviation" label="简称" value="${major.abbreviation!}" maxlength="100"/]    
    [@b.formfoot]
      [@b.reset/]&nbsp;&nbsp;[@b.submit value="action.submit"/]
    [/@]
   [/@]
 [/@]
  [#if major.id??]
	  [@b.tab label="建设过程"]
	  [@b.div href="major-journal!search?majorJournal.major.id=${major.id}"/]
	   [/@]
  [/#if]
  [/@]
[@b.foot/]