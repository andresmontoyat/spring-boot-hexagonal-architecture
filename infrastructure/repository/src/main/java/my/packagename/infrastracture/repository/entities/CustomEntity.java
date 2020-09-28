package my.packagename.infrastracture.repository.entities;

import lombok.Data;
import my.packagename.infrastracture.repository.entities.support.Entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "table_name")
public class CustomEntity extends Entities<Long> {

    @Column(name = "name", nullable = false, length = 20)
    private String name;
}
