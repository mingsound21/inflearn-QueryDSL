package study.querydsl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.querydsl.entity.Member;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long> {
    // 정적 쿼리 - 메소드 이름으로 JPQL 쿼리 생성
    // select m from Member m where m.username = ?;
    List<Member> findByUsername(String username);
}
