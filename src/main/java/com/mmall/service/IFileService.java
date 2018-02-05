package com.mmall.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author fan
 * @date 2018/1/28 20:57
 */
public interface IFileService {

    String upload(MultipartFile file, String path);
}
