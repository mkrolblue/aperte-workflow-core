package pl.net.bluesoft.rnd.processtool.bpm;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.model.UserData;

import java.util.Collection;

/**
 * @author tlipski@bluesoft.net.pl
 */
public interface ProcessToolSessionFactory {
	ProcessToolBpmSession createSession(UserData user);

	ProcessToolBpmSession createSession(String userLogin);
	ProcessToolBpmSession createSession(String userLogin, Collection<String> roles);

    ProcessToolBpmSession createAutoSession();
	ProcessToolBpmSession createAutoSession(ProcessToolContext ctx);
	ProcessToolBpmSession createAutoSession(Collection<String> roles);
}
