package com.neu.hotel.common.result;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.neu.hotel.common.utils.PageUtil;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通用 CRUD 控制器基类
 *
 * <p>为所有实体提供一套标准的增删改查接口，子类继承后自动获得以下接口：
 * <ul>
 *   <li>GET  /list          - 查询所有记录</li>
 *   <li>GET  /{id}          - 根据 ID 查询单条记录</li>
 *   <li>POST                - 新增记录</li>
 *   <li>PUT                  - 更新记录</li>
 *   <li>DELETE /{id}        - 根据 ID 删除记录</li>
 *   <li>GET  /page          - 分页查询（参数：current=页码，size=每页条数）</li>
 *   <li>GET  /batch         - 批量删除（参数：ids=ID列表）</li>
 * </ul>
 *
 * <p>子类必须通过依赖注入提供 getBaseService() 方法的实现，
 * 返回对应的 MyBatis-Plus IService 实例。
 *
 * <p>使用示例：
 * <pre>
 * &#64;RestController
 * &#64;RequestMapping("/dept")
 * public class DeptController extends CommonController&lt;DeptService, Dept&gt; {
 *     &#64;Autowired
 *     private DeptService deptService;
 *
 *     &#64;Override
 *     protected DeptService getBaseService() {
 *         return deptService;
 *     }
 * }
 * </pre>
 *
 * @param <S> IService 子类型
 * @param <T> 实体类型
 */
public class CommonController<S extends IService<T>, T> {

    /**
     * 查询所有记录
     *
     * @return 全部数据列表
     */
    @GetMapping("/list")
    public Result<List<T>> list() {
        return Result.success(getBaseService().list());
    }

    /**
     * 根据 ID 查询单条记录
     *
     * @param id 实体主键 ID
     * @return 对应记录，ID 不存在时返回 null
     */
    @GetMapping("/{id}")
    public Result<T> getById(@PathVariable Long id) {
        return Result.success(getBaseService().getById(id));
    }

    /**
     * 新增记录
     *
     * @param entity 实体对象（包含所有字段值）
     * @return 是否新增成功
     */
    @PostMapping
    public Result<Boolean> add(@RequestBody T entity) {
        return Result.success(getBaseService().save(entity));
    }

    /**
     * 更新记录
     *
     * @param entity 实体对象（包含主键 ID 和待更新字段值）
     * @return 是否更新成功
     */
    @PutMapping
    public Result<Boolean> update(@RequestBody T entity) {
        return Result.success(getBaseService().updateById(entity));
    }

    /**
     * 根据 ID 删除记录（物理删除）
     *
     * @param id 实体主键 ID
     * @return 是否删除成功
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(getBaseService().removeById(id));
    }

    /**
     * 分页查询记录
     *
     * @param current 当前页码（从 1 开始，默认 1）
     * @param size    每页记录数（默认 10）
     * @return 包含总数、当前页数据及分页参数的分页结果
     */
    @GetMapping("/page")
    public Result<PageResult<T>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        IPage<T> page = getBaseService().page(PageUtil.buildPage(current, size));
        return Result.success(new PageResult<>(page.getTotal(), page.getRecords(), page.getCurrent(), page.getSize()));
    }

    /**
     * 批量删除记录
     *
     * @param ids 待删除的 ID 列表
     * @return 是否批量删除成功
     */
    @GetMapping("/batch")
    public Result<Boolean> batchDelete(@RequestParam List<Long> ids) {
        return Result.success(getBaseService().removeByIds(ids));
    }

    /**
     * 获取 IService 实例
     *
     * <p>子类必须重写此方法，返回对应的 MyBatis-Plus IService 实例。
     * 若未重写，调用 CRUD 方法时将抛出 UnsupportedOperationException。
     *
     * @return 对应实体的 IService 实例
     * @throws UnsupportedOperationException 子类未实现此方法时抛出
     */
    protected S getBaseService() {
        throw new UnsupportedOperationException("Subclass must implement getBaseService()");
    }
}
