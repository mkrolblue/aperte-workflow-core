package org.aperteworkflow.service;


import org.aperteworkflow.service.fault.AperteWebServiceError;
import org.aperteworkflow.util.ContextUtil;
import org.aperteworkflow.util.AperteErrorCodes;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ReturningProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.hibernate.ResultsPageWrapper;
import pl.net.bluesoft.rnd.processtool.model.*;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessQueueConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateAction;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.soap.SOAPException;

import java.util.*; 

import static org.aperteworkflow.util.ContextUtil.withContext;
import static org.aperteworkflow.util.HibernateBeanUtil.fetchHibernateData; 

/**
 * Most of WebMethods works, some of them are taged as (exclude=true), because they allow for too much interference in the aperet workflow  data.
 * To make them work again just delete WebMethod annotaion.
 * @author tlipski@bluesoft.net.pl  
 * @author kkolodziej@bluesoft.net.pl
 */
@WebService
public class AperteWorkflowDataServiceImpl implements AperteWorkflowDataService { 

	@Override
    @WebMethod  (exclude=true)
    public long saveProcessInstance(@WebParam(name="processInstance")final ProcessInstance processInstance) {
        return withContext(new ReturningProcessToolContextCallback<Long>() {
            @Override
            public Long processWithContext(ProcessToolContext ctx) {
                return ctx.getProcessInstanceDAO().saveProcessInstance(processInstance);
            }
        });
    }
	
	@Override
    @WebMethod
	public List<ProcessStateAction> getAllActionsListFromDefinition(
			@WebParam(name = "definitionName") final String definitionName) throws  AperteWebServiceError {
		final ProcessDefinitionConfig definition = getActiveConfigurationByKey(definitionName);
		return withContext(new ReturningProcessToolContextCallback<List<ProcessStateAction>>() {
			
			@Override
			public List<ProcessStateAction> processWithContext(ProcessToolContext ctx) {
				
				return fetchHibernateData(ctx.getProcessStateActionDAO().getActionsListByDefinition(definition));
			}
		});

	}
	
	@Override
    @WebMethod
	public List<ProcessStateAction> getAvalivableActionForProcess( 
			@WebParam(name = "internalId") final String internalId) throws AperteWebServiceError {

		 final ProcessInstance instance = getProcessInstanceByInternalId(internalId);
		 final ProcessDefinitionConfig definition = instance.getDefinition();
		
		 
		return withContext(new ReturningProcessToolContextCallback<List<ProcessStateAction>>() {
			
			@Override
			public List<ProcessStateAction> processWithContext(ProcessToolContext ctx) {
				String state = instance.getState();
				if(state==null || state.isEmpty()){//TODO its for compatibility with 1.X aperte data. In future its should be removed
					if(instance.getStatus().equals(ProcessStatus.NEW)){
						List<BpmTask> bpmTasks = getSession(ctx).findProcessTasks(instance, ctx);
						state=bpmTasks.get(0).getTaskName();
					}
					
				 } 
				return fetchHibernateData(ctx.getProcessStateActionDAO().getActionsBasedOnStateAndDefinitionId(state, definition.getId())); 
			}
		});
 
	}
	
	@Override
    @WebMethod  (exclude=true)
	public List<ProcessStateAction> getActionsListByNameFromInstance(
			@WebParam(name = "internalId") final String internalId,@WebParam(name = "actionName") final String actionName) throws AperteWebServiceError {

		ProcessInstance instanceByInternalId = getProcessInstanceByInternalId(internalId);
		final ProcessDefinitionConfig definition = instanceByInternalId.getDefinition();
		return withContext(new ReturningProcessToolContextCallback<List<ProcessStateAction>>() {
			
			@Override
			public List<ProcessStateAction> processWithContext(ProcessToolContext ctx) {
				
				return fetchHibernateData(ctx.getProcessStateActionDAO().getActionByNameFromDefinition(definition, actionName));
			}
		});

	}

