package org.anonymous.file.services;

import lombok.RequiredArgsConstructor;
import com.querydsl.core.BooleanBuilder;
import org.anonymous.file.FileProperties;
import org.anonymous.file.entites.FileInfo;
import org.anonymous.file.entites.QFileInfo;
import org.anonymous.global.libs.Utils;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Service;
import org.anonymous.file.constants.FileStatus;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Lazy;
import org.anonymous.file.repositories.FileInfoRepository;
import org.anonymous.file.exceptions.FileNotFoundException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.util.List;
import java.util.Objects;

import static org.springframework.data.domain.Sort.Order.asc;

@Lazy
@Service
@RequiredArgsConstructor
@EnableConfigurationProperties(FileProperties.class)
public class FileInfoService {
    private final Utils utils;
    private final FileProperties properties;
    private final HttpServletRequest request;
    private final FileInfoRepository infoRepository;

    public FileInfo get(Long seq) {
        FileInfo item = infoRepository.findById(seq).orElseThrow(FileNotFoundException::new);

        addInfo(item); // 추가 정보 처리.

        return item;
    }
    public List<FileInfo> getList(String gid, String location, FileStatus status) {
        status = Objects.requireNonNullElse(status, FileStatus.ALL);

        QFileInfo fileInfo = QFileInfo.fileInfo; // Q객체. 검색을 위한.
        BooleanBuilder andBuilder = new BooleanBuilder();

        andBuilder.and(fileInfo.gid.eq(gid)); // 필수

        if (StringUtils.hasText(location)) { // 선택
            andBuilder.and(fileInfo.location.eq(location));
        }

        // 파일 작업 완료 상태
        if(status != FileStatus.ALL) {
            andBuilder.and(fileInfo.done.eq(status == FileStatus.DONE));
        }

        List<FileInfo> items = (List<FileInfo>) infoRepository.findAll(andBuilder, Sort.by(asc("listOrder"), asc("createdAt")));

        // 추가 정보 처리
        items.forEach(this::addInfo);

        return items;
    }

    public List<FileInfo> getList(String gid, String location) {
        return getList(gid, location, FileStatus.DONE);
    }

    public List<FileInfo> getList(String gid) { // 파일 그룹 작업 완료된 파일
        return getList(gid, null);
    }

    /**
     * 추가 정보 처리
     *
     * @param item
     */
    public void addInfo(FileInfo item) {
        // filepath - 서버에 올라간 실제 경로(다운로드, 삭제 시 활용...)
        item.setFilePath(getFilePath(item));

        // fileUrl - 접근할 수 있는 주소(브라우저)
        item.setFileUrl(getFileUrl(item));

        // thumbUrl - 이미지 형식인 경우
        if (item.getContentType().contains("image/")) {
            item.setThumbUrl(String.format("%s/api/file/thumb?seq=%d", request.getContextPath(), item.getSeq()));
        }
    }

    public String getFilePath(FileInfo item) {
        Long seq = item.getSeq();
        String extension = Objects.requireNonNullElse(item.getExtension(), "");
        return String.format("%s%s/%s", properties.getPath(), getFolder(seq), seq + extension);
    }

    public String getFilePath(Long seq) {
        FileInfo item = infoRepository.findById(seq).orElseThrow(FileNotFoundException::new);
        return getFilePath(item);
    }

    public String getFileUrl(FileInfo item) {
        Long seq = item.getSeq();
        String extension = Objects.requireNonNullElse(item.getExtension(), "");
        /*return String.format("%s%s%s/%s", request.getContextPath(), properties.getUrl(), getFolder(seq), seq + extension);*/
        return utils.getUrl(String.format("%s%s/%s", properties.getUrl(), getFolder(seq), seq + extension));

        // http://localhost:3000/uploads/9/959.jpg
    }

    public String getFileUrl(Long seq) {
        FileInfo item = infoRepository.findById(seq).orElseThrow(FileNotFoundException::new);
        return getFileUrl(item);
    }

    private long getFolder(long seq) {
        return seq % 10L;
    }
}
