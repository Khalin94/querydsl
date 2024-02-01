package study.querydsl.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.repository.MemberJPARepository;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class MemberController {

    private final MemberJPARepository memberJPARepository;

    @GetMapping("/v1/members")
    public List<MemberTeamDto> members(MemberSearchCondition cond){

        return memberJPARepository.searchByConditionWhere(cond);
    }
}
