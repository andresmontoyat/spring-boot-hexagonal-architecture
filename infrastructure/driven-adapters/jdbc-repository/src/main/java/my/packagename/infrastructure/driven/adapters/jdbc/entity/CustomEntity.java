package my.packagename.infrastructure.driven.adapters.jdbc.entity;

import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/** Row shape of {@code custom}. A null version marks a row that has not been inserted yet. */
@Table("custom")
public record CustomEntity(
    @Id @Column("id") UUID id,
    @Column("name") String name,
    @Version @Column("version") Long version) {}
