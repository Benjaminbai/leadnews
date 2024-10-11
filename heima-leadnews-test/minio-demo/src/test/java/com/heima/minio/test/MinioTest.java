package com.heima.minio.test;

import com.heima.file.service.FileStorageService;
import com.heima.minio.MinioApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

@SpringBootTest(classes = MinioApplication.class)
@RunWith(SpringRunner.class)
public class MinioTest {
    @Autowired
    private FileStorageService fileStorageService;

    @Test
    public void test() throws FileNotFoundException {
        FileInputStream fileInputStream = new FileInputStream("/Users/benjamin/study/heima-leadnews/heima-leadnews-test/02-list.html");
        String path = fileStorageService.uploadHtmlFile("", "list.html", fileInputStream);
        System.out.println(path);

    }

//    public static void main(String[] args) {
//        try {
//            FileInputStream fileInputStream = new FileInputStream("/Users/benjamin/study/heima-leadnews/heima-leadnews-test/02-list.html");
//
//            MinioClient minioClient = MinioClient.builder().credentials("minio", "minio@123").endpoint("http://localhost:9090/").build();
//            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
//                    .object("02-list.html")
//                    .contentType("text/html")
//                    .bucket("leadnews")
//                    .stream(fileInputStream, fileInputStream.available(), -1)
//                    .build();
//            minioClient.putObject(putObjectArgs);
//            System.out.print("http://localhost:9000/leadnews/02-list.html");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//
//    }
}
