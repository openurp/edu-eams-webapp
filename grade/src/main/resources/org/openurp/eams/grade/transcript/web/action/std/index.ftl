[#ftl/]
[#include "../gradeMacros.ftl"/]
[#include "/template/print.ftl"/]
[@b.toolbar title='${b.text("common.personGradeTablePrint")}']
	  bar.addPrint("${b.text('action.print')}");
   	  bar.addClose();
[/@]
[#include "visualPrint.ftl"/]
[@b.foot/]