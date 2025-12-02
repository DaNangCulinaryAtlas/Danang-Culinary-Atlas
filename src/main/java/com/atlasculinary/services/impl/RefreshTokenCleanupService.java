package com.atlasculinary.services.impl;

import com.atlasculinary.repositories.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class RefreshTokenCleanupService {
    
    private static final Logger LOGGER = Logger.getLogger(RefreshTokenCleanupService.class.getName());
    private final RefreshTokenRepository refreshTokenRepository;
    
    @Scheduled(fixedRate = 21600000)
    @Transactional
    public void cleanupExpiredTokens() {
        try {
            refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
            LOGGER.info("Đã xóa các refresh token đã hết hạn");
        } catch (Exception e) {
            LOGGER.severe("Lỗi khi xóa refresh token hết hạn: " + e.getMessage());
        }
    }
}
