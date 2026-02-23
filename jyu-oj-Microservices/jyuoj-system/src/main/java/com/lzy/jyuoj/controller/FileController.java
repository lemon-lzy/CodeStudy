package sspu.zzx.sspuoj.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sspu.zzx.sspuoj.common.BaseResponse;
import sspu.zzx.sspuoj.common.ErrorCode;
import sspu.zzx.sspuoj.common.ResultUtils;
import sspu.zzx.sspuoj.exception.BusinessException;
import sspu.zzx.sspuoj.utils.file.minio.MinioUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("/file")
public class FileController
{

    @Autowired
    MinioUtils minioService;

    //列表
    @GetMapping("/minio/list")
    public BaseResponse<List<String>> list()
    {
        List<String> strings = minioService.listObjects();
        return ResultUtils.success(strings);
    }

    //删除
    @PutMapping("/minio/delete")
    public BaseResponse<Boolean> delete(@RequestParam String filename)
    {
        minioService.deleteObject(filename);
        return ResultUtils.success(true);
    }

    //上传文件
    @SaIgnore
    @PostMapping("/minio/upload")
    public BaseResponse<String> upload(@RequestParam("file") MultipartFile file)
    {
        try
        {
            InputStream is = file.getInputStream(); //得到文件流
            String fileName = file.getOriginalFilename(); //文件名
            String newFileName = System.currentTimeMillis() + "." + StringUtils.substringAfterLast(fileName, ".");
            String contentType = file.getContentType();  //类型
            minioService.uploadObject(is, newFileName, contentType);
            return ResultUtils.success(newFileName);
        } catch (Exception e)
        {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "上传失败");
        }
    }

    //下载minio服务的文件
    @GetMapping("/minio/download")
    public void download(@RequestParam String filename, HttpServletResponse response)
    {
        try
        {
            InputStream fileInputStream = minioService.getObject(filename);
            // todo 完善文件命名逻辑
            String newFileName = System.currentTimeMillis() + "." + StringUtils.substringAfterLast(filename, ".");
            response.setHeader("Content-Disposition", "attachment;filename=" + newFileName);
            response.setContentType("application/force-download");
            response.setCharacterEncoding("UTF-8");
            IOUtils.copy(fileInputStream, response.getOutputStream());
        } catch (Exception e)
        {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "下载失败");
        }
    }

    //获取minio文件的下载地址
    @GetMapping("/minio/getHttpUrl")
    public BaseResponse<String> getHttpUrl(@RequestParam("filename") String filename, @RequestParam("permanent") Boolean permanent)
    {
        try
        {
            String url = minioService.getObjectUrl(filename, true);
            return ResultUtils.success(url);
        } catch (Exception e)
        {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, e.getMessage());
        }
    }


}

