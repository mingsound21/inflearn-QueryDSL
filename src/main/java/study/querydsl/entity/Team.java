package study.querydsl.entity;


import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@ToString(of = {"id", "name"})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter @Setter
@Entity
public class Team {

    @Id @GeneratedValue
    @Column(name = "team_id")
    private Long id;

    private String name;

    @OneToMany(mappedBy = "team") // 연관관계 주인의 반대, mappedBy의 값으로는 Member의 Team 필드명 작성
    private List<Member> members = new ArrayList<>();

    public Team(String name){
        this.name = name;
    }
}
