package com.backend_project_template.common.image;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
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
  private static final float WEBP_QUALITY = 0.82f;
  private static final float JPEG_QUALITY = 0.84f;
  private static final int EXIF_ORIENTATION_TAG = 0x0112;
  private static final int EXIF_SHORT_TYPE = 3;
  private static final int JPEG_START_OF_IMAGE = 0xd8;
  private static final int JPEG_APP1_MARKER = 0xe1;
  private static final int TIFF_MAGIC = 42;
  private static final int TIFF_ENTRY_SIZE = 12;
  private static final int ORIENTATION_NORMAL = 1;
  private static final int ORIENTATION_FLIP_HORIZONTAL = 2;
  private static final int ORIENTATION_ROTATE_180 = 3;
  private static final int ORIENTATION_FLIP_VERTICAL = 4;
  private static final int ORIENTATION_TRANSPOSE = 5;
  private static final int ORIENTATION_ROTATE_90 = 6;
  private static final int ORIENTATION_TRANSVERSE = 7;
  private static final int ORIENTATION_ROTATE_270 = 8;
  private static final String UPLOAD_DIR = "uploads/images";
  private static final Set<String> ACCEPTED_MIME_TYPES = Set.of(
      "image/jpeg",
      "image/png",
      "image/webp");
  private static final Map<String, String> MIME_TYPE_ALIASES = Map.of(
      "image/jpg", "image/jpeg",
      "image/pjpeg", "image/jpeg",
      "image/x-png", "image/png");

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

    String contentType = normalizeMimeType(file.getContentType());
    if (contentType == null || !ACCEPTED_MIME_TYPES.contains(contentType)) {
      throw new ImageUploadException("Format image non supporté");
    }

    String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
    if (extension != null && "svg".equalsIgnoreCase(extension)) {
      throw new ImageUploadException("Les images SVG ne sont pas acceptées");
    }
  }

  private BufferedImage readImage(MultipartFile file) throws IOException {
    try (InputStream inputStream = file.getInputStream()) {
      byte[] imageBytes = inputStream.readAllBytes();
      BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
      if (image == null) {
        throw new ImageUploadException("Image invalide");
      }
      return applyExifOrientation(image, readExifOrientation(imageBytes));
    }
  }

  static BufferedImage applyExifOrientation(BufferedImage source, int orientation) {
    if (orientation < ORIENTATION_FLIP_HORIZONTAL || orientation > ORIENTATION_ROTATE_270) {
      return source;
    }

    int sourceWidth = source.getWidth();
    int sourceHeight = source.getHeight();
    boolean swapsDimensions = orientation >= ORIENTATION_TRANSPOSE;
    int targetWidth = swapsDimensions ? sourceHeight : sourceWidth;
    int targetHeight = swapsDimensions ? sourceWidth : sourceHeight;
    BufferedImage oriented = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);

    for (int sourceY = 0; sourceY < sourceHeight; sourceY++) {
      for (int sourceX = 0; sourceX < sourceWidth; sourceX++) {
        PixelPosition target = orientedPosition(sourceX, sourceY, sourceWidth, sourceHeight, orientation);
        oriented.setRGB(target.x(), target.y(), source.getRGB(sourceX, sourceY));
      }
    }
    return oriented;
  }

  private static PixelPosition orientedPosition(
      int x,
      int y,
      int width,
      int height,
      int orientation) {
    return switch (orientation) {
      case ORIENTATION_FLIP_HORIZONTAL -> new PixelPosition(width - 1 - x, y);
      case ORIENTATION_ROTATE_180 -> new PixelPosition(width - 1 - x, height - 1 - y);
      case ORIENTATION_FLIP_VERTICAL -> new PixelPosition(x, height - 1 - y);
      case ORIENTATION_TRANSPOSE -> new PixelPosition(y, x);
      case ORIENTATION_ROTATE_90 -> new PixelPosition(height - 1 - y, x);
      case ORIENTATION_TRANSVERSE -> new PixelPosition(height - 1 - y, width - 1 - x);
      case ORIENTATION_ROTATE_270 -> new PixelPosition(y, width - 1 - x);
      default -> new PixelPosition(x, y);
    };
  }

  @SuppressWarnings("checkstyle:MagicNumber")
  static int readExifOrientation(byte[] imageBytes) {
    if (imageBytes.length < 4 || unsignedByte(imageBytes, 0) != 0xff
        || unsignedByte(imageBytes, 1) != JPEG_START_OF_IMAGE) {
      return ORIENTATION_NORMAL;
    }

    int offset = 2;
    while (offset + 4 <= imageBytes.length) {
      if (unsignedByte(imageBytes, offset) != 0xff) {
        return ORIENTATION_NORMAL;
      }
      int marker = unsignedByte(imageBytes, offset + 1);
      offset += 2;
      if (marker == 0xd9 || marker == 0xda) {
        return ORIENTATION_NORMAL;
      }

      int segmentLength = readUnsignedShort(imageBytes, offset, false);
      if (segmentLength < 2 || offset + segmentLength > imageBytes.length) {
        return ORIENTATION_NORMAL;
      }
      if (marker == JPEG_APP1_MARKER) {
        int orientation = readOrientationFromApp1(imageBytes, offset + 2, segmentLength - 2);
        if (orientation != ORIENTATION_NORMAL) {
          return orientation;
        }
      }
      offset += segmentLength;
    }
    return ORIENTATION_NORMAL;
  }

  @SuppressWarnings("checkstyle:MagicNumber")
  private static int readOrientationFromApp1(byte[] bytes, int offset, int length) {
    if (length < 14 || !hasExifHeader(bytes, offset)) {
      return ORIENTATION_NORMAL;
    }

    int tiffOffset = offset + 6;
    boolean littleEndian;
    if (bytes[tiffOffset] == 'I' && bytes[tiffOffset + 1] == 'I') {
      littleEndian = true;
    } else if (bytes[tiffOffset] == 'M' && bytes[tiffOffset + 1] == 'M') {
      littleEndian = false;
    } else {
      return ORIENTATION_NORMAL;
    }
    if (readUnsignedShort(bytes, tiffOffset + 2, littleEndian) != TIFF_MAGIC) {
      return ORIENTATION_NORMAL;
    }

    long ifdRelativeOffset = readUnsignedInt(bytes, tiffOffset + 4, littleEndian);
    long ifdOffsetLong = tiffOffset + ifdRelativeOffset;
    int app1End = offset + length;
    if (ifdOffsetLong < tiffOffset || ifdOffsetLong + 2 > app1End) {
      return ORIENTATION_NORMAL;
    }

    int ifdOffset = (int) ifdOffsetLong;
    int entryCount = readUnsignedShort(bytes, ifdOffset, littleEndian);
    for (int index = 0; index < entryCount; index++) {
      int entryOffset = ifdOffset + 2 + index * TIFF_ENTRY_SIZE;
      if (entryOffset + TIFF_ENTRY_SIZE > app1End) {
        return ORIENTATION_NORMAL;
      }
      int tag = readUnsignedShort(bytes, entryOffset, littleEndian);
      int type = readUnsignedShort(bytes, entryOffset + 2, littleEndian);
      long count = readUnsignedInt(bytes, entryOffset + 4, littleEndian);
      if (tag == EXIF_ORIENTATION_TAG && type == EXIF_SHORT_TYPE && count > 0) {
        int orientation = readUnsignedShort(bytes, entryOffset + 8, littleEndian);
        return orientation >= ORIENTATION_NORMAL && orientation <= ORIENTATION_ROTATE_270
            ? orientation
            : ORIENTATION_NORMAL;
      }
    }
    return ORIENTATION_NORMAL;
  }

  @SuppressWarnings("checkstyle:MagicNumber")
  private static boolean hasExifHeader(byte[] bytes, int offset) {
    return bytes[offset] == 'E' && bytes[offset + 1] == 'x' && bytes[offset + 2] == 'i'
        && bytes[offset + 3] == 'f' && bytes[offset + 4] == 0 && bytes[offset + 5] == 0;
  }

  @SuppressWarnings("checkstyle:MagicNumber")
  private static int readUnsignedShort(byte[] bytes, int offset, boolean littleEndian) {
    int first = unsignedByte(bytes, offset);
    int second = unsignedByte(bytes, offset + 1);
    return littleEndian ? first | second << 8 : first << 8 | second;
  }

  @SuppressWarnings("checkstyle:MagicNumber")
  private static long readUnsignedInt(byte[] bytes, int offset, boolean littleEndian) {
    long first = unsignedByte(bytes, offset);
    long second = unsignedByte(bytes, offset + 1);
    long third = unsignedByte(bytes, offset + 2);
    long fourth = unsignedByte(bytes, offset + 3);
    return littleEndian
        ? first | second << 8 | third << 16 | fourth << 24
        : first << 24 | second << 16 | third << 8 | fourth;
  }

  @SuppressWarnings("checkstyle:MagicNumber")
  private static int unsignedByte(byte[] bytes, int offset) {
    return bytes[offset] & 0xff;
  }

  private String normalizeMimeType(String contentType) {
    if (contentType == null) {
      return null;
    }

    String normalized = contentType.toLowerCase(Locale.ROOT).split(";")[0].trim();
    return MIME_TYPE_ALIASES.getOrDefault(normalized, normalized);
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

  private record PixelPosition(int x, int y) {
  }
}
