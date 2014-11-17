<#include "/template/head.ftl"/>
<script type="text/javascript" src="${base}/static/scripts/utils/StringUtils.js"></script>
<script type="text/javascript" src="${base}/static/scripts/common/multiSelectChoiceForReport.js"></script>
<script type="text/javascript" src='${base}/dwr/interface/projectMultiSelectForESD.js'></script>
<script type="text/javascript" src="${base}/static/scripts/validator.js"></script>
<body>
    <table id="bar"></table>
    <table class="formTable" width="80%" align="center">
        <form method="post" action="" name="actionForm" >
            <input type="hidden" name="scope.id" value="${(scope.id)?if_exists}"/>
        <tr>
            <td colspan="4" class="darkColumn" style="font-size:11pt;text-align:center">成绩检查评教参数配置</td>
        </tr>
        <tr>
            <td class="title" rowspan="4" id="f_project">教学项目范围<font color="red">*</font>：</td>
            <td rowspan="4">
                供选范围：<br>
                <select id="fromProject" style="width:250px;height:200px" multiple onDblClick="setIsChange(true);selectMoveAnyOne(this, $('toProject'))">
                    <#list projects?sort_by("name") as project>
                    <option value="${project.id}">${project.name}</option>
                    </#list>
                </select>
            </td>
            <td width="50px" align="center" style="border-bottom-width:0px"><button style="width:30px" onclick="setIsChange($('toProject').options.length > 0);selectMoveAll($('toProject'), $('fromProject'))"><<</button></td>
            <td rowspan="4">
                所选范围：<br>
                <select id="toProject" style="width:250px;height:200px" multiple onDblClick="setIsChange(true);selectMoveAnyOne(this, $('fromProject'))">
                </select>
            </td>
        </tr>
        <tr>
            <td align="center" style="border-top-width:0px;border-bottom-width:0px"><button style="width:30px" onclick="setIsChange($('toProject').selectedIndex > -1);selectMoveAnyOne($('toProject'), $('fromProject'))"><</button></td>
        </tr>
        <tr>
            <td align="center" style="border-top-width:0px;border-bottom-width:0px"><button style="width:30px" onclick="setIsChange($('fromProject').selectedIndex > -1);selectMoveAnyOne($('fromProject'), $('toProject'))">></button></td>
        </tr>
        <tr>
            <td align="center" style="border-top-width:0px"><button style="width:30px" onclick="setIsChange($('fromProject').options.length > 0);selectMoveAll($('fromProject'), $('toProject'))">>></button></td>
        </tr>
        <tr>
            <td class="title" rowspan="4" id="f_education">学历层次范围：</td>
            <td rowspan="4">
                供选范围：<br>
                <select id="fromEducation" style="width:250px;height:200px" multiple onDblClick="setIsChange(false);selectMoveAnyOne(this, $('toEducation'))">
                </select>
            </td>
            <td align="center" style="border-bottom-width:0px"><button style="width:30px" onclick="setIsChange(false);selectMoveAll($('toEducation'), $('fromEducation'))"><<</button></td>
            <td rowspan="4">
                所选范围：<br>
                <select id="toEducation" style="width:250px;height:200px" multiple onDblClick="setIsChange(false);selectMoveAnyOne(this, $('fromEducation'))">
                <#list (scope.educations)?if_exists as education>
                    <option value="${education.id}">${education.name}</option>
                </#list>
                </select>
            </td>
        </tr>
        <tr>
            <td align="center" style="border-top-width:0px;border-bottom-width:0px"><button style="width:30px" onclick="setIsChange(false);selectMoveAnyOne($('toEducation'), $('fromEducation'))"><</button></td>
        </tr>
        <tr>
            <td align="center" style="border-top-width:0px;border-bottom-width:0px"><button style="width:30px" onclick="setIsChange(false);selectMoveAnyOne($('fromEducation'), $('toEducation'))">></button></td>
        </tr>
        <tr>
            <td align="center" style="border-top-width:0px"><button style="width:30px" onclick="setIsChange(false);selectMoveAll($('fromEducation'), $('toEducation'))">>></button></td>
        </tr>
        <tr>
            <td class="title" rowspan="4" id="f_studentType">学生类别范围：</td>
            <td rowspan="4">
                供选范围：<br>
                <select id="fromStudentType" style="width:250px;height:200px" multiple onDblClick="setIsChange(false);selectMoveAnyOne(this, $('toStudentType'))">
                </select>
            </td>
            <td align="center" style="border-bottom-width:0px"><button style="width:30px" onclick="setIsChange(false);selectMoveAll($('toStudentType'), $('fromStudentType'))"><<</button></td>
            <td rowspan="4">
                所选范围：<br>
                <select id="toStudentType" style="width:250px;height:200px" multiple onDblClick="setIsChange(false);selectMoveAnyOne(this, $('fromStudentType'))">
                <#list (scope.stdTypes)?if_exists as studentType>
                    <option value="${studentType.id}">${studentType.name}</option>
                </#list>
                </select>
            </td>
        </tr>
        <tr>
            <td align="center" style="border-top-width:0px;border-bottom-width:0px"><button style="width:30px" onclick="setIsChange(false);selectMoveAnyOne($('toStudentType'), $('fromStudentType'))"><</button></td>
        </tr>
        <tr>
            <td align="center" style="border-top-width:0px;border-bottom-width:0px"><button style="width:30px" onclick="setIsChange(false);selectMoveAnyOne($('fromStudentType'), $('toStudentType'))">></button></td>
        </tr>
        <tr>
            <td align="center" style="border-top-width:0px"><button style="width:30px" onclick="setIsChange(false);selectMoveAll($('fromStudentType'), $('toStudentType'))">>></button></td>
        </tr>
        <tr>
            <td class="title" rowspan="4" id="f_enrollYear">需提醒年份列表<font color="red">*</font>：</td>
            <td rowspan="4">
                供选范围：<br>
                <select id="fromEnrollYear" style="width:250px;height:200px" multiple onDblClick="setIsChange(false);selectMoveAnyOne(this, $('toEnrollYear'))">
                <#list (enrollYears?sort?reverse)?if_exists as enrollYear>
                    <#if !("," + (scope.enrollYears)?default("") + ",")?contains(enrollYear)>
                    <option value="${enrollYear}">${enrollYear}</option>
                    </#if>
                </#list>
                </select>
            </td>
            <td align="center" style="border-bottom-width:0px"><button style="width:30px" onclick="setIsChange(false);selectMoveAll($('toEnrollYear'), $('fromEnrollYear'))"><<</button></td>
            <td rowspan="4">
                所选范围：<br>
                <select id="toEnrollYear" style="width:250px;height:200px" multiple onDblClick="setIsChange(false);selectMoveAnyOne(this, $('fromStudentType'))">
                <#list (scope.enrollYears?split(","))?if_exists as enrollYear>
                    <option value="${enrollYear}">${enrollYear}</option>
                </#list>
                </select>
            </td>
        </tr>
        <tr>
            <td align="center" style="border-top-width:0px;border-bottom-width:0px"><button style="width:30px" onclick="setIsChange(false);selectMoveAnyOne($('toEnrollYear'), $('fromEnrollYear'))"><</button></td>
        </tr>
        <tr>
            <td align="center" style="border-top-width:0px;border-bottom-width:0px"><button style="width:30px" onclick="setIsChange(false);selectMoveAnyOne($('fromEnrollYear'), $('toEnrollYear'))">></button></td>
        </tr>
        <tr>
            <td align="center" style="border-top-width:0px"><button style="width:30px" onclick="setIsChange(false);selectMoveAll($('fromEnrollYear'), $('toEnrollYear'))">>></button></td>
        </tr>
        <input type="hidden" name="projectIds" value=""/>
        <input type="hidden" name="educationIds" value=""/>
        <input type="hidden" name="stdTypeIds" value=""/>
        <input type="hidden" name="scope.enrollYears" value="${(scope.enrollYears)?if_exists}"/>
        <tr>
            <td class="title">是否检查评教：</td>
            <td colspan="3"><@htm.radio2 name="scope.checkEvaluation" value=(scope.checkEvaluation)?default(true)/></td>
        </tr>
        <tr>
            <td colspan="4" class="darkColumn" style="font-size:11pt;text-align:center"><button onclick="save()">提交</button></td>
        </tr>
        </form>
    </table>
    <form method="post" action="" name="backForm" >
        <#assign paramSet = Parameters["params"]?split("&")/>
        <#list paramSet as paramItem>
            <#assign params = paramItem?split("=")/>
            <#if (params?size > 1)>
        <input type="hidden" name="${params[0]}" value="${params[1]}"/>
            </#if>
        </#list>
    </form>
    <script>
        var bar = new ToolBar("bar", "成绩检查评教设置", null, true, true);
        bar.setMessage('<@getMessage/>');
        bar.addItem("返回", "toIndex()", "backward.gif");
        
        var form = document.actionForm;
        
        var isChange = false;
        
        <#if (scope.projects)?exists>
        for (var i = 0; i < $("fromProject").options.length; i++) {
            <#list scope.projects as project>
            if ("${project.id}" == $("fromProject").options[i].value) {
                 $("fromProject").options[i].selected = true;
            }
            </#list>
        }
        setIsChange(true);
        selectMoveAnyOne($('fromProject'), $('toProject'));
        </#if>
        
        function setIsChange(isChange) {
            this.isChange = isChange;
        }
        
        function customFunction() {
            if (isChange) {
                var projectIdSeq = getMultiSelectIds($("toProject"));
                var educationIdSeq = getMultiSelectIds($("toEducation"));
                var studentTypeIdSeq = getMultiSelectIds($("toStudentType"));
                projectMultiSelectForESD.educationAndDeparts(projectIdSeq,educationIdSeq,"",studentTypeIdSeq,setEducationAndDepartOptions);
            }
        }
        
        function getMultiSelectIds(obj) {
            var seqId = "";
            if (0 != obj.options.length) {
                for (var i = 0; i < obj.options.length; i++) {
                    seqId += obj.options[i].value;
                    if (i + 1 < obj.options.length) {
                        seqId += ",";
                    }
                }
            }
            return seqId;
        }
        
        function setEducationAndDepartOptions(datas){
            $("fromEducation").options.length = 0;
            $("fromStudentType").options.length = 0;
            var data=datas[0];
            for(var i=0;i<data.length;i++){
                $("fromEducation").options.add(new Option(data[i][1], data[i][0]));
            }
            data=datas[2];
            for(var i=0;i<data.length;i++){
                $("fromStudentType").options.add(new Option(data[i][1], data[i][0]));
            }
        }
        
        function save() {
            form["projectIds"].value = getMultiSelectIds($("toProject"));
            form["educationIds"].value = getMultiSelectIds($("toEducation"));
            form["stdTypeIds"].value = getMultiSelectIds($("toStudentType"));
            form["scope.enrollYears"].value = getMultiSelectIds($("toEnrollYear"));
            var a_fields = {
                'projectIds':{'l':"教学项目范围", 'r':true, 't':'f_project'},
                'scope.enrollYears':{'l':"需提醒年份列表", 'r':true, 't':'f_enrollYear'}
            };
            var v = new validator(form, a_fields, null);
            if (v.exec()) {
                form.action = "gradeViewScope.action?method=save";
                form.target = "_self";
                form.submit();
            } else {
                form["projectIds"].value = "";
                form["educationIds"].value = "";
                form["stdTypeIds"].value = "";
                form["scope.enrollYears"].value = "";
            }
        }
        
        var backForm = document.backForm;
        
        function toIndex() {
            backForm.action = "gradeViewScope.action?method=search";
            backForm.target = "_self";
            backForm.submit();
        }
        
        parent.toResize(document.body);
    </script>
</body>
<#include "/template/foot.ftl"/>