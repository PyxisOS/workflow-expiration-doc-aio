Alfresco.forms.validation.checkEmailValidity = function checkEmailValidity(field, args, event, form, silent, message) {
	
	var valid = true;
    if(Alfresco.logger.isDebugEnabled()) Alfresco.logger.debug(field.value);
	//controlla che il campo non sia vuoto
	valore = YAHOO.lang.trim(field.value).length !== 0;
	//alert(event);
	if (valore) {
		arrayEmails = field.value.split(";");
		for(var i = 0; i < arrayEmails.length; i++){
			var re = /^([\w-]+(?:\.[\w-]+)*)@((?:[\w-]+\.)*\w[\w-]{0,66})\.([a-z]{2,6}(?:\.[a-z]{2})?)$/i;
		   //valid = re.test(field.value);
		    var mail = YAHOO.lang.trim(arrayEmails[i]);
		    //se la stringa non è vuota
		    if(mail) {
		    	valid = re.test(mail);
		    	if (!valid) return false; 
		    	//if(valid) YAHOO.util.Dom.setStyle(field.id, "border", "2px solid blue");  
		    	//if(Alfresco.logger.isDebugEnabled()) Alfresco.logger.debug(mail + " : " + valid);
		    }
		}
	}
	return valid;
	//console.log("stai funzionando");
} 
