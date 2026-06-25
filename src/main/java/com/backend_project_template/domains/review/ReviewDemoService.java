package com.backend_project_template.domains.review;

import com.backend_project_template.core.Constant;
import com.backend_project_template.domains.presence.dto.UserPresenceDTO;
import com.backend_project_template.domains.presence.SaloonMapDTO;
import com.backend_project_template.domains.saloon.Saloon;
import com.backend_project_template.domains.saloon.SaloonDTO;
import com.backend_project_template.domains.saloonChat.SaloonMessageDTO;
import com.backend_project_template.domains.user.PublicUserDTO;
import com.backend_project_template.domains.user.User;
import com.backend_project_template.domains.user.UserRepository;
import com.backend_project_template.domains.user.UserService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ReviewDemoService {

  private static final String REVIEW_DEMO_SALOON_NAME = "Saloons Review Demo";
  private static final String DEMO_USER_CAMILLE_EMAIL = "apple-review-camille-demo@saloons.fr";
  private static final String DEMO_USER_ALEX_EMAIL = "apple-review-alex-demo@saloons.fr";
  private static final int REVIEW_MIN_CONNECTED_COUNT = 3;
  private static final long FIRST_DEMO_MESSAGE_ID = -1L;
  private static final long SECOND_DEMO_MESSAGE_ID = -2L;
  private static final long THIRD_DEMO_MESSAGE_ID = -3L;
  private static final long FIRST_DEMO_MESSAGE_MINUTES_AGO = 8L;
  private static final long SECOND_DEMO_MESSAGE_MINUTES_AGO = 6L;
  private static final long THIRD_DEMO_MESSAGE_MINUTES_AGO = 4L;
  private static final String REVIEW_DEMO_SALOON_IMAGE = "saloonDemo.jpg";
  private static final String DEMO_USER_CAMILLE_IMAGE = "Camille.png";
  private static final String DEMO_USER_ALEX_IMAGE = "Alex.png";

  private final UserRepository userRepository;
  private final UserService userService;

  @Value("${app.base-url:http://localhost:8080}")
  private String baseUrl;

  public ReviewDemoService(UserRepository userRepository, UserService userService) {
    this.userRepository = userRepository;
    this.userService = userService;
  }

  public boolean isReviewDemo(User user, Saloon saloon) {
    return user != null
        && saloon != null
        && user.getRoles().contains(Constant.REVIEWER)
        && Boolean.TRUE.equals(saloon.getIsPrivate())
        && REVIEW_DEMO_SALOON_NAME.equals(saloon.getName());
  }

  public boolean isReviewDemoSaloon(Saloon saloon) {
    return saloon != null
        && Boolean.TRUE.equals(saloon.getIsPrivate())
        && REVIEW_DEMO_SALOON_NAME.equals(saloon.getName());
  }

  public boolean isDemoUser(User user) {
    return user != null
        && (DEMO_USER_CAMILLE_EMAIL.equals(user.getEmail())
            || DEMO_USER_ALEX_EMAIL.equals(user.getEmail()));
  }

  public void applyReviewDemoSaloonImage(SaloonDTO dto, Saloon saloon) {
    if (isReviewDemoSaloon(saloon)) {
      dto.setImgUrl(imageUrl(REVIEW_DEMO_SALOON_IMAGE));
    }
  }

  public void applyReviewDemoSaloonImage(SaloonMapDTO dto, Saloon saloon) {
    if (isReviewDemoSaloon(saloon)) {
      dto.setImgUrl(imageUrl(REVIEW_DEMO_SALOON_IMAGE));
    }
  }

  public void applyReviewDemoPublicUserImage(PublicUserDTO dto, User user) {
    if (isDemoUser(user)) {
      dto.setImgUrl(demoUserImageUrl(user));
    }
  }

  public int ensureReviewConnectedCount(int realConnectedCount) {
    return Math.max(realConnectedCount, REVIEW_MIN_CONNECTED_COUNT);
  }

  public List<UserPresenceDTO> withDemoUsers(List<UserPresenceDTO> connectedUsers) {
    List<UserPresenceDTO> result = new ArrayList<>(connectedUsers);
    for (User demoUser : getDemoUsers()) {
      boolean alreadyPresent = result.stream().anyMatch(user -> demoUser.getId().equals(user.getId()));
      if (!alreadyPresent) {
        result.add(toPresenceDTO(demoUser));
      }
    }
    return result;
  }

  public List<SaloonMessageDTO> withDemoMessages(Long saloonId, List<SaloonMessageDTO> realMessages) {
    List<User> demoUsers = getDemoUsers();
    List<SaloonMessageDTO> messages = new ArrayList<>();
    if (demoUsers.size() >= 2) {
      LocalDateTime now = LocalDateTime.now();
      messages.add(toMessageDTO(
          FIRST_DEMO_MESSAGE_ID,
          saloonId,
          demoUsers.get(0),
          "Salut, quelqu’un est déjà dans le saloon ce soir ?",
          now.minusMinutes(FIRST_DEMO_MESSAGE_MINUTES_AGO)));
      messages.add(toMessageDTO(
          SECOND_DEMO_MESSAGE_ID,
          saloonId,
          demoUsers.get(1),
          "Oui, je viens d’arriver. Le chat fonctionne bien.",
          now.minusMinutes(SECOND_DEMO_MESSAGE_MINUTES_AGO)));
      messages.add(toMessageDTO(
          THIRD_DEMO_MESSAGE_ID,
          saloonId,
          demoUsers.get(0),
          "Apple peut ouvrir mon profil depuis le chat et tester le signalement.",
          now.minusMinutes(THIRD_DEMO_MESSAGE_MINUTES_AGO)));
    }
    messages.addAll(realMessages);
    return messages;
  }

  private List<User> getDemoUsers() {
    return List.of(
        findDemoUser(DEMO_USER_CAMILLE_EMAIL),
        findDemoUser(DEMO_USER_ALEX_EMAIL))
        .stream()
        .flatMap(Optional::stream)
        .toList();
  }

  private Optional<User> findDemoUser(String email) {
    return userRepository.findByEmail(email);
  }

  private UserPresenceDTO toPresenceDTO(User user) {
    return new UserPresenceDTO(
        user.getId(),
        user.getUserName(),
        demoUserImageUrl(user),
        user.getProfileImageUpdatedAt(),
        userService.calculateAge(user.getBirthDate()),
        user.getCity());
  }

  private SaloonMessageDTO toMessageDTO(
      Long id,
      Long saloonId,
      User sender,
      String content,
      LocalDateTime createdAt) {
    SaloonMessageDTO dto = new SaloonMessageDTO();
    dto.setId(id);
    dto.setSaloonId(saloonId);
    dto.setSenderId(sender.getId());
    dto.setSenderName(sender.getUserName());
    dto.setSenderImg(demoUserImageUrl(sender));
    dto.setContent(content);
    dto.setCreatedAt(createdAt);
    return dto;
  }

  private String demoUserImageUrl(User user) {
    if (DEMO_USER_CAMILLE_EMAIL.equals(user.getEmail())) {
      return imageUrl(DEMO_USER_CAMILLE_IMAGE);
    }
    if (DEMO_USER_ALEX_EMAIL.equals(user.getEmail())) {
      return imageUrl(DEMO_USER_ALEX_IMAGE);
    }
    return user.getImgUrl();
  }

  private String imageUrl(String filename) {
    String normalizedBaseUrl = baseUrl.endsWith("/")
        ? baseUrl.substring(0, baseUrl.length() - 1)
        : baseUrl;
    return normalizedBaseUrl + "/images/" + filename;
  }
}
