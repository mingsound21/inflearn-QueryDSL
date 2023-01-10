package study.querydsl.repository;

import com.querydsl.core.BooleanBuilder;
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
     * 검색
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
                .select(new QMemberTeamDto(
                        member.id.as("memberId"), // member Entity는 필드명 id, MemberTeamDto는 필드명 memberId
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(builder) // builder
                .fetch();
    }
}
