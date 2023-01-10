package study.querydsl.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;

import javax.persistence.EntityManager;
import java.util.List;

import static org.springframework.util.StringUtils.hasText;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;


public class MemberRepositoryImpl implements MemberRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    public MemberRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<MemberTeamDto> search(MemberSearchCondition condition){
        return queryFactory
                .select(new QMemberTeamDto(
                        member.id,
                        member.username,
                        member.age,
                        team.id,
                        team.name
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()), // 아래에서 조건식을 반환하는 함수 생성
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                ) // where절 파라미터 사용
                .fetch();
    }

    /**
     * 간단한 페이징 - fetchResults
     * 컨텐츠 쿼리, count 쿼리 총 2개의 쿼리를 날림
     */
    @Override
    public Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable) {
        QueryResults<MemberTeamDto> results = queryFactory
                .select(new QMemberTeamDto(
                        member.id,
                        member.username,
                        member.age,
                        team.id,
                        team.name
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()), // 아래에서 조건식을 반환하는 함수 생성
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                ) // where절 파라미터 사용
                .offset(pageable.getOffset()) // 어디부터
                .limit(pageable.getPageSize()) // 한 페이지당 몇개
                .fetchResults();// fetchResults - 컨텐츠 쿼리, count 쿼리 총 2개의 쿼리를 날림

        List<MemberTeamDto> content = results.getResults();
        long total = results.getTotal();

        return new PageImpl<>(content, pageable, total); // PageImpl : Page의 구현체
    }

    /**
     * 복잡한 페이징 - fetch, fetchCount
     * 데이터 내용과 전체 카운트를 별도로 조회
     */
    @Override
    public Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable) {
        // fetch - 컨텐츠 가져오는 쿼리
        List<MemberTeamDto> content = getContent(condition, pageable);// 컨텐츠만 가져옴

        // 전체 데이터 수
        long total = getTotal(condition);

        return new PageImpl<>(content, pageable, total); // PageImpl : Page의 구현체
    }

    /**
     * 컨텐츠 가져오는 쿼리
     */
    private long getTotal(MemberSearchCondition condition) {
        return queryFactory
                .selectFrom(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()), // 아래에서 조건식을 반환하는 함수 생성
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                ) // where절 파라미터 사용
                .fetchCount();
    }

    /**
     * total count 가져오는 쿼리
     */
    private List<MemberTeamDto> getContent(MemberSearchCondition condition, Pageable pageable) {
        return queryFactory
                .select(new QMemberTeamDto(
                        member.id,
                        member.username,
                        member.age,
                        team.id,
                        team.name
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()), // 아래에서 조건식을 반환하는 함수 생성
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                ) // where절 파라미터 사용
                .offset(pageable.getOffset()) // 어디부터
                .limit(pageable.getPageSize()) // 한 페이지당 몇개
                .fetch();
    }


    private BooleanExpression usernameEq(String username) { // 반환 타입이 Predicate(interface)보다는 BooleanExpression이 낫다! -> 그래야 나중에 조립 가능
        return hasText(username) ?  member.username.eq(username) : null;
    }

    private BooleanExpression teamNameEq(String teamName) {
        return hasText(teamName) ?  team.name.eq(teamName) : null; // 바로 team.name
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe != null ? member.age.goe(ageGoe) : null;
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe != null ? member.age.loe(ageLoe) : null;
    }
}
