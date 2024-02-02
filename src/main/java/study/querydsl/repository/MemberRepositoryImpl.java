package study.querydsl.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;


public class MemberRepositoryImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @PersistenceContext
    private EntityManager em;

    public MemberRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<MemberTeamDto> search(MemberSearchCondition cond) {

        return queryFactory.select(new QMemberTeamDto(member.id.as("memberId"),
                                   member.username,
                                   member.age,
                                   team.id.as("teamId"),
                                   team.name.as("teamName")))
                           .from(member)
                           .join(member.team, team)
                           .where(
                                   usernameEq(cond.getUsername()),
                                   teamNameEq(cond.getTeamName()),
                                   ageGoe(cond.getAgeGoe()),
                                   ageLoe(cond.getAgeLoe())
                           )
                           .fetch();
    }

    @Override
    public Page<MemberTeamDto> searchSimple(MemberSearchCondition cond, Pageable pageable) {
        QueryResults<MemberTeamDto> results = queryFactory.select(new QMemberTeamDto(member.id.as("memberId"),
                                                                  member.username,
                                                                  member.age,
                                                                  team.id.as("teamId"),
                                                                  team.name.as("teamName")))
                                                          .from(member)
                                                          .join(member.team, team)
                                                          .where(
                                                                  usernameEq(cond.getUsername()),
                                                                  teamNameEq(cond.getTeamName()),
                                                                  ageGoe(cond.getAgeGoe()),
                                                                  ageLoe(cond.getAgeLoe())
                                                          )
                                                          .orderBy(member.username.asc()) // count 쿼리에서는 알아서 order by를 제외한다.
                                                          .offset(pageable.getOffset())
                                                          .limit(pageable.getPageSize())
                                                          .fetchResults();// total 과 content를 조회 , 현시점 deprecated content와 total 따로 조회?

        long total = results.getTotal();
        List<MemberTeamDto> content = results.getResults();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<MemberTeamDto> searchComplex(MemberSearchCondition cond, Pageable pageable) {
        List<MemberTeamDto> content = queryFactory.select(
                                                          new QMemberTeamDto(member.id.as("memberId"),
                                                                  member.username,
                                                                  member.age,
                                                                  team.id.as("teamId"),
                                                                  team.name.as("teamName")
                                                          ))
                                                  .from(member)
                                                  .join(member.team, team)
                                                  .where(
                                                          usernameEq(cond.getUsername()),
                                                          teamNameEq(cond.getTeamName()),
                                                          ageGoe(cond.getAgeGoe()),
                                                          ageLoe(cond.getAgeLoe())
                                                  )
                                                  .offset(pageable.getOffset())
                                                  .limit(pageable.getPageSize())
                                                  .fetch();

        long total = queryFactory.select(member)
                                 .from(member)
                                 .join(member.team, team)
                                 .where(
                                         usernameEq(cond.getUsername()),
                                         teamNameEq(cond.getTeamName()),
                                         ageGoe(cond.getAgeGoe()),
                                         ageLoe(cond.getAgeLoe())
                                 )
                                 .fetchCount();

          // fetchCount가 deprecated 되어 이렇게 카운트를 조회하면 될 듯 하다.
//        Long total = queryFactory.select(member.id.count())
//                                 .from(member)
//                                 .join(member.team, team)
//                                 .where(
//                                         usernameEq(cond.getUsername()),
//                                         teamNameEq(cond.getTeamName()),
//                                         ageGoe(cond.getAgeGoe()),
//                                         ageLoe(cond.getAgeLoe())
//                                 )
//                                 .fetchFirst();

        return new PageImpl<>(content, pageable, total);
    }

    private Predicate usernameEq(String username) {
        return StringUtils.hasText(username) ? member.username.eq(username) : null;
    }

    private Predicate teamNameEq(String teamName) {
        return StringUtils.hasText(teamName) ? team.name.eq(teamName) : null;
    }

    private Predicate ageGoe(Integer ageGoe) {
        return ageGoe != null ? member.age.goe(ageGoe) : null;
    }

    private Predicate ageLoe(Integer ageLoe) {
        return ageLoe != null ? member.age.loe(ageLoe) : null;
    }
}
