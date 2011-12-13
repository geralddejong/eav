<label for="${path}">${prompt}</label>
<#if rows??>
    <textarea name="${path}" id="${path}"
    <#if maximumLength??> maxlength="${maximumLength}"</#if>
    <#if rows??> rows="${rows}"</#if>
    class="textInput eavStringField"><#if value??>${value}</#if></textarea>
<#else>
    <input name="${path}" id="${path}"
    <#if maximumLength??> maxlength="${maximumLength}"</#if>
    <#if value??> value="${value}"</#if>
    type="text" class="textInput eavStringField"/>
</#if>
<#if hint??>
    <p class="formHint">
    ${hint}
    <#if helpUrl??>
        <a href="${helpUrl}" target="_blank">${helpPrompt}</a>
    </#if>
    </p>
</#if>
