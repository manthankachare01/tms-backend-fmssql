package com.tms.restapi.toolsmanagement.issuance.repository;

import com.tms.restapi.toolsmanagement.issuance.model.ReturnRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReturnRepository extends JpaRepository<ReturnRecord, Long> {
	// Find return records by the associated issuance's location
	java.util.List<ReturnRecord> findByIssuance_Location(String location);

	// Find return records by the associated issuance's trainer id
	java.util.List<ReturnRecord> findByIssuance_TrainerId(Long trainerId);

	// Find return records by both issuance location and trainer id
	java.util.List<ReturnRecord> findByIssuance_LocationAndIssuance_TrainerId(String location, Long trainerId);
}
