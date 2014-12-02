var normalExamStatusId="1";// "${NORMAL}"

    
function GradeTable() {
    /*this.valueStyle = {
                       "positiveInteger":{"validator":/^\d+$/, "caption":"请输入0或正整数", "shortCaption":"0或正整数"},
                       "unsignedReal":{"validator":/^\d*\.?\d{1}$/, "caption":"请输入0或正数，且保留一位小数", "shortCaption":"0、正数或保留一位小数的正数"}
                      };
                      */
    this.valueStyle = [
                      {"validator":/^\d+$/, "caption":"请输入0或正整数", "shortCaption":"0或正整数"},
                      {"validator":/^\d*\.?\d{1}$/, "caption":"请输入0或正数，且保留一位小数", "shortCaption":"0、正数或保留一位小数的正数"}
                      ];
    this.isExchanged = false;
    this.gradeState = new Array();
    this.gradeMap = new Object();
    this.gradeArray = new Array();
    this.add = addCourseGrade;
    this.precision = 0;
    this.onReturn = null;
    this.change = changeCourseGrade;
    this.changeTabIndex=changeTabIndex;
    this.tabByStd=null;
    this.hasEmpty = hasEmpty;
    this.onReturn=null;
    this.displayExamStatus = displayExamStatus;
    this.changePrecision = changePrecision;
    this.calcGa = calcGaScore;
    this.hasGa=false;
    this.hasGradeSelect = false;
    this.isSecond = false;
    this.setHasGradeSelect=setHasGradeSelect;
    this.setIsSecond=setIsSecond;
}

function setHasGradeSelect(hasGradeSelect) {
    this.hasGradeSelect = hasGradeSelect;
}

function setIsSecond(isSecond) {
    this.isSecond = isSecond;
}

function hasEmpty() {
    for (var i = 0; i < this.gradeState.length; i++) {
        for (var j = 0; j < this.gradeArray.length; j++) {
            grade = this.gradeArray[j];
            inputs = jQuery("input[name='"+this.gradeState[i].name + '_' + grade.stdId+"']");
            selects = jQuery("select[name='"+this.gradeState[i].name + '_' + grade.stdId+"']");
            examStatuses=jQuery("select[name='examStatus_"+this.gradeState[i].name + '_' + grade.stdId+"']");
            if (null != inputs && inputs.length>0) {
            	examStatusId=1;
            	if(examStatuses.length>0) examStatusId=examStatuses.val();
                if ((inputs.val() == "" || inputs.val() == null) &&  examStatusId==1 && inputs.is(':visible')) {
                    return true;
                }
            }else if(null != selects && selects.length>0){
            	examStatusId=1;
            	if(examStatuses.length>0) examStatusId=examStatuses.val();
                if ((selects.val() == "" || selects.val() == null) &&  examStatusId==1 && selects.is(':visible')) {
                    return true;
                }
            }
        }
    }
    return false;
}

function displayExamStatus() {
    for (var i = 0; i < this.gradeState.length; i++) {
        for (var j = 0; j < this.gradeArray.length; j++) {
            grade = this.gradeArray[j];
            inputs = document.getElementsByName(this.gradeState[i].name + "_" + grade.stdId);
            var statusElem = document.getElementById("examStatus_" + this.gradeState[i].name + "_" + (j + 1));
            if (null != inputs && inputs.length>0 && null != statusElem && inputs[0].value == "") {
                inputs[0].style.display = "none";
                statusElem.style.display = "block";
                statusElem.disabled=false;
                gradeTable.isExchanged = true;
            }
        }
    }
    if (gradeTable.isExchanged) {
        document.getElementById("bnJustSave").style.display = "none";
        document.getElementById("bnSubmit").value = "修改考试情况后提交";
        alert("请修改学生成绩的考试情况。");
    }
}

function CourseGrade(index, stdId, courseTakeTypeId, gradeTable) {
    this.index = index;
    this.stdId = stdId;
    this.courseTakeTypeId = courseTakeTypeId;
    this.gradeTable = gradeTable;
    this.examGrades = new Object();
    this.change = changeScore;
}

function addCourseGrade(index, stdId, courseTakeTypeId, gradeTable) {
    var grade = new CourseGrade(index, stdId, courseTakeTypeId, gradeTable);
    this.gradeMap[stdId] = grade;
    this.gradeArray[index] = grade;
    return grade;
}

