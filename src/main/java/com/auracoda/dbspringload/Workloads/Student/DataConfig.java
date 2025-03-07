package com.auracoda.dbspringload.Workloads.Student;

/*
 * 
 * ORM Model for a simulation of Students using standard Java Spring ORM methodologies
 * 
 */

import java.time.LocalDate;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataConfig {

    final boolean SaveStudents = false;

    @Bean
    CommandLineRunner commandLineRunner(DataRepository studentRepository) {
        // This example runs on startup when the SaveStudents flag is true
        return args -> {
            if (SaveStudents) {
                DataEntity conf1 = new DataEntity("configName11", LocalDate.of(2001, 4, 5), "conf-email-1");
                DataEntity conf2 = new DataEntity("configName22", LocalDate.of(2002, 4, 5), "conf-email-2");
                DataEntity conf3 = new DataEntity("configName33", LocalDate.of(2003, 4, 5), "conf-email-3");
                studentRepository.saveAll(List.of(conf1, conf2, conf3));
            }
        };
    }
}
