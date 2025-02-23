package com.himedia.luckydokiaiapi.config;

import com.himedia.luckydokiaiapi.domain.product.dto.ProductDTO;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Configuration
public class LuckyDokiLoader {

    private final VectorStore vectorStore;
    private final JdbcClient jdbcClient;
    private final RestTemplate restTemplate;

    @Value("${api.product.url}")
    private String productApiUrl;

    public LuckyDokiLoader(VectorStore vectorStore, JdbcClient jdbcClient, RestTemplateBuilder restTemplateBuilder) {
        this.vectorStore = vectorStore;
        this.jdbcClient = jdbcClient;
        this.restTemplate = restTemplateBuilder.build();
    }

    @PostConstruct
    public void init() throws Exception {
        Integer count = jdbcClient.sql("select count(*) from mall_vector")
                .query(Integer.class)
                .single();
        log.info("No of Records in the PG Vector Store={}", count);
        
        if (count == 0) {
            loadProductData();
        }
    }

    private void loadProductData() {
        try {
            log.info("Starting to load product data from API...");
            
            // API에서 상품 정보 가져오기
            ProductDTO.Response[] products = restTemplate.getForObject(
                    productApiUrl + "/api/product/list",
                    ProductDTO.Response[].class
            );

            if (products == null || products.length == 0) {
                log.warn("No products found from API");
                return;
            }

            log.info("Fetched {} products from API", products.length);

            TextSplitter textSplitter = new TokenTextSplitter();
            
            for (ProductDTO.Response product : products) {
                // 상품 정보를 문서화
                String productInfo = formatProductInfo(product);
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("type", "product");
                metadata.put("productId", product.getId());
                metadata.put("category", product.getCategoryAllName());

                Document document = new Document(productInfo, metadata);
                List<Document> splittedDocs = textSplitter.split(document);
                
                log.info("Processing product: {}", product.getName());
                vectorStore.add(splittedDocs);
                
                Thread.sleep(100); // Rate limiting
            }
            
            log.info("Successfully loaded {} products into vector store", products.length);
            
        } catch (Exception e) {
            log.error("Error loading product data: ", e);
        }
    }

    private String formatProductInfo(ProductDTO.Response product) {
        return String.format("""
                상품pk: %d
                상품명: %s
                상품이미지: %s
                카테고리: %s
                상품코드: %s
                판매자(셀러): %d
                판매자(셀러): %s (%s)
                판매자(셀러) 이미지: %s
                상품 태그: %s
                가격 정보:
                - 정가: %d원
                - 할인가: %d원 (할인율: %d%%)
                상품 상태:
                - 재고: %d개
                - 신상품: %s
                - 베스트: %s
                - 이벤트: %s
                상품 평가:
                - 평균 평점: %.1f점
                - 리뷰 수: %d개
                - 좋아요 수: %d개
                - 판매량: %d개
                등록일: %s
                최근 수정일: %s
                """,
                product.getId(),
                product.getName(),
                product.getUploadFileNames().get(0),
                product.getCategoryAllName(),
                product.getCode(),
                product.getShopId(),
                product.getShopName(), product.getNickName(),
                product.getShopImage(),
                String.join(", ", product.getTagStrList()),
                product.getPrice(),
                product.getDiscountPrice(),
                product.getDiscountRate(),
                product.getStockNumber(),
                "Y".equals(product.getIsNew()) ? "예" : "아니오",
                "Y".equals(product.getBest()) ? "예" : "아니오",
                "Y".equals(product.getEvent()) ? "예" : "아니오",
                product.getReviewAverage(),
                product.getReviewCount(),
                product.getLikesCount(),
                product.getSalesCount(),
                product.getCreatedAt(),
                product.getModifiedAt()
        );
    }
}
