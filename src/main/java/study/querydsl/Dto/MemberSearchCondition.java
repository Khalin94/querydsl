package study.querydsl.Dto;

import lombok.Data;

@Data
public class MemberSearchCondition {

    private String username;
    private Integer ageGoe;
    private Integer ageLoe;
    private String teamName;
}
