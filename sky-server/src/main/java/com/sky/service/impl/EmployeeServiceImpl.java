package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // TODO 后期需要进行md5加密，然后再进行比对
        //进行密码的加密md5
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    @Override
    public void save(EmployeeDTO employeeDto) {
        //1.对请求数据进行封装
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDto,employee);

        //2.添加其他属性值
        employee.setCreateTime(LocalDateTime.now());
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
        //1表示正常用户
        employee.setStatus(1);
        employee.setUpdateTime(LocalDateTime.now());

        //todo后续获得当前的用户id,在threadlocal中获得
        employee.setCreateUser(BaseContext.getCurrentId());
        employee.setUpdateUser(BaseContext.getCurrentId());
        //3.将数据存入数据库
        employeeMapper.save(employee);
    }

    @Override
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        //1.使用第三方插件实现分页查询(获取从前端传入的页码和页面的大小),返回的数据是page类型，page类型其实就是集合类型
        //就是放到了threadlocal中，动态的拼接到mapper中
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());

        //2.在mapper层中实现查询
        Page<Employee> page= employeeMapper.pageQuery(employeePageQueryDTO);

        //3.封装好数据进行返回
        long total = page.getTotal();
        List<Employee> result = page.getResult();

        return new PageResult(total,result);
    }

    @Override
    public void updateStatus(Integer status, Long id) {
        //1.新建原本的数据对象
        Employee employee = Employee.builder()
                .id(id)
                .status(status)
                .build();

        //2.在数据层进行更新
        employeeMapper.update(employee);
    }

}
