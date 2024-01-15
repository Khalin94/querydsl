package study.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
public class QuerydslBasicTest {

    @PersistenceContext
    EntityManager em;

    JPAQueryFactory queryFactory;

    @BeforeEach
    void before(){
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
    void jpqlTest(){
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
        QMember m = new QMember("m");

        Member member = queryFactory.select(m)
                                     .from(m)
                                     .where(m.username.eq("member1"))
                                     .fetchOne();

        assertThat(member.getUsername()).isEqualTo("member1");

    }
}
