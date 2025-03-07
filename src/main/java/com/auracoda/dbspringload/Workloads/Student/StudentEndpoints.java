package com.auracoda.dbspringload.Workloads.Student;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping(path = "api/v1/student")
public class StudentEndpoints {

    private final BusinessLogicService studentService;

    @Autowired
    public StudentEndpoints(BusinessLogicService studentService) {
        this.studentService = studentService;
    };

    @GetMapping
    public List<DataEntity> getAllStudents() {
        return studentService.getAllStudents();
    };

    /*
     * Example via CURL
     * curl --header "Content-Type: application/json" \
     * --request POST \
     * --data '{"name":"xyz","dob":"2004-01-01", "email":"mark"}' \
     * http://localhost:8080/api/v1/student
     */
    @PostMapping
    public void saveNewStudent(@RequestBody DataEntity studentDataObj) {
        studentService.saveNewStudent(studentDataObj);
    };

    /*
     * Example via CURL
     * curl --request DELETE
     * http://localhost:8080/api/v1/student/7b0d6005-d0cd-44b0-81db-73b7e09166fc
     */
    @DeleteMapping(path = "{someStudentID}")
    public void deleteStudent(@PathVariable("someStudentID") UUID incomingStudentID) {
        studentService.deleteStudent(incomingStudentID);
    }

    /*
     * Email already taken example via CURL
     * curl --request PUT \
     * "http://localhost:8080/api/v1/student/7b0d6005-d0cd-44b0-81db-73b7e09166fc?newEmail=conf-email-1"
     */
    @PutMapping("{someStudentID}")
    public void putMethodName(
            @PathVariable("someStudentID") UUID incomingStudentID,
            @RequestParam(required = false) String newName,
            @RequestParam(required = true) String newEmail) {

        studentService.updateStudent(incomingStudentID, newName, newEmail);
    }
}
