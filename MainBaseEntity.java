//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package ir.rbp.nabcore.model.domainmodel;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

@MappedSuperclass
public class MainBaseEntity implements Serializable {
    private static final long serialVersionUID = -4960957756039804651L;
    @Id
    @Column(
        name = "ID",
        updatable = false,
        nullable = false
    )
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "SEQ_GENERATOR"
    )
    private Long id;
    @Column(
        name = "IP"
    )
    private String ip;
    @Version
    @Column(
        name = "VERSION"
    )
    private Long version;

    public MainBaseEntity() {
    }

    public MainBaseEntity(Long id) {
        this.id = id;
    }

    public MainBaseEntity(Long id, Long version) {
        this.id = id;
        this.version = version;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIp() {
        return this.ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Long getVersion() {
        return this.version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            MainBaseEntity that = (MainBaseEntity)o;
            return Objects.equals(this.id, that.id) && Objects.equals(this.ip, that.ip) && Objects.equals(this.version, that.version);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.id, this.ip, this.version});
    }
}
