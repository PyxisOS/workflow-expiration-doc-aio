/**
 * Copyright (C) 2015 Alfresco Software Limited.
 * <p/>
 * This file is part of the Alfresco SDK Samples project.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pyxisos.actions;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.ServiceRegistry;
//import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import javax.activation.DataHandler;
import javax.activation.MimetypesFileTypeMap;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.util.ByteArrayDataSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.*;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.StringWriter;
import java.io.Writer;


import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.service.cmr.repository.ChildAssociationRef; 

/**
 * Alfresco Repository Action that can send emails with file attachments.
 *
 * @author martin.bergljung@alfresco.com
 */
public class SendEmailWithAttachments extends ActionExecuterAbstractBase {
    private static Log logger = LogFactory.getLog(SendEmailWithAttachments.class);

    public static final String PARAM_EMAIL_TO_NAME = "to";
    //public static final String PARAM_EMAIL_SUBJECT_NAME = "subject";
    public static final String PARAM_EMAIL_MESSAGE_NAME = "message_text";
    public static final String PARAM_EMAIL_TEMPLATE_NAME = "template";
    public static final String PARAM_WORKFLOW_PRIORITY = "workflowPriority";
    public static final String PARAM_WORKFLOW_DUE_DATE = "workflowDueDate";
    public static final String PARAM_WORKFLOW_DESCRIPTION = "workflowDescription";
    public static final String PARAM_WORKFLOW_PACKAGE = "workflowPackage";

