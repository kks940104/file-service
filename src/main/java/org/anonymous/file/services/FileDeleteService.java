package org.anonymous.file.services;

import lombok.RequiredArgsConstructor;
import org.anonymous.file.constants.FileStatus;
import org.anonymous.file.entites.FileInfo;
import org.anonymous.file.repositories.FileInfoRepository;
import org.anonymous.global.exceptions.UnAuthorizedException;
import org.anonymous.member.MemberUtil;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.List;

@Lazy
@Service
@RequiredArgsConstructor
public class FileDeleteService {
    private final FileInfoService infoService;
    private final FileInfoRepository infoRepository;
    private final MemberUtil memberUtil;

    public FileInfo delete(Long seq) {
        FileInfo item = infoService.get(seq);
        String filePath = item.getFilePath();

        // region 0. 파일 소유자만 삭제 가능하게 통제 - 다만 관리자는 다 가능하게.

        String createdBy = item.getCreatedBy();
        if (!memberUtil.isAdmin()
                && StringUtils.hasText(createdBy)
                && (!memberUtil.isLogin() || !memberUtil.getMember().getEmail().equals(createdBy))) {
            throw new UnAuthorizedException();
        }

        // endregion

        // region 1. DB에서 정보를 제거

        infoRepository.delete(item);
        infoRepository.flush();

        // endregion

        // region 2. 파일이 서버에 존재하면 파일도 삭제

        File file = new File(filePath);

        if (file.exists() && file.isFile()) {
            file.delete();
        }

        // endregion

        // 3. 삭제된 파일 정보를 반환
        return item;
    }

    public List<FileInfo> deletes(String gid, String location) {
        List<FileInfo> items = infoService.getList(gid, location, FileStatus.ALL);
        items.forEach(i -> delete(i.getSeq()));
        
        return items;
    }

    public List<FileInfo> deletes(String gid) {
        return deletes(gid, null);
    }
}
