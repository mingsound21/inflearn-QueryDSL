package study.querydsl.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;

import javax.persistence.EntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {

    @Autowired
    EntityManager em; // Q. 지금 Test에서 em.find()이런 em을 사용하는 코드 없으니까 필요없는거 아냐? 실제로 주석처리하고 아래의 2개 테스트 돌리면 테스트 성공함.

    @Autowired MemberJpaRepository memberJpaRepository;

    // 기본적인 만들어둔 함수들이 정상 작동하는지 확인하기 위한 테스트
    @Test
    public void basicTest() throws Exception {
        // given
        Member member = new Member("member1", 10);
        memberJpaRepository.save(member);

        // when
    
        // then
        Member findMember = memberJpaRepository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);

        List<Member> result1 = memberJpaRepository.findAll();
        assertThat(result1).containsExactly(member);

        List<Member> result2 = memberJpaRepository. findByUsername("member1");
        assertThat(result2).containsExactly(member);
    }

    // Querydsl 버전 함수들이 정상 작동하는지 확인하기 위한 테스트
    @Test
    public void basicQuerydslTest() throws Exception {
        // given
        Member member = new Member("member1", 10);
        memberJpaRepository.save(member);

        // when

        // then
        Member findMember = memberJpaRepository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);

        List<Member> result1 = memberJpaRepository.findAll_Querydsl(); // querydsl 버전으로 변경
        assertThat(result1).containsExactly(member);

        List<Member> result2 = memberJpaRepository. findByUsername_Querydsl("member1"); // querydsl 버전으로 변경
        assertThat(result2).containsExactly(member);

    }

}