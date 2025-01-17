package org.anonymous.file.services;

import lombok.RequiredArgsConstructor;
import org.anonymous.file.FileProperties;
import org.anonymous.file.entites.FileInfo;
import org.springframework.util.StringUtils;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Lazy;
import org.anonymous.file.controllers.RequestThumb;
import org.springframework.web.client.RestTemplate;
import org.anonymous.file.exceptions.FileNotFoundException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;


import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

@Lazy
@Service
@RequiredArgsConstructor
@EnableConfigurationProperties(FileProperties.class)
public class ThumbnailService {

    private final FileProperties properties;
    private final FileInfoService infoService;
    private final RestTemplate restTemplate;

    public String create(RequestThumb form) {

        Long seq = form.getSeq();
        String url = form.getUrl();
        int width = Math.max(form.getWidth(), 30);
        int height = Math.max(form.getHeight(), 30);

        String thumbPath = getThumbPath(seq, url, width, height);
        File file = new File(thumbPath);
        if (file.exists()) { // 이미 Thumbnail 이미지를 만든 경우
            return thumbPath;
        }

        try {
            // thumbPath = D:/uploads/thumbs/9/959_350_250.jpg
            if (seq != null && seq > 0L) { // 서버에 올라간 파일
                FileInfo item = infoService.get(seq);
                Thumbnails.of(item.getFilePath())
                        .size(width, height)
                        .toFile(file);
            }
            // D:/uploads/thumbs/urls/hash_350_250.jpg
            else if (StringUtils.hasText(url)) { // 원격 URL 이미지
                String original = String.format("%s_original", thumbPath);
                // original = D:/uploads/thumbs/urls/hash_350_250.jpg_original
                byte[] bytes = restTemplate.getForObject(URI.create(url), byte[].class);
                Files.write(Paths.get(original), bytes);

                Thumbnails.of(original)
                        .size(width, height)
                        .toFile(file);
            } else {
                thumbPath = null;
            }
        } catch (Exception e) {}
        return thumbPath;
    }

    /**
     * Thumbnail 경로
     * thumbs/폴더번호/seq_너비_높이.확장자
     * thumbs/urls/정수해시코드_너비_높이.확장자
     */
    public String getThumbPath(Long seq, String url, int width, int height) {
        String thumbPath = properties.getPath() + "thumbs/";
        // D:/uploads/thumbs/

        if (seq != null && seq > 0L) { // 직접 서버에 올린 파일
            FileInfo item = infoService.get(seq);
            if (item == null) {
                throw new FileNotFoundException();
            }
            thumbPath = thumbPath + String.format("%d/%d_%d_%d%s", seq % 10L, seq, width, height , item.getExtension());
            // D:/uploads/thumbs/9/959_350_250.jpg
        } else if (StringUtils.hasText(url)) { // 원격 URL 이미지인 경우
            String extension = url.lastIndexOf(".") == -1 ? "" : url.substring(url.lastIndexOf("."));
            // extension = .jpg
            if (StringUtils.hasText(extension)) {
                extension = extension.split("[?#]")[0];
                // .jpg?type=nf220_150
                // extension[0] = .jpg
                // extension[1] = type=nf220_150
            }
            thumbPath = thumbPath + String.format("urls/%d_%d_%d%s", Objects.hash(url), width, height, extension);
            // D:/uploads/thumbs/urls/hash_350_250.jpg
        }

        File file = new File(thumbPath);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        return thumbPath;
    }
}