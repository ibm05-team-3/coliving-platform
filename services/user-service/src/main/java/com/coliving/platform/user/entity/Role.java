package com.coliving.platform.user.entity;

/**
 * 사용자 역할.
 * 계정에는 USER, CANDIDATE, RESIDENT, ADMIN 중 하나를 저장한다.
 * GUEST는 로그인하지 않은 방문자라 계정에 저장하지 않는다.
 */
public enum Role {
    /** 비로그인 상태에서 매물 조회만 가능한 방문자. */
    GUEST,

    /** 회원가입 후 로그인한 일반 회원. 매물 검색·조회, 입주 신청, 마이페이지, 계약 진행. */
    USER,

    /** 입주 신청서를 제출하여 계약·결제 절차를 진행 중인 사용자. 프로필 작성, 본인인증, 전자서명, 결제, 관리자와의 DM. */
    CANDIDATE,

    /** 계약 완료 및 입주 후 실제 거주자. 공용시설 예약, 커뮤니티 채팅, 알림. */
    RESIDENT,

    /** 소유·운영하는 호스트 최고 관리자. 운영 대시보드, 모니터링, 로그 분석, 시스템 설정. */
    ADMIN;

    /** 가입 직후 계정에 부여하는 역할. */
    public static Role defaultAccountRole() {
        return USER;
    }

    /** 로그인 계정에 저장할 수 있는 역할이면 true. */
    public boolean isAccountRole() {
        return this != GUEST;
    }
}
