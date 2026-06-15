package com.pcm.alert.core;

public interface AlertMessageRenderer {
    AlertMessage render(AlertEvent event);
}
