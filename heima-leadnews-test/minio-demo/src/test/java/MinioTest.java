import com.heima.minio.MinioApplication;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

@SpringBootTest(classes = MinioApplication.class)
@RunWith(SpringRunner.class)
public class MinioTest {
//    @Autowired
//    private FileStorage

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
