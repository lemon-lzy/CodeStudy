package sspu.zzx.sspuoj.utils;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Random;

public class AdaptiveAvatarGeneratorUtils
{
    public static BufferedImage generateAdaptiveAvatar(String fullName, boolean isSave)
    {
        // 设置图像边长
        int imageSize = 200;

        // 随机生成纯色背景
        Color backgroundColor = getRandomColor();

        // 创建一个正方形的图像，大小为 imageSize x imageSize 像素
        BufferedImage image = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_RGB);

        // 获取图像上下文
        Graphics graphics = image.getGraphics();

        // 设置背景色
        graphics.setColor(backgroundColor);
        graphics.fillRect(0, 0, imageSize, imageSize);

        // 使用微软雅黑字体，初始字体大小为图像高度的一半
        Font font = new Font("Microsoft YaHei", Font.PLAIN, imageSize / 2);

        // 动态调整字体大小以适应图像边长
        FontMetrics fontMetrics = graphics.getFontMetrics(font);
        int textWidth = fontMetrics.stringWidth(fullName);
        int textHeight = fontMetrics.getHeight();
        while (textWidth > imageSize || textHeight > imageSize)
        {
            font = new Font("Microsoft YaHei", Font.PLAIN, font.getSize() - 1);
            graphics.setFont(font);
            fontMetrics = graphics.getFontMetrics(font);
            textWidth = fontMetrics.stringWidth(fullName);
            textHeight = fontMetrics.getHeight();
        }

        // 计算文本在图像中的位置，使其居中显示
        int textX = (imageSize - textWidth) / 2;
        int textY = (imageSize - textHeight) / 2 + fontMetrics.getAscent();

        // 在图像上绘制头像元素
        graphics.setColor(Color.WHITE);
        graphics.drawString(fullName, textX, textY);

        // 保存生成的头像
        if (isSave)
        {
            try
            {
                ImageIO.write(image, "png", new File("doc/generated_avatar.png"));
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        // 释放资源
        graphics.dispose();
        return image;
    }

    private static Color getRandomColor()
    {
        Random random = new Random();
        return new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }

    /**
     * image转file
     * @param image
     * @param fileName
     * @return
     * @throws IOException
     */
    public static MultipartFile convertToMultipartFile(BufferedImage image, String fileName) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);

        return new MockMultipartFile(fileName, fileName, "image/png", baos.toByteArray());
    }

    public static void main(String[] args)
    {
        generateAdaptiveAvatar("待上传",true);
    }
}
