package com.github.ixtf.jax.rs.demo.domain.listener;

import com.github.ixtf.persistence.IEntity;

import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;

/**
 * @author jzb 2019-04-30
 */
public class LuceneListener {

    @PostPersist
    @PostUpdate
    private void setLastUpdate(IEntity entity) {
//        final ApplicationEvents applicationEvents = MesAutoConfig.guice(ApplicationEvents.class);
//        final LuceneMessageDTO dto = LuceneMessageDTO.of(Type.INDEX, entity, entity.getId());
//        applicationEvents.fire(dto);
    }

    @PostRemove
    private void setDestroy(IEntity entity) {
//        final ApplicationEvents applicationEvents = MesAutoConfig.guice(ApplicationEvents.class);
//        final LuceneMessageDTO dto = LuceneMessageDTO.of(Type.DELETE, entity, entity.getId());
//        applicationEvents.fire(dto);
    }

}
