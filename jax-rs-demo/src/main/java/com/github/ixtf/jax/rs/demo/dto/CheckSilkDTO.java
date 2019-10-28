package com.github.ixtf.jax.rs.demo.dto;

import com.github.ixtf.jax.rs.demo.domain.data.SilkCarPosition;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;

/**
 * @author jzb 2018-12-19
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CheckSilkDTO extends SilkCarPosition {
    @NotBlank
    private String code;
}
