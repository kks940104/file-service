package org.anonymous.member;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.anonymous.member.constants.Authority;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Member {
    private Long seq;
    private String email;
    private String name;
    private List<Authority> authorities;
}
