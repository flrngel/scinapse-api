package io.scinapse.api.service;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;
import com.amazonaws.services.s3.transfer.model.UploadResult;
import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.api.error.BadRequestException;
import io.scinapse.api.error.ExternalApiCallException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@XRayEnabled
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class ImageUploadService {

    private static final String SCINAPSE_MEDIA_BUCKET = "scinapse-media";
    public static final String PROFILE_IMAGE_PATH = "image/profile/";

    private AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
    private TransferManager tm = TransferManagerBuilder.standard().withS3Client(s3Client).build();

    public String uploadImage(String imagePath, MultipartFile imageFile) {
        if (!StringUtils.startsWithIgnoreCase(imageFile.getContentType(), "image")) {
            throw new BadRequestException("The content type is not supported: " + imageFile.getContentType());
        }
        try {
            String objectKey = imagePath + UUID.randomUUID().toString();

            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType(imageFile.getContentType());
            objectMetadata.setContentLength(imageFile.getSize());

            PutObjectRequest putObjectRequest = new PutObjectRequest(SCINAPSE_MEDIA_BUCKET, objectKey, imageFile.getInputStream(), objectMetadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead);

            Upload upload = tm.upload(putObjectRequest);
            UploadResult uploadResult = upload.waitForUploadResult();
            return uploadResult.getKey();
        } catch (IOException | InterruptedException e) {
            throw new ExternalApiCallException("Fail to upload image: " + e.getMessage());
        }
    }

}
