package com.demo.tikpic.timeline;

import androidx.annotation.NonNull;

class Photo {

    private final String url;

    Photo(@NonNull final String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
