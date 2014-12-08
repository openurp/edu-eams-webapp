[#ftl]
[@b.head/]
[@b.toolbar title="修改加分项"]bar.addBack();[/@]
  [#assign sa][#if bonusItem.id??]!update?id=${bonusItem.id!}[#else]!save[/#if][/#assign]
[@b.tabs]
  [@b.form action=sa theme="list"]
    [@b.textfield name="bonusItem.code" label="代码" value="${bonusItem.code!}" required="true" maxlength="22"/]
    [@b.textfield name="bonusItem.name" label="名称" value="${bonusItem.name!}" required="true" maxlength="222"/]
    [@b.textfield name="bonusItem.grade" label="年级" value="${bonusItem.grade!}" required="true"/]
    [@b.textfield name="bonusItem.maxScore" label="最大加分值" value="${bonusItem.maxScore!}" required="true"/]
    [@b.select name="bonusItem.stdLabel.id" label="学生标签" value="${(bonusItem.stdLabel.id)!}" required="true" 
               style="width:200px;" items=stdLabels empty="..."/]
    [@b.select name="bonusItem.beginTime.id" label="开始学期" value="${(bonusItem.beginTime.id)!}" required="true" 
               style="width:200px;" items=beginTimes option=r"${item.schoolYear}-${item.name}" empty="..."/]
    [@b.select name="bonusItem.endTime.id" label="结束学期" value="${(bonusItem.endTime.id)!}"
               style="width:200px;" items=endTimes option=r"${item.schoolYear}-${item.name}" empty="..."/]
    [@b.field label="排除加分课程" required="true"]
      <select id="courses" name="coursesId2nd" multiple="multiple" style="width:400px" data-placeholder="请选择不需要加分的课程...">
        [#macro optionList collection values]
          [#list collection as entity]
            [#assign found = false/]
            [#list values as value]
              [#if value.id == entity.id][#assign found = true /][#break][/#if]
            [/#list]
            <option value="${entity.id}" ${found?string('selected','')}>${entity.code} ${entity.name} ${entity.credits!}</option>
          [/#list]
        [/#macro]
        [@optionList courses bonusItem.courses /]
      </select>
      [@b.validity]
        jQuery('#courses', document.majorForm).assert(function() {
          return jQuery('#courses').val() !== null;
        }, '加分课程不能为空');
      [/@]
    [/@]
    [@b.formfoot]
      [@b.reset/]&nbsp;&nbsp;[@b.submit value="action.submit"/]
    [/@]
  [/@]
[/@]
<script src="${request.contextPath}/static/js/chosen/jquery-chosen.js"></script>
<link rel="stylesheet" href="${request.contextPath}/static/js/chosen/chosen.css">
<script>
  (function() {
      jQuery.validity.outputs.chosenCompatibleOutputMode = {
          start:function() {
              jQuery.validity.outputs.label.start();
          },
          end:function(results) { 
            jQuery.validity.outputs.label.end(results);
          },
          raise:function($obj, msg) {
            var jqChosenDiv = $obj.next('div.chosen-container');
            if(jqChosenDiv.length > 0) {
              jQuery.validity.outputs.label.raise(jqChosenDiv, msg);
            } else {
              jQuery.validity.outputs.label.raise($obj, msg);
            }
          },
          raiseAggregate:function($obj, msg){ 
            jQuery.validity.outputs.label.raiseAggregate($obj, msg);
          },
      }
  })();
  
  jQuery.validity.setup({ outputMode:'chosenCompatibleOutputMode' });
  jQuery('#courses').chosen();
</script>
[@b.foot/]