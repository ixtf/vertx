package com.github.ixtf.jax.rs.demo.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

/**
 * @author jzb 2018-07-30
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SilkCarRecordDTO extends EntityDTO {
    @NotNull
    private EntityDTO silkCar;
}
