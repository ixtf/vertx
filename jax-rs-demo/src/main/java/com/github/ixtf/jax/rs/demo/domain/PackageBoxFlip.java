package com.github.ixtf.jax.rs.demo.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.ixtf.jax.rs.demo.domain.data.PackageBoxFlipType;
import com.github.ixtf.persistence.IEntityLoggable;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Collection;
import java.util.Date;

/**
 * 翻包
 *
 * @author jzb 2018-06-22
 */
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
public class PackageBoxFlip implements IEntityLoggable<Operator> {
    @ToString.Include
    @EqualsAndHashCode.Include
    @Getter
    @Setter
    @Id
    @NotBlank
    private String id;
    @Getter
    @Setter
    @Column
    @NotNull
    private PackageBoxFlipType type;
    @Getter
    @Setter
    @Column
    @NotNull
    private PackageBox packageBox;
    @Getter
    @Setter
    @Column
    @Size(min = 1)
    @NotNull
    private Collection<Silk> inSilks;
    @Getter
    @Setter
    @Column
    @Size(min = 1)
    @NotNull
    private Collection<Silk> outSilks;

    @Getter
    @Setter
    @Column
    @NotNull
    private Operator creator;
    @Getter
    @Setter
    @Column(name = "cdt")
    @NotNull
    private Date createDateTime;
    @JsonIgnore
    @Getter
    @Setter
    @Column
    private Operator modifier;
    @JsonIgnore
    @Getter
    @Setter
    @Column(name = "mdt")
    private Date modifyDateTime;
    @JsonIgnore
    @Getter
    @Setter
    @Column
    private boolean deleted;
}
