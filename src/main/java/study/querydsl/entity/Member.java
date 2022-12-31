package study.querydsl.entity;

import lombok.*;

import javax.persistence.*;

@ToString(of = {"id", "username", "age"}) // @ToString은 연관관계 없는 필드들에만 사용! 연관관계 있는 필드 작성시 무한루프 발생 가능성 존재.(Team 쪽 @ToString에서 다시 member 쪽 @ToString 호출할 경우)
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 기본 생성자 필수, PROTECTED까지 허용. 기본생성자 함부로 사용하지 않도록 방지.
@Getter @Setter
@Entity
public class Member {
    @Id
    @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String username;
    private int age;

    @ManyToOne(fetch = FetchType.LAZY) // XXXToOne LAZY 명시적 작성 필수
    @JoinColumn(name = "team_id") // 연관관계 주인, name에는 외래키 작성
    private Team team;

    // 생성자 - 여러개 만든 이유: 예제에서 필요해서
    // ver1. 이름만
    public Member(String username) {
        this(username, 0);
    }

    // ver2. 이름, 나이
    public Member(String username, int age) {
        this(username, age, null); // 아래 이름, 나이, 팀 3개의 파라미터를 받는 생성자 사용
    }

    // ver3. 이름, 나이, 팀
    public Member(String username, int age, Team team) {
        this.username = username;
        this.age = age;
        if(team != null){
            this.team = team;
        }
    }

    // 팀 변경 메서드, 연관관계 편의 메서드
    public void changeTeam(Team team){
        this.team = team;
        team.getMembers().add(this);// Team쪽 데이터까지 관리
    }
}
