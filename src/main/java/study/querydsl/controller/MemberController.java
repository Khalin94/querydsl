package study.querydsl.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.repository.MemberJPARepository;
import study.querydsl.repository.MemberRepository;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class MemberController {

    private final MemberJPARepository memberJPARepository;
    private final MemberRepository memberRepository;

    @GetMapping("/v1/members")
    public List<MemberTeamDto> members(MemberSearchCondition cond){

        return memberJPARepository.searchByConditionWhere(cond);
    }

    @GetMapping("/v2/members")
    public Page<MemberTeamDto> membersSimple(MemberSearchCondition cond, Pageable pageable) {
        return memberRepository.searchSimple(cond, pageable);
    }

    @GetMapping("/v3/members")
    public Page<MemberTeamDto> membersComplex(MemberSearchCondition cond, Pageable pageable) {
        return memberRepository.searchComplex(cond, pageable);
    }

    @GetMapping("/v4/members")
    public Page<MemberTeamDto> membersComplexOpt(MemberSearchCondition cond, Pageable pageable) {
        return memberRepository.searchComplexOptimize(cond, pageable);
    }
}
