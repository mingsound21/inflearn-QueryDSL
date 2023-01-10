package study.querydsl.dto;

import lombok.Data;

@Data
public class MemberSearchCondition {
    // 회원명, 팀명, 나이(ageGoe, ageLoe) -> 화면에서 이러한 데이터가 넘어오면, 이 조건들로 search

    private String username;
    private String teamName;
    private Integer ageGoe;
    private Integer ageLoe;

}
