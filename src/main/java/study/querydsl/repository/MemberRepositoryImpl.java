package study.querydsl.repository;

import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
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
