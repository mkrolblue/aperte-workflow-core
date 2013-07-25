package pl.net.bluesoft.rnd.pt.ext.usersubstitution.step;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import pl.net.bluesoft.rnd.processtool.model.BpmStep;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserSubstitution;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.util.lang.Formats;

import static pl.net.bluesoft.rnd.processtool.ProcessToolContext.Util.getThreadProcessToolContext;

@AliasName(name = "HandleSubstitutionAcceptanceStep")
public class HandleSubstitutionAcceptance implements ProcessToolProcessStep {
    private static final Logger logger = Logger.getLogger(HandleSubstitutionAcceptance.class.getName());

    @Override
    public String invoke(BpmStep step, Map params) throws Exception {
        ProcessInstance processInstance = step.getProcessInstance();
        try {
			UserSubstitution userSubstitution = new UserSubstitution();

			userSubstitution.setUserLogin(processInstance.getCreatorLogin());
            userSubstitution.setDateFrom(Formats.parseShortDate(processInstance.getSimpleAttributeValue("dateFrom")));
            userSubstitution.setDateTo(Formats.parseShortDate(processInstance.getSimpleAttributeValue("dateTo")));
			userSubstitution.setUserSubstituteLogin(processInstance.getSimpleAttributeValue("userSubstitute"));

            getThreadProcessToolContext().getUserSubstitutionDAO().saveOrUpdate(userSubstitution);
            logger.warning("Added substitution for user " + userSubstitution.getUserLogin());
            return STATUS_OK;
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            return STATUS_ERROR;
        }
    }
}
