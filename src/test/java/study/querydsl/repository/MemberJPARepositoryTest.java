package study.querydsl.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.Dto.MemberSearchCondition;
import study.querydsl.Dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
class MemberJPARepositoryTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private MemberJPARepository memberJPARepository;

    @Test
    public void basicTest() throws Exception {
        //given
        Member newMember = new Member("member1", 10);

        //when
        memberJPARepository.save(newMember);
        Member member = memberJPARepository.findById(newMember.getId())
                                           .get();
        List<Member> allMembers = memberJPARepository.findAll();

        List<Member> findMembers = memberJPARepository.findByUsername("member1");

        //then
        assertThat(member).isEqualTo(newMember);
        assertThat(allMembers).containsExactly(newMember);
        assertThat(findMembers).containsExactly(newMember);

    }

    @Test
    public void basicQueryDslTest() throws Exception {
        //given
        Member newMember = new Member("member1", 10);
        memberJPARepository.save(newMember);

        //when
        List<Member> allMembers = memberJPARepository.findAllQueryDsl();
        List<Member> findMembers = memberJPARepository.findByUsernameQueryDsl("member1");

        //then
        assertThat(allMembers).containsExactly(newMember);
        assertThat(findMembers).containsExactly(newMember);

    }

    @Test
    public void searchTest() throws Exception {
        //given
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

        // 동적쿼리를 만들 때 주의점은 데이터를 다 가지고 오기 때문에 기본 조건이나 limit이 기본으로 걸려있는게 좋다.
        MemberSearchCondition cond = new MemberSearchCondition();
//        cond.setAgeGoe(35);
//        cond.setAgeLoe(40);
        cond.setTeamName("teamB");

        //when
        List<MemberTeamDto> members = memberJPARepository.searchByCondition(cond);

        //then
//        assertThat(members).extracting("username").containsExactly("member4");
        assertThat(members).extracting("username").containsExactly("member3", "member4");

    }

}