    /**
     * The Alfresco Service Registry that gives access to all public content services in Alfresco.
     */
    private ServiceRegistry serviceRegistry;
    //private FileFolderService fileFolderService;
   

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }
    
    /*public void setFileFolderService(FileFolderService fileFolderService){
    	this.fileFolderService = fileFolderService;
    }*/
    

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
        for (String s : new String[]{PARAM_EMAIL_TO_NAME, PARAM_EMAIL_MESSAGE_NAME,PARAM_EMAIL_TEMPLATE_NAME, PARAM_WORKFLOW_PRIORITY, PARAM_WORKFLOW_DUE_DATE, PARAM_WORKFLOW_DESCRIPTION, PARAM_WORKFLOW_PACKAGE}) {
            paramList.add(new ParameterDefinitionImpl(s, DataTypeDefinition.TEXT, false, getParamDisplayLabel(s)));
        }
        //paramList.add(new ParameterDefinitionImpl(PARAM_WORKFLOW_PACKAGE2, DataTypeDefinition.ANY, false, "workflowPackage2"));
        
    }

    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {
            
        if (serviceRegistry.getNodeService().exists(actionedUponNodeRef) == true) {
            // Get the email properties entered via Share Form
            String to = (String) action.getParameterValue(PARAM_EMAIL_TO_NAME);
            //String subject = (String) action.getParameterValue(PARAM_EMAIL_SUBJECT_NAME);
            String message_text = (String) action.getParameterValue(PARAM_EMAIL_MESSAGE_NAME);
            String workflowDescription = (String) action.getParameterValue(PARAM_WORKFLOW_DESCRIPTION);
            Integer workflowPriority = (Integer) action.getParameterValue(PARAM_WORKFLOW_PRIORITY);
            Date workflowDueDate = (Date) action.getParameterValue(PARAM_WORKFLOW_DUE_DATE);
            NodeRef template = (NodeRef) action.getParameterValue(PARAM_EMAIL_TEMPLATE_NAME);
            //NodeRef workflowPackage = (NodeRef) action.getParameterValue(PARAM_WORKFLOW_PACKAGE);
            //Serializable workflowPackage2 = action.getParameterValue(PARAM_WORKFLOW_PACKAGE);
            
            //creo la lista dei figli associati al bpm_package
            NodeRef bpmPackage = (NodeRef) action.getParameterValue(PARAM_WORKFLOW_PACKAGE);
            List<ChildAssociationRef> childAssocs = serviceRegistry.getNodeService().getChildAssocs(bpmPackage);
        
        

            // Get document filename
            /*Serializable filename = serviceRegistry.getNodeService().getProperty(
                    actionedUponNodeRef, ContentModel.PROP_NAME);
            if (filename == null) {
                throw new AlfrescoRuntimeException("Il nome del file è vuoto");
            }
            String documentName = (String) filename;*/
            
            

            try {
                // Create mail session
                Properties mailServerProperties = new Properties();
                mailServerProperties = System.getProperties();
                mailServerProperties.put("mail.smtp.host", "smtp.grupposamed.com");
                mailServerProperties.put("mail.smtp.port", "587");
                mailServerProperties.put("mail.smtp.auth", "true");
                mailServerProperties.put("mail.smtp.username", "refertionline@grupposamed.com");
                mailServerProperties.put("mail.smtp.password", "Referti1");
                
               // Session session = Session.getDefaultInstance(mailServerProperties, null);
                
                
                Session session = Session.getDefaultInstance(mailServerProperties, new javax.mail.Authenticator() 
				{
					protected PasswordAuthentication getPasswordAuthentication() 
					{
						return new PasswordAuthentication("refertionline@grupposamed.com","Referti1");
					}
			   	});
                
                session.setDebug(false);

                // Definiamo il messaggio email
                Message message = new MimeMessage(session);
                String fromAddress = "info@grupposamed.com";
                message.setFrom(new InternetAddress(fromAddress));
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
                //message.setRecipients(Message.RecipientType.CC, InternetAddress.parse("zarbano@pyxisos.com,lucia.zarbano@gmail.com"));
                //message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("zarbano@pyxisos.com, lucia.zarbano@gmail.com"));
                message.setSubject("Revisione Documentazione");
                
                
                
				//sistemo il body
				//String bodyText = "Gentile utente, le è stato assegnato un compito relativo a della documentazione da visionare che trova in allegato.";
				//bodyText = bodyText +"</br> La scadenza del compito è prevista per la seguente data:";
				//bodyText = bodyText + "</br> La priorità del compito è la seguente:  </br> Il messaggio diretto a lei è il seguente: </br> Distinti Saluti </br> Lo staff del Gruppo Samed" ;
				
				
				/*NodeRef repository = getCompanyHome();
				String pathtemplate = "Dizionario Dati/Modelli di e-mail/Notifica Workflow/wf-email-cstm_it.html.ftl";
            	List<String> pathElements = Arrays.asList(StringUtils.split(pathtemplate, '/'));
 				NodeRef templateRef = this.fileFolderService.resolveNamePath(companyHome, pathElements).getNodeRef();*/
 				

				
                HashMap<String, Object> model = new HashMap<String, Object>();
                model.put("messaggio", message_text.trim()); 
                model.put("workflowPriority", workflowPriority);
                model.put("workflowDueDate", workflowDueDate);
                model.put("workflowDescription", workflowDescription.trim());
                model.put("data", new Date());
                String bodyText = serviceRegistry.getTemplateService().processTemplate("freemarker",template.toString(), model);   
 				
				
				
				/*HashMap<String, String> map = new HashMap<String, String>();
				map.put("messaggio", "Ok");
				bodyText = this.serviceRegistry.getTemplateService().processTemplate(template,map);*/
				
				
				message.setContent(bodyText, "text/html; charset=utf-8");


				
                // Create the message part with body text
                BodyPart messageBodyPart = new MimeBodyPart();
               // messageBodyPart.setText(bodyText);
                messageBodyPart.setContent( bodyText, "text/html; charset=utf-8" );
                
                Multipart multipart = new MimeMultipart();
                multipart.addBodyPart(messageBodyPart);

                // Create the Attachment part
                //
                //  Get the document content bytes
                /*byte[] documentData = getDocumentContentBytes(actionedUponNodeRef, documentName);
                if (documentData == null) {
                    throw new AlfrescoRuntimeException("Document content is null");
                }*/
                
                
                //  Attach document
               /* messageBodyPart = new MimeBodyPart();
                messageBodyPart.setDataHandler(new DataHandler(new ByteArrayDataSource(
                        documentData, new MimetypesFileTypeMap().getContentType(documentName))));
                messageBodyPart.setFileName(documentName);
                multipart.addBodyPart(messageBodyPart); */   
                


				//attach multiple document
				for (ChildAssociationRef childAssoc : childAssocs) {
					NodeRef childNodeRef = childAssoc.getChildRef();
					//logger.debug(childNodeRef.getId());
					Serializable childNodeRefFilename = serviceRegistry.getNodeService().getProperty(
						childNodeRef, ContentModel.PROP_NAME);
					if (childNodeRefFilename == null) {
						throw new AlfrescoRuntimeException("Il nome del file è vuoto");
					}	
					String childNodeRefName = (String) childNodeRefFilename;
					logger.debug(childNodeRefName);
				
					//  Get the document content bytes
					byte[] childNodeRefData = getDocumentContentBytes(childNodeRef, childNodeRefName);
					if (childNodeRefData == null) {
						throw new AlfrescoRuntimeException("Document content is null");
					}
				
					 //  Attach document 2
					messageBodyPart = new MimeBodyPart();
					messageBodyPart.setDataHandler(new DataHandler(new ByteArrayDataSource(
							childNodeRefData, new MimetypesFileTypeMap().getContentType(childNodeRefName))));
					messageBodyPart.setFileName(childNodeRefName);
					multipart.addBodyPart(messageBodyPart);
    			}
				

                // Put parts in message
                message.setContent(multipart);

                // Send mail
                Transport.send(message);

                // Set status on node as "sent via email"
                Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
                properties.put(ContentModel.PROP_ORIGINATOR, fromAddress);
                properties.put(ContentModel.PROP_ADDRESSEE, to);
                //properties.put(ContentModel.PROP_SUBJECT, subject);
                properties.put(ContentModel.PROP_SENTDATE, new Date());
                serviceRegistry.getNodeService().addAspect(actionedUponNodeRef, ContentModel.ASPECT_EMAILED, properties);
            } catch (MessagingException me) {
                me.printStackTrace();
                throw new AlfrescoRuntimeException("Could not send email: " + me.getMessage());
            }
        }
    }

    /**
     * Get the content bytes for the document with passed in node reference.
     *
     * @param documentRef      the node reference for the document we want the content bytes for
     * @param documentFilename document filename for logging
     * @return a byte array containing the document content or null if not found
     */
    private byte[] getDocumentContentBytes(NodeRef documentRef, String documentFilename) {
        // Get a content reader
        ContentReader contentReader = serviceRegistry.getContentService().getReader(
                documentRef, ContentModel.PROP_CONTENT);
        if (contentReader == null) {
            logger.error("Content reader was null [filename=" + documentFilename + "][docNodeRef=" + documentRef + "]");

            return null;
        }

        // Get the document content bytes
        InputStream is = contentReader.getContentInputStream();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] documentData = null;

        try {
            byte[] buf = new byte[1024];
            int len = 0;
            while ((len = is.read(buf)) > 0) {
                bos.write(buf, 0, len);
            }
            documentData = bos.toByteArray();
        } catch (IOException ioe) {
            logger.error("Content could not be read: " + ioe.getMessage() +
                    " [filename=" + documentFilename + "][docNodeRef=" + documentRef + "]");
            return null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Throwable e) {
                    logger.error("Could not close doc content input stream: " + e.getMessage() +
                            " [filename=" + documentFilename + "][docNodeRef=" + documentRef + "]");
                }
            }
        }

        return documentData;
    }
}