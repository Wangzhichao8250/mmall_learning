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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author achao
 * @create 2020/8/15
 */
@Service
public class FileServiceImpl implements IFileService {

    private Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    public String upLoad(MultipartFile file, String path) {
        String fileName = file.getOriginalFilename();

        //获取扩展名
        String fileExtensionName = fileName.substring(fileName.lastIndexOf(".") + 1);


        //避免文件名重复
        String uploadFileName = UUID.randomUUID().toString() + "." + fileExtensionName;
        logger.info("开始上传文件,上传的文件名{},上传的路径{},新文件名{}", fileName, fileExtensionName, uploadFileName);

        //创建目录
        File fileDir = new File(path);
        if (!fileDir.exists()) {
            fileDir.setWritable(true);
            fileDir.mkdirs();
        }
        File targetFile = new File(path, uploadFileName);

        try {
            file.transferTo(targetFile);    //文件已上传成功
//            FTPUtil.uploadFile(Lists.newArrayList(targetFile)); //TODO 已经上传到ftp服务器上    暂时不用多个
            FTPUtil.uploadFile(targetFile); //已经上传到ftp服务器上  看看单个
            targetFile.delete();    //删除本地upload里的文件
        } catch (IOException e) {
            logger.error("上传文件异常", e.getMessage());
            return null;
        }
        return targetFile.getName();
    }
}
