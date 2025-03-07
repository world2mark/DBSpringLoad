package com.auracoda.dbspringload.Workloads.Student;

import java.time.LocalDate;
import java.time.Period;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;


/*
  CREATE TABLE my_students (
      id UUID NOT NULL DEFAULT gen_random_uuid(),
      name STRING NOT NULL,
      dob TIMESTAMPTZ NULL,
      email STRING NOT NULL,
      CONSTRAINT my_students_pkey PRIMARY KEY (id ASC)
  );
 */
@Entity
@Table(name="my_students")
public class DataEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDate dob;

    @Transient
    @Column(nullable = false)
    private Integer age;

    @Column(nullable = false)
    private String email;

    public DataEntity() {
    }

    public DataEntity(String name, LocalDate dob, String email) {
        // this.UUID.randomUUID(), // .toString()??
        this.name = name;
        this.dob = dob;
        this.email = email;
    }

    public UUID getID() {
        return id;
    };

    public String getName() {
        return name;
    }

    public void setName(String newName) {
        name = newName;
    }

    public LocalDate getDOB() {
        return dob;
    }

    public int getAge() {
        return Period.between(dob, LocalDate.now()).getYears();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String newEmail) {
        email = newEmail;
    }

    @Override
    public String toString() {
        final StringBuilder mySB = new StringBuilder();
        mySB.append("Student{");
        mySB.append("id=\'");
        mySB.append(this.id);
        mySB.append("\', name=\'");
        mySB.append(this.name);
        mySB.append("\', email=\'");
        mySB.append(this.email);
        mySB.append("\', dob=\'");
        mySB.append(this.dob);
        mySB.append("\'}");
        return mySB.toString();
    };
}