function calcGaScore(index) {
    if(!this.hasGa)return;
    var gradeContents = "grade.courseTakeType.id=" + document.getElementById("courseTakeType_" + index).value;
//    gradeContents += "&grade.project.id=" + document.getElementById("courseTake_project_" + index).value;
    var myExamStatus=normalExamStatusId;
    for(var i=0 ;i<this.gradeState.length;i++){
        var state=this.gradeState[i];
        if(!state.inputable) continue;
        var statePrefix= state.id + "_";
        var examScore=(null == document.getElementById(statePrefix + index) || "" == document.getElementById(statePrefix + index).value ? "" : document.getElementById(statePrefix + index).value);
        var examScorePercent=(null == document.getElementById("personPercent_"+statePrefix + index) || "" == document.getElementById("personPercent_"+statePrefix + index).value ? "" : document.getElementById("personPercent_"+statePrefix + index).value);
        var examStatus=normalExamStatusId;
        if(null!=document.getElementById("examStatus_" + statePrefix + index) && !document.getElementById("examStatus_" + statePrefix + index).disabled){
           examStatus = document.getElementById("examStatus_"+ statePrefix + index).value;
        }
        //没有成绩也能进行页面总评计算
        //if(isEmpty(examScore)){
        //	return ;
       // }
        if(examScore!=""||examStatus!=normalExamStatusId){
            gradeContents += "&examGrade"+ state.id + ".gradeType.id="+ state.id +"&examGrade"+state.id+".score=" + examScore;
            gradeContents += "&examGrade"+ state.id +".examStatus.id="+examStatus;
        }
        if(!isEmpty(examScorePercent)){
        	gradeContents += "&examGrade" + state.id + ".percent="+examScorePercent;
        }
    }
    var gaTd=document.getElementById("GA_" + index);
//    gradeContents += "&state.precision=" + this.precision;
//    gradeCalcualtor.calcGa(this.gradeStateId,gradeContents, function(data){
//        fillGaScore(gaTd,data);
//    });
    $.post(beangle.contextPath + '/eams/grade/teacher/end-ga/calcGa', 
		{'gradeStateId':this.gradeStateId, 'gradeContent': gradeContents}, 
		function (data){
			console.log(data);
			fillGaScore(gaTd, data);
		}
	);
}

function fillGaScore(gaTd,data) {
    var results = data.split(",");
    if (null == data || null == results || null == results[0]) {
        jQuery(gaTd).html("");
    } else if (!Boolean(parseInt(results[1],10))) {
    	jQuery(gaTd).html("<font color=\"red\">" + results[0] + "</font>");
    } else {
    	jQuery(gaTd).html(results[0]);
    }
}

// 检查分数的合法性
function alterErrorScore(input, msg) {
    alert(msg);
    input.value = "";
    return true;
}

function checkScore(index, input) {
    var score = input.value;
    var error = false;
    var scoreInt = parseInt(score,10);
	var maxScore=100;
   	var minScore=0;
    if(input.name.indexOf("MAKEUP")>-1) maxScore=60;
    if (scoreInt > maxScore) error = alterErrorScore(input, "输入成绩不能超过"+ maxScore +"分");
    if (scoreInt < minScore) error = alterErrorScore(input, "输入成绩不能小于"+ minScore +"分");
    if (!error) {
        if (gradeTable.isSecond) {
            gradeTable.change(input);
        }
        gradeTable.calcGa(index);
    }
}

function changeTabIndex(form,tabByStd){
	this.onReturn = new OnReturn(form);
    if (this.tabByStd != tabByStd){
        this.tabByStd = tabByStd;
    } else {
        return;
    }
    var input = null;
    var inputIndex = 0;
    if (!this.tabByStd) {
        for(var i = 0; i < this.gradeState.length; i++) {
            for (var j = 0;j < this.gradeArray.length; j++) {
                grade = this.gradeArray[j];
                input = document.getElementById(this.gradeState[i].name + "_" + (j + 1));
                if (null != input) {
                    input.tabIndex = j + i * this.gradeArray.length + 1;
                    this.onReturn.elemts[input.tabIndex] = input.name;
                }
            }
        }
    } else {
        for(var i = 0;i < this.gradeArray.length; i++) {
            grade = this.gradeArray[i];
            for (var j = 0;j < this.gradeState.length; j++) {
                input = document.getElementById(this.gradeState[j].name + "_" + (i + 1));
                if (null != input) {
                    input.tabIndex = j + i * this.gradeState.length + 1;
                    this.onReturn.elemts[input.tabIndex] = input.name;
                }
            }
        }
    }
}

