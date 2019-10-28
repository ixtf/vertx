package com.github.ixtf.jax.rs.demo.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author jzb 2018-06-22
 */
@Data
public class EntityDTO implements Serializable {
    @NotBlank
    private String id;
}
