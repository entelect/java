package za.co.entelect.tools.sample.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * Created by ronald.mthombeni on 2017/09/22.
 */
//https://github.com/kilim/kilim/blob/master/src/kilim/tools/Javac.java
@Entity
public class Person implements Serializable {
    @Id
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
