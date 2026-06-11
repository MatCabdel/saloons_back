package com.backend_project_template.common.image;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageStorageService {

  private static final long MAX_IMAGE_SIZE_BYTES = 10L * 1024L * 1024L;
  private static final int MAX_PROFILE_DIMENSION = 1024;
  private static final int MAX_CONTENT_DIMENSION = 1600;
  private static final int MIME_DETECTION_MARK_LIMIT = 32;
  private static final float WEBP_QUALITY = 0.82f;
  private static final float JPEG_QUALITY = 0.84f;
  private static final String UPLOAD_DIR = "uploads/images";
  private static final Set<String> ACCEPTED_MIME_TYPES = Set.of(
      "image/jpeg",
      "image/png",
      "image/webp");

  @Value("${app.base-url:http://localhost:8080}")
  private String baseUrl;

  public StoredImage storeProfileImage(MultipartFile file) {
    return storeImage(file, MAX_PROFILE_DIMENSION, PublicImagePath.USER_UPLOAD);
  }

  public StoredImage storeContentImage(MultipartFile file) {
    return storeImage(file, MAX_CONTENT_DIMENSION, PublicImagePath.UPLOADS_IMAGES);
  }

  public void deleteManagedImage(String imageUrl) {
    String filename = extractManagedFilename(imageUrl);
    if (filename == null) {
      return;
    }

    try {
      Path uploadPath = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();
      Path filePath = uploadPath.resolve(filename).normalize();
      if (filePath.startsWith(uploadPath)) {
        Files.deleteIfExists(filePath);
      }
    } catch (IOException ignored) {
      // Deleting an old image is best effort and must not break profile updates.
    }
  }

  private StoredImage storeImage(MultipartFile file, int maxDimension, PublicImagePath publicPath) {
    validateMultipart(file);

    try {
      BufferedImage source = readImage(file);
      BufferedImage resized = resizeIfNeeded(source, maxDimension);
      EncodedImage encodedImage = encodeOptimized(resized);

      Path uploadPath = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();
      Files.createDirectories(uploadPath);

      String filename = UUID.randomUUID() + "." + encodedImage.extension();
      Path filePath = uploadPath.resolve(filename).normalize();
      if (!filePath.startsWith(uploadPath)) {
        throw new ImageUploadException("Nom de fichier image invalide");
      }

      Files.write(filePath, encodedImage.bytes());
      String publicUrl = baseUrl + publicPath.getPathPrefix() + filename;
      return new StoredImage(filename, publicUrl, encodedImage.contentType(), encodedImage.bytes().length);
    } catch (IOException e) {
      throw new ImageUploadException("Erreur lors du traitement de l'image", e);
    }
  }

  private void validateMultipart(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new ImageUploadException("Image manquante");
    }
    if (file.getSize() > MAX_IMAGE_SIZE_BYTES) {
      throw new ImageUploadException("Image trop lourde");
    }

    String contentType = file.getContentType();
    if (contentType == null || !ACCEPTED_MIME_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
      throw new ImageUploadException("Format image non supporté");
    }

    String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
    if (extension != null && "svg".equalsIgnoreCase(extension)) {
      throw new ImageUploadException("Les images SVG ne sont pas acceptées");
    }
  }

  private BufferedImage readImage(MultipartFile file) throws IOException {
    try (InputStream inputStream = new BufferedInputStream(file.getInputStream())) {
      inputStream.mark(MIME_DETECTION_MARK_LIMIT);
      String detectedType = URLConnection.guessContentTypeFromStream(inputStream);
      inputStream.reset();

      if (detectedType != null && !ACCEPTED_MIME_TYPES.contains(detectedType.toLowerCase(Locale.ROOT))) {
        throw new ImageUploadException("Le contenu du fichier n'est pas une image acceptée");
      }

      BufferedImage image = ImageIO.read(inputStream);
      if (image == null) {
        throw new ImageUploadException("Image invalide");
      }
      return image;
    }
  }

  private BufferedImage resizeIfNeeded(BufferedImage source, int maxDimension) {
    int width = source.getWidth();
    int height = source.getHeight();
    int largestSide = Math.max(width, height);

    if (largestSide <= maxDimension) {
      return normalizeColorModel(source);
    }

    double ratio = (double) maxDimension / largestSide;
    int targetWidth = Math.max(1, (int) Math.round(width * ratio));
    int targetHeight = Math.max(1, (int) Math.round(height * ratio));
    BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
    Graphics2D graphics = resized.createGraphics();
    graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    graphics.drawImage(source, 0, 0, targetWidth, targetHeight, null);
    graphics.dispose();
    return resized;
  }

  private BufferedImage normalizeColorModel(BufferedImage source) {
    if (source.getType() == BufferedImage.TYPE_INT_RGB) {
      return source;
    }

    BufferedImage normalized = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
    Graphics2D graphics = normalized.createGraphics();
    graphics.drawImage(source, 0, 0, null);
    graphics.dispose();
    return normalized;
  }

  private EncodedImage encodeOptimized(BufferedImage image) throws IOException {
    EncodedImage webp = tryEncode(image, "webp", WEBP_QUALITY, "image/webp", "webp");
    if (webp != null) {
      return webp;
    }

    EncodedImage jpeg = tryEncode(image, "jpeg", JPEG_QUALITY, "image/jpeg", "jpg");
    if (jpeg != null) {
      return jpeg;
    }

    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      if (!ImageIO.write(image, "png", outputStream)) {
        throw new ImageUploadException("Aucun encodeur image disponible");
      }
      return new EncodedImage(outputStream.toByteArray(), "png", "image/png");
    }
  }

  private EncodedImage tryEncode(
      BufferedImage image,
      String formatName,
      float quality,
      String contentType,
      String extension) throws IOException {
    Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(formatName);
    if (!writers.hasNext()) {
      return null;
    }

    ImageWriter writer = writers.next();
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(outputStream)) {
      writer.setOutput(imageOutputStream);
      ImageWriteParam writeParam = writer.getDefaultWriteParam();
      if (writeParam.canWriteCompressed()) {
        writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        writeParam.setCompressionQuality(quality);
      }
      writer.write(null, new IIOImage(image, null, null), writeParam);
      return new EncodedImage(outputStream.toByteArray(), extension, contentType);
    } finally {
      writer.dispose();
    }
  }

  private String extractManagedFilename(String imageUrl) {
    if (imageUrl == null || imageUrl.isBlank()) {
      return null;
    }
    if (isExternalUrl(imageUrl)) {
      return null;
    }

    String path = imageUrl;
    try {
      URI uri = new URI(imageUrl);
      path = uri.getPath();
    } catch (URISyntaxException ignored) {
      int queryIndex = path.indexOf('?');
      if (queryIndex >= 0) {
        path = path.substring(0, queryIndex);
      }
    }

    if (!path.contains("/user/upload/") && !path.contains("/uploads/images/")) {
      return null;
    }
    String filename = path.substring(path.lastIndexOf('/') + 1);
    if (filename.isBlank() || filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
      return null;
    }
    return filename;
  }

  private boolean isExternalUrl(String imageUrl) {
    return imageUrl.startsWith("http://") || imageUrl.startsWith("https://")
        ? !imageUrl.startsWith(baseUrl)
        : false;
  }

  private record EncodedImage(byte[] bytes, String extension, String contentType) {
  }
}
