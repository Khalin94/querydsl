package study.querydsl.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;

import java.util.List;

public interface MemberRepositoryCustom {

    List<MemberTeamDto> search(MemberSearchCondition cond);

    Page<MemberTeamDto> searchSimple(MemberSearchCondition cond, Pageable pageable);

    Page<MemberTeamDto> searchComplex(MemberSearchCondition cond, Pageable pageable);

    Page<MemberTeamDto> searchComplexOptimize(MemberSearchCondition cond, Pageable pageable);
}
