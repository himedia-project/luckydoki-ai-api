package com.himedia.luckydokiaiapi.domain.report.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@ToString
public class MemberDetailDTO {
    private String email;
    private String phone;
    private String nickName;
    private String profileImage;
    private String roleName;
    private Long shopId;
    private String shopImage;
    private boolean sellerRequested;    // seller_application 여부
    private Long activeCouponCount;     // 사용가능한 쿠폰 수
    private int monthlySales;           // 월 매출
    private int reviewCount;            // 리뷰 수
    private int monthlyPurchase;        // 월 구매량

}
