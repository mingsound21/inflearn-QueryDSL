package study.querydsl.entity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
//@Commit 테이블에 데이터 남아있으면 다른 테스트에 영향을 줌. (단, 테스트 끝날 때마다 rollback해서 테스트 종료 후에는 테이블에 데이터 없음.)
class MemberTest {

    @Autowired
    EntityManager em;

    @Test
    public void testEntity() {
        // given
        // 팀 2개 생성, 저장
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");

        em.persist(teamA);
        em.persist(teamB);

        // 멤버 4명 생성, 저장
        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);

        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        // em 초기화
        em.flush();
        em.clear();

        // then
        // 모든 멤버 조회 쿼리
        List<Member> members = em.createQuery("select m from Member m", Member.class)
                .getResultList();

        // 지금은 눈으로 보기 위해서 sout으로 하고, 실제로 자동화된 테스트 작성할때는 assert 사용!
        for (Member member : members) {
            System.out.println("member = " + member); // @ToString
            System.out.println("-> member.team = " + member.getTeam()); // member의 소속 팀 출력
        }

    }
}