	@Override
	@WebMethod (exclude=true)
    public ProcessInstance getProcessInstance(@WebParam(name="id")final long id) {
        return withContext(new ReturningProcessToolContextCallback<ProcessInstance>() {
            @Override
            public ProcessInstance processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(ctx.getProcessInstanceDAO().getProcessInstance(id));
            }
        });
    }

	@Override
	@WebMethod (exclude=true)
    public List<ProcessInstance> getProcessInstances(@WebParam(name="ids")final Collection<Long> ids) {
        return withContext(new ReturningProcessToolContextCallback<List<ProcessInstance>>() {
            @Override
            public List<ProcessInstance> processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(ctx.getProcessInstanceDAO().getProcessInstances(ids));
            }
        });
    }

	@Override
    @WebMethod
    public ProcessInstance getProcessInstanceByInternalId(@WebParam(name="internalId")final String internalId) throws  AperteWebServiceError {
         ProcessInstance processInstance = withContext(new ReturningProcessToolContextCallback<ProcessInstance>() {
            @Override
            public ProcessInstance processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(ctx.getProcessInstanceDAO().getProcessInstanceByInternalId(internalId));
            }
        });
        
         if(processInstance==null){
        	
        	throw new AperteWebServiceError(AperteErrorCodes.PROCESS.getErrorCode(),AperteErrorCodes.PROCESS.getMessage());
        }
         return processInstance;
        
    }

	@Override
	@WebMethod (exclude=true)
    public ProcessInstance getProcessInstanceByExternalId(@WebParam(name="externalId")final String externalId) {
        return withContext(new ReturningProcessToolContextCallback<ProcessInstance>() {
            @Override
            public ProcessInstance processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(ctx.getProcessInstanceDAO().getProcessInstanceByExternalId(externalId));
            }
        });
    }

	@Override
	@WebMethod (exclude=true)
    public List<ProcessInstance> findProcessInstancesByKeyword(@WebParam(name="key")final String key, @WebParam(name="processType")final String processType) {
        return withContext(new ReturningProcessToolContextCallback<List<ProcessInstance>>() {
            @Override
            public List<ProcessInstance> processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(ctx.getProcessInstanceDAO().findProcessInstancesByKeyword(key, processType));
            }
        }); 
    }

	@Override 
	@WebMethod
	public Map<String, ProcessInstance> getProcessInstanceByInternalIdMap(@WebParam(name="internalIds")final Collection<String> internalIds) {
		 return withContext(new ReturningProcessToolContextCallback<Map<String, ProcessInstance>>() {
	            @Override
	            public Map<String, ProcessInstance> processWithContext(ProcessToolContext ctx) {
	                return fetchHibernateData(ctx.getProcessInstanceDAO().getProcessInstanceByInternalIdMap(internalIds));
	            }
	        });
	}

	@Override
	@WebMethod
    public void deleteProcessInstance(@WebParam(name="internalId")final String internalId) {
        withContext(new ReturningProcessToolContextCallback() {
            @Override
            public Object processWithContext(ProcessToolContext ctx) {
                ctx.getProcessInstanceDAO().deleteProcessInstanceByInternalId(internalId);
                return null;
            }
        }); 
    }

	@Override
    @WebMethod
    public Collection<ProcessInstanceLog> getUserHistory(@WebParam(name="userLogin")final String userLogin,
                                                         @WebParam(name="startDate")final Date startDate,
                                                         @WebParam(name="endDate")final Date endDate) throws AperteWebServiceError {
		final UserData loadUserByLogin = findUser(userLogin);
        return withContext(new ReturningProcessToolContextCallback<Collection<ProcessInstanceLog>>() {
            @Override
            public Collection<ProcessInstanceLog> processWithContext(ProcessToolContext ctx)  {
            	
                return fetchHibernateData(ctx.getProcessInstanceDAO().getUserHistory(loadUserByLogin, startDate, endDate));
            }
        });
    }

	
	@Override
	    @WebMethod
	    public UserData findUser(@WebParam(name="userLogin")final String userLogin) throws AperteWebServiceError {
	         UserData userData = withContext(new ReturningProcessToolContextCallback<UserData>() {
	            @Override
	            public UserData processWithContext(ProcessToolContext ctx) {
	                return fetchHibernateData(ctx.getUserDataDAO().loadUserByLogin(userLogin));
	            }
	        });
	         if (userLogin!= null && userData==null){
     			throw new AperteWebServiceError(AperteErrorCodes.USER.getErrorCode(), AperteErrorCodes.USER.getMessage());
     			
     		} 
     		return userData;
	    }
	
	
	
	@Override 
    @WebMethod
    public ProcessInstanceSimpleAttribute setSimpleAttribute(@WebParam(name="key")final String key,@WebParam(name="newValue")
    final String newValue,@WebParam(name="internalId")final String internalId) throws AperteWebServiceError {
		final ProcessInstance processInstance = getProcessInstanceByInternalId(internalId);
		return withContext(new ReturningProcessToolContextCallback<ProcessInstanceSimpleAttribute>() {
            @Override
            public ProcessInstanceSimpleAttribute processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(ctx.getProcessInstanceSimpleAttributeDAO().setSimpleAttribute(key,  newValue, processInstance));
            }
        });
    } 
	
	@Override
    @WebMethod
    public String getSimpleAttributeValue(@WebParam(name="key")final String key,@WebParam(name="internalId")final String internalId) throws AperteWebServiceError {
		final ProcessInstance processInstance = getProcessInstanceByInternalId(internalId);
		return withContext(new ReturningProcessToolContextCallback<String>() { 
            @Override
            public String processWithContext(ProcessToolContext ctx) {
                return ctx.getProcessInstanceSimpleAttributeDAO().getSimpleAttributeValue(key, processInstance);
            }
        });
    }
	
	
	@Override 
    @WebMethod
    public  List<ProcessInstanceSimpleAttribute> getSimpleAttributesList(@WebParam(name="internalId")final String internalId) throws AperteWebServiceError {
		final ProcessInstance processInstance = getProcessInstanceByInternalId(internalId);
		return withContext(new ReturningProcessToolContextCallback<List<ProcessInstanceSimpleAttribute>>() {
            @Override
            public List<ProcessInstanceSimpleAttribute> processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(ctx.getProcessInstanceSimpleAttributeDAO().getSimpleAttributesList(processInstance));
            }
        });
    }
	
	
	
	
	
	@Override
	@WebMethod 
    public UserData findOrCreateUser(@WebParam(name="user")final UserData user) {
        return withContext(new ReturningProcessToolContextCallback<UserData>() {
            @Override
            public UserData processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(ctx.getUserDataDAO().findOrCreateUser(user)); 
            }
        });
    }

	@Override
    @WebMethod
    public Collection<ProcessInstance> searchProcesses(@WebParam(name="filter")final String filter,
                                                       @WebParam(name="offset")final int offset,
                                                       @WebParam(name="limit")final int limit,
                                                       @WebParam(name="onlyRunning")final boolean onlyRunning,
                                                       @WebParam(name="userRoles")final String[] userRoles,
                                                       @WebParam(name="assignee")final String assignee,
                                                       @WebParam(name="queues")final String... queues) {
        return withContext(new ReturningProcessToolContextCallback<Collection<ProcessInstance>>() {
            @Override
            public Collection<ProcessInstance> processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(ctx.getProcessInstanceDAO().searchProcesses(filter, offset, limit, onlyRunning, userRoles, assignee, queues));
            }
        });
    }

	@Override
    @WebMethod
    public Collection<ProcessInstance> getUserProcessesBetweenDatesByUserLogin(@WebParam(name="userLogin")final String userLogin,
                                                                 @WebParam(name="minDate")final Calendar minDate,
                                                                 @WebParam(name="maxDate")final Calendar maxDate) throws AperteWebServiceError {
		
		final UserData user = findUser(userLogin);
        return withContext(new ReturningProcessToolContextCallback<Collection<ProcessInstance>>() {
            @Override
            public Collection<ProcessInstance> processWithContext(ProcessToolContext ctx) {
            	
                return fetchHibernateData(ctx.getProcessInstanceDAO().getUserProcessesBetweenDates(user, minDate, maxDate));
            }
        });
    }
	
	
	@Override
	@WebMethod (exclude=true)
    public Collection<ProcessInstance> getUserProcessesAfterDate(@WebParam(name="user")final UserData user,
                                                                 @WebParam(name="minDate")final Calendar minDate) {
        return withContext(new ReturningProcessToolContextCallback<Collection<ProcessInstance>>() {
            @Override
            public Collection<ProcessInstance> processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(ctx.getProcessInstanceDAO().getUserProcessesAfterDate(user, minDate));
            }
        });
    }

	@Override
	@WebMethod (exclude=true)
    public ResultsPageWrapper<ProcessInstance> getRecentProcesses(@WebParam(name="user")final UserData user,
                                                                  @WebParam(name="minDate")final Calendar minDate,
                                                                  @WebParam(name="offset")final Integer offset,
                                                                  @WebParam(name="limit")final Integer limit) {
        return withContext(new ReturningProcessToolContextCallback<ResultsPageWrapper<ProcessInstance>>() {
            @Override
            public ResultsPageWrapper<ProcessInstance> processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(ctx.getProcessInstanceDAO().getRecentProcesses(user, minDate, offset, limit));
            }
        });
    }

