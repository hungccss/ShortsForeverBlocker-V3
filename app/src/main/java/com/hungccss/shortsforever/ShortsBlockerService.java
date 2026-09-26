package com.hungccss.shortsforever;

import android.accessibilityservice.AccessibilityService;
import android.os.SystemClock;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Locale;

public class ShortsBlockerService extends AccessibilityService {
    private static final String YOUTUBE_PACKAGE = "com.google.android.youtube";
    private long lastBackAt = 0L;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getPackageName() == null) return;
        if (!YOUTUBE_PACKAGE.contentEquals(event.getPackageName())) return;

        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            AccessibilityNodeInfo source = event.getSource();
            if (source != null && looksLikeShortsButton(source)) {
                blockNow();
                return;
            }
        }

        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return;

        if (hasSelectedShortsTab(root) || looksLikeShortsPlayer(root)) {
            blockNow();
        }
    }

    private void blockNow() {
        long now = SystemClock.elapsedRealtime();
        if (now - lastBackAt < 1200L) return;
        lastBackAt = now;
        performGlobalAction(GLOBAL_ACTION_BACK);
    }

    private boolean looksLikeShortsButton(AccessibilityNodeInfo node) {
        String s = nodeText(node);
        return equalsAny(s, "shorts", "youtube shorts");
    }

    private boolean hasSelectedShortsTab(AccessibilityNodeInfo node) {
        if (node == null) return false;
        String s = nodeText(node);
        if (node.isSelected() && containsAny(s, "shorts")) return true;
        if (node.isChecked() && containsAny(s, "shorts")) return true;

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null && hasSelectedShortsTab(child)) return true;
        }
        return false;
    }

    private boolean looksLikeShortsPlayer(AccessibilityNodeInfo root) {
        Counter c = new Counter();
        scan(root, c);

        // Shorts commonly exposes Remix/Use sound plus several action buttons.
        // Requiring a Shorts-specific cue reduces false positives on normal videos.
        boolean shortsSpecific = c.remix || c.useSound || c.shortsPlayer;
        int actions = (c.like ? 1 : 0) + (c.comments ? 1 : 0) + (c.share ? 1 : 0) + (c.subscribe ? 1 : 0);
        return shortsSpecific && actions >= 2;
    }

    private void scan(AccessibilityNodeInfo node, Counter c) {
        if (node == null) return;
        String s = nodeText(node);

        if (containsAny(s, "shorts player", "trình phát shorts")) c.shortsPlayer = true;
        if (containsAny(s, "remix", "phối lại")) c.remix = true;
        if (containsAny(s, "use this sound", "use sound", "dùng âm thanh", "sử dụng âm thanh")) c.useSound = true;
        if (containsAny(s, "like", "thích")) c.like = true;
        if (containsAny(s, "comment", "bình luận")) c.comments = true;
        if (containsAny(s, "share", "chia sẻ")) c.share = true;
        if (containsAny(s, "subscribe", "đăng ký")) c.subscribe = true;

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) scan(child, c);
        }
    }

    private String nodeText(AccessibilityNodeInfo node) {
        StringBuilder sb = new StringBuilder();
        if (node.getText() != null) sb.append(node.getText()).append(' ');
        if (node.getContentDescription() != null) sb.append(node.getContentDescription());
        return sb.toString().trim().toLowerCase(Locale.ROOT);
    }

    private boolean equalsAny(String s, String... values) {
        for (String v : values) if (s.equals(v)) return true;
        return false;
    }

    private boolean containsAny(String s, String... values) {
        for (String v : values) if (s.contains(v)) return true;
        return false;
    }

    @Override
    public void onInterrupt() {
    }

    private static class Counter {
        boolean shortsPlayer;
        boolean remix;
        boolean useSound;
        boolean like;
        boolean comments;
        boolean share;
        boolean subscribe;
    }
}
