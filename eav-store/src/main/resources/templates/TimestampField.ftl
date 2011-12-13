<label for="${path}">${prompt}</label>
<input name="${path}" id="${path}" size="15"
<#if value??>
 value="${value}"
</#if>
 type="text" class="textInput eavTimestampField"/>
<#if hint??>
    <p class="formHint">
    ${hint}
    <#if helpUrl??>
        <a href="${helpUrl}" target="_blank">${helpPrompt}</a>
    </#if>
    </p>
</#if>
