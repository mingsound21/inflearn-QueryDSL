package study.querydsl.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;

import java.util.List;

public interface MemberRepositoryCustom {
    List<MemberTeamDto> search(MemberSearchCondition condition);
    Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable); // 전체 count까지 한번의 쿼리로
    Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable); // 데이터 내용과 전체 카운트를 별도로 조회
    // 참고로 Page, Pageable은 springframework.data의 것을 import

}
