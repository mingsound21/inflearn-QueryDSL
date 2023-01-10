package study.querydsl.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

@Data
public class MemberTeamDto {
    private Long memberId;
    private String username;
    private int age;
    private Long teamId;
    private String teamName;

    @QueryProjection // 생성자로 DTO 조회하는 방법 사용시 필요, gradle - compileQuerydsl - Q파일 생성 필요
    public MemberTeamDto(Long memberId, String username, int age, Long teamId, String teamName) {
        this.memberId = memberId;
        this.username = username;
        this.age = age;
        this.teamId = teamId;
        this.teamName = teamName;
    }
}

// @QueryProjection 단점
// DTO가 가급적 순수 했으면 좋겠지만, DTO가 querydsl 라이브러리에 의존하게됨(import querydsl)
// -> 싫다면 Projections.bean(), Projections.fields(), Projections.contructor() 사용

