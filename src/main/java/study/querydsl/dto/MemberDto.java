package study.querydsl.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor// querydsl DTO조회시 프로퍼티 접근 - setter 방법 때 기본 생성자 필요
@Data// @Getter, @Setter, @RequiredArgsConstructor, @ToString 등이 모두 포함
public class MemberDto {

    private String username;
    private int age;

    @QueryProjection // 생성자에 @QueryProjection
    public MemberDto(String username, int age) {
        this.username = username;
        this.age = age;
    }
}
