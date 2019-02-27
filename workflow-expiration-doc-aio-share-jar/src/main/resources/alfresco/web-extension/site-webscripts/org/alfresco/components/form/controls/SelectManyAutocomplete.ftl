<#include "/org/alfresco/components/form/controls/common/utils.inc.ftl" />

<style>
#${fieldHtmlId} { 
	width:780px; /* set width here or else widget will expand to fit its container */ 
} 
#${fieldHtmlId}-autoComplete { 
	padding-bottom:2em;
}
.yui-skin-sam .yui-ac-content li.yui-ac-highlight { 
	background:#CCC;color:#000; 	
} 
</style>

<#if field.control.params.optionSeparator??>
   <#assign optionSeparator=field.control.params.optionSeparator>
<#else>
   <#assign optionSeparator=",">
</#if>
<#if field.control.params.labelSeparator??>
   <#assign labelSeparator=field.control.params.labelSeparator>
<#else>
   <#assign labelSeparator="|">
</#if>

<#assign fieldValue=field.value>

<#if fieldValue?string == "" && field.control.params.defaultValueContextProperty??>
   <#if context.properties[field.control.params.defaultValueContextProperty]??>
      <#assign fieldValue = context.properties[field.control.params.defaultValueContextProperty]>
   <#elseif args[field.control.params.defaultValueContextProperty]??>
      <#assign fieldValue = args[field.control.params.defaultValueContextProperty]>
   </#if>
</#if>

<div class="form-field">
   <#if form.mode == "view">
      <div class="viewmode-field">
         <#if field.mandatory && !(fieldValue?is_number) && fieldValue?string == "">
            <span class="incomplete-warning"><img src="${url.context}/res/components/form/images/warning-16.png" title="${msg("form.field.incomplete")}" /><span>
         </#if>
         <span class="viewmode-label">${field.label?html}:</span>         
         <#if fieldValue?string == "">
            <#assign valueToShow=msg("form.control.novalue")>
         <#else>
            <#assign valueToShow=fieldValue>
            <#if field.control.params.options?? && field.control.params.options != "">
               <#list field.control.params.options?split(optionSeparator) as nameValue>
                  <#if nameValue?index_of(labelSeparator) == -1>
                     <#if nameValue == fieldValue?string || (fieldValue?is_number && fieldValue?c == nameValue)>
                        <#assign valueToShow=nameValue>
                        <#break>
                     </#if>
                  <#else>
                     <#assign choice=nameValue?split(labelSeparator)>
                     <#if choice[0] == fieldValue?string || (fieldValue?is_number && fieldValue?c == choice[0])>
                        <#assign valueToShow=msgValue(choice[1])>
                        <#break>
                     </#if>
                  </#if>
               </#list>
            </#if>
         </#if>
         <span class="viewmode-value">${valueToShow?html}</span>
      </div>
   <#else>
       <!--<label for="${fieldHtmlId}">${field.label?html}:<#if field.mandatory><span class="mandatory-indicator">${msg("form.required.fields.marker")}</span></#if></label>
         <select id="${fieldHtmlId}" name="${field.name}" tabindex="0"
               <#if field.description??>title="${field.description}"</#if>
               <#if field.control.params.size??>size="${field.control.params.size}"</#if> 
               <#if field.control.params.styleClass??>class="${field.control.params.styleClass}"</#if>
               <#if field.control.params.style??>style="${field.control.params.style}"</#if>
               <#if field.disabled  && !(field.control.params.forceEditable?? && field.control.params.forceEditable == "true")>disabled="true"</#if>>
         </select>-->
			<label for="${fieldHtmlId}-autoCompleteInput">${field.label?html}:<#if field.mandatory><span class="mandatory-indicator">${msg("form.required.fields.marker")}</span></#if></label>
			<div id="${fieldHtmlId}-autoComplete"> 
				<input id="${fieldHtmlId}" type="text" name="${field.name}" tabindex="0"
					   <#if field.description??>title="${field.description}"</#if>
					   <#if field.control.params.size??>size="${field.control.params.size}"</#if> 
					   <#if field.control.params.styleClass??>class="${field.control.params.styleClass}"</#if>
					   <#if field.control.params.style??>style="${field.control.params.style}"</#if>
					   <#if field.disabled  && !(field.control.params.forceEditable?? && field.control.params.forceEditable == "true")>disabled="true"</#if>>
				 </input>
				<div id="${fieldHtmlId}-Container"></div> 
			</div> 
   </#if>
</div>


<script type="text/javascript">//<![CDATA[
YAHOO.util.Event.onContentReady("${fieldHtmlId}", function ()
{ 
		  var emailsAddressArray = new Array();
   	      
   			//chiamata json per recuperare le emails della rubrica
   	      Alfresco.util.Ajax.jsonGet({
          url: encodeURI(Alfresco.constants.PROXY_URI + '/grupposamed/workflowcustom/emailaddress'),
          successCallback:
          {
             fn: function loadWebscript_successCallback(response, config)
             {
                 var obj = eval('(' + response.serverResponse.responseText + ')');
                 if (obj)
                 {
                 	var emailsAddress = obj.emails; //array delle emails
				  	for (i = 0; i < emailsAddress.length; i++) {
					var newOption = document.createElement('option');
					//alert(emailsAddress);
					emailsAddressArray.push(emailsAddress[i]);
					//newOption.value = emailsAddress[i];
					//newOption.text = emailsAddress[i];
					//YAHOO.util.Dom.get("${fieldHtmlId}").options.add(newOption);
				  }
		        	  
				  // Current value
				  //var sp = document.getElementById("${fieldHtmlId}");
				 // sp.value = "${field.value}";
		        	  
                 }
             }
          }
	   	});
	   	
		var dsLocalArray = new YAHOO.util.LocalDataSource(emailsAddressArray);
        var myAutoComp = new YAHOO.widget.AutoComplete("${fieldHtmlId}","${fieldHtmlId}-Container", dsLocalArray);
        myAutoComp.delimChar = ";";
        //con questa funziona riesco a prendere le voci che ho selezionato
        var vociSelezionate = function(sType, aArgs){
        	var myAc = aArgs[0];
        	var elLI = aArgs[1];
        	var oData = aArgs[2];
        	
        	//alert("myAc: " + myAc + " eliLI:" + elLI + " oData: " + oData); 	
        } 
        myAutoComp.itemSelectEvent.subscribe(vociSelezionate); 
		
    
}, this);
//]]></script>