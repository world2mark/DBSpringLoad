package com.auracoda.dbspringload.Workloads.Student;

// https://www.youtube.com/watch?v=9SGDpanrc8U
// @Amigoscode
//
// http://localhost:8080/api/v1/student
//

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BusinessLogicService {

    private final DataRepository studentRepostory;

    @Autowired
    public BusinessLogicService(DataRepository studentRepository) {
        this.studentRepostory = studentRepository;
    };

    public List<DataEntity> getAllStudentsOldList() {
        return List.of(
                new DataEntity(
                        "Mark",
                        LocalDate.of(1970, 1, 1),
                        "world2mark"));
    }

    public List<DataEntity> getAllStudents() {
        return studentRepostory.findAll();
    }

    public void saveNewStudent(DataEntity studentObj) {
        System.out.println(studentObj);
        Optional<DataEntity> myStudentByEmail = studentRepostory.findByEmail(studentObj.getEmail());
        if (myStudentByEmail.isPresent()) {
            throw new IllegalStateException("Email is already taken!");
        }
        studentRepostory.save(studentObj);
    }

    public void deleteStudent(UUID myStudentID) {
        final boolean studentExists = studentRepostory.existsById(myStudentID);
        if (!studentExists) {
            throw new IllegalStateException("Student with id \"" + myStudentID + "\" does not exist!");
        }
        ;
        studentRepostory.deleteById(myStudentID);
    }

    @Transactional
    public void updateStudent(UUID myStudentID, String newName, String newEmail) {
        final DataEntity myStudent = studentRepostory.findById(myStudentID).orElseThrow(
                () -> new IllegalStateException("Student with id \"" + myStudentID + "\" does not exist!"));
                
        if (newName != null && newName.length() > 0 && !Objects.equals(newName, myStudent.getName())) {
            myStudent.setName(newName);
        }

        if (newEmail != null && newEmail.length() > 0 && !Objects.equals(newEmail, myStudent.getEmail())) {

            final Optional<DataEntity> foundExistingEmail = studentRepostory.findByEmail(newEmail);
            if(foundExistingEmail.isPresent()) {
                throw new IllegalStateException("Email \"" + newEmail + "\" is already taken!");
            };
            myStudent.setEmail(newEmail);
        }
    }
}
