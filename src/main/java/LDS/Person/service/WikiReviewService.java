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
     *
     * @param request 审核创建请求
     * @return 审核响应
     */
    WikiReviewResponse createReview(WikiReviewCreateRequest request);

    /**
     * 更新审核状态（1=通过并覆盖 Wiki 数据，2=拒绝）
     *
     * @param request 审核状态更新请求
     * @return 是否更新成功
     */
    boolean updateReviewStatus(WikiReviewUpdateRequest request);

    /**
     * 分页查询审核记录
     *
     * @param request 分页查询请求
     * @return 分页响应
     */
    PageResponse<WikiReviewListResponse> pageQuery(WikiReviewPageQueryRequest request);

    /**
     * 根据审核 ID 获取审核详情
     *
     * @param wikireviewId 审核记录 ID
     * @return 审核详情响应
     */
    WikiReviewResponse getReviewDetail(Long wikireviewId);
}