//	@Override
//	@WebMethod
//    public ResultsPageWrapper<ProcessInstance> getProcessInstanceByInternalIdMapWithFilter(@WebParam(name="internalIds")final Collection<String> internalIds,
//                                                                                           @WebParam(name="filter")final ProcessInstanceFilter filter,
//                                                                                           @WebParam(name="offset")final Integer offset,
//                                                                                           @WebParam(name="limit")final Integer limit) {
//        return withContext(new ReturningProcessToolContextCallback<ResultsPageWrapper<ProcessInstance>>() {
//            @Override
//            public ResultsPageWrapper<ProcessInstance> processWithContext(ProcessToolContext ctx) {
//                return fetchHibernateData(ctx.getProcessInstanceDAO().getProcessInstanceByInternalIdMapWithFilter(internalIds, filter, offset, limit));
//            }
//        });
//    }

	@Override
	@WebMethod (exclude=true)
    public Collection<ProcessDefinitionConfig> getAllConfigurations() {
        return withContext(new ReturningProcessToolContextCallback<Collection<ProcessDefinitionConfig>>() {
            @Override
            public Collection<ProcessDefinitionConfig> processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(ctx.getProcessDefinitionDAO().getAllConfigurations());
            }
        });
    }

	@Override
	@WebMethod 
    public Collection<ProcessDefinitionConfig> getActiveConfigurations() {
        return withContext(new ReturningProcessToolContextCallback<Collection<ProcessDefinitionConfig>>() {
            @Override
            public Collection<ProcessDefinitionConfig> processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(ctx.getProcessDefinitionDAO().getActiveConfigurations());
            }
        });
    }

	@Override
	@WebMethod (exclude=true)
    public ProcessDefinitionConfig getActiveConfigurationByKey(@WebParam(name="key")final String key) {
        return withContext(new ReturningProcessToolContextCallback<ProcessDefinitionConfig>() {
            @Override
            public ProcessDefinitionConfig processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(ctx.getProcessDefinitionDAO().getActiveConfigurationByKey(key));
            }
        });
    }

	@Override
	@WebMethod (exclude=true)
    public Collection<ProcessQueueConfig> getQueueConfigs() {
        return withContext(new ReturningProcessToolContextCallback<Collection<ProcessQueueConfig>>() {
            @Override
            public Collection<ProcessQueueConfig> processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(ctx.getProcessDefinitionDAO().getQueueConfigs());
            }
        });
    }

	@Override
	@WebMethod (exclude=true)
    public ProcessStateConfiguration getProcessStateConfiguration(@WebParam(name="task")final BpmTask task) {
        return withContext(new ReturningProcessToolContextCallback<ProcessStateConfiguration>() {
            @Override
            public ProcessStateConfiguration processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(ctx.getProcessDefinitionDAO().getProcessStateConfiguration(task));
            }
        });
    }

	@Override
	@WebMethod (exclude=true)
    public void updateOrCreateProcessDefinitionConfig(@WebParam(name="cfg")final ProcessDefinitionConfig cfg) {
        withContext(new ReturningProcessToolContextCallback<ProcessStateConfiguration>() {
            @Override
            public ProcessStateConfiguration processWithContext(ProcessToolContext ctx) {
                ctx.getProcessDefinitionDAO().updateOrCreateProcessDefinitionConfig(cfg);
                return null;
            }
        });
    }

	@Override
	@WebMethod (exclude=true)
    public void setConfigurationEnabled(@WebParam(name="cfg")final ProcessDefinitionConfig cfg, @WebParam(name="enabled")final boolean enabled) {
        withContext(new ReturningProcessToolContextCallback<ProcessStateConfiguration>() {
            @Override
            public ProcessStateConfiguration processWithContext(ProcessToolContext ctx) {
                ctx.getProcessDefinitionDAO().setConfigurationEnabled(cfg, enabled);
                return null;
            }
        });
    }

	@Override
	@WebMethod (exclude=true)
    public Collection<ProcessDefinitionConfig> getConfigurationVersions(@WebParam(name="cfg")final ProcessDefinitionConfig cfg) {
        return withContext(new ReturningProcessToolContextCallback<Collection<ProcessDefinitionConfig>>() {
            @Override
            public Collection<ProcessDefinitionConfig> processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(ctx.getProcessDefinitionDAO().getConfigurationVersions(cfg));
            }
        });
    }

	@Override
	@WebMethod (exclude=true)
    public void updateOrCreateQueueConfigs(@WebParam(name="cfgs")final Collection<ProcessQueueConfig> cfgs) {
        withContext(new ReturningProcessToolContextCallback<ProcessStateConfiguration>() {
            @Override
            public ProcessStateConfiguration processWithContext(ProcessToolContext ctx) {
                ctx.getProcessDefinitionDAO().updateOrCreateQueueConfigs(cfgs);
                return null;
            }
        });
    }

	@Override
	@WebMethod (exclude=true)
    public void removeQueueConfigs(@WebParam(name="cfgs")final Collection<ProcessQueueConfig> cfgs) {
        withContext(new ReturningProcessToolContextCallback<ProcessStateConfiguration>() {
            @Override
            public ProcessStateConfiguration processWithContext(ProcessToolContext ctx) {
                ctx.getProcessDefinitionDAO().removeQueueConfigs(cfgs);
                return null;
            }
        });
    }

	@Override
	@WebMethod (exclude=true)
    public List<String> getAvailableLogins(@WebParam(name="filter")final String filter) {
        return withContext(new ReturningProcessToolContextCallback<List<String>>() {
            @Override
            public List<String> processWithContext(ProcessToolContext ctx) {
                return getSession(ctx)
                        .getAvailableLogins(filter);
            }
        });
    }

	@Override
    @WebMethod
    public byte[] getProcessLatestDefinition(@WebParam(name="bpmDefinitionKey")final String bpmDefinitionKey,
                                             @WebParam(name="processName")final String processName) {
        return ContextUtil.withContext(new ReturningProcessToolContextCallback<byte[]>() {
            @Override
            public byte[] processWithContext(ProcessToolContext ctx) {
                return getSession(ctx)
                        .getProcessLatestDefinition(bpmDefinitionKey, processName);
            }
        });
    }
	
	

	@Override
    @WebMethod
    public byte[] getProcessDefinition(@WebParam(name="internalId")final String internalId) throws  AperteWebServiceError {
		final ProcessInstance processInstanceByInternalId = getProcessInstanceByInternalId(internalId);
        return withContext(new ReturningProcessToolContextCallback<byte[]>() {
            @Override
            public byte[] processWithContext(ProcessToolContext ctx) {
            	
                return getSession(ctx)
                        .getProcessDefinition(processInstanceByInternalId);
            }
        });
    }

	@Override
    @WebMethod
    public byte[] getProcessMapImage(@WebParam(name="internalId")final String internalId) throws  AperteWebServiceError {
		final ProcessInstance processInstanceByInternalId = getProcessInstanceByInternalId(internalId);
        return withContext(new ReturningProcessToolContextCallback<byte[]>() {
            @Override
            public byte[] processWithContext(ProcessToolContext ctx) {           	
                return getSession(ctx)
                        .getProcessMapImage(processInstanceByInternalId);
            }
        });
    }

	private ProcessToolBpmSession getSession(ProcessToolContext ctx) {
		return getSession(ctx, null);
	}

	private ProcessToolBpmSession getSession(ProcessToolContext ctx, UserData user) {
		return ctx.getProcessToolSessionFactory().createSession(user, new HashSet<String>());
	}
}