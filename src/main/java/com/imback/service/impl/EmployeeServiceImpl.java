package com.imback.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.imback.dao.EmployeeMapper;
import com.imback.entity.Employee;
import com.imback.service.EmployeeService;
import org.springframework.stereotype.Service;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {
}
