[#ftl/]
[@b.head/]
[#include "/com/ekingstar/eams/teach/grade/page/report/common/gradeMacros.ftl"/]
[@b.toolbar title='${b.text("common.personGradeTablePrint")}']
  	bar.addPrint("${b.text('action.print')}");
	bar.addClose();
[/@]
[#include "/template/print.ftl"/]
[#include "template/" + (setting.template)?default('default') + ".ftl"/]
<script defer="defer" language="Javascript">
     setLeftMargin(5.0);
     setTopMargin(10.0);
     setRightMargin(5.0);
     setBottomMargin(10.0);
</script>
[@b.foot/]