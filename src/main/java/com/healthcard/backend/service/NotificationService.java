package com.healthcard.backend.service;

import com.healthcard.backend.dto.response.NotificationResponse;
import com.healthcard.backend.entity.Notification;
import com.healthcard.backend.entity.User;
import com.healthcard.backend.entity.enums.NotificationType;
import com.healthcard.backend.repository.NotificationRepository;
import com.healthcard.backend.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Writes an in-app (SYSTEM) notification, and best-effort fires an email.
 * SMS is wired through an interface (see SmsSender) so a real provider like
 * Twilio/AWS SNS can be dropped in by implementing one method - no controller
 * or service code elsewhere needs to change.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;

    @Value("${app.notifications.email.enabled:true}")
    private boolean emailEnabled;

    @Value("${app.notifications.sms.enabled:false}")
    private boolean smsEnabled;

    public void notifyUser(User user, String title, String message) {
        notificationRepository.save(Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(NotificationType.SYSTEM)
                .build());

        if (emailEnabled) {
            sendEmailAsync(user.getEmail(), title, message);
        }
        if (smsEnabled && user.getPhone() != null) {
            sendSms(user.getPhone(), title + ": " + message);
        }
    }

    @Async
    public void sendEmailAsync(String toEmail, String subject, String body) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setTo(toEmail);
            helper.setSubject("[Health Card System] " + subject);
            helper.setText(body, false);
            mailSender.send(mimeMessage);
        } catch (MessagingException | org.springframework.mail.MailException e) {
            // Don't let a misconfigured/missing SMTP server break the request flow.
            log.warn("Email notification failed for {}: {}", toEmail, e.getMessage());
        }
    }

    /**
     * Plug point for an SMS gateway. Wire Twilio/AWS SNS here and flip
     * app.notifications.sms.enabled to true in application.yml.
     */
    private void sendSms(String phone, String message) {
        log.info("[SMS-STUB] Would send SMS to {} -> {}", phone, message);
    }

    public List<NotificationResponse> getMyNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(n -> NotificationResponse.builder()
                        .id(n.getId())
                        .title(n.getTitle())
                        .message(n.getMessage())
                        .type(n.getType())
                        .isRead(n.isRead())
                        .createdAt(n.getCreatedAt())
                        .build())
                .toList();
    }

    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }
}
