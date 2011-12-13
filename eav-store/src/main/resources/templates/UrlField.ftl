<label for="${path}">${prompt}</label>
<input name="${path}" id="${path}"
<#if maximumLength??>
 size="${maximumLength}"
</#if>
<#if value??>
 value="${value}"
</#if>
 type="text" class="textInput eavUrlField"/>
<#if hint??>
    <p class="formHint">
    ${hint}
    <#if helpUrl??>
        <a href="${helpUrl}" target="_blank">${helpPrompt}</a>
    </#if>
    </p>
</#if>
