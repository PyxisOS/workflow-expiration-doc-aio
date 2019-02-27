package pyxisos.scripts;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SendEmailToExternalUsers implements ExecutionListener{
	
	private static final String EMAIL_FROM = "info@grupposamed.com";
	//private static final String EMAIL_TO = "lucia.zarbano@gmail.com";
	
	private static final String EMAIL_SUBJECT = "Test Email Class Java";
	private static final String EMAIL_TEXT = "stiamo testando un po di cose";
	
	//variabili del workflow
	private static final String EMAIL_TO = "gswe_externalUsersEmails";
	
	private static Log logger = LogFactory.getLog(SendEmailToExternalUsers.class);

	/* taken from ActivitiScriptBase.java */
    protected ServiceRegistry getServiceRegistry() {
        ProcessEngineConfigurationImpl config = Context.getProcessEngineConfiguration();
        if (config != null) {
            // Fetch the registry that is injected in the activiti spring-configuration
            ServiceRegistry registry = (ServiceRegistry) config.getBeans().get(ActivitiConstants.SERVICE_REGISTRY_BEAN_KEY);
            if (registry == null) {
                throw new RuntimeException(
                            "Service-registry not present in ProcessEngineConfiguration beans, expected ServiceRegistry with key" + 
                            ActivitiConstants.SERVICE_REGISTRY_BEAN_KEY);
            }
            return registry;
        }
        throw new IllegalStateException("No ProcessEngineCOnfiguration found in active context");
    }

	@Override
	public void notify(DelegateExecution execution) throws Exception {
		String destinatario = (String) execution.getVariable(SendEmailToExternalUsers.EMAIL_TO);
		
		//String bpmDueDate = execution.getVariable(SpedizioneEmailUtentiEsterni.SCADENZA_WORKFLOW);
		//logger.debug("email: " +  bpmDueDate.toString());
		
		//costruisco il body dell'email
		StringBuffer sb = new StringBuffer();
        sb.append("Gentilissimo, ha un documento da visionare in scadenza.");
         
        //sb.append(getLink(task.getId(), "Approve"));
        //sb.append(getLink(task.getId(), "Reject"));
		
		
		//utilizzo il service delle action
		ActionService actionService = getServiceRegistry().getActionService();
		//creo l'azione email
		Action mailAction = actionService.createAction(MailActionExecuter.NAME);
		mailAction.setParameterValue(MailActionExecuter.PARAM_FROM, SendEmailToExternalUsers.EMAIL_FROM);
		mailAction.setParameterValue(MailActionExecuter.PARAM_TO, destinatario);
		mailAction.setParameterValue(MailActionExecuter.PARAM_SUBJECT, SendEmailToExternalUsers.EMAIL_SUBJECT);
		//mailAction.setParameterValue(MailActionExecuter.PARAM_TEXT, SpedizioneEmailUtentiEsterni.EMAIL_TEXT);
		mailAction.setParameterValue(MailActionExecuter.PARAM_TEXT, sb.toString());
		
		//eseguo l'azione
		actionService.executeAction(mailAction, null);
		
		logger.debug("Mail action executed");
        
        return;
		
	}   

}
