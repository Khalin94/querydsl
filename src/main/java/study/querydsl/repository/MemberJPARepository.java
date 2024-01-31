package study.querydsl.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import study.querydsl.Dto.MemberSearchCondition;
import study.querydsl.Dto.MemberTeamDto;
import study.querydsl.Dto.QMemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static study.querydsl.entity.QMember.*;
import static study.querydsl.entity.QTeam.*;

@Repository
public class MemberJPARepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public MemberJPARepository(EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }

    public void save(Member member) {
        em.persist(member);
    }

    public Optional<Member> findById(Long id) {
        Member member = em.find(Member.class, id);

        return Optional.ofNullable(member);
    }

    public List<Member> findAll() {
        List<Member> members = em.createQuery("select m from Member m", Member.class)
                                 .getResultList();
        return members;
    }

    public List<Member> findAllQueryDsl() {
        return queryFactory.selectFrom(member)
                           .fetch();
    }

    public List<Member> findByUsername(String username) {
        // 컴파일 시점에 오류가 나지 않음.
        List<Member> members = em.createQuery("select m from Member m where username = :username", Member.class)
                                 .setParameter("username", username)
                                 .getResultList();

        return members;
    }

    public List<Member> findByUsernameQueryDsl(String username) {
        // queryDsl의 경우 컴파일 시점에 오류가 남.
        return queryFactory.selectFrom(member)
                           .where(member.username.eq(username))
                           .fetch();
    }

    public List<MemberTeamDto> searchByCondition(MemberSearchCondition cond) {

        BooleanBuilder builder = new BooleanBuilder();

        if (StringUtils.hasText(cond.getUsername())) {
            builder.and(member.username.eq(cond.getUsername()));
        }
        if (StringUtils.hasText(cond.getTeamName())) {
            builder.and(team.name.eq(cond.getTeamName()));
        }
        if (cond.getAgeGoe() != null) {
            builder.and(member.age.goe(cond.getAgeGoe()));
        }
        if (cond.getAgeLoe() != null) {
            builder.and(member.age.loe(cond.getAgeLoe()));
        }

        return queryFactory.select(new QMemberTeamDto(
                                   member.id.as("memberId"),
                                   member.username,
                                   member.age,
                                   team.id.as("teamId"),
                                   team.name.as("teamName")
                           ))
                           .from(member)
                           .join(member.team, team)
                           .where(builder)
                           .fetch();
    }

    public List<MemberTeamDto> searchByConditionWhere(MemberSearchCondition cond) {
        return queryFactory.select(new QMemberTeamDto(
                                   member.id.as("memberId"),
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
                           .fetch();
    }

    public List<Member> searchMembers(MemberSearchCondition cond) {
        return queryFactory.selectFrom(member)
                           .join(member.team, team)
                           .where(
                                   usernameEq(cond.getUsername()),
                                   teamNameEq(cond.getTeamName()),
                                   ageGoe(cond.getAgeGoe()),
                                   ageLoe(cond.getAgeLoe())
                           )
                           .fetch();
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
