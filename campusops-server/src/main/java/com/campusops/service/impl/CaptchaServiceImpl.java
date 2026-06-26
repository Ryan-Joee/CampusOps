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

    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;
    private static final int PAD_X = 15;
    private static final int PAD_Y = 10;
    private static final int FONT_SIZE = 20;
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
        long expiresAt = System.currentTimeMillis() + EXPIRES_SECONDS * 1000L;
        store.put(id, new CaptchaEntry(code, expiresAt));

        log.info("验证码生成: captchaId={}, expiresInSeconds={}", id, EXPIRES_SECONDS);

        String base64 = generateImage(code);
        return CaptchaResponse.builder()
                .captchaId(id)
                .imageBase64(base64)
                .expiresIn(EXPIRES_SECONDS)
                .build();
    }

    @Override
    public boolean validateAndRemove(String captchaId, String code) {
        if (captchaId == null || code == null) {
            log.warn("验证码校验失败: 原因=参数为空, captchaId={}", captchaId);
            return false;
        }
        CaptchaEntry entry = store.remove(captchaId);
        if (entry == null) {
            log.warn("验证码校验失败: 原因=不存在或已使用, captchaId={}", captchaId);
            return false;
        }
        if (System.currentTimeMillis() > entry.expiresAt) {
            log.warn("验证码校验失败: 原因=已过期, captchaId={}", captchaId);
            return false;
        }
        if (!entry.code.equalsIgnoreCase(code)) {
            log.warn("验证码校验失败: 原因=答案不匹配, captchaId={}", captchaId);
            return false;
        }
        return true;
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
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 背景
        g.setColor(new Color(245, 248, 252));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // 干扰线（限制在 padding 内）
        g.setColor(new Color(220, 220, 220));
        int drawW = WIDTH - 2 * PAD_X;
        int drawH = HEIGHT - 2 * PAD_Y;
        for (int i = 0; i < 4; i++) {
            int x1 = PAD_X + RANDOM.nextInt(drawW);
            int y1 = PAD_Y + RANDOM.nextInt(drawH);
            int x2 = PAD_X + RANDOM.nextInt(drawW);
            int y2 = PAD_Y + RANDOM.nextInt(drawH);
            g.drawLine(x1, y1, x2, y2);
        }

        // 文字：Monospaced 等宽，均分绘制区，每字符居中在其槽位内，无旋转
        Font font = new Font("Monospaced", Font.BOLD, FONT_SIZE);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();

        int drawAreaW = WIDTH - 2 * PAD_X;
        int slotWidth = drawAreaW / CODE_LENGTH;
        int baselineY = (HEIGHT + fm.getAscent() - fm.getDescent()) / 2;

        for (int i = 0; i < code.length(); i++) {
            g.setColor(new Color(20 + RANDOM.nextInt(80), 40 + RANDOM.nextInt(80), 80 + RANDOM.nextInt(100)));
            int cw = fm.charWidth(code.charAt(i));
            int x = PAD_X + i * slotWidth + (slotWidth - cw) / 2;
            g.drawString(String.valueOf(code.charAt(i)), x, baselineY);
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