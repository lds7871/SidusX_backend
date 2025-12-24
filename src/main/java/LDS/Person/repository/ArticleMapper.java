package LDS.Person.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import LDS.Person.entity.Article;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文章 Mapper 接口
 */
@Mapper
public interface ArticleMapper extends BaseMapper<Article> {
}
