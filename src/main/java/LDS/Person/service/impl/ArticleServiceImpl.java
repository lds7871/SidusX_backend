package LDS.Person.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import LDS.Person.entity.Article;
import LDS.Person.repository.ArticleMapper;
import LDS.Person.service.ArticleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 文章服务实现类
 */
@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    private static final Logger logger = LoggerFactory.getLogger(ArticleServiceImpl.class);

    @Override
    public Page<Article> queryArticleByPage(Integer pageNum, Integer pageSize, String title, String tags) {
        try {
            // 设置默认值
            if (pageNum == null || pageNum < 1) {
                pageNum = 1;
            }
            if (pageSize == null || pageSize < 1) {
                pageSize = 10;
            }

            // 创建分页对象
            Page<Article> page = new Page<>(pageNum, pageSize);

            // 创建查询条件
            QueryWrapper<Article> queryWrapper = new QueryWrapper<>();

            // 按标题模糊查询
            if (title != null && !title.isEmpty()) {
                queryWrapper.like("title", title);
            }

            // 按标签模糊查询
            if (tags != null && !tags.isEmpty()) {
                queryWrapper.like("tags", tags);
            }

            // 按更新时间倒序排列
            queryWrapper.orderByDesc("update_time");

            // 执行分页查询
            logger.info("✅ 开始分页查询文章，pageNum: {}, pageSize: {}, title: {}, tags: {}", pageNum, pageSize, title, tags);
            Page<Article> result = this.page(page, queryWrapper);
            logger.info("✅ 分页查询成功，查询到 {} 条记录", result.getTotal());

            return result;
        } catch (Exception e) {
            logger.error("❌ 分页查询文章失败", e);
            throw new RuntimeException("查询失败: " + e.getMessage());
        }
    }

    @Override
    public boolean createArticle(Article article) {
        try {
            // 设置创建时间和更新时间
            LocalDateTime now = LocalDateTime.now();
            article.setCreateTime(now);
            article.setUpdateTime(now);

            logger.info("✅ 开始创建文章，标题: {}", article.getTitle());
            boolean result = this.save(article);
            logger.info("✅ 文章创建成功，文章ID: {}", article.getArticleId());

            return result;
        } catch (Exception e) {
            logger.error("❌ 创建文章失败", e);
            throw new RuntimeException("创建失败: " + e.getMessage());
        }
    }
}
