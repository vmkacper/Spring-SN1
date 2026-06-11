package com.example.sn1.rest;


import com.example.sn1.DTOs.EmployeeDto;
import com.example.sn1.models.Employee;
import com.example.sn1.repositories.EmployeeRepository;
import com.sun.net.httpserver.Headers;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    private EmployeeRepository employeeRepository;
    private ModelMapper modelMapper;

    public EmployeeController(EmployeeRepository employeeRepository, ModelMapper modelMapper) {
        this.employeeRepository = employeeRepository;
        this.modelMapper = modelMapper;
    }

    private EmployeeDto convertToDto(Employee employee) {
        return modelMapper.map(employee, EmployeeDto.class);
    }

    private Employee convertToEntity(EmployeeDto employeeDto) {
        return modelMapper.map(employeeDto, Employee.class);
    }

    @GetMapping
    public ResponseEntity<Collection<EmployeeDto>> getEmployees() {
        List<Employee> allEmployees = employeeRepository.findAll();

        List<EmployeeDto> result = allEmployees.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/{empId}")
    public ResponseEntity<EmployeeDto> getEmployeeById(@PathVariable Long empId) {
        Optional<Employee> emp = employeeRepository.findById(empId);

        if (emp.isPresent()){
            EmployeeDto empDto = convertToDto(emp.get());
            return new ResponseEntity<>(empDto, HttpStatus.OK);
        }else{
            return new ResponseEntity<>((HttpHeaders) null, HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    public ResponseEntity saveNewEmployee(@RequestBody EmployeeDto empDto){
        Employee entity = convertToEntity(empDto);

        employeeRepository.save(entity);
        HttpHeaders headers = new HttpHeaders();

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(entity.getId())
                .toUri();
        headers.add("Location", location.toString());

        return new ResponseEntity(headers, HttpStatus.CREATED);
    }

    @PutMapping("{empId}")
    public ResponseEntity updateEmployee(@PathVariable Long empId,
                                         @RequestBody EmployeeDto employeeDto) {
        Optional<Employee> currentEmployee = employeeRepository.findById(empId);
        if (currentEmployee.isPresent()){
            employeeDto.setId(empId);
            Employee entity = convertToEntity(employeeDto);
            employeeRepository.save(entity);
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        }else{
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{empId}")
    public ResponseEntity deleteEmployee(@PathVariable Long empId) {
        if(employeeRepository.existsById(empId)){
            employeeRepository.deleteById(empId);
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        }else {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
    }
}
