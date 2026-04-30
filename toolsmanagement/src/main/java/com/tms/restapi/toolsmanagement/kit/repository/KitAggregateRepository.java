package com.tms.restapi.toolsmanagement.kit.repository;

import com.tms.restapi.toolsmanagement.kit.model.KitAggregate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KitAggregateRepository extends JpaRepository<KitAggregate, Long> {
}
