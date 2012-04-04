package pl.net.bluesoft.rnd.processtool.model;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "pt_user_attribute")
public class UserAttribute extends UserAttributesSupport {
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserData user;

    private String key;
    private String value;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "parent_id")
    private UserAttribute parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<UserAttribute> attributes;

    public UserAttribute() {
    }

    public UserAttribute(String key, String value) {
        this(key, value, null, null);
    }

    public UserAttribute(UserAttribute attribute) {
        this(attribute.getKey(), attribute.getValue(), attribute.getUser(), attribute.getParent());
    }

    public UserAttribute(String key, String value, UserData user) {
        this(key, value, user, null);
    }

    public UserAttribute(String key, String value, UserData user, UserAttribute parent) {
        this.user = user;
        this.key = key;
        this.value = value;
        this.parent = parent;
    }

    @Override
    public Set<UserAttribute> getAttributes() {
        return attributes != null ? attributes : (attributes = new HashSet<UserAttribute>());
    }

    public void setAttributes(Set<UserAttribute> attributes) {
        this.attributes = attributes;
    }

    @Override
    public UserData getUser() {
        return user;
    }

    public void setUser(UserData user) {
        this.user = user;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public UserAttribute getParent() {
        return parent;
    }

    public void setParent(UserAttribute parent) {
        this.parent = parent;
    }
}
