package pl.net.bluesoft.lot.casemanagement.model;

import org.hibernate.annotations.Index;
import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;

import javax.persistence.*;

/**
 * Created by pkuciapski on 2014-04-18.
 */
@MappedSuperclass
public abstract class AbstractCaseAttribute extends PersistentEntity {
    @Column(name = "key")
    private String key;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = Case.CASE_ID)
    private Case caseInstance;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Case getCase() {
        return caseInstance;
    }

    public void setCase(Case caseInstance) {
        this.caseInstance = caseInstance;
    }
}