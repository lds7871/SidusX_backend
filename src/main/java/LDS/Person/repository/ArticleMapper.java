package LDS.Person.repository;

import LDS.Person.entity.Article;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 文章 Mapper 接口
 */
@Mapper
public interface ArticleMapper {

    @Insert("INSERT INTO article (title, cover, info, texts, tags, create_time, update_time) " +
            "VALUES (#{title}, #{cover}, #{info}, #{texts}, #{tags}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "articleId", keyColumn = "article_id")
    int insert(Article article);

    @Update("UPDATE article SET title = #{title}, cover = #{cover}, info = #{info}, " +
            "texts = #{texts}, tags = #{tags}, update_time = #{updateTime} " +
            "WHERE article_id = #{articleId}")
    int updateById(Article article);

    @Delete("DELETE FROM article WHERE article_id = #{articleId}")
    int deleteById(Long articleId);

    @Select("SELECT article_id, title, cover, info, texts, tags, create_time, update_time " +
            "FROM article WHERE article_id = #{articleId}")
    @Results({
        @Result(property = "articleId", column = "article_id"),
        @Result(property = "createTime", column = "create_time"),
        @Result(property = "updateTime", column = "update_time")
    })
    Article selectById(Long articleId);

    @Select("SELECT article_id, title, cover, info, texts, tags, create_time, update_time FROM article")
    @Results({
        @Result(property = "articleId", column = "article_id"),
        @Result(property = "createTime", column = "create_time"),
        @Result(property = "updateTime", column = "update_time")
    })
    List<Article> selectAll();
}
