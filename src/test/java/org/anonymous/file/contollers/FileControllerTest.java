package org.anonymous.file.contollers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"default", "test", "dev"})
public class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private MockMultipartFile file1;
    private MockMultipartFile file2;

    @BeforeEach
    void init() {
        file1 = new MockMultipartFile("file", "test1.png", MediaType.IMAGE_PNG_VALUE, new byte[] {1, 2, 3, 4});
        file2 = new MockMultipartFile("file", "test1.png", MediaType.IMAGE_PNG_VALUE, new byte[] {1, 2, 3, 4});
    }

    @Test
    void fileUploadTest() throws Exception{
        mockMvc.perform(multipart("/upload")
                        .file(file1)
                        .file(file2)
                        .param("gid", "testgid")
                        .param("location", "testlocation"))
                .andDo(print());
    }
}