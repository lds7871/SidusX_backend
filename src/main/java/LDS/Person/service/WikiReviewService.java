package LDS.Person.service;

import LDS.Person.dto.request.WikiReviewCreateRequest;
import LDS.Person.dto.request.WikiReviewPageQueryRequest;
import LDS.Person.dto.request.WikiReviewUpdateRequest;
import LDS.Person.dto.response.PageResponse;
import LDS.Person.dto.response.WikiReviewListResponse;
import LDS.Person.dto.response.WikiReviewResponse;

/**
 * Wiki 审核服务接口
 */
public interface WikiReviewService {

    /**
     * 创建审核记录
     */
    WikiReviewResponse createReview(WikiReviewCreateRequest request);

    /**
     * 更新审核状态
     */
    boolean updateReviewStatus(WikiReviewUpdateRequest request);

    /**
     * 分页查询审核记录
     */
    PageResponse<WikiReviewListResponse> pageQuery(WikiReviewPageQueryRequest request);

    /**
     * 根据审核 ID 获取审核详情
     */
    WikiReviewResponse getReviewDetail(Long wikireviewId);
}
