package study.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.*;


@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory queryFactory; // 필드 레벨로 가져와도 됨. 동시성 문제 고려 안해도 됨.

    @BeforeEach // 모든 테스트 전에 실행됨
    public void before() {
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
    }

    @Test
    public void startJPQL() {
        // memeber1을 찾아라
        String qlString = "select m from Member m where m.username = :username";

        Member findMember = em.createQuery(qlString, Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void startQuerydsl() {
        // memeber1을 찾아라

        // 1. 일단 JPAQueryFactory 생성으로 시작
        queryFactory = new JPAQueryFactory(em); // em 넘겨줘야함

        // 2. Q클래스 생성
        // QMember, QTeam 파일 생성: gradle 탭 - Tasks - other - compileQuerydsl 실행
        // >> build/generated 안에 생성된 Q파일 확인 가능
        QMember m = new QMember("m");// "m"이라는 값으로 어떤 QMember인지 구분

        // 3. 쿼리 작성
        Member findMember = queryFactory
                .select(m)
                .from(m)
                .where(m.username.eq("member1")) // 중요!) 파라미터 바인딩 대신 eq로 짜도 자동으로 JDBC에 있는 prepareStatement로 파라미터 바인딩함. -> 쿼리 나간 거 보면 ?가 있음.
                                                        // ** 파라미터 바인딩 -> SQL Injection 공격 방지 가능
                .fetchOne();

        // 검증
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

}
