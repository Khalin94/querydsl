package study.querydsl.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;

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


}