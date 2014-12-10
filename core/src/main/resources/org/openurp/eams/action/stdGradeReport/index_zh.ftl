[@b.head /]
[#include "print.ftl"/]
[#assign fontsize=10/]
    <style>
        .semester{
            text-align:center;
            font-size:${fontsize+2}px;
            font-family:楷体;
            border-top:2px #000 solid;
            border-right:2px #000 solid;
            border-left:2px #000 solid;
            border-bottom:2px #000 solid;
        }
        .blank{
            text-align:center;
            font-size:${fontsize}px;
            font-family:楷体;
            border-right:2px #000 solid;
            border-left:2px #000 solid;
        }
        .tds{
            text-align:center;
        }
        .tableclass{
            border-collapse:collapse;
            font-size:${fontsize}px;
            border-top:2px #000 solid;
            border-right:2px #000 solid;
            border-left:2px #000 solid;
            border-bottom:2px #000 solid;
        }
        .titlecss{
            text-align:center;
            font-size:${fontsize}px;
            width:250px;
            font-family:楷体;
        }
        .title{
            text-align:center;
            font-size:${fontsize}px;
            font-family:楷体;
            width:50px;
        }
       .container{
            width:100%;
            padding-right: 25px;
            padding-left: 25px;
        }
    </style>
    [#--最大成绩行数--]
    [#assign maxRows = 35/]
    [#assign maxCols = 16/]
    [#--每列最大学期数--]
[#list stdGradeReports as report]
   [#assign schoolName]${report.std.project.school.name}[/#assign]
[#assign std=report.std/]
    [#assign stdTypeName = (report.std.type1.name)!"" /]
    <div  style="[#if report_index>0]PAGE-BREAK-BEFORE: always[/#if]">
    <table  width='100%'  valign='top' >
        <tr><td colspan="5" align="center"><h2 style="font-size:${fontsize+10}px">${schoolName}${(report.std.grade + "级")?replace("-3级","级(春季)")?replace("-9级","级(秋季)")}${stdTypeName}学生成绩单表</h2></td></tr>
        <tr style="font-size:${fontsize}px">
         <td >层&nbsp;&nbsp;&nbsp;&nbsp;次：${(std.education.name)!}</td>
         <td >专&nbsp;&nbsp;&nbsp;&nbsp;业：${(std.major.name)?default("")}</td>
         <td >学&nbsp;&nbsp;&nbsp;&nbsp;号：${((std.code)?default(""))?trim}</td>
         <td >姓&nbsp;&nbsp;&nbsp;&nbsp;名：${std.person.name}</td>
         <td >性&nbsp;&nbsp;&nbsp;&nbsp;别：${((std.person.gender.name)?default(""))?trim}</td>
        </tr>
        </table>
        <table width='100%' border="1" id="transcript${std.id}" class="tableclass">
            [#list 1..maxRows as row]
                <tr height='20px' >
                [#list 0..15 as col]<td  id="transcript${std.id}_${(col/4)?int*4*maxRows+(col%4)+(row-1)*4}" [#if col==3 || col==7 || col=11] style="border-right:2px #000 solid;" [/#if] [#if col==0 || col==4 || col==8 || col==12] width="250px"  [/#if]  [#if col!=0 && col !=4 && col != 8 && col != 12 ] width="50px"  [/#if]>&nbsp;</td>[/#list]
                </tr>
            [/#list]
        </table>
    <script type="application/javascript">
    var index=0;
    var semesterCourses={};
    var nowsemsenumber=0;
    var blankRow=0;
    var array =[];
    //统计学期总个数，并且去掉重复元素
    function semsernumber(semsename){
         array.push(semsename);
         array = array || [];
         var a = {};
        for (var i=0; i<array.length; i++) {
            var v = array[i];
            if (typeof(a[v]) == 'undefined'){
                a[v] = 1;
            }
        };
        array.length=0;
        for (var i in a){
           array[array.length] = i;
        }
    }
    
    //移除重复元素
    function unique(){
        data = data || [];
        var a = {};
        for (var i=0; i<data.length; i++) {
            var v = data[i];
            if (typeof(a[v]) == 'undefined'){
                a[v] = 1;
            }
        };
        data.length=0;
        for (var i in a){
            data[data.length] = i;
        }
        return data.length;
   }
   
    //添加学期和以下空白等说明
    function setTitle(table,tbindex,value){
        var td = document.getElementById(table+"_"+tbindex);
        td.innerHTML=value;
        td.parentNode.removeChild(td.nextSibling);
        td.parentNode.removeChild(td.nextSibling);
        td.parentNode.removeChild(td.nextSibling);
        td.colSpan=4;
        if(value!="以下空白"){
            td.className="semester";
        }else{
            td.className="blank";
        }
    }
    function calcRow(tdindex){
        return parseInt(tdindex/${maxRows*4})*4 + tdindex%4;
    }
    
    function calcCol(tdindex){
        return parseInt((tdindex-parseInt(tdindex/${maxRows*4})*4*${maxRows})/4) +1;
    }
    semesterTds={}
    //添加表头
    function addTitle(table){
        var col = calcRow(index);
        var row = calcCol(index);
        if(row>${maxRows} || col >= ${maxCols}) {
            return;
        }
        document.getElementById(table+"_"+(index)).className="titlecss";
        document.getElementById(table+"_"+(index)).innerHTML="课程名称";
        document.getElementById(table+"_"+(index+1)).className="title";
        document.getElementById(table+"_"+(index+1)).innerHTML="类型";
        document.getElementById(table+"_"+(index+2)).className="title";
        document.getElementById(table+"_"+(index+2)).innerHTML="学分";
        document.getElementById(table+"_"+(index+3)).className="title";
        document.getElementById(table+"_"+(index+3)).innerHTML="成绩";
        index+=4;
    }
    //添加学年学期
    function addSemester(table,semesterId,value){
        nowsemsenumber++;//当前是第几个学期
        var col = calcRow(index);
        var row = calcCol(index);
        var myCourseCnt= semesterCourses['c'+semesterId];
        if(row>${maxRows} || col >= ${maxCols}) {
            return;
        }
      if(array.length<=8 &&( nowsemsenumber==3 || nowsemsenumber ==5 || nowsemsenumber ==7)){
            //转到下一列的第一行
            addBlank(table);
            index=${maxRows}*(col+4);
            if(calcRow(index)>${maxRows}|| calcCol(index) >= ${maxCols}) {return;}
        }
      if( array.length>8 && (${maxRows} - row-1) < myCourseCnt){
            //转到下一列的第一行
            addBlank(table);
            index=${maxRows}*(col+4);
            if(calcRow(index)>${maxRows}|| calcCol(index) >= ${maxCols}) {return;}
      }
      setTitle(table,index,value);
      semesterTds["semester"+semesterId]=index
      index+=4;
    }
    
    //添加以下空白
    function addBlank(table){
        var col = calcRow(index);
        var row = calcCol(index);
        if(row>${maxRows}|| col >= ${maxCols}) {return;}
        //空白行不放在第一行
        if(row==1)return;
        setTitle(table,index,"以下空白");
    }
    function addScore(table,name,courseTypeName,credit,score){
        var col = calcRow(index);
        var row = calcCol(index);
        if(row>${maxRows} || col >= ${maxCols}) {return;}
        document.getElementById(table+"_"+(index)).innerHTML=name;
        document.getElementById(table+"_"+(index+1)).className="tds";
        document.getElementById(table+"_"+(index+2)).className="tds";
        document.getElementById(table+"_"+(index+3)).className="tds";
        document.getElementById(table+"_"+(index+1)).innerHTML=courseTypeName;
        document.getElementById(table+"_"+(index+2)).innerHTML=credit;
        document.getElementById(table+"_"+(index+3)).innerHTML=score;
        index+=4;
        if (blankRow<row)  blankRow=row;
    }
    /**累计每学期课程*/
    function addSemesterCourse(semesterId){
        //学年学期占一行,所以初始为1
        if(typeof semesterCourses['c'+semesterId] == "undefined")
            semesterCourses['c'+semesterId]=1;
        else semesterCourses['c'+semesterId]= semesterCourses['c'+semesterId]+1;
    }
   function term(name){
      if (name=='1') return "一"
      else if(name=='2') return "二"
      else return name
   }
   
    /**添加备注*/
   function addRemark(table,content){
        var col = calcRow(index);
        var row = calcCol(index);
        if(row>${maxRows} || col >= ${maxCols}) {return;}
        document.getElementById(table+"_"+(index)).innerHTML=content;
        document.getElementById(table+"_"+(index)).colSpan=4
        var parentNode = document.getElementById(table+"_"+(index)).parentNode
        parentNode.removeChild(document.getElementById(table+"_"+(index+1)));
        parentNode.removeChild(document.getElementById(table+"_"+(index+2)));
        parentNode.removeChild(document.getElementById(table+"_"+(index+3)));
        index+=4;
        if (blankRow<row)  blankRow=row;
    }
    function removeTr(){
      if(${maxRows}-blankRow>0){
          var t1=document.getElementById("transcript${std.id}");
          var maxr =${maxRows};
          for(var i=0;i<=maxr;i++){
              if(i>blankRow) t1.deleteRow(blankRow);
          }
      }
   }
   function getGpa(){
      jQuery.getJSON("http://192.168.103.24:8080/teach-ws/teach/grade/gpa-stats/2007137130",function(gpa){
          var semesterGa=null;
          var td=null
          for(i=0;i < gpa.semesterGpas.length;i++){
            semesterGa =gpa.semesterGpas[i];
            td =document.getElementById("transcript${std.id}_"+semesterTds["semester"+semesterGa.semester.id])
            td.innerHTML = td.innerHTML + "(平均绩点:"+semesterGa.gpa+")"
          }
          document.getElementById("TD_GPA").innerHTML=  document.getElementById("TD_GPA").innerHTML+gpa.gpa
          document.getElementById("TD_TC").innerHTML=  document.getElementById("TD_TC").innerHTML+gpa.credits
       });
    }
   jQuery.getJSON("http://192.168.103.24:8080/teach-ws/teach/grade/std-course-grades?request_locale=zh_CN",function(grades){
       var semester_id="0"
       grades.sort(function(a, b){
         if(a.semester.code<b.semester.code) return -1
         else if(a.semester.code>b.semester.code) return 1
         else a.course.code < b.course.code
         }
         );
       for(var i=0;i<grades.length;i++){
            grade = grades[i]
            addSemesterCourse(grade.semester.id);
            semsernumber(grade.semester.id);
            if(grade.semester.id !=semester_id){
               semester_id=grade.semester.id
               addSemester("transcript${std.id}",grade.semester.id, grade.semester.schoolYear
                                       + "学年第" + term(grade.semester.name)  + "学期");
               addTitle("transcript${std.id}");
            }
            addScore("transcript${std.id}" , grade.course.name, grade.courseType.code.charAt(0), grade.course.credits, grade.scoreText);
          }
       addBlank("transcript${std.id}");
       removeTr();
       index= ((${maxRows}*3-1)*4)+((blankRow-7)*4)
       var title = document.getElementById("transcript${std.id}_"+(index)); 
       title.className="semester"
       addRemark("transcript${std.id}",'类别备注');
       addRemark("transcript${std.id}",'1 代表公共基础必修课');
       addRemark("transcript${std.id}",'2 代表学科基础必修课');
       addRemark("transcript${std.id}",'3 代表专业方向必修课');
       addRemark("transcript${std.id}",'4 代表学科基础选修课');
       addRemark("transcript${std.id}",'5 代表专业方向选修课');
       addRemark("transcript${std.id}",'6 代表实践教学课');
       addRemark("transcript${std.id}",'7 代表公共基础选修课');
       getGpa();
     });

    </script>
    <table width='100%' border=0  valign='bottom' style="font-family:宋体;font-size:${fontsize+2}px;">
        <tr>
            <td  align='left' id="TD_TC">总学分:</td>
            <td  align='left' id="TD_GPA">平均绩点:</td>
            <td  align='right' >上海金融学院教务处</td>
            <td  align='right' width="100px" >${b.now?string('yyyy年MM月dd日')}</td>
        </tr>
    </table>
        </div>
[/#list]