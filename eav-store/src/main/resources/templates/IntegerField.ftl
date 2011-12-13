<label for="${path}">${prompt}</label>
<input name="${path}" id="${path}"
<#if value??> value="${value}"</#if>
 size="20" type="text" class="textInput eavIntegerField"/>
<#if hint??>
    <p class="formHint">
    ${hint}
    <#if helpUrl??>
        <a href="${helpUrl}" target="_blank">${helpPrompt}</a>
    </#if>
    </p>
</#if>
