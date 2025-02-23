package com.himedia.luckydokiaiapi.domain.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberMetricsResponse {

    private String email;
    private String phone;
    private String nickName;
    private String profileImage;
    private String roleName;
    private Long shopId;
    private String shopImage;
    private boolean sellerRequested;    // seller_application 여부
    private Long activeCouponCount;     // 사용가능한 쿠폰 수
    private int monthlySales;           // 월 매출(셀러용)
    private int reviewCount;            // 리뷰 수
    private int monthlyPurchase;        // 월 구매량(유저용)

}
