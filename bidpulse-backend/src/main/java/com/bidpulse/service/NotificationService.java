package com.bidpulse.service;

import com.bidpulse.model.Notification;
import com.bidpulse.model.NotificationType;

import java.util.List;

public interface NotificationService {
    List<Notification> getUnread(Long userId);
    void markRead(Long notificationId);
    
    // --- ADD THIS LINE TO FIX THE ERROR ---
    void notifyOutbid(Long userId, Long auctionId, Long bidId, String message);
    void notify(Long userId, NotificationType type, String message);
}