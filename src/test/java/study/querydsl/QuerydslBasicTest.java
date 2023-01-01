package study.querydsl;

import com.querydsl.core.QueryResults;
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

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static study.querydsl.entity.QMember.*;


@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory queryFactory; // 필드 레벨로 가져와도 됨. 동시성 문제 고려 안해도 됨.

    @BeforeEach // 모든 테스트 전에 실행됨
    public void before() {
        queryFactory = new JPAQueryFactory(em); // queryFactory 생성

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
        QMember m = new QMember("m");// 방법 1) 별칭 직접 지정, "m"이라는 값으로 어떤 QMember인지 구분
        // QMember m2 = member; // 방법 2) 기본 인스턴스 사용

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

    // 권장하는 방법, 깔끔함
    // 기본 인스턴스를 static import와 함께 사용
    @Test
    public void startQuerydsl2() {
        // memeber1을 찾아라

        Member findMember = queryFactory
                .select(member)// QMember.member 에서 QMember static import하면 member라고 적을 수 있음.
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        // 검증
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    /**
     * 검색 조건 쿼리 - .and() 체이닝 사용
     */
    @Test
    public void search() {
        // 이름이 member1이면서 나이가 10살인 사람 조회

        Member findMember = queryFactory
                .selectFrom(member) // .select, .from을 합친 것
                .where(  // where 조건 추가: .and, .or로 체이닝해서 조건 추가 가능
                        member.username.eq("member1")
                        .and(member.age.eq(10))
                )
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    /**
     * 검색 조건 쿼리 - where param으로 넘겨 and 사용
     * # 김영한 선생님은 and 조건만 있는 경우 이 방법을 선호
     */
    @Test
    public void searchAndParam() {
        // 이름이 member1이면서 나이가 10살인 사람 조회

        Member findMember = queryFactory
                .selectFrom(member)
                .where(// where: param 넘긴 것들 다 and로 묶임
                        member.username.eq("member1"),
                        member.age.eq(10) // .and 말고 그냥 ,로 끊어서 작성해도 and로 JPQL 작성됨.
                )
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    /**
     * 결과 조회 쿼리
     */
    @Test
    public void resultFetch() {
        // 1. fetch() : 리스트 조회
        List<Member> fetch = queryFactory
                .selectFrom(member)
                .fetch();

        // 2. fetchOne() : 단 건 조회
        Member fetchOne = queryFactory
                .selectFrom(member)
                .fetchOne();

        // 3. fetchFirst() : 처음 1개 조회
        Member fetchFirst = queryFactory
                .selectFrom(member)
                .fetchFirst();// .limit(1).fetchOne()과 같음

        // 4. fetchResults() : 페이징 정보 포함. total count 쿼리까지 총 쿼리 2방 나감
        // 쿼리 2방 나감 (total count 가져와야해서)
        QueryResults<Member> results = queryFactory
                .selectFrom(member)
                .fetchResults();
        
        // 페이징 관련 함수들 사용 가능
        results.getTotal();
        List<Member> content = results.getResults(); // contents 꺼내기
        results.getLimit(); // 쿼리에 쓰인 limit 값 가져오기

        
        // 5. fetchCount() : count쿼리로 변경해서 count 수 조회
        long total = queryFactory
                .selectFrom(member)
                .fetchCount();

    }

}
