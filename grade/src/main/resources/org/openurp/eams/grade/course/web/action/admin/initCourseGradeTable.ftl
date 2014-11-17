<#macro displayGrades(index, courseTake)>
    <#if gradeMap.get(courseTake.std)??>
        <#local grade = gradeMap.get(courseTake.std)/>
    </#if>
    <td align="center">${index + 1}</td>
    <td>${courseTake.std.code}<input type="hidden" value="${(courseTake.std.project.id)?if_exists}" id="courseTake_project_${index + 1}"></td>
    <td>${courseTake.std.name}<#if courseTake.courseTakeType.id == RESTUDY>(重修)</#if></td>
    <script>courseGrade = gradeTable.add(${index}, "${courseTake.std.id}", ${courseTake.courseTakeType.id}, gradeTable);</script>
    <#local isSecond = (minEnrol > 1)/>
    <input type="hidden" id="courseTakeType_${index + 1}" value="${courseTake.courseTakeType.id}"/>
    <#list gradeTypes as gradeType>
        <#if gradeType.id != GA.id>
    <script>courseGrade.examGrades["${gradeType.shortName}"] = "<#if isTeacher>${(grade.getScoreText(gradeType)?trim)?if_exists}</#if>";</script>
        </#if>
        <#if courseTake.courseTakeType.id == REEXAM && gradeType.id == USUAL.id>
    <td>免修不免试</td>
        <#elseif gradeType.examType?exists &&!(courseTake.isAttendExam(gradeType.examType))>
    <td>${courseTake.getExamTake(gradeType.examType).examStatus.name!}</td>
        <#elseif gradeType.id == GA.id>
    <td align="center" id="GA_${index + 1}" width="80px"><#if grade?exists && (!isTeacher || isTeacher && isSecond && enrolTimeMap[gradeType.id?string]?default(0) == 2)><#if (grade.getExamGrade(GA).passed)?default(false)>${grade.getScoreText(GA)!}<#else><font color="red">${grade.getScoreText(GA)!}</font></#if></#if></td>
        <#else>
            <#if stdExamTypeMap[courseTake.std.id + "_" + (gradeType.examType.id)?default("")]?exists
                 && stdExamTypeMap[courseTake.std.id + "_" + (gradeType.examType.id)?default("")].examStatus.id != NORMAL
                 && !stdExamTypeMap[courseTake.std.id + "_" + (gradeType.examType.id)?default("")].examStatus.attended>
    <td>${stdExamTypeMap[courseTake.std.id + "_" + (gradeType.examType.id)?default("")].examStatus!}</td>
            <#else>
                <#local examStatusId = ABSENT?string/>
                <#if grade?exists && grade?string != "null">
                    <#if grade.getExamGrade(gradeType)?exists>
                        <#local examStatusId = grade.getExamGrade(gradeType).examStatus.id?string/>
                    <#elseif examStatusId == NORMAL?string>
                        <#local examStatusId = ABSENT?string/>
                    </#if>
                </#if>
                <@gradeTd grade?default("null"), gradeType, courseTake, index, isSecond, examStatusId/>
            </#if>
        </#if>
    </#list>
</#macro>

<#macro gradeTd(grade, gradeType, courseTake, index, isSecond, examStatusId)>
    <td id="TD_${gradeType.shortName}_${courseTake.std.id}">
        <#if gradeState.scoreMarkStyle.numStyle>
        <input type="text" class="text"
            onfocus="this.style.backgroundColor='yellow'" 
            onblur="checkScore(${index + 1}, this);this.style.backgroundColor='white';"
            tabIndex="${index+1}"
            id="${gradeType.shortName}_${index + 1}" name="${gradeType.shortName}_${courseTake.std.id}"
            value="<#if grade?string != "null" && !isSecond>${(grade.getScoreText(gradeType))?if_exists}</#if>" style="width:80px" maxlength="4"/>
        <#else>
            <select onfocus="this.style.backgroundColor='yellow'"
                    onblur="this.style.backgroundColor='white'" style="width:80px"
                    onchange="checkScore(${index + 1}, this)"
                    id="${gradeType.shortName}_${index + 1}" name="${gradeType.shortName}_${courseTake.std.id}"
                    style="width:80px">
                <option value="">...</option>
                <#list gradeConverterConfig.items as item>
                <option value="${item.defaultScore}">${item.grade}</option>
                </#list>
            </select>
        </#if>
        <@b.select items=examStatuses value=examStatusId?if_exists name="examStatus_" + gradeType.shortName + "_" + courseTake.std.id id="examStatus_" + gradeType.shortName + "_" + (index + 1) style="width:80px;display:none" disabled="disabled" onchange="checkScore(${index + 1}, this)"></@>
    </td>
</#macro>
