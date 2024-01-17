package study.querydsl;

import com.querydsl.core.NonUniqueResultException;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QTeam;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.*;

@Transactional
@SpringBootTest
public class QuerydslBasicTest {

    @PersistenceContext
    EntityManager em;

    JPAQueryFactory queryFactory;

    private Member createMember(String username, int age) {
        return createMember(username, age, null);
    }

    private Member createMember(String username, int age, Team team) {
        return Member.builder()
                     .username(username)
                     .age(age)
                     .team(team)
                     .build();
    }

    @BeforeEach
    void before() {
        queryFactory = new JPAQueryFactory(em);
        Team teamA = Team.builder()
                         .name("teamA")
                         .build();
        Team teamB = Team.builder()
                         .name("teamB")
                         .build();

        em.persist(teamA);
        em.persist(teamB);

        Member member1 = Member.builder()
                               .username("member1")
                               .age(10)
                               .team(teamA)
                               .build();
        Member member2 = Member.builder()
                               .username("member2")
                               .age(20)
                               .team(teamA)
                               .build();
        Member member3 = Member.builder()
                               .username("member3")
                               .age(30)
                               .team(teamB)
                               .build();

        Member member4 = Member.builder()
                               .username("member4")
                               .age(40)
                               .team(teamB)
                               .build();
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @DisplayName("jpql을 이용해 member1을 찾을 수 있다.")
    @Test
    void jpqlTest() {
        // find member1
        Member member = em.createQuery("select m from Member m where username = :username", Member.class)
                          .setParameter("username", "member1")
                          .getSingleResult();

        assertThat(member.getUsername()).isEqualTo("member1");
    }

    @DisplayName("querydsl을 이용해 member1을 찾을 수 있다")
    @Test
    public void querydslTest() {
//        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
//        QMember m = new QMember("m");
//        QMember m = QMember.member;

//        Member member = queryFactory.select(QMember.member)
//                                     .from(QMember.member)
//                                     .where(QMember.member.username.eq("member1"))
//                                     .fetchOne();
        Member member1 = queryFactory.select(member)
                                     .from(member)
                                     .where(member.username.eq("member1"))
                                     .fetchOne();

        assertThat(member1.getUsername()).isEqualTo("member1");

    }

    @Test
    public void search() {
        //given
        //when
        Member member1 = queryFactory.selectFrom(member)
                                     .where(member.age.eq(10)
                                                      .and(member.username.eq("member1"))
                                     )
                                     .fetchOne();

        //then
        assertThat(member1.getUsername()).isEqualTo("member1");
    }

    @Test
    public void searchAndParam() {
        //given
        //when
        Member member1 = queryFactory.selectFrom(member)
                                     .where(
                                             member.username.eq("member1"),
                                             member.age.between(10, 30)
                                     )
                                     .fetchOne();

        //then
        assertThat(member1.getUsername()).isEqualTo("member1");
        assertThat(member1.getAge()).isEqualTo(10);
    }

    @Test
    public void fetchTest() {
        //given
        //when
        List<Member> members = queryFactory.selectFrom(member)
                                           .where(member.username.contains("member"))
                                           .fetch();

        //then
        assertThat(members.size()).isEqualTo(4);

        // 리스트가 반환되는 쿼리에 fetchOne을 사용하면 javax.persistence.NonUniqueResultException 이 발생한다.
        assertThatThrownBy(() -> {
            queryFactory.selectFrom(member)
                        .fetchOne();
        }).isInstanceOf(NonUniqueResultException.class);

        //fetchFirst = limit 1과 같다
        Member member1 = queryFactory.selectFrom(member)
                                     .orderBy(member.username.asc())
                                     .fetchFirst();
        assertThat(member1.getUsername()).isEqualTo("member1");

        QueryResults<Member> results = queryFactory.selectFrom(member)
                                                   .fetchResults();

        long total = results.getTotal();
        List<Member> results1 = results.getResults();

        assertThat(total).isEqualTo(4);
        assertThat(results1.get(0)
                           .getUsername()).isEqualTo("member1");

    }

    @Test
    public void orderTest() {
        //given
        Member member5 = createMember(null, 50);
        Member member6 = createMember("member6", 50);
        Member member7 = createMember("member7", 50);
        em.persist(member5);
        em.persist(member6);
        em.persist(member7);
        //when
        List<Member> members = queryFactory.selectFrom(member)
                                           .orderBy(member.age.desc(), member.username.asc()
                                                                                      .nullsLast()) // null 이면 마지막에 나온다.
                                           .fetch();

        //then
        assertThat(members.get(0)
                          .getUsername()).isEqualTo("member6");
        assertThat(members.get(1)
                          .getUsername()).isEqualTo("member7");
        assertThat(members.get(2)
                          .getUsername()).isNull();

    }

    @Test
    public void paging1() {
        //given
        //when
        List<Member> members = queryFactory.selectFrom(member)
                                           .orderBy(member.username.desc())
                                           .offset(1)
                                           .limit(2)
                                           .fetch();

        for (Member member1 : members) {
            System.out.println("member1 = " + member1);
        }

        //then
        assertThat(members.size()).isEqualTo(2);
    }

    @Test
    public void paging2() {
        //given
        //when
        QueryResults<Member> results = queryFactory.selectFrom(member)
                                                   .orderBy(member.username.desc())
                                                   .offset(1)
                                                   .limit(2)
                                                   .fetchResults();

        //then
        assertThat(results.getTotal()).isEqualTo(4);
        assertThat(results.getResults()
                          .size()).isEqualTo(2);
        assertThat(results.getLimit()).isEqualTo(2);
        assertThat(results.getOffset()).isEqualTo(1);
    }

    @Test
    public void aggregation() throws Exception {
        //given
        //when
        List<Tuple> memberAggregation = queryFactory.select(member.count(),
                                                member.age.sum(),
                                                member.age.max(),
                                                member.age.min(),
                                                member.age.avg()
                                        )
                                        .from(member)
                                        .fetch();

        Tuple tuple = memberAggregation.get(0);

        //then
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
    }

    @Test
    public void groupBy() throws Exception {
        //given
        //when
        List<Tuple> fetch = queryFactory.select(team.name, member.age.avg())
                                        .from(member)
                                        .join(member.team, team)
                                        .groupBy(team.name)
                                        .fetch();

        Tuple teamA = fetch.get(0);
        Tuple teamB = fetch.get(1);


        //then
        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);

        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);

        List<Tuple> fetch1 = queryFactory.select(team.name, member.age.max())
                                         .from(team)
                                         .join(member)
                                         .on(team.id.eq(member.team.id))
                                         .groupBy(team.name)
                                         .fetch();
        Tuple teamAA = fetch1.get(0);
        Tuple teamBB = fetch1.get(1);

        assertThat(teamAA.get(team.name)).isEqualTo("teamA");
        assertThat(teamAA.get(member.age.max())).isEqualTo(20);

        assertThat(teamBB.get(team.name)).isEqualTo("teamB");
        assertThat(teamBB.get(member.age.max())).isEqualTo(40);

    }

}
