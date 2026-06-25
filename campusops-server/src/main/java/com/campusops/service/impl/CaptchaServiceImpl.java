package com.campusops.service.impl;

import com.campusops.service.CaptchaService;
import com.campusops.vo.auth.CaptchaResponse;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 图形验证码实现。使用 JVM 内存 ConcurrentHashMap 存储，内置过期清理。
 */
@Slf4j
@Service
public class CaptchaServiceImpl implements CaptchaService {

    private static final int WIDTH = 130;
    private static final int HEIGHT = 50;
    private static final int CODE_LENGTH = 4;
    private static final int EXPIRES_SECONDS = 120;
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ConcurrentMap<String, CaptchaEntry> store = new ConcurrentHashMap<>();

    @PostConstruct
    void startCleaner() {
        Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "captcha-cleaner");
            t.setDaemon(true);
            return t;
        }).scheduleWithFixedDelay(() -> {
            long now = System.currentTimeMillis();
            store.entrySet().removeIf(e -> now > e.getValue().expiresAt);
        }, 60, 60, TimeUnit.SECONDS);
    }

    @Override
    public CaptchaResponse generate() {
        String code = randomCode();
        String id = UUID.randomUUID().toString().replace("-", "");
        store.put(id, new CaptchaEntry(code, System.currentTimeMillis() + EXPIRES_SECONDS * 1000L));

        String base64 = generateImage(code);
        return CaptchaResponse.builder()
                .captchaId(id)
                .imageBase64(base64)
                .expiresIn(EXPIRES_SECONDS)
                .build();
    }

    @Override
    public boolean validateAndRemove(String captchaId, String code) {
        if (captchaId == null || code == null) return false;
        CaptchaEntry entry = store.remove(captchaId);
        if (entry == null) return false;
        if (System.currentTimeMillis() > entry.expiresAt) return false;
        return entry.code.equalsIgnoreCase(code);
    }

    private String randomCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    private String generateImage(String code) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // 背景
        g.setColor(new Color(245, 248, 252));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // 干扰线
        g.setColor(new Color(220, 220, 220));
        for (int i = 0; i < 6; i++) {
            int x1 = RANDOM.nextInt(WIDTH);
            int y1 = RANDOM.nextInt(HEIGHT);
            int x2 = RANDOM.nextInt(WIDTH);
            int y2 = RANDOM.nextInt(HEIGHT);
            g.drawLine(x1, y1, x2, y2);
        }

        // 文字
        Font font = new Font("Arial", Font.BOLD, 28);
        g.setFont(font);
        for (int i = 0; i < code.length(); i++) {
            g.setColor(new Color(20 + RANDOM.nextInt(80), 40 + RANDOM.nextInt(80), 80 + RANDOM.nextInt(100)));
            g.translate(8 + i * 30 + RANDOM.nextInt(5), 18 + RANDOM.nextInt(12));
            g.rotate(Math.toRadians(-15 + RANDOM.nextInt(30)));
            g.drawString(String.valueOf(code.charAt(i)), 0, 0);
            g.rotate(Math.toRadians(15 - RANDOM.nextInt(30)));
            g.translate(-(8 + i * 30 + RANDOM.nextInt(5)), -(18 + RANDOM.nextInt(12)));
        }

        g.dispose();

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", bos);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(bos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("生成验证码图片失败", e);
        }
    }

    private static class CaptchaEntry {
        final String code;
        final long expiresAt;

        CaptchaEntry(String code, long expiresAt) {
            this.code = code;
            this.expiresAt = expiresAt;
        }
    }
}