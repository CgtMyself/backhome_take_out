package com.imback.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.imback.common.BaseContext;
import com.imback.common.Result;
import com.imback.entity.Employee;
import com.imback.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登录
     * @param request 用来返回session
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public Result<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        //1.对密码进行MD5加密
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);
        if(emp == null){
            //用户名不存在
            return Result.error("登陆失败");
        }
        if(!password.equals(emp.getPassword())){
            //密码错误
            return Result.error("登陆失败");
        }
        //查看员工状态
        if(emp.getStatus() == 0){
            return Result.error("账号已禁用");
        }
        //登陆成功，将员工id缓存到session
        request.getSession().setAttribute("employee",emp.getId());
        return Result.success(emp);
    }

    /**
     *员工退出登录
     * @param request 清除缓存到session中的员工id
     * @return
     */
    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request){
        //清除缓存到session中的员工id
        request.getSession().removeAttribute("employee");
        return Result.success("退出成功");
    }

    /**
     * 新增员工
     * @param request
     * @param emp
     * @return
     */
    @PostMapping
    public Result<String> addEmp(HttpServletRequest request,@RequestBody Employee emp){
        //1.设置初始密码，并进行MD5加密
        emp.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes(StandardCharsets.UTF_8)));

        /*Long empId = (Long)request.getSession().getAttribute("employee"); //从session中获取登录用户的Id
        emp.setCreateTime(LocalDateTime.now());
        emp.setUpdateTime(LocalDateTime.now());
        emp.setCreateUser(empId);
        emp.setUpdateUser(empId);*/

        employeeService.save(emp);
        return Result.success("新增成功");
    }

    /**
     *  查询员工
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public Result<Page> page(int page,int pageSize,String name){
        log.info("page = {},pageSize = {},name = {}",page,pageSize,name);
        //分页构造器
       Page pageInfo = new Page(page,pageSize);
        //条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper();
        //添加过滤条件
        queryWrapper.like(!StringUtils.isEmpty(name), Employee::getName, name);
        //添加排序条件
        queryWrapper.orderByDesc(Employee::getCreateTime);
        //执行查询
        employeeService.page(pageInfo,queryWrapper);
        return Result.success(pageInfo);
    }

    /**
     * 根据Id修改员工信息
     * @param request
     * @param emp
     * @return
     */
    @PutMapping
    public Result<String> update(HttpServletRequest request,@RequestBody Employee emp){

        /*Long empId = (Long)request.getSession().getAttribute("employee");
        emp.setUpdateTime(LocalDateTime.now());
        emp.setUpdateUser(empId);*/
        employeeService.updateById(emp);
        return Result.success("修改成功");
    }

    /**
     * 根据Id查询员工信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<Employee> getById(@PathVariable Long id){
        log.info("id = {}",id);
        Employee emp = employeeService.getById(id);
        return emp != null? Result.success(emp):Result.error("员工不存在");
    }
}
