package org.anonymous.file.services;


import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.anonymous.file.entites.FileInfo;
import org.anonymous.file.exceptions.FileNotFoundException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;

@Lazy
@Service
@RequiredArgsConstructor
public class FileDownloadService {
    private final FileInfoService infoService;
    private final HttpServletResponse response;

    public void process(Long seq) {

        FileInfo item = infoService.get(seq);

        String fileName = item.getFileName();
        // 윈도우에서 한글 깨짐 방지 StandardCharsets.ISO_8859_1 얘가 2바이트
        fileName = new String(fileName.getBytes(), StandardCharsets.ISO_8859_1);

        String contentType = item.getContentType();
        contentType = StringUtils.hasText(contentType) ? contentType : "application/octet-stream";

        File file = new File(item.getFilePath());
        if (!file.exists()) {
            throw new FileNotFoundException();
        }

        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis)) {
            // 바디의 출력을 filename에 지정된 파일로 변경
            response.setHeader("Content-Disposition", "attachment; filename" + fileName); // 중요. Header Key값 중요.
            response.setContentType(contentType);
            response.setHeader("Cache-Control", "no-cache"); // 캐시컨트롤 이거 안하면 캐시에 저장되어있는것들 다 저장함.
            response.setHeader("Pragma", "no-cache"); // 예전 브라우저 및 컴퓨터 지원하기위해.
            response.setIntHeader("Expires", 0); // 만료시간을 없앰.
            response.setContentLengthLong(file.length()); // 파일 용량

            OutputStream out = response.getOutputStream();
            out.write(bis.readAllBytes());


        } catch (IOException e) {
            e.printStackTrace();
        }


        // region 예시
/*        try {
response.setHeader("Content-Disposition", "attachment; filename=test.txt");
            PrintWriter out = response.getWriter();
            out.println("test1");
            out.println("test2");
            out.println("test3");
        } catch (Exception e) {

        }*/

        // endregion
    }
}
