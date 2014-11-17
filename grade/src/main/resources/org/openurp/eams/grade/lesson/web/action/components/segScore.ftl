[#ftl]
[#macro addSeqToForm formName]
	//默认分段统计标准
if(typeof seg == "undefined"){
	var seg = new Array();
	seg[0]=new Object();
	seg[0].min=0;seg[0].max=59
	seg[1]=new Object();
	seg[1].min=60;seg[1].max=69;
	seg[2]=new Object();
	seg[2].min=70;seg[2].max=79;
	seg[3]=new Object();
	seg[3].min=80;seg[3].max=89;
	seg[4]=new Object();
	seg[4].min=90;seg[4].max=100;
}
for(var i=0;i<seg.length;i++){
	var segAttr="segStat.scoreSegments["+i+"]";
	bg.form.addInput(${formName},segAttr+".min",seg[i].min);
	bg.form.addInput(${formName},segAttr+".max",seg[i].max);
}
bg.form.addInput(${formName},"scoreSegmentsLength",seg.length);
[/#macro]

[#macro addSeqToParams formName]
	paramValue="&scoreSegmentsLength="+seg.length;
	for(var i=0;i<seg.length;i++){
		segAttr="&segStat.scoreSegments["+i+"]";
		paramValue+=(segAttr + ".min=" + seg[i].min);
		paramValue+=(segAttr + ".max=" + seg[i].max);
	}
	bg.form.addParamsInput(${formName},paramValue);
[/#macro]
