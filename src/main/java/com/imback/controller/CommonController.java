package com.imback.controller;

import com.imback.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

/**
 * 文件上传和下载
 */

@Slf4j
@RestController
@RequestMapping("/common")
public class CommonController {

    @Value("${backhome.path}")
    private String basePath;

    /**
     * 文件上传
     * @param file //必须和前台传递的参数名一致
     * @return
     */
    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file) {
        //file是临时文件，如果不指定转存的位置，那么本次请求后临时文件会被删除
        log.info(file.toString());
        //1.获取原始文件名，然后截取后缀
        String originalFilename = file.getOriginalFilename();
        //截取后缀 例如：.jpg
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

        //2.使用UUID生成新的文件名，防止文件名相同后者覆盖前者
        String fileName = UUID.randomUUID().toString() + suffix;

        //3.创建目录对象
        File dir = new File(basePath);
        //判断当前目录是否存在
        if(!dir.exists()){
            //不存在创建一个目录
            dir.mkdir();
        }

        //4.将临时文件转存到指定位置
        try {
            file.transferTo(new File(basePath + fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Result.success(fileName);
    }

    /**
     * 文件下载
     * @param name
     * @param response
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response){
        try {
            //1.通过输入流读取文件内容
            FileInputStream fileInputStream = new FileInputStream(new File(basePath + name));
            //2.通过输出流将文件写回浏览器
            ServletOutputStream outputStream = response.getOutputStream();

            response.setContentType("image/jpeg");

            int len = 0;
            byte[] bytes = new byte[1024];
            while((len = fileInputStream.read(bytes)) != -1){
                outputStream.write(bytes,0,len);
                outputStream.flush();
            }

            //3.关闭资源
            fileInputStream.close();
            outputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
