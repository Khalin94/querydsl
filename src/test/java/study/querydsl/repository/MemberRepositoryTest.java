package study.querydsl.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
class MemberRepositoryTest {

    @PersistenceContext
    private EntityManager em;
    
    @Autowired
    MemberRepository memberRepository;

    private void setTeamsAndMembers() {
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
    
    
    @Test
    public void basicTest() throws Exception {
        //given
        Member member = new Member("member1", 10);
        //when
        memberRepository.save(member);
        Optional<Member> findMember = memberRepository.findById(member.getId());
        List<Member> findMembers = memberRepository.findAll();
        List<Member> findMember1 = memberRepository.findByUsername("member1");

        //then
        assertThat(findMember.orElseThrow(RuntimeException::new).getUsername()).isEqualTo("member1");
        assertThat(findMembers).extracting("username").containsExactly("member1");
        assertThat(findMember1).extracting("username").containsExactly("member1");
    }

    @Test
    public void memberRepositoryCustomTest() throws Exception {
        //given
        setTeamsAndMembers();
        MemberSearchCondition cond = new MemberSearchCondition();
        cond.setTeamName("teamB");
        cond.setAgeGoe(30);
        cond.setAgeLoe(40);
        //when
        List<MemberTeamDto> specMembers = memberRepository.search(cond);

        //then
        assertThat(specMembers).extracting("username").containsExactly("member3", "member4");
    }


}