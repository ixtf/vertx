package com.github.ixtf.jax.rs.demo.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.ixtf.jax.rs.demo.domain.data.RoleType;
import com.github.ixtf.persistence.IOperator;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.Collection;

/**
 * @author jzb 2018-06-22
 */
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
public class Operator implements IOperator {
    @ToString.Include
    @EqualsAndHashCode.Include
    @Getter
    @Setter
    @Id
    @NotBlank
    private String id;
    @ToString.Include
    @Getter
    @Setter
    @Column
    @NotBlank
    private String name;
    @Getter
    @Setter
    @Column
    private String hrId;
    @Getter
    @Setter
    @Column
    private String oaId;
    @Getter
    @Setter
    @Column
    private String phone;
    @Getter
    @Setter
    @Column
    private boolean admin;
    @JsonIgnore
    @Getter
    @Setter
    @Column
    private Collection<OperatorGroup> groups;
    @JsonIgnore
    @Getter
    @Setter
    @Column
    private Collection<RoleType> roles;
    @JsonIgnore
    @Getter
    @Setter
    @Column
    private Collection<Permission> permissions;
    @JsonIgnore
    @Getter
    @Setter
    @Column
    private boolean deleted;

    @PostPersist
    @PostUpdate
    private void test() {
        System.out.println(this.getClass() + "PostPersist   PostUpdate");
    }

}
