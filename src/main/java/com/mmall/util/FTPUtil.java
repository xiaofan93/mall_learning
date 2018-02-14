package com.mmall.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * @author fan
 * @date 2018/1/28 21:34
 */
@Slf4j
public class FTPUtil {


    private static String ftpIp = PropertiesUtil.getProperty("ftp.server.ip");
    private static String ftpUser = PropertiesUtil.getProperty("ftp.user");
    private static String ftpPwd = PropertiesUtil.getProperty("ftp.pass");

    private FTPUtil(String ip, int port, String user, String pwd) {
        this.ip = ip;
        this.port = port;
        this.user = user;
        this.pwd = pwd;
    }

    //对外只暴露这个方法
    public static boolean uploadFile(List<File> fileList) throws IOException {
        FTPUtil ftpUtil = new FTPUtil(ftpIp,21,ftpUser,ftpPwd);
        log.info("开始连接ftp服务器");
        boolean result =  ftpUtil.uploadFile("img",fileList);
        log.info("开始连接ftp服务器,结束上传，上传结果:{}");
       System.out.println("4444444444444444444444444444");
        return result;
    }

    private boolean uploadFile(String remotePath,List<File> fileList) throws IOException {
        boolean upload = true;
        FileInputStream fis = null;
        //连接ftp服务器
        if (connectServer(this.ip, this.port, this.user, this.pwd)) {
            try {
               // System.out.println(upload);
                ftpClient.changeWorkingDirectory(remotePath);
                ftpClient.setBufferSize(1024);
                ftpClient.setControlEncoding("utf-8");
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
                ftpClient.enterLocalPassiveMode();
                for (File fileItem : fileList) {
                    fis = new FileInputStream(fileItem);
                    ftpClient.storeFile(fileItem.getName(),fis);
                }
                System.out.println("111111111111111111");
            } catch (IOException e) {
                log.error("文件上传异常", e);
                upload = false;
                e.printStackTrace();
                System.out.println("222222222222222222222222");
            } finally {
                System.out.println("3333333333333333333333333");
                fis.close();
                ftpClient.disconnect();
            }
        }
        return upload;
    }

    private boolean connectServer(String ip,int port,String user,String pwd) {
        boolean isSuccess = false;
        //初始化
         ftpClient = new FTPClient();
        try {
            ftpClient.connect(ip,port);
            isSuccess = ftpClient.login(user,pwd);
            System.out.println(isSuccess);
        } catch (IOException e) {
            log.error("连接FTP服务器异常",e);
        }
        return isSuccess;
    }



    private String ip;
    private int port;
    private String user;
    private String pwd;
    private FTPClient ftpClient;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public FTPClient getFtpClient() {
        return ftpClient;
    }

    public void setFtpClient(FTPClient ftpClient) {
        this.ftpClient = ftpClient;
    }
}
