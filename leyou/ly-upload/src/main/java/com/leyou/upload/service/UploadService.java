package com.leyou.upload.service;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Service
public class UploadService {


    Logger logger = LoggerFactory.getLogger(UploadService.class);


    //文件的上传对象
    @Autowired
    private FastFileStorageClient storageClient;

    public String uploadImage(MultipartFile file) {
        String url = null;


        try {
            //允许上传的文档的类型
            Set<String> extCT = new HashSet<>();
            extCT.add("image/jpeg");
            extCT.add("image/png");

            //上传文件
            //1,校验文件合法性
            //获取文档类型
            String contentType = file.getContentType();

            //文档的类型没有在允许的范围中
            if (!extCT.contains(contentType)){

                logger.info("文件类型不受支持:{}，请上传普通jpg或png文件",contentType);
                return null;
            }


            //2,校验文件内容的合法性

            //使用图片流来读取图片
            BufferedImage read = ImageIO.read(file.getInputStream());


            //说明图片内容有误，
            if (null==read){

                logger.info("文件的内容不是个图片，别闹");

                return null;

            }


            //3,实现图片的上传

            String originalFilename = file.getOriginalFilename();//asdfdsf.jpg


            String ext = StringUtils.substringAfterLast(originalFilename,".");

            //通过io工具直接把对象转存，需要传入新的地址
            StorePath storePath = storageClient.uploadFile(file.getInputStream(), file.getSize(), ext, null);


            if (null!=storePath){
                return "http://image.leyou.com/"+storePath.getFullPath();//获取文件的唯一地址
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        return null;
    }
}
