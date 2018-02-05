package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.mmall.service.IFileService;
import com.mmall.util.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;


/**
 * @author fan
 * @date 2018/1/28 20:58
 */
@Service("iFileService")
public class FileServiceImpl implements IFileService {

    private Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    //springmvc文件上传
    public String upload(MultipartFile file,String path) {
        String fileName = file.getOriginalFilename();
        //获取扩展名
        String fileExtensionName = fileName.substring(fileName.lastIndexOf(".") + 1);
        //构建上传后的文件名
        String uploadFileName = UUID.randomUUID().toString()+ "." +fileExtensionName;
        logger.info("开始上传文件，上传文件的文件名:{},上传的路径:{},新文件名:{}",fileName,path,uploadFileName);

        File fileDir =  new File(path);
        if (!fileDir.exists()) {
            fileDir.setWritable(true);
            fileDir.mkdirs();
        }
       File targetFile =  new File(path,uploadFileName);

        try {
            file.transferTo(targetFile);
             //文件已经上传成功

              //上传到FTP服务器
            FTPUtil.uploadFile(Lists.newArrayList(targetFile));
              //删除upload文件夹下的文件
            targetFile.delete();
        } catch (IOException e) {
            logger.error("文件上传失败",e);
            return null;
        }
        return targetFile.getName();
    }



}
