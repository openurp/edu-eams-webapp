 <#macro emptyTd count>
    <#if (count>0)>
    <#list 1..count as i>
    <td>&nbsp;</td>
    </#list>
    </#if>
 </#macro>
