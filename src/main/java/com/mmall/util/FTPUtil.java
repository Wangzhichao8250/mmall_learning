package com.mmall.util;


import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author achao
 * @create 2020/8/15
 */
public class FTPUtil {

    private static Logger logger = LoggerFactory.getLogger(FTPUtil.class);

    private static String ftpIp = PropertiesUtil.getProperty("ftp.server.ip");
    private static String ftpUser = PropertiesUtil.getProperty("ftp.user");
    private static String ftpPass = PropertiesUtil.getProperty("ftp.pass");

    private String ip;
    private int port;
    private String user;
    private String pwd;
    private FTPClient ftpClient;

    public FTPUtil(String ip, int port, String user, String pwd) {
        this.ip = ip;
        this.port = port;
        this.user = user;
        this.pwd = pwd;
    }

    public static boolean uploadFile(File fileList) throws IOException {
        FTPUtil ftpUtil = new FTPUtil(ftpIp, 21, ftpUser, ftpPass);
        logger.info("开始连接ftp服务器");
        boolean result = ftpUtil.uploadFile("image",fileList);
        logger.info("结束上传,上传结果:{}",result);
        return result;
    }

    private boolean uploadFile(String remotePath, File file) throws IOException {
        boolean uploaded = true;
        FileInputStream fis = null;
        //连接ftp服务器
        if (connectFtpServer(this.ip, this.port, this.user, this.pwd)) {
            //连接成功后进行一系列操作
            try {
                //如果路径不存在则创建！
                if(!ftpClient.changeWorkingDirectory(remotePath)){
                    ftpClient.makeDirectory(remotePath);
                }
                ftpClient.changeWorkingDirectory(remotePath);//切换路径
                ftpClient.setBufferSize(1024);          //设置字节缓冲流
                ftpClient.setControlEncoding("UTF-8");      //设置utf-8存储
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);  //设置二进制
                ftpClient.enterLocalPassiveMode();      //打开被动模式

                /**
                 * 暂时不用多个上传
                 */
//                for (File fileItems : fileList) {
//                    fis = new FileInputStream(fileItems);
//                    ftpClient.storeFile(fileItems.getName(), fis);
//                }
                fis = new FileInputStream(file);
                ftpClient.storeFile(file.getName(),fis);

            } catch (IOException e) {
                logger.error("上传文件异常", e);
                e.printStackTrace();
                uploaded = false;
            } finally {
                fis.close();
                ftpClient.disconnect();
            }
        }else{
            uploaded=false;
        }
        return uploaded;
    }

    private boolean connectFtpServer(String ip, int port, String user, String pwd) {
        boolean isSuccess = false;
        ftpClient = new FTPClient();
        try {
            ftpClient.connect(ip);
            isSuccess = ftpClient.login(user, pwd);

        } catch (IOException e) {
            e.printStackTrace();
            logger.info("连接服务器异常", e);
        }
        return isSuccess;
    }

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
