package study.querydsl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import study.querydsl.entity.Member;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom, QuerydslPredicateExecutor<Member> { // 인터페이스 다중 상속 가능
    // 정적 쿼리 - 메소드 이름으로 JPQL 쿼리 생성
    // select m from Member m where m.username = ?;
    List<Member> findByUsername(String username);
}
