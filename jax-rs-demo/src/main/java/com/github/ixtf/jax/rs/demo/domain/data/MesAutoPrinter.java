package com.github.ixtf.jax.rs.demo.domain.data;

import com.github.ixtf.jax.rs.demo.dto.EntityDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;

/**
 * @author jzb 2018-11-22
 */
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class MesAutoPrinter extends EntityDTO {
    @NotBlank
    private String name;
}
