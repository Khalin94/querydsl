package study.querydsl.dto;

import lombok.Data;

@Data
public class MemberSearchCondition {

    private String username;
    private Integer ageGoe;
    private Integer ageLoe;
    private String teamName;
}
