package com.demo.tikpic.timeline;

import androidx.annotation.NonNull;

class Photo {

    private final String url;

    Photo(@NonNull final String url) {
        this.url = url;
    }

    String getUrl() {
        return url;
    }
}
