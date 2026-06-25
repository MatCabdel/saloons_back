package com.backend_project_template.unit.tests;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.backend_project_template.domains.block.BlockController;
import com.backend_project_template.domains.block.BlockedUser;
import com.backend_project_template.domains.block.BlockedUserRepository;
import com.backend_project_template.domains.conversation.Conversation;
import com.backend_project_template.domains.conversation.ConversationParticipant;
import com.backend_project_template.domains.conversation.ConversationParticipantRepository;
import com.backend_project_template.domains.conversation.ConversationRepository;
import com.backend_project_template.domains.heartRequest.HeartRequestRepository;
import com.backend_project_template.domains.match.MatchService;
import com.backend_project_template.domains.user.User;
import com.backend_project_template.domains.user.UserRepository;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class BlockControllerTest {

    @Mock
    private BlockedUserRepository blockedUserRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ConversationRepository conversationRepository;
    @Mock
    private ConversationParticipantRepository participantRepository;
    @Mock
    private HeartRequestRepository heartRequestRepository;
    @Mock
    private MatchService matchService;

    @InjectMocks
    private BlockController blockController;

    private User blocker;
    private User blocked;
    private Principal principal;

    @BeforeEach
    void setUp() {
        blocker = new User();
        blocker.setEmail("alice@example.com");

        blocked = new User();

        // Use reflection-like field setting via setters if available
        setId(blocker, 1L);
        setId(blocked, 2L);

        principal = () -> "alice@example.com";
    }

    // Helper : set id via reflection (User has no public setId in JPA entity)
    private void setId(User user, Long id) {
        try {
            var field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(user, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void blockUser_returnsOk_whenValid() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(blocker));
        when(userRepository.findById(2L)).thenReturn(Optional.of(blocked));
        when(blockedUserRepository.existsByBlockerIdAndBlockedId(1L, 2L)).thenReturn(false);
        when(blockedUserRepository.save(any(BlockedUser.class))).thenAnswer(i -> i.getArgument(0));
        when(conversationRepository.findConversationBetweenUsers(blocker, blocked))
                .thenReturn(Optional.empty());

        ResponseEntity<?> response = blockController.blockUser(2L, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(blockedUserRepository).save(any(BlockedUser.class));
    }

    @Test
    void blockUser_returnsBadRequest_whenBlockingSelf() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(blocker));

        ResponseEntity<?> response = blockController.blockUser(1L, principal);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(blockedUserRepository, never()).save(any());
    }

    @Test
    void blockUser_returnsConflict_whenAlreadyBlocked() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(blocker));
        when(userRepository.findById(2L)).thenReturn(Optional.of(blocked));
        when(blockedUserRepository.existsByBlockerIdAndBlockedId(1L, 2L)).thenReturn(true);

        ResponseEntity<?> response = blockController.blockUser(2L, principal);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        verify(blockedUserRepository, never()).save(any());
    }

    @Test
    void blockUser_returnsUnauthorized_whenNotAuthenticated() {
        ResponseEntity<?> response = blockController.blockUser(2L, null);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void blockUser_disablesExistingConversation() {
        Conversation conv = new Conversation();
        ConversationParticipant cp = new ConversationParticipant();
        cp.setUser(blocker);
        // leftAt is null initially
        conv.getConversationParticipants().add(cp);

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(blocker));
        when(userRepository.findById(2L)).thenReturn(Optional.of(blocked));
        when(blockedUserRepository.existsByBlockerIdAndBlockedId(1L, 2L)).thenReturn(false);
        when(blockedUserRepository.save(any(BlockedUser.class))).thenAnswer(i -> i.getArgument(0));
        when(conversationRepository.findConversationBetweenUsers(blocker, blocked))
                .thenReturn(Optional.of(conv));
        when(heartRequestRepository.findByConversationId(any())).thenReturn(List.of());

        blockController.blockUser(2L, principal);

        // The conversation participant should have had setLeftAt called
        assertNotNull(cp.getLeftAt());
        verify(participantRepository).save(cp);
        verify(matchService).leaveMatch(blocker, blocked);
    }

    @Test
    void getBlockedUsers_returnsIds() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(blocker));
        when(blockedUserRepository.findMutuallyBlockedIds(1L)).thenReturn(Set.of(2L, 3L));

        ResponseEntity<Map<String, Set<Long>>> response = blockController.getBlockedUsers(principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("blockedUserIds").containsAll(Set.of(2L, 3L)));
    }
}
