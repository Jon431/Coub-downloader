package com.coubdownloader.classes;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Coub {
    private String title;
    private String sanitizedTitle;
    private String idLink;
    private String url;
    private String videoUrl;
    private String audioUrl;
}
