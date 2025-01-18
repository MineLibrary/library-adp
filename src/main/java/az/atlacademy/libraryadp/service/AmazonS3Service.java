package az.atlacademy.libraryadp.service;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;

import az.atlacademy.libraryadp.exception.AmazonS3Exception;
import az.atlacademy.libraryadp.model.dto.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AmazonS3Service 
{
    private final AmazonS3 s3Client; 

    @Value("${application.aws.bucket-name}")
    private String bucketName; 

    public BaseResponse<Void> uploadFile(String key, File file)
    {
        PutObjectRequest request = new PutObjectRequest(bucketName, key, file);
        s3Client.putObject(request);

        log.info("File {} uploaded to S3 bucket {}", key, bucketName);

        return BaseResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("File uploaded successfully.")
                .success(true)
                .build(); 
    }

    public BaseResponse<byte[]> getFile(String key)
    {
        try
        {
            byte[] fileBytes = s3Client.getObject(bucketName, key).getObjectContent().readAllBytes();

            log.info("File {} retrieved from S3 bucket {}", key, bucketName);

            return BaseResponse.<byte[]>builder()
                    .success(true)
                    .data(fileBytes)
                    .message("File retrieved successfully.")
                    .status(HttpStatus.OK.value())
                    .build();
        }
        catch (IOException e)
        {
            throw new AmazonS3Exception("An error occured : " + e.getMessage());
        }
    }

    public BaseResponse<Void> deleteFile(String key) 
    {
        s3Client.deleteObject(bucketName, key);

        log.info("File {} deleted from S3 bucket {}", key, bucketName);
    
        return BaseResponse.<Void>builder()
                .success(true)
                .message("File deleted successfully.")
                .status(HttpStatus.OK.value())
                .build();
    }
    
}
