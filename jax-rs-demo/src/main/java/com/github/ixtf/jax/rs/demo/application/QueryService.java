package com.github.ixtf.jax.rs.demo.application;

import com.github.ixtf.jax.rs.demo.domain.*;
import com.github.ixtf.jax.rs.demo.dto.EntityByCodeDTO;
import com.github.ixtf.jax.rs.demo.dto.EntityDTO;
import org.reactivestreams.Publisher;

import java.security.Principal;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * @author jzb 2019-05-02
 */
public interface QueryService {

    <T> CompletionStage<Optional<T>> find(Class<T> clazz, String id);

    default CompletionStage<Optional<Login>> findLogin(String id) {
        return find(Login.class, id);
    }

    default CompletionStage<Optional<Login>> findLogin(Operator operator) {
        return find(Login.class, operator.getId());
    }

    default CompletionStage<Optional<Operator>> findOperator(String id) {
        return find(Operator.class, id);
    }

    default CompletionStage<Optional<Operator>> find(Principal principal) {
        return findOperator(principal.getName());
    }

    Publisher<Silk> listSilkByCode(Collection<EntityByCodeDTO> dtos);

    default CompletionStage<Optional<SilkCar>> findSilkCar(String id) {
        return find(SilkCar.class, id);
    }

    default CompletionStage<Optional<SilkCar>> findSilkCar(EntityDTO dto) {
        return findSilkCar(dto.getId());
    }

    CompletionStage<Optional<Silk>> findSilkByCode(String code);

    default CompletionStage<Optional<PackageBox>> findPackageBox(EntityDTO dto) {
        return find(PackageBox.class, dto.getId());
    }

    CompletionStage<Optional<PackageBox>> findPackageBoxByCode(String code);

    default CompletionStage<Optional<PackageBox>> findPackageBox(EntityByCodeDTO dto) {
        return findPackageBoxByCode(dto.getCode());
    }
}
