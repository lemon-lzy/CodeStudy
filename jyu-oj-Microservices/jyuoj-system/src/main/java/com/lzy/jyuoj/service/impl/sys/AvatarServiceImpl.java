package sspu.zzx.sspuoj.service.impl.sys;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sspu.zzx.sspuoj.common.BaseResponse;
import sspu.zzx.sspuoj.controller.FileController;
import sspu.zzx.sspuoj.service.AvatarService;
import sspu.zzx.sspuoj.utils.AdaptiveAvatarGeneratorUtils;

import javax.annotation.Resource;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * @version 1.0
 * @Author ZZX
 * @Date 2023/11/18 20:44
 */
@Service
public class AvatarServiceImpl implements AvatarService
{
    @Resource
    private FileController fileController;

    @Override
    public String getDefaultAvatarByFullName(String fullName) throws IOException
    {
        // 获取image对象
        BufferedImage image = AdaptiveAvatarGeneratorUtils.generateAdaptiveAvatar(fullName, false);
        // 转换成MultiFile
        String fileName = fullName + ".png";
        MultipartFile file = AdaptiveAvatarGeneratorUtils.convertToMultipartFile(image, fileName);
        // 上传至minio
        BaseResponse<String> upload = fileController.upload(file);
        String newFileName = upload.getData();
        // 获取文件地址并返回
        BaseResponse<String> res = fileController.getHttpUrl(newFileName, true);
        return res.getData();
    }
}