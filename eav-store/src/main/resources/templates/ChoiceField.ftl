<label for="${path}">${prompt}</label>
<#if style="SELECT">
    <select class="selectInput eavChoiceField" name="${path}" id="${path}">
        <option value="">---</option>
        <#list options as option>
           <#if value?? && option=value>
        <option value="${option}" selected="selected">${option}</option>
           <#else>
        <option value="${option}">${option}</option>
           </#if>
        </#list>
    </select>
<#elseif style="RADIO">
    <div class="multiField">
        <#list options as option>
           <#if value?? && option=value>
               <input type="radio" name="${path}" value="${option}" checked='checked'>${option}</input>
           <#else>
               <input type="radio" name="${path}" value="${option}">${option}</input>
           </#if>
        </#list>
    </div>
</#if>
<#if hint??>
    <p class="formHint">
    ${hint}
    <#if helpUrl??>
        <a href="${helpUrl}" target="_blank">${helpPrompt}</a>
    </#if>
    </p>
</#if>


