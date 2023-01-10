package study.querydsl.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Profile("local")
@Component // spring bean 자동 등록
@RequiredArgsConstructor
public class InitMember {

    private final InitMemberService initMemberService;

    @PostConstruct// 의존성 주입이 이루어진 후 초기화를 수행하는 메서드
    public void init(){
        initMemberService.init();
    }

    // 내부 클래스
    @Component // 생성자 주입
    static class InitMemberService{
        @PersistenceContext
        private EntityManager em;

        @Transactional
        public void init(){
            Team teamA = new Team("teamA");
            Team teamB = new Team("teamB");
            em.persist(teamA);
            em.persist(teamB);

            for (int i = 0; i <100; i++){
                Team selectedTeam = i % 2 == 0 ? teamA : teamB;
                em.persist(new Member("member" + i, i, selectedTeam));
            }
        }

    }
}

// 질문
// Q. InitMemberService의 init 함수의 내용을 @PostConstruct가 붙은 init함수 안에 그냥 직접 적으면 안되나요?
// A. 스프링 life cycle상 @Transactional과 @PostConstruct가 같이 있을 수가 없어서 안됨.
