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
import io.scinapse.api.error.ImageProcessingFailedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.UUID;

@Slf4j
@XRayEnabled
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class ImageUploadService {

    private static final String SCINAPSE_MEDIA_BUCKET = "scinapse-media";
    public static final String PROFILE_IMAGE_PATH = "image/profile/";

    private static final String JPEG = "JPEG";
    private static final String JPEG_MIME = "image/jpeg";
    private static final String JPEG_EXTENSION = ".jpg";

    private AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
    private TransferManager tm = TransferManagerBuilder.standard().withS3Client(s3Client).build();

    public String uploadImage(String imagePath, MultipartFile imageFile) {
        if (!StringUtils.startsWithIgnoreCase(imageFile.getContentType(), "image")) {
            throw new BadRequestException("The content type is not supported: " + imageFile.getContentType());
        }

//        BufferedImage scaledImage = getScaledImage(imageFile);
//        ByteArrayInputStream inputStream = convertToJpegStream(scaledImage);

        ByteArrayInputStream inputStream = convertToJpegStream(imageFile);

        String objectKey = imagePath + UUID.randomUUID().toString() + JPEG_EXTENSION;

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(JPEG_MIME);
        objectMetadata.setContentLength(inputStream.available());

        PutObjectRequest putObjectRequest = new PutObjectRequest(SCINAPSE_MEDIA_BUCKET, objectKey, inputStream, objectMetadata)
                .withCannedAcl(CannedAccessControlList.PublicRead);

        try {
            Upload upload = tm.upload(putObjectRequest);
            UploadResult uploadResult = upload.waitForUploadResult();
            return uploadResult.getKey();
        } catch (InterruptedException e) {
            throw new ExternalApiCallException("Fail to upload image: " + e.getMessage());
        }
    }

    private ByteArrayInputStream convertToJpegStream(MultipartFile file) {
        int scaledLength = getScaledLength(file);

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        try {
            Thumbnails.of(file.getInputStream())
                    .size(scaledLength, scaledLength)
                    .crop(Positions.CENTER)
                    .imageType(BufferedImage.TYPE_INT_RGB)
                    .outputFormat(JPEG)
                    .outputQuality(0.89f) // set efficient quality
                    .toOutputStream(os);
        } catch (IOException e) {
            throw new ImageProcessingFailedException("Cannot process the image file: " + e.getMessage());
        }

        byte[] buffer = os.toByteArray();
        return new ByteArrayInputStream(buffer);
    }

    private int getScaledLength(MultipartFile file) {
        BufferedImage originalImage;
        try {
            originalImage = ImageIO.read(file.getInputStream());
        } catch (IOException e) {
            throw new BadRequestException("Cannot read the image file: " + e.getMessage());
        }

        if (originalImage == null) {
            throw new BadRequestException("Cannot read the image file: " + file.getContentType());
        }

        return Math.min(400, Math.min(originalImage.getWidth(), originalImage.getHeight()));
    }

    private ByteArrayInputStream convertToJpegStream(BufferedImage scaledImage) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        try (ImageOutputStream ios = ImageIO.createImageOutputStream(os)) {

            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(JPEG);
            ImageWriter writer = writers.next();

            writer.setOutput(ios);

            JPEGImageWriteParam param = new JPEGImageWriteParam(null);
            param.setProgressiveMode(ImageWriteParam.MODE_DEFAULT); // make progressive
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(0.89f); // set efficient quality

            try {
                writer.write(null, new IIOImage(scaledImage, null, null), param);
            } finally {
                writer.dispose();
                os.flush();
            }

        } catch (IOException e) {
            throw new ImageProcessingFailedException("Cannot process the image file: " + e.getMessage());
        }

        byte[] buffer = os.toByteArray();
        return new ByteArrayInputStream(buffer);
    }

    private BufferedImage getScaledImage(MultipartFile imageFile) {
        BufferedImage originalImage;
        try {
            originalImage = ImageIO.read(imageFile.getInputStream());
        } catch (IOException e) {
            throw new BadRequestException("Cannot read the image file: " + e.getMessage());
        }

        if (originalImage == null) {
            throw new BadRequestException("Cannot read the image file: " + imageFile.getContentType());
        }

        int scaledWidth = Math.min(400, originalImage.getWidth());
        Image scaledInstance = originalImage.getScaledInstance(scaledWidth, -1, Image.SCALE_SMOOTH);

        BufferedImage scaledImage = new BufferedImage(
                scaledInstance.getWidth(null),
                scaledInstance.getHeight(null),
                BufferedImage.TYPE_INT_RGB);

        Graphics graphics = scaledImage.getGraphics();
        graphics.drawImage(scaledInstance, 0, 0, Color.WHITE, null);
        graphics.dispose();

        return scaledImage;
    }

}
