package com.auracoda.dbspringload.Workloads.Student;

import java.util.UUID;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;


@Repository
public interface DataRepository extends JpaRepository<DataEntity, UUID> {

    @Query("select s from data_entity s where s.email = ?1")
    Optional<DataEntity> findByEmail(String email);
}
