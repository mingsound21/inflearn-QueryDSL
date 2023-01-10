package study.querydsl.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired
    EntityManager em; // Q. 지금 Test에서 em.find()이런 em을 사용하는 코드 없으니까 필요없는거 아냐? 실제로 주석처리하고 아래의 2개 테스트 돌리면 테스트 성공함.

    @Autowired MemberRepository memberRepository;

    // 기본적인 만들어둔 함수들이 정상 작동하는지 확인하기 위한 테스트
    @Test
    public void basicTest() throws Exception {
        // given
        Member member = new Member("member1", 10);
        memberRepository.save(member);

        // when

        // then
        Member findMember = memberRepository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);

        List<Member> result1 = memberRepository.findAll();
        assertThat(result1).containsExactly(member);

        List<Member> result2 = memberRepository. findByUsername("member1");
        assertThat(result2).containsExactly(member);
    }
}