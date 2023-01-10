package study.querydsl.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberDto;
import study.querydsl.dto.QMemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static org.springframework.util.StringUtils.*;
import static study.querydsl.entity.QMember.*;
import static study.querydsl.entity.QTeam.*;

@Repository // @Transactional없어서 Service단에서 붙여주거나, 이 Repository에 추가 필요함!!
public class MemberJpaRepository {
    private final EntityManager em;
    private final JPAQueryFactory queryFactory;


    public MemberJpaRepository(EntityManager em) { // 생성자에서 em injection하면 스프링에서 알아서 injection 해줌
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }

    /**
     * 저장
     */
    public void save(Member member){
        em.persist(member);
    }

    /**
     * id로 회원 조회
     */
    public Optional<Member> findById(Long id){
        Member findMember = em.find(Member.class, id);
        return Optional.ofNullable(findMember);
    }

    /**
     * 회원 전체 조회
     */
    public List<Member> findAll(){
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    // querydsl 버전
    public List<Member> findAll_Querydsl(){
        return queryFactory
                .selectFrom(member)
                .fetch();
    }

    /**
     * 특정 이름을 가진 회원들 조회
     */
    public List<Member> findByUsername(String username){
        return em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", username)
                .getResultList();
    }

    // querydsl 버전
    public List<Member> findByUsername_Querydsl(String username){
        return queryFactory
                .selectFrom(member)
                .where(member.username.eq(username))
                .fetch();
    }

    /**
     * 검색 - 동적 쿼리 생성 by BooleanBuilder
     */
    public List<MemberTeamDto> searchByBuilder(MemberSearchCondition memberSearchCondition){
        BooleanBuilder builder = new BooleanBuilder();

        if (hasText(memberSearchCondition.getUsername())) { // StringUtils.hasText 사용한 이유 : 이름이 null로 들어올 수도 있고 "" 빈 문자열로 들어올 수도 있어서
            builder.and(member.username.eq(memberSearchCondition.getUsername()));
        }
        if (hasText(memberSearchCondition.getTeamName())) {
            builder.and(team.name.eq(memberSearchCondition.getTeamName()));
        }
        if (memberSearchCondition.getAgeGoe() != null){
            builder.and(member.age.goe(memberSearchCondition.getAgeGoe()));
        }
        if (memberSearchCondition.getAgeLoe() != null){
            builder.and(member.age.loe(memberSearchCondition.getAgeLoe()));
        }

        return queryFactory
                .select(new QMemberTeamDto(// @QueryProjection으로 생성한 QDto의 생성자를 사용하기에 Entity, Dto 필드명 달라도 별칭 부여 필요 X
                        member.id, // 생성자를 사용하는 경우에는 Entity, Dto 필드 이름이 달라도 별칭 부여 필요 X - 필드 타입을 체크해서
                        member.username,
                        member.age,
                        team.id,
                        team.name
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(builder) // builder
                .fetch();
    }

    /**
     * 검색 - 동적 쿼리 생성 by where절 파라미터
     *
     * 김영한 선생님은 거의 이것을 사용!
     *  -> 가독성 굿 + 조건식 반환하는 함수들 재사용 가능 및 조립 가능
     */
    public List<MemberTeamDto> searchByWhere(MemberSearchCondition condition){
        return queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"), // member Entity는 필드명 id, MemberTeamDto는 필드명 memberId
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
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
     * where절 파라미터로 동적 쿼리 생성 - 조건식 반환 함수 재사용 예시
     *
     * MemberTeamDto말고 Member객체를 반환
     */
    public List<Member> searchByWhere_returnEntity(MemberSearchCondition condition){
        return queryFactory
                .selectFrom(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()), // 함수들 재사용
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                ) // where절 파라미터 사용
                .fetch();
    }

    /**
     * where절 파라미터에 사용되는 조건식을 반환하는 함수들
     * 큰 장점 : 재사용 가능!!!
     */
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
