package com.backend_project_template.domains.user;

import com.backend_project_template.common.image.ImageStorageService;
import com.backend_project_template.common.image.ImageUploadException;
import com.backend_project_template.common.image.StoredImage;
import jakarta.persistence.EntityNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/user/upload")
public class FileUploadController {

  private static final String UPLOAD_DIR = "uploads/images/";
  private static final int IMAGE_CACHE_DAYS = 30;

  private final UserRepository userRepository;
  private final ImageStorageService imageStorageService;

  @Autowired
  public FileUploadController(
      UserRepository userRepository,
      ImageStorageService imageStorageService) {
    this.userRepository = userRepository;
    this.imageStorageService = imageStorageService;
  }

  @PostMapping("/image/user/{userId}")
  public ResponseEntity<UserDTO> uploadImage(@RequestParam("file") MultipartFile file, @PathVariable Long userId,
      @AuthenticationPrincipal UserDetails userDetails) {
    // Sécurité : vérifier que l'utilisateur authentifié est bien le propriétaire
    if (userDetails == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    User authenticatedUser = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
    if (authenticatedUser == null || !authenticatedUser.getId().equals(userId)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    try {
      User updatedUser = userRepository.findById(userId)
          .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable : " + userId));

      StoredImage storedImage = imageStorageService.storeProfileImage(file);
      imageStorageService.deleteManagedImage(updatedUser.getImgUrl());

      updatedUser.setImgUrl(storedImage.publicUrl());
      updatedUser.setProfileImageUpdatedAt(java.time.LocalDateTime.now());
      UserDTO res = UserDTO.fromEntity(userRepository.save(updatedUser));
      return ResponseEntity.ok(res);
    } catch (ImageUploadException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
  }

  @GetMapping("/{filename}")
  public ResponseEntity<Resource> getImage(@PathVariable String filename) {
    try {
      Path uploadPath = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();
      Path filePath = uploadPath.resolve(filename).normalize();
      if (!filePath.startsWith(uploadPath)) {
        return ResponseEntity.notFound().build();
      }

      UrlResource resource = new UrlResource(filePath.toUri());

      if (resource.exists() && resource.isReadable()) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, Files.probeContentType(filePath));
        headers.setCacheControl(CacheControl.maxAge(java.time.Duration.ofDays(IMAGE_CACHE_DAYS)).cachePublic());
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
      } else {
        return ResponseEntity.notFound().build();
      }
    } catch (MalformedURLException e) {
      return ResponseEntity.notFound().build();
    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
}