function changePrecision(precision){
    this.precision = precision == 0 ? "positiveInteger" : "unsignedReal";
    if (0 == precision) {
        for(var i = 0; i < this.gradeArray.length; i++) {
            var grade = this.gradeArray[i];
            for (var j = 0; j < this.gradeState.length; j++) {
                input = document.getElementById(this.gradeState[j].name + "_" + (i + 1));
                if (isNotEmpty(input) && isNotEmpty(input.value)) {
                    input.value = Math.floor(parseInt(input.value,10));
                    grade.gradeTable.calcGa(j + 1);
                }
            }
        }
    }
}


function changeScore(input) {
    var gradeInfos = input.name.split("_");
    var examType = gradeInfos[0];
    var score = (isEmpty(input.value) ? null : input.value);
    if (null == score) {
        return;
    }
    if (this.examGrades[examType] == score) {
        this.gradeTable.calcGa(input.id.split("_")[1]);
    } else {
        if(null != this.examGrades[examType]) {
           if (confirm("成绩录入和上次录入结果不一致!\n第一次录入结果为:" + this.examGrades[examType] + "\n第二次录入结果:" + input.value
                       + "\n是否要以第二次录入的成绩作为该成绩?")) {
               this.gradeTable.calcGa(input.id.split("_")[1]);
           } else {
               score = this.gradeTable.valueStyle[this.gradeTable.precision].validator.test(this.examGrades[examType]) ? this.examGrades[examType] : "";
               if (this.gradeTable.hasGradeSelect) {
                   setSelected(input.value, score);
               } else {
                   input.value = score;
               }
           }
       }
    }
}

function changeCourseGrade(input) {
    this.gradeMap[input.name.split("_")[1]].change(input, this);
}

// ///////////////////////////////////////////////////////////////////////

var intervalId = null;

function saveGrade(justSave) {
	clearInterval(timer);
	document.getElementById("timeElapse").innerHTML = "";
    if (!justSave) {
        if (gradeTable.hasEmpty()) {
        	alert("当前成绩中有没有录入的，请完成录入");
            return false;
        }
    	if(!confirm("确定提交成绩?")) return false;
    }
    form =document.gradeForm;
    
    bg.form.addInput(form, "justSave", justSave, "hidden");
    document.getElementById("submitTd").innerHTML = "成绩" + (justSave ? "暂存" : "提交" ) + "中，请稍侯……";
    if (null != intervalId) {
        clearInterval(intervalId);
        document.getElementById("timeElapse").innerHTML = "";
    }
   	return true;
}

// 为保存成绩定时提示
var timeMin=5;// 5分钟
var time = timeMin * 60; 
var timeElapse = 0;
function refreshTime() {
	if(document.getElementById("timeElapse")==null){
		clearInterval(timer);
		return;
	}
    document.getElementById("timeElapse").style.textAlign = "left";
    var sec = timeElapse % 60;
    var min = Math.floor(timeElapse / 60) % 60;
    var hh = Math.floor(timeElapse / 3600);
    document.getElementById("timeElapse").innerHTML = "（" + timeMin + "分钟自动保存）时间：" + hh + ":" + (min < 10 ? "0" : "") + min + ":" + (sec < 10 ? "0" : "") + sec;
    if (timeElapse > 0 && timeElapse % time == 0) {
        if(saveGrade(true)) document.gradeForm.submit();
    }
    timeElapse++;
}
//var timer = setInterval('refreshTime()',1000);

/**
 * 改变考试状态对分数进行清空和隐藏
 */
function changeExamStatus(scoreId,obj){
	if(obj && obj.value != '1'){
		for(var s in emptyScoreStatuses){
			if(obj.value == emptyScoreStatuses[s]){ 
				jQuery("#"+scoreId).val('').hide();
				break;
			}
		}
	}else{
		jQuery("#"+scoreId).show();
	}
}

function isEmpty(str){
	return /^\s*$/.test(str);
}