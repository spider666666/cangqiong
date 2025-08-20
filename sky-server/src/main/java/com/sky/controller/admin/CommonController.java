package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/admin/common")
@Api("通用接口")//用来上传文件
public class CommonController {

    @Autowired
    public AliOssUtil aliOssUtil;

    //由于业务逻辑比较简单，可以直接书写在业务层
    @ApiOperation("文件的上传")
    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file){
        log.debug("进行文件的上传......");
        //1.对文件的名字进行重写
        String filename= file.getOriginalFilename();
        //1.1会获取文件后缀(.png)

        String extend = filename.substring(filename.lastIndexOf("."));

        //1.2使用uuid
        String newFileName = UUID.randomUUID().toString() + extend;

        //2.调用工具包上传文件
        String path = null;
        try {
            path = aliOssUtil.upload(file.getBytes(), newFileName);
            log.debug("文件路径为：" + path);
            //3.返回文件路径
            return Result.success(path);
        } catch (IOException e) {
            log.error("文件上传失败:",e);
        }

        return Result.error("未知错误");

    }

}
