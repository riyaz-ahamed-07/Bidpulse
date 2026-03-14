package com.bidpulse.service.impl;

import com.bidpulse.model.Notification;
import com.bidpulse.model.NotificationType;
import com.bidpulse.model.User;
import com.bidpulse.repository.NotificationRepository;
import com.bidpulse.repository.UserRepository;
import com.bidpulse.service.NotificationService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messaging;

    @Override
    @Transactional
    public void notify(Long userId, NotificationType type, String message) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Notification n = Notification.builder()
                .user(user)
                .type(type)
                .message(message)
                .seen(false)
                .build();
        notificationRepository.save(n);

        // send via websocket to user queue (user destination uses username by default; we'll use userId as destination)
        // Using convertAndSendToUser requires a principal name mapping; here we send to userId string
        messaging.convertAndSendToUser(userId.toString(), "/queue/notifications", n);
    }

    @Override
    public List<Notification> getUnread(Long userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUnread'");
    }

    @Override
    public void markRead(Long notificationId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'markRead'");
    }

    @Override
    public void notifyOutbid(Long userId, Long auctionId, Long bidId, String message) {
        // TODO Auto-generated method stub
        System.out.println("NOTIFICATION: User " + userId + " was outbid on auction " + auctionId);
    }
}