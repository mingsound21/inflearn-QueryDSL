package study.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static study.querydsl.entity.QMember.*;
import static study.querydsl.entity.QTeam.*;


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

    /**
     * 정렬
     * 회원 정렬 순서
     * 1. 회원 나이 내림차순(desc)
     * 2. 회원 이름 올림차순(asc)
     * 단, 2에서 회원 이름이 없으면 마지막에 출력(nulls last)
     */
    @Test
    public void sort() {
        // 기존 4명의 회원에 3명의 회원 추가
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(100)) // 회원 중 나이가 100인 회원 찾기
                .orderBy(member.age.desc(), member.username.asc().nullsLast()) // 정렬
                .fetch();

        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member memberNull = result.get(2);

        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isNull();
    }

    /**
     * 페이징
     */
    @Test
    public void paging1() {
        List<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1) // 앞에 몇개 스킵할건지, 0부터 시작
                .limit(2) // 몇개의 데이터 가져올건지
                .fetch();

        assertThat(result.size()).isEqualTo(2);
    }

    /**
     * 페이징 - 전체 조회 수 필요
     */
    @Test
    public void paging2() {
        // fetchResults : 쿼리 2번(count query, contents가져오는 query)
        QueryResults<Member> queryResults = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1) // 앞에 몇개 스킵할건지, 0부터 시작
                .limit(2) // 몇개의 데이터 가져올건지, 최대 2건 조회
                .fetchResults();

        assertThat(queryResults.getTotal()).isEqualTo(4);
        assertThat(queryResults.getLimit()).isEqualTo(2);
        assertThat(queryResults.getOffset()).isEqualTo(1);
        assertThat(queryResults.getResults().size()).isEqualTo(2); // 컨텐츠
    }

    /**
     * 집합
     * JPQL이 제공하는 집합함수 모두 제공
     *
     *  == JPQL ==
     *  select
     *  COUNT(m), //회원수
     *  SUM(m.age), //나이 합
     *  AVG(m.age), //평균 나이
     *  MAX(m.age), //최대 나이
     *  MIN(m.age) //최소 나이
     *  from Member m
     */
    @Test
    public void aggregation() {
        List<Tuple> result = queryFactory // 결과 타입 : querydsl이 제공하는 Tuple
                .select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min()
                )
                .from(member)
                .fetch();

        Tuple tuple = result.get(0); // tuple 리스트에서 tuple 꺼내오고
        assertThat(tuple.get(member.count())).isEqualTo(4); // tuple.get(위에서 select 다음에 썼던 거 똑같이 작성)
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }

    /**
     * group by
     *
     * 팀의 이름과 각 팀의 평균 연령을 구해라
     */
    @Test
    public void group() {
        List<Tuple> result = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                // .having() // having도 지원
                .fetch();

        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        assertThat(teamA.get(team.name)).isEqualTo("teamA"); // tuple.get(select절에 썼던 거)
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);

        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);
    }

    /**
     * 조인
     *
     * 팀 A에 소속된 모든 회원을 조회
     */
    @Test
    public void join() {
        List<Member> result = queryFactory
                .selectFrom(member)
                .join(member.team, team) // 2번째 파라미터는 QTeam.team을 의미
                .where(team.name.eq("teamA"))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("member1", "member2");
    }

    /**
     * 세타 조인 - 연관관계 없는 필드 조인
     * 회원의 이름이 팀 이름과 같은 회원 조회
     */
    @Test
    public void theta_join() {
        // 멤버 생성
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        List<Member> result = queryFactory
                .select(member)
                .from(member, team) // 그냥 from절에 Q타입 나열 -> 모든 member 테이블 row, 모든 team 테이블의 row 조인
                .where(member.username.eq(team.name))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("teamA", "teamB");
    }
}
