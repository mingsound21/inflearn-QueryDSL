package study.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberDto;
import study.querydsl.dto.UserDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import java.util.List;

import static com.querydsl.jpa.JPAExpressions.*;
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

    /**
     * ON절 - 1. 조인 대상 필터링
     *
     * 회원과 팀 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
     * JPQL: SELECT m, t FROM Member m LEFT JOIN m.team t ON t.name = 'teamA';
     */
    @Test
    public void join_on_filtering() {
        List<Tuple> result = queryFactory
                .select(member, team) // 결과 tuple인 이유: select에서 여러 타입 지정되어서
                .from(member)
                .leftJoin(member.team, team).on(team.name.eq("teamA"))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }

        // 내부 조인에서의 on절은 where절에 작성해도 결과가 같다.
        List<Tuple> innerJoin_where = queryFactory
                .select(member, team)
                .from(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        for (Tuple tuple : innerJoin_where) {
            System.out.println("tuple = " + tuple); // 대신 결과에서는 모든 회원이 아니라, teamA인 회원만 나옴(2명)
        }
    }

    /**
     *  ON절 - 2. 연관관계 없는 엔티티 외부 조인
     *
     * 예) 회원의 이름과 팀의 이름이 같은 대상 외부 조인
     *
     * JPQL: SELECT m, t FROM Member m LEFT JOIN Team t on m.username = t.name
     * SQL: SELECT m.*, t.* FROM Member m LEFT JOIN Team t ON m.username = t.name
     */
    @Test
    public void join_on_no_relation() {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));

        // on 조인
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team).on(member.username.eq(team.name)) // member.username과 team.name은 관계가 없는데, 이 두 필드로 외부 조인
                .fetch();

        // 일반 leftjoin
        List<Tuple> result2 = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team) // member.team_id = team.team_id
                .fetch();

        // leftJoin 부분 잘 보기
        // 일반 조인 : leftJoin(member.team, team)
        // on 조인 : from(member).leftJoin(team).on(xxx)


        System.out.println("======= <ON 조인> : .from(member).leftJoin(team).on(member.username.eq(team.name)) =======");
        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
        System.out.println("\n\n");

        System.out.println("======= <일반 조인> : .from(member).leftjoin(member.team, team) =======");
        for (Tuple tuple : result2) {
            System.out.println("tuple = " + tuple);
        }

    }

    @PersistenceUnit
    EntityManagerFactory emf;

    /**
     * 페치 조인 - 미적용
     */
    @Test
    public void fetchJoinNo() throws Exception {
        // 영속성 컨텍스트 깨끗하게 비움
        em.flush();
        em.clear();

        // 멤버 1명 조회
        // member(N) - team(1) : LAZY -> member 조회시 team은 프록시
        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam()); // emf.getPersistenceUnitUtil().isLoaded(객체)) : 해당 객체가 초기화되었는지 여부
        assertThat(loaded).as("페치 조인 미적용").isFalse();
    }

    /**
     * 페치 조인 - 적용
     */
    @Test
    public void fetchJoinUse() throws Exception {
        // 영속성 컨텍스트 깨끗하게 비움
        em.flush();
        em.clear();

        // 멤버 1명 조회
        // member(N) - team(1) : LAZY -> member 조회시 team은 프록시
        Member findMember = queryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin() // join 뒤에 .fetchJoin() : member 조회시 연관된 Team까지 한방쿼리로 가져옴
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam()); // emf.getPersistenceUnitUtil().isLoaded(객체)) : 해당 객체가 초기화되었는지 여부
        assertThat(loaded).as("페치 조인 적용").isTrue();
    }

    /**
     * 서브 쿼리 - eq 사용
     */
    @Test
    public void subQuery() throws Exception {
        QMember memberSub = new QMember("memberSub"); // 서브쿼리 테이블명은 메인쿼리 테이블명과 달라야하기때문

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq( // eq 사용) 메인 쿼리: 멤버의 나이가 최댓값인 회원 조회
                        // 서브쿼리: member의 나이의 최댓값 조회
                        select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetch();

        assertThat(result).extracting("age")
                .containsExactly(40);

    }

    /**
     * 서브 쿼리 - goe 사용
     */
    @Test
    public void subQueryGoe() throws Exception {
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.goe( // goe 사용) 메인 쿼리: 멤버의 나이가 평균값보다 크거나 같은 멤버 조회
                        // 서브쿼리: member의 나이의 평균값 조회
                        select(memberSub.age.avg())
                                .from(memberSub)
                ))
                .fetch();

        assertThat(result).extracting("age")
                .containsExactly(30, 40);

    }

    /**
     * 서브 쿼리 - in
     */
    @Test
    public void subQueryIn() throws Exception {
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.in( // in 사용) 메인 쿼리: 멤버의 나이 10살보다 많은 멤버 조회
                        // 서브쿼리: 10살보다 많은 나이값들 조회
                        select(memberSub.age)
                                .from(memberSub)
                                .where(memberSub.age.gt(10))
                ))
                .fetch();

        assertThat(result).extracting("age")
                .containsExactly(20, 30, 40);
    }

    /**
     * 서브 쿼리 - select 절
     */
    @Test
    public void selectSubQuery() throws Exception {
        QMember memberSub = new QMember("memberSub");

        List<Tuple> result = queryFactory
                .select(member.username,
                        // 서브 쿼리: 유저 평균 나이 조회
                        select(memberSub.age.avg())
                                .from(memberSub))
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    /**
     * case문 - 단순 조건
     */
    @Test
    public void basicCase() throws Exception {
        List<String> result = queryFactory
                .select(member.age
                        .when(10).then("열살")
                        .when(20).then("스무살")
                        .otherwise("기타"))
                .from(member) // from절 안쓰면 No sources given 오류 발생
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    /**
     * case문 - 복잡한 조건 - CaseBuilder()사용
     */
    @Test
    public void complexCase() throws Exception {
        List<String> result = queryFactory
                .select(new CaseBuilder()
                        .when(member.age.between(0, 20)).then("0~20살")
                        .when(member.age.between(21, 30)).then("21~30살")
                        .otherwise("기타"))
                .from(member) // from절 안쓰면 No sources given 오류 발생
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    /**
     * case문 - orderBy에서 case문 함께 사용
     */
    @Test
    public void orderByCase() throws Exception {
        // 1. 0 ~ 30살 아닌 회원 가장 먼저 출력
        // 2. 0 ~ 20살 회원 출력
        // 3. 21 ~ 30살 회원 출력

        // 위의 설명에 따라서 우선순위 부여(우선순위 높을 수록 큰 숫자 부여)
        NumberExpression<Integer> rankPath = new CaseBuilder()
                .when(member.age.between(0, 20)).then(2)
                .when(member.age.between(21, 30)).then(1)
                .otherwise(3);

        List<Tuple> result = queryFactory
                .select(member.username, member.age, rankPath) // 나이에 따라서 rankPath는 1, 2, 3이 조회됨
                .from(member)
                .orderBy(rankPath.desc()) // rankPath로 정렬
                .fetch();

        for (Tuple tuple : result) {
            String username = tuple.get(member.username);
            Integer age = tuple.get(member.age);
            Integer rank = tuple.get(rankPath);
            System.out.println("username = " + username + " age = " + age + " rank = "
                    + rank);
        }
    }

    /**
     * 상수 - Expressions.constant("상수")
     */
    @Test
    public void constant() throws Exception {
        List<Tuple> result = queryFactory
                .select(member.username, Expressions.constant("A")) // 상수
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    /**
     * 문자 더하기
     */
    @Test
    public void concat() throws Exception {

        // username_age로 문자열 합쳐서 select하고 싶을 때
        List<String> result = queryFactory
                .select(member.username.concat("_").concat(member.age.stringValue())) // 문자 아닌 타입들은 stringValue()로 문자로 변환 가능(ENUM 처리시에도 자주 사용!)
                .from(member)
                .where(member.username.eq("member1"))
                .fetch();


        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    /**
     * 프로젝션과 결과반환 기본 - 프로젝션 대상이 하나: 타입 정확히 지정 가능
     */
    @Test
    public void simpleProjection() throws Exception {
        List<String> result = queryFactory
                .select(member.username)
                .from(member)
                .fetch();

        // member 객체 조회 -> 이것도 프로젝션 대상이 하나(반환 타입 명확하게 지정 Member라고 가능)
        List<Member> result2 = queryFactory
                .select(member)
                .from(member)
                .fetch();
    }

    /**
     * 프로젝션 - 프로젝션 대상이 둘 이상: 튜플 조회
     */
    @Test
    public void tupleProjection() throws Exception {
        List<Tuple> result = queryFactory
                .select(member.username, member.age) // username, age 2개 프로젝션 -> 반환 타입 지정 못하니까 Tuple
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            String username = tuple.get(member.username); // 튜플에서 값 꺼내기 : Tuple.get(Q타입.속성)
            Integer age = tuple.get(member.age);

            System.out.println("username = " + username);
            System.out.println("age = " + age);
        }
        

    }

    /**
     * 프로젝션 결과 반환 - DTO 조회
     *
     * JPQL 버전
     */
    @Test
    public void findDtoByJPQL() throws Exception {
        List<MemberDto> result = em.createQuery("select new study.querydsl.dto.MemberDto(m.username, m.age) from Member m", MemberDto.class) // select절에 MemberDto의 생성자 호출(단, 패키지 명을 풀로 작성)
                .getResultList();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    /**
     * 프로젝션 결과 반환 - DTO 조회
     *
     * querydsl 버전
     */
    // 방법 1) 프로퍼티 접근 - Setter
    // * 기본 생성자, setter 필요
    @Test
    public void findDtoBySetter() throws Exception {
        List<MemberDto> result = queryFactory
                .select(Projections.bean(MemberDto.class, // bean
                        member.username, // 순서 상관 X
                        member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    // 방법 2) 필드 직접 접근
    // * getter, setter 필요 X, 필드에 바로 값을 넣음.
    // * 기본 생성자는 필요
    @Test
    public void findDtoByField() throws Exception {
        List<MemberDto> result = queryFactory
                .select(Projections.fields(MemberDto.class, // fields
                        member.age, // 순서 상관 X
                        member.username))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    // 방법 3) 생성자 사용
    // * 기본 생성자 필요 X, 생성자 파라미터 순서 지켜야함.
    @Test
    public void findDtoByConstructor() throws Exception {
        List<MemberDto> result = queryFactory
                .select(Projections.constructor(MemberDto.class, // constructor
                        member.username,// 대신 이 순서가 DTO의 생성자와 순서가 맞아야함.
                        member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    /**
     * setter 프로퍼티접근, 필드 직접 접근 : DTO와 Entity의 필드명이 다를 경우 별칭 부여 필요
     *
     * + 서브 쿼리 별칭 부여 필요
     */
    @Test
    public void findUserDto() throws Exception {
        QMember memberSub = new QMember("memberSub"); // 서브쿼리용

        // 내용 1) Entity와 Dto의 필드명이 다를 경우 : as로 해결!
        // as : 필드에 별칭 적용
        List<UserDto> result = queryFactory
                .select(Projections.fields(UserDto.class,
                        //member.username, // 문제 : 프로퍼티, 필드 접근 방식에서 이름이 다를 경우 제대로 select 안됨 -> sout name = null로 나옴
                        member.username.as("name"),// 해결 : as.("DTO의 필드명") 또는 ExpressionUtils(member.username, "name")
                        member.age))
                .from(member)
                .fetch();

        // 내용 2) ExpressionUtils.as(source, alias) : 필드, 서브쿼리에 별칭 적용
        List<UserDto> result2 = queryFactory
                .select(Projections.fields(UserDto.class,
                        member.username.as("name"),
                        // 서브쿼리 작성시, 어떤 필드에 값을 넣어줘야하는지 필드명 별칭부여 필요
                        ExpressionUtils.as(select(memberSub.age.max())
                                .from(memberSub), "age")
                ))
                .from(member)
                .fetch();

        for (UserDto userDto : result) {
            System.out.println("userDto = " + userDto);
        }

        for (UserDto userDto : result2) {
            System.out.println("userDto = " + userDto);
        }
    }

    /**
     * 생성자 사용의 경우: DTO와 Entity의 필드명이 달라도, 이름이 아닌 타입을 보기 때문에 상관없다.
     */
    @Test
    public void findUserDtoByConstructor() throws Exception {
        List<UserDto> result = queryFactory
                .select(Projections.constructor(UserDto.class, // constructor, UserDto
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (UserDto userDto : result) {
            System.out.println("userDto = " + userDto);
        }
    }
}
