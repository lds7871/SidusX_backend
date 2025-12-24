package LDS.Person.repository;

import LDS.Person.entity.WikiReview;
import org.apache.ibatis.annotations.*;

/**
 * Wiki 审核数据访问层
 */
@Mapper
public interface WikiReviewMapper {

    /**
     * 插入审核记录
     */
    @Insert("INSERT INTO wiki_review (wiki_id, texts, tags, version, update_time, update_user, wiki_states) " +
            "VALUES (#{wikiId}, #{texts}, #{tags, typeHandler=org.apache.ibatis.type.ArrayTypeHandler}, " +
            "#{version}, #{updateTime}, #{updateUser}, #{wikiStates})")
    @Options(useGeneratedKeys = true, keyProperty = "wikireviewId", keyColumn = "wikireview_id")
    int insertReview(WikiReview review);

    /**
     * 更新审核状态
     */
    @Update("UPDATE wiki_review SET wiki_states = #{wikiStates}, update_time = #{updateTime}, update_user = #{updateUser} " +
            "WHERE wikireview_id = #{wikireviewId}")
    int updateReviewStatus(@Param("wikireviewId") Long wikireviewId, 
                           @Param("wikiStates") Integer wikiStates, 
                           @Param("updateTime") java.time.LocalDateTime updateTime,
                           @Param("updateUser") String updateUser);
}
