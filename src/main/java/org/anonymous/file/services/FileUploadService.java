package org.anonymous.file.services;

import lombok.RequiredArgsConstructor;
import org.anonymous.file.FileProperties;
import org.anonymous.file.entites.FileInfo;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Lazy;
import org.anonymous.file.controllers.RequestUpload;
import org.springframework.web.multipart.MultipartFile;
import org.anonymous.file.repositories.FileInfoRepository;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Lazy
@Service
@RequiredArgsConstructor
@EnableConfigurationProperties(FileProperties.class)
public class FileUploadService {

    private final FileProperties properties;
    private final FileInfoRepository fileInfoRepository;
    private final FileInfoService infoService;

    public List<FileInfo> upload(RequestUpload form) {

        String gid = form.getGid();
        gid = StringUtils.hasText(gid) ? gid : UUID.randomUUID().toString(); // 4자리 - 4자리 - 4자리
        String location = form.getLocation();
        MultipartFile[] files = form.getFiles();

        String rootPath = properties.getPath();

        // 파일 업로드 성공 파일 정보 목록.
        List<FileInfo> uploadedItems = new ArrayList<>();

        for (MultipartFile file : files) {
            String contentType = file.getContentType();
            // 이미지 형식의 파일만 허용하는 경우 - 이미지가 아닌 파일은 건너띄기.
            if (form.isImageOnly() && contentType.indexOf("image/") == -1) {
                continue;
            }


            // region 1. 파일 업로드 정보 - DB에 기록
            // 파일명.확장자
            String fileName = file.getOriginalFilename();
            String extension = fileName.substring(fileName.lastIndexOf("."));

            FileInfo item = new FileInfo();
            item.setGid(gid);
            item.setLocation(location);
            item.setFileName(fileName);
            item.setExtension(extension);
            item.setContentType(contentType);
            fileInfoRepository.saveAndFlush(item);

            // endregion

            // region 2. 파일 업로드 처리

            long seq = item.getSeq();
            String uploadFileName = seq + extension;
            long folder = seq % 10L; // 0 ~ 9가 나옴.
            File dir = new File(rootPath + folder);

            // 디렉토리가 존재하지 않거나 파일로만 있는 경우 생성.
            if (!dir.exists() || !dir.isDirectory()) {
                dir.mkdirs();
            }
            File _file = new File(dir, uploadFileName);
            try {
                file.transferTo(_file);

                // 추가 정보 처리
                infoService.addInfo(item);

                uploadedItems.add(item);
            } catch (IOException e) {
                // 파일 업로드 실패 -> DB 저장된 데이터를 삭제해야함.
                fileInfoRepository.delete(item);
                fileInfoRepository.flush();
            }

            // endregion

        }

        return uploadedItems;
    }
}
