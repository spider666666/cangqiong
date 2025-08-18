package com.sky.controller.admin;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
@Api(tags = "员工相关的接口")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @PostMapping("/login")
    @ApiOperation(value = "员工登录")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * 退出
     *
     * @return
     */
    @ApiOperation(value = "登出功能")
    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success();
    }

    /**
     * 实现用户的添加
     * @param employeeDto
     * @return
     */

    @PostMapping
    public Result save(@RequestBody EmployeeDTO employeeDto){

        employeeService.save(employeeDto);
        return Result.success();
    }
    /**
     * 实现分页查询实现
     * @param 从前端传过来的一个类
     * @return
     *
     */
    @GetMapping("/page")
    @ApiOperation("员工分页查询")
    //传入泛型（一般返回需要具体的对象数据时，最好是使用泛型的形式）
    public Result<PageResult> pageQuery(EmployeePageQueryDTO employeePageQueryDTO){
        PageResult pageResult = employeeService.pageQuery(employeePageQueryDTO);
        return Result.success(pageResult);

    }

    //实现员工状态的改变
    @PostMapping("/status/{status}")
    @ApiOperation("员工状态的改变")
    public Result startOrStop(@PathVariable("status") Integer status,Long id){
        employeeService.updateStatus(status,id);
        return Result.success();
    }


    /**
     * 实现用户的查询
     * @param id
     * @return employee
     */
    @GetMapping("/{id}")
    @ApiOperation("根据员工id进行查询")
    public Result<Employee> getById(@PathVariable("id") Long id){
        Employee employee = employeeService.getById(id);
        return Result.success(employee);
    }

    /**
     * 修改员工的信息，利用了方法的幂等性，也就是说同一种方法多次请求只会改变一次数据
     * @param employeeDTO
     *
     */
    @PutMapping
    @ApiOperation("修改员工信息")
    public Result update(@RequestBody EmployeeDTO employeeDTO){
        employeeService.update(employeeDTO);
        return Result.success();

    }
}
