package com.atguigu.gmall.manage.util;

import com.atguigu.gmall.constants.ManageConstant;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class ManageUploadUtil {
    public static String imgUploda(MultipartFile multipartFile){

        try {
            ClientGlobal.init("tracker.conf");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }

        TrackerClient trackerClient = new TrackerClient();

        TrackerServer trackerServer = null;
        try {
            trackerServer = trackerClient.getConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

        StorageClient storageClient = new StorageClient(trackerServer, null);

        //获取上传文件的原始文件名
        String originalFilename = multipartFile.getOriginalFilename();

        //截取文件名的后缀
        String extenFIleName = originalFilename.substring(originalFilename.lastIndexOf('.') + 1);
        String[] bytes = null;
        try {
             bytes = storageClient.upload_appender_file(multipartFile.getBytes(), extenFIleName, null);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }
        //设置fdfs服务器地址
        String fdfsUrl = ManageConstant.imgUrl;
        //获取组名
        String groupName = bytes[0];
        //获取文件名
        String fileName = bytes[1];

        String imgUrl = fdfsUrl + "/" + groupName + "/" + fileName;

        return imgUrl;
    }

}
