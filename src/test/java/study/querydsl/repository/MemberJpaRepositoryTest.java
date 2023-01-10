package study.querydsl.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {

    @Autowired
    EntityManager em; // Q. 지금 Test에서 em.find()이런 em을 사용하는 코드 없으니까 필요없는거 아냐? 실제로 주석처리하고 아래의 2개 테스트 돌리면 테스트 성공함.

    @Autowired MemberJpaRepository memberJpaRepository;

    // 기본적인 만들어둔 함수들이 정상 작동하는지 확인하기 위한 테스트
    @Test
    public void basicTest() throws Exception {
        // given
        Member member = new Member("member1", 10);
        memberJpaRepository.save(member);

        // when
    
        // then
        Member findMember = memberJpaRepository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);

        List<Member> result1 = memberJpaRepository.findAll();
        assertThat(result1).containsExactly(member);

        List<Member> result2 = memberJpaRepository. findByUsername("member1");
        assertThat(result2).containsExactly(member);
    }

    // Querydsl 버전 함수들이 정상 작동하는지 확인하기 위한 테스트
    @Test
    public void basicQuerydslTest() throws Exception {
        // given
        Member member = new Member("member1", 10);
        memberJpaRepository.save(member);

        // when

        // then
        Member findMember = memberJpaRepository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);

        List<Member> result1 = memberJpaRepository.findAll_Querydsl(); // querydsl 버전으로 변경
        assertThat(result1).containsExactly(member);

        List<Member> result2 = memberJpaRepository. findByUsername_Querydsl("member1"); // querydsl 버전으로 변경
        assertThat(result2).containsExactly(member);

    }

    @Test
    public void searchTest() throws Exception {
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

        // when

        // Condition 객체 생성
        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(35);
        condition.setAgeLoe(40);
        condition.setTeamName("teamB");

        // repository의 검색관련 함수 사용
        List<MemberTeamDto> result = memberJpaRepository.searchByBuilder(condition);

        // then
        assertThat(result).extracting("username").containsExactly("member4");

        // 주의) 대신 condition 객체에 아무 값도 설정하지 않으면 모든 데이터 조회! -> 실제 운영할 때는 데이터가 엄청 많다! limit, 페이징 처리 필요! 혹은 기본 조건 넣어주기!
    }

    @Test
    public void searchTest_where파라미터사용() throws Exception {
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

        // when

        // Condition 객체 생성
        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(35);
        condition.setAgeLoe(40);
        condition.setTeamName("teamB");

        // repository의 검색관련 함수 사용
        List<MemberTeamDto> result = memberJpaRepository.searchByWhere(condition); // repository의 검색 함수만 where 파라미터 사용한 것으로 변경

        // then
        assertThat(result).extracting("username").containsExactly("member4");
    }
}