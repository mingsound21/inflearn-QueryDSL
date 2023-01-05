package study.querydsl.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static study.querydsl.entity.QMember.*;

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
}
