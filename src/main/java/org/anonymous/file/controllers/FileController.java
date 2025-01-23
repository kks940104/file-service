package org.anonymous.file.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.anonymous.file.constants.FileStatus;
import org.anonymous.file.entites.FileInfo;
import org.anonymous.file.services.*;
import org.anonymous.global.exceptions.BadRequestException;
import org.anonymous.global.libs.Utils;
import org.anonymous.global.rests.JSONData;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.util.List;

@Tag(name="파일 API", description = "파일 업로드, 조회, 다운로드, 삭제 기능 제공.")
@RestController
@RequiredArgsConstructor
public class FileController {
    private final Utils utils;
    private final FileInfoService infoService;
    private final FileDoneService doneService;
    private final FileImageService imageService;
    private final FileUploadService uploadService;
    private final FileDeleteService deleteService;
    private final ThumbnailService thumbnailService;
    private final FileDownloadService downloadService;

    /**
     * 파일 업로드
     * @return
     */
    @Operation(summary = "파일 업로드 처리")
    @ApiResponse(
            responseCode = "201",
            description = "파일 업로드 처리, 업로드 성공시에는 업로드 완료된 파일 목록을 반환한다. " +
                          "요청시 반드시 요청 헤더에 multipart/form-data 형식으로 전송."
    )
    @Parameters({
            @Parameter(name = "gid", description = "파일 그룹 ID", required = true),
            @Parameter(name = "location", description = "파일 그룹 내에서 위치 코드"),
            @Parameter(name = "file", description = "업로드 파일, 복수개 전송 가능", required = true)
    })
    @PostMapping("/upload")
    @ResponseStatus(HttpStatus.CREATED)
    public JSONData upload(@RequestPart("file") MultipartFile[] files, @Valid RequestUpload form, Errors errors) {
        if (errors.hasErrors()) {
            throw new BadRequestException(utils.getErrorMessages(errors));
        }
        form.setFiles(files);

        /**
         * 단일 파일 업로드
         *      - 기 업로드된 파일을 삭제하고 새로 추가
         */

        if (form.isSingle()) {
            deleteService.deletes(form.getGid(), form.getLocation());
        }

        List<FileInfo> uploadedFiles = uploadService.upload(form);

        if (form.isDone()) { // 업로드 하자마자 완료처리함.
            doneService.process(form.getGid(), form.getLocation());
        }


        JSONData data = new JSONData(uploadedFiles);
        data.setStatus(HttpStatus.CREATED);

        return data;
    }

    /**
     * 파일 다운로드
     * 응답 헤더에 대한 통제가 가장 중요함. -> 다운로드. 파일이름으로 출력을 바꾸고 다운로드가 진행됨.
     * @param seq
     */
    @GetMapping("/download/{seq}")
    public void download(@PathVariable("seq") Long seq) {
        downloadService.process(seq);
    }

    /**
     * 파일 단일 조회
     * @param seq
     * @return
     */
    @GetMapping("/info/{seq}")
    public JSONData info(@PathVariable("seq") Long seq) {

        FileInfo item = infoService.get(seq);
        return new JSONData(item);
    }

    /**
     * 파일 목록 조회
     * gid, location
     * required = 필수냐 아니냐.
     */
    @GetMapping(path = {"/list/{gid}", "/list/{gid}/{location}"})
    public JSONData list(@PathVariable("gid") String gid,
                         @PathVariable(name = "location", required = false) String location,
                         @RequestParam(name = "status", /*required = false*/defaultValue = "DONE") FileStatus status) {

        List<FileInfo> items = infoService.getList(gid, location, status);

        return new JSONData(items);
    }


    /**
     * 파일 단일 삭제
     * @return
     */
    @DeleteMapping("/delete/{seq}")
    public JSONData delete(@PathVariable("seq") Long seq) {
        FileInfo item = deleteService.delete(seq);
        return new JSONData(item);
    }

    @DeleteMapping(path = {"deletes/{gid}", "deletes/{gid}/{location}"} )
    public JSONData deletes(@PathVariable("gid") String gid,
                            @PathVariable(name="location", required = false) String location) {

        List<FileInfo> items = deleteService.deletes(gid, location);
        return new JSONData(items);
    }

    @GetMapping("/thumb")
    public void thumb(RequestThumb form, HttpServletResponse response) {
        String path = thumbnailService.create(form);
        if (!StringUtils.hasText(path)) {
            return;
        }

        File file = new File(path);
        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis)) {

            String contentType = Files.probeContentType(file.toPath());
            response.setContentType(contentType);

            OutputStream out = response.getOutputStream();
            out.write(bis.readAllBytes());
        } catch (IOException e){

        }
    }

    @GetMapping("/select/{seq}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void select(@PathVariable("seq") Long seq) {
        imageService.select(seq);
    }

    /**
     * 파일 그룹작업 완료 처리
     * @param gid
     * @param location
     */
    @GetMapping("/done/{gid}")
    public void processDone(@PathVariable("gid") String gid, @RequestParam(name="location", required = false) String location) {
        doneService.process(gid, location);
    }

    // region Test했던것


/*    @GetMapping
    public void test(@Valid RequestLogin form, Errors errors) {

        if (errors.hasErrors()) {
            throw new BadRequestException(utils.getErrorMessages(errors));
        }
    }*/


    // endregion
}












