package com.github.ixtf.jax.rs.demo.domain.listener;

import com.github.ixtf.jax.rs.demo.domain.DyeingResult;

import javax.persistence.PostPersist;
import javax.persistence.PostRemove;

/**
 * @author jzb 2019-04-30
 */
public class DyeingResultListener {

    @PostPersist
    private void PostPersist(DyeingResult dyeingResult) {
//        final ApplicationEvents applicationEvents = MesAutoConfig.guice(ApplicationEvents.class);
//        final DyeingResultMessageDTO dto = DyeingResultMessageDTO.of(CREATE, dyeingResult.getId());
//        applicationEvents.fire(dto);
    }

    @PostRemove
    private void PostRemove(DyeingResult dyeingResult) {
//        final ApplicationEvents applicationEvents = MesAutoConfig.guice(ApplicationEvents.class);
//        final DyeingResultMessageDTO dto = DyeingResultMessageDTO.of(DELETE, dyeingResult.getId());
//        applicationEvents.fire(dto);
    }

}
