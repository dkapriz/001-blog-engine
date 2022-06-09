package main.service;

import lombok.AllArgsConstructor;
import main.config.BlogConfig;
import main.exception.ResultIllegalParameterException;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;

@Service
@AllArgsConstructor
public class ImageService {

    private static final double ratioBytesToMb = 0.00000095367432;
    @Autowired
    private BlogConfig config;

    public BufferedImage resizeImage(BufferedImage image, int newHeight, int newWidth) {
        BufferedImage outputImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Image resultingImage = image.getScaledInstance(newWidth, newHeight, Image.SCALE_DEFAULT);

        outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);

        return outputImage;
    }

    public byte[] imageToByte(BufferedImage image, String imageFormat) {
        byte[] result = new byte[0];
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(image, imageFormat, byteArrayOutputStream);
            result = byteArrayOutputStream.toByteArray();
        } catch (IOException ex) {
            BlogConfig.LOGGER.error(ex.getMessage());
            ex.printStackTrace();
        }
        return result;
    }

    public String loadImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new ResultIllegalParameterException(BlogConfig.ERROR_IMAGE_FRONTEND_NAME,
                    BlogConfig.ERROR_FAILED_UPLOAD_IMAGE_FRONTEND_MSG);
        }
        checkFileSize(file);
        String fileFormat = checkImageFormat(file);
        String folderPathDestination = generateFilePath(config.getImageCountSubFolders(),
                config.getImageCountCharInFolderName());
        if (Files.notExists(Paths.get(folderPathDestination))) {
            new File(folderPathDestination).mkdirs();
        }
        String fileNameDestination = generateFileName(config.getImageCountCharInFileName(), fileFormat);
        Path resultFilePath = Paths.get(folderPathDestination, fileNameDestination);
        Files.copy(file.getInputStream(), resultFilePath, StandardCopyOption.REPLACE_EXISTING);
        return File.separator + resultFilePath;
    }

    private void checkFileSize(MultipartFile file) {
        checkFileSize(file, BlogConfig.ERROR_IMAGE_FRONTEND_NAME, BlogConfig.ERROR_FAILED_SIZE_IMAGE_FRONTEND_MSG);
    }

    private void checkFileSize(MultipartFile file, String typeErrorMsg, String errorMsg) {
        double currentFileSize = file.getSize() * ratioBytesToMb;
        if (currentFileSize > config.getImageMaxSize()) {
            throw new ResultIllegalParameterException(typeErrorMsg, errorMsg);
        }
    }

    private String checkImageFormat(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        assert fileName != null;
        if (fileName.isEmpty()) {
            throw new ResultIllegalParameterException(BlogConfig.ERROR_IMAGE_FRONTEND_NAME,
                    BlogConfig.ERROR_MISSING_FORMAT_IMAGE_FRONTEND_MSG);
        }
        String[] partsFileName = fileName.split("\\.");
        String fileFormat = partsFileName[partsFileName.length - 1];
        for (String format : config.getImageFormat()) {
            if (fileFormat.equals(format)) {
                return format;
            }
        }
        throw new ResultIllegalParameterException(BlogConfig.ERROR_IMAGE_FRONTEND_NAME,
                BlogConfig.ERROR_FAILED_FORMAT_IMAGE_FRONTEND_MSG);
    }

    private String generateFilePath(int countSubFolders, int countCharInFolderName) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        sb.append(config.getImagePath()).append(File.separator);
        for (int i = 0; i < countSubFolders; i++) {
            for (int j = 0; j < countCharInFolderName; j++) {
                sb.append(BlogConfig.SYMBOLS.charAt(random.nextInt(BlogConfig.SYMBOLS.length())));
            }
            sb.append(File.separator);
        }
        return sb.toString();
    }

    private String generateFileName(int countCharInFileName, String format) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        sb.append(config.getImagePrefixFileName()).append("-");
        for (int i = 0; i < countCharInFileName; i++) {
            sb.append(BlogConfig.SYMBOLS.charAt(random.nextInt(BlogConfig.SYMBOLS.length())));
        }
        sb.append(".").append(format);
        return sb.toString();
    }

    public String generateFileIconPath() {
        String path = config.getImagePath() +
                File.separator +
                config.getImageAvatarFolderName();
        if (Files.notExists(Paths.get(path))) {
            new File(path).mkdirs();
        }
        return path;
    }

    public String SaveResizerImage(MultipartFile multipartFile, String fileName) throws IOException {
        return SaveResizerImage(multipartFile, config.getImageAvatarSize(),
                generateFileIconPath(), fileName, config.getImageAvatarFormat());
    }

    public String SaveResizerImage(MultipartFile multipartFile, int targetSize,
                                   String dstFolder, String fileName, String formatImage) throws IOException {
        checkFileSize(multipartFile, BlogConfig.ERROR_PHOTO_FRONTEND_NAME,
                BlogConfig.ERROR_FAILED_SIZE_PHOTO_FRONTEND_MSG);
        BufferedImage image = ImageIO.read(multipartFile.getInputStream());
        if (image == null) {
            throw new IOException("Ошибка чения файла");
        }
        BufferedImage resizeImage = Scalr.resize(image, targetSize);
        String newFilePath = dstFolder + File.separator + fileName + "." + formatImage;
        File newFile = new File(newFilePath);
        ImageIO.write(resizeImage, formatImage, newFile);
        image.flush();
        resizeImage.flush();
        return File.separator + newFilePath;
    }

    public void removeFile(String dstFilePath) {
        String path = dstFilePath;
        if (path == null || path.isEmpty()) {
            return;
        }
        if (path.charAt(0) == File.separator.charAt(0)) {
            path = path.substring(1);
        }
        File file = new File(path);
        if (!file.delete()) {
            BlogConfig.LOGGER.info(BlogConfig.MARKER_UNSUCCESSFUL_REQUEST,
                    "Ошибка удаления файла: {}", dstFilePath);
        }
    }
}
