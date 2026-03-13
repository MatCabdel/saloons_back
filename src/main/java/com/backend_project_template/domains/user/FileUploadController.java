package com.backend_project_template.domains.user;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.ServletContext;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
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

  private final UserRepository userRepository;
  private final ServletContext servletContext;

  @Value("${app.base-url:http://localhost:8080}")
  private String baseUrl;

  @Autowired
  public FileUploadController(UserRepository userRepository, ServletContext servletContext) {
    this.userRepository = userRepository;
    this.servletContext = servletContext;
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
      String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
      Path filePath = Paths.get(UPLOAD_DIR + fileName);

      Files.createDirectories(filePath.getParent());
      Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

      User updatedUser = userRepository.findById(userId)
          .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable : " + userId));

      String oldFilename = getLastPartOfUrl(updatedUser.getImgUrl());
      if (!oldFilename.isEmpty()) {
        Path oldFilePath = Paths.get(UPLOAD_DIR + oldFilename);
        if (Files.exists(oldFilePath)) {
          Files.delete(oldFilePath);
        }
      }

      updatedUser.setImgUrl(baseUrl + "/user/upload/" + fileName);
      UserDTO res = UserDTO.fromEntity(userRepository.save(updatedUser));
      return ResponseEntity.ok(res);
    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @GetMapping("/{filename}")
  public ResponseEntity<Resource> getImage(@PathVariable String filename) {
    try {
      Path filePath = Paths.get(UPLOAD_DIR + filename);
      UrlResource resource = new UrlResource(filePath.toUri());

      if (resource.exists() && resource.isReadable()) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, Files.probeContentType(filePath));
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

  public static String getLastPartOfUrl(String url) {
    if (url == null || url.isEmpty()) {
      return "";
    }
    int idx = url.lastIndexOf('/');
    return (idx == -1) ? url : url.substring(idx + 1);
  }
}
