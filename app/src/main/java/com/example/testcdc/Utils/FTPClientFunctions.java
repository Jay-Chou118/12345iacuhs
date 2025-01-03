package com.example.testcdc.Utils;

import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.FileInputStream;

public class FTPClientFunctions {

    private static final String TAG = "MICAN_TFTP";

    private FTPClient ftpClient = null; // FTP客户端

    /**
     * 连接到FTP服务器
     *
     * @param host     ftp服务器域名或ip地址 ，注意：前缀不要带'ftp://'字符
     * @param username 访问用户名
     * @param password 访问密码
     * @param port     端口
     * @return 是否连接成功
     */
    public boolean ftpConnect(String host, String username, String password, int port) {
        try {
            ftpClient = new FTPClient();
            Log.e(TAG, "connecting to the ftp server " + host + " ：" + port);
            // 进行连接,连接失败会抛出异常
            ftpClient.connect(host, port);
            // 根据返回的状态码，判断链接是否建立成功
            if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                Log.e(TAG, "login to the ftp server");
                boolean status = ftpClient.login(username, password);
                Log.e(TAG,"status " + status);
                /*
                 * 设置文件传输模式
                 * 避免一些可能会出现的问题，在这里必须要设定文件的传输格式。
                 * 在这里我们使用BINARY_FILE_TYPE来传输文本、图像和压缩文件。
                 */
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                ftpClient.enterLocalPassiveMode();
                return status;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error: could not connect to host " + host + e.toString());
        }
        return false;
    }

    /**
     * 断开ftp服务器连接
     *
     * @return 断开结果
     */
    public boolean ftpDisconnect() {
        // 判断空指针
        if (ftpClient == null) {
            return true;
        }

        // 断开ftp服务器连接
        try {
            ftpClient.logout();
            ftpClient.disconnect();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error occurred while disconnecting from ftp server.");
        }
        return false;
    }

    /**
     * ftp 文件上传
     *
     * @param srcFilePath  源文件目录
     * @param desFileName  文件名称
     * @param desDirectory 目标文件
     * @return 文件上传结果
     */
    public boolean ftpUpload(String srcFilePath, String desFileName, String desDirectory) {
        boolean status = false;
        try {
            Log.i(TAG,"=====start upload " + srcFilePath +"=====");
            Log.i(TAG,"destName: " + desFileName);
            FileInputStream srcFileStream = new FileInputStream(srcFilePath);
            status = ftpClient.storeFile(desFileName, srcFileStream);
            srcFileStream.close();
            Log.i(TAG,"============end upload  ==========");
            return status;
        } catch (Exception e) {
            Log.e(TAG, "upload failed: " + e.getLocalizedMessage());
        }
        return status;
    }

    /**
     * ftp 更改目录
     * @param path 更改的路径
     * @return 更改是否成功
     */
    public boolean ftpChangeDir(String path) {
        boolean status = false;
        try {
            status = ftpClient.changeWorkingDirectory(path);
        } catch (Exception e) {
            Log.e(TAG, "change directory failed: " + e.getLocalizedMessage());
        }
        return status;
    }

}