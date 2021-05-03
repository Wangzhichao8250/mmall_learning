package com.mmall.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author achao
 * @create 2020/8/15
 */
public interface IFileService {
    String upLoad(MultipartFile file, String path);
}
