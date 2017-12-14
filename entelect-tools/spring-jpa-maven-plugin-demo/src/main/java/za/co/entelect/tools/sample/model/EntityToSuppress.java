package za.co.entelect.tools.sample.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * Created by ronald.mthombeni on 2017/09/22.
 */
@Entity
public class EntityToSuppress implements Serializable {
    @Id
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
