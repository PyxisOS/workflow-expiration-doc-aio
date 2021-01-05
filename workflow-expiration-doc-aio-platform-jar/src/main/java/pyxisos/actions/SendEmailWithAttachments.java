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
 * @author zarbano@pyxisos.com
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
    
    //SMTP PARAMETERS
    public static final String MAIL_SMTP_HOST = "mail.host";
    public static final String MAIL_SMTP_PORT = "mail.port";
    public static final String MAIL_SMTP_USERNAME = "mail.username";
    public static final String MAIL_SMTP_PASSWORD = "mail.password";
    public static final String MAIL_SMTP_AUTH = "mail.smtp.auth";
    public static final String MAIL_SMTP_FROM_DEFAULT = "mail.from.default";
        
    /**
     * The Alfresco Service Registry that gives access to all public content services in Alfresco.
     */
    private ServiceRegistry serviceRegistry;
    private Properties properties;


    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
        for (String s : new String[]{PARAM_EMAIL_TO_NAME, PARAM_EMAIL_MESSAGE_NAME,PARAM_EMAIL_TEMPLATE_NAME, PARAM_WORKFLOW_PRIORITY, PARAM_WORKFLOW_DUE_DATE, PARAM_WORKFLOW_DESCRIPTION, PARAM_WORKFLOW_PACKAGE}) {
            paramList.add(new ParameterDefinitionImpl(s, DataTypeDefinition.TEXT, false, getParamDisplayLabel(s)));
        }        
    }

    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {
            
        if (serviceRegistry.getNodeService().exists(actionedUponNodeRef) == true) {
            // Get the email properties entered via Share Form
            String to = (String) action.getParameterValue(PARAM_EMAIL_TO_NAME);
            String message_text = (String) action.getParameterValue(PARAM_EMAIL_MESSAGE_NAME);
            String workflowDescription = (String) action.getParameterValue(PARAM_WORKFLOW_DESCRIPTION);
            Integer workflowPriority = (Integer) action.getParameterValue(PARAM_WORKFLOW_PRIORITY);
            Date workflowDueDate = (Date) action.getParameterValue(PARAM_WORKFLOW_DUE_DATE);
            NodeRef template = (NodeRef) action.getParameterValue(PARAM_EMAIL_TEMPLATE_NAME);
            NodeRef bpmPackage = (NodeRef) action.getParameterValue(PARAM_WORKFLOW_PACKAGE);
            List<ChildAssociationRef> childAssocs = serviceRegistry.getNodeService().getChildAssocs(bpmPackage);
        
            //GET SMTP PARAMETERS
            String mail_host = (String)properties.getProperty(MAIL_SMTP_HOST);
            String mail_port = (String)properties.getProperty(MAIL_SMTP_PORT);
            final String mail_username = (String)properties.getProperty(MAIL_SMTP_USERNAME);
            final String mail_password = (String)properties.getProperty(MAIL_SMTP_PASSWORD);
            String mail_auth = (String)properties.getProperty(MAIL_SMTP_AUTH);
            String mail_from_default = (String)properties.getProperty(MAIL_SMTP_FROM_DEFAULT);
            String mail_subject = "Revisione Documentazione";
           

            try {
                // Create mail session
                Properties mailServerProperties = new Properties();
                mailServerProperties = System.getProperties();
                mailServerProperties.put(MAIL_SMTP_HOST, mail_host);
                mailServerProperties.put(MAIL_SMTP_PORT, mail_port);
                mailServerProperties.put(MAIL_SMTP_AUTH, mail_auth);
                mailServerProperties.put(MAIL_SMTP_USERNAME, mail_username);
                mailServerProperties.put(MAIL_SMTP_PASSWORD, mail_password);
                   
                
                Session session = Session.getDefaultInstance(mailServerProperties, new javax.mail.Authenticator() 
				{
					protected PasswordAuthentication getPasswordAuthentication() 
					{
						return new PasswordAuthentication(mail_username,mail_password);
					}
			   	});
                
                session.setDebug(false);

                // Definiamo il messaggio email
                Message message = new MimeMessage(session);
                String fromAddress = mail_from_default;
                message.setFrom(new InternetAddress(fromAddress));
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
                
                message.setSubject(mail_subject);
	
                HashMap<String, Object> model = new HashMap<String, Object>();
                model.put("messaggio", message_text.trim()); 
                model.put("workflowPriority", workflowPriority);
                model.put("workflowDueDate", workflowDueDate);
                model.put("workflowDescription", workflowDescription.trim());
                model.put("data", new Date());
                String bodyText = serviceRegistry.getTemplateService().processTemplate("freemarker",template.toString(), model);   

				message.setContent(bodyText, "text/html; charset=utf-8");
				
                // Create the message part with body text
                BodyPart messageBodyPart = new MimeBodyPart();
               // messageBodyPart.setText(bodyText);
                messageBodyPart.setContent( bodyText, "text/html; charset=utf-8" );
                
                Multipart multipart = new MimeMultipart();
                multipart.addBodyPart(messageBodyPart);

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
    
    
    //SETTER AND GETTER
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }
  
    public void setProperties(Properties properties) {
    	this.properties = properties;
    }
}
