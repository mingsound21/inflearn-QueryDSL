package study.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Hello;
import study.querydsl.entity.QHello;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@Commit
class QuerydslApplicationTests {

	@Autowired
	EntityManager em;
	// 참고: spring 표준 스펙에서는 @Autowired 대신 @PersistenceContext 사용, spring 최신버전에서는 @Autowired 사용 가능

	@Test
	void contextLoads() {
		Hello hello = new Hello();
		em.persist(hello); // hello 엔티티 저장

		JPAQueryFactory query = new JPAQueryFactory(em);// querydsl 사용을 위해 JPAQueryFactory 생성
		QHello qHello = new QHello("h");// alias(별칭) 설정
		/*
		QHello qHello = new QHello("h");를
		다음과 같이 사용도 가능: QHello qHello = QHello.hello;

		만들어진 QHello를 보면 static final로 hello라고 자기 자신을 만들어서 필드로 선언해놓음
		 */


		// querydsl사용 - 내 생각) em.find(hello)랑 같은건데 querydsl을 사용한 버전인 듯
		Hello result = query
				.selectFrom(qHello) // querydsl을 쓸 때, query와 관련된것은 Q타입을 넣어야함
				.fetchOne();

		assertThat(result).isEqualTo(hello);// querydsl 동작 확인
		assertThat(result.getId()).isEqualTo(hello.getId());// 롬복 동작 확인
	}
}
