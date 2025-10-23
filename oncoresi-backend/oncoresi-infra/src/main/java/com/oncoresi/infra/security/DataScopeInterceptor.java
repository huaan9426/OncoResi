package com.oncoresi.infra.security;

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

/**
 * MyBatis 数据权限拦截器
 * 自动在SQL中添加数据权限过滤条件
 */
@Slf4j
@Intercepts({
        @Signature(
                type = Executor.class,
                method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}
        )
})
public class DataScopeInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        Object parameter = args[1];

        // 获取数据权限上下文
        DataScopeContext context = SecurityContextHolder.getDataScopeContext();

        // 如果没有数据权限上下文或者是ALL权限，直接放行
        if (context == null || context.getScopeType() == DataScopeContext.DataScopeType.ALL) {
            return invocation.proceed();
        }

        // 获取原始SQL
        BoundSql boundSql = ms.getBoundSql(parameter);
        String originalSql = boundSql.getSql();

        try {
            // 解析SQL
            Statement statement = CCJSqlParserUtil.parse(originalSql);

            if (statement instanceof Select) {
                Select select = (Select) statement;
                SelectBody selectBody = select.getSelectBody();

                if (selectBody instanceof PlainSelect) {
                    PlainSelect plainSelect = (PlainSelect) selectBody;

                    // 添加数据权限过滤
                    Expression dataScopeCondition = buildDataScopeCondition(plainSelect, context);

                    if (dataScopeCondition != null) {
                        Expression where = plainSelect.getWhere();
                        if (where != null) {
                            plainSelect.setWhere(new net.sf.jsqlparser.expression.operators.conditional.AndExpression(where, dataScopeCondition));
                        } else {
                            plainSelect.setWhere(dataScopeCondition);
                        }

                        String newSql = select.toString();
                        log.debug("数据权限过滤 - 原始SQL: {}", originalSql);
                        log.debug("数据权限过滤 - 新SQL: {}", newSql);

                        // 使用反射修改BoundSql中的sql
                        java.lang.reflect.Field field = BoundSql.class.getDeclaredField("sql");
                        field.setAccessible(true);
                        field.set(boundSql, newSql);
                    }
                }
            }

        } catch (Exception e) {
            log.error("数据权限SQL解析失败，使用原始SQL: {}", originalSql, e);
        }

        return invocation.proceed();
    }

    /**
     * 构建数据权限过滤条件
     */
    private Expression buildDataScopeCondition(PlainSelect plainSelect, DataScopeContext context) {
        // 获取主表（FROM子句的第一个表）
        FromItem fromItem = plainSelect.getFromItem();
        String tableName = extractTableName(fromItem);

        if (tableName == null) {
            return null;
        }

        // 根据表名和权限类型构建条件
        return switch (context.getScopeType()) {
            case DEPT -> buildDeptCondition(tableName, context.getDeptId());
            case SUPERVISED -> buildSupervisedCondition(tableName, context.getUserId());
            case SELF -> buildSelfCondition(tableName, context.getUserId());
            default -> null;
        };
    }

    /**
     * 构建科室权限条件
     */
    private Expression buildDeptCondition(String tableName, Long deptId) {
        // 只对包含dept_id字段的表生效
        if (!hasColumn(tableName, "dept_id")) {
            return null;
        }

        EqualsTo equalsTo = new EqualsTo();
        equalsTo.setLeftExpression(new Column("dept_id"));
        equalsTo.setRightExpression(new LongValue(deptId));
        return equalsTo;
    }

    /**
     * 构建导师权限条件（带教学员）
     */
    private Expression buildSupervisedCondition(String tableName, Long supervisorId) {
        // 只对sys_user表或包含trainee_id字段的表生效
        if ("sys_user".equals(tableName)) {
            // WHERE id IN (SELECT trainee_id FROM sys_supervisor_trainee WHERE supervisor_id = ?)
            try {
                String subquery = String.format(
                        "(SELECT trainee_id FROM sys_supervisor_trainee WHERE supervisor_id = %d)",
                        supervisorId
                );
                InExpression inExpression = new InExpression();
                inExpression.setLeftExpression(new Column("id"));
                inExpression.setRightExpression(CCJSqlParserUtil.parseExpression(subquery));
                return inExpression;
            } catch (Exception e) {
                log.error("构建导师权限条件失败", e);
                return null;
            }
        } else if (hasColumn(tableName, "trainee_id")) {
            // WHERE trainee_id IN (SELECT trainee_id FROM sys_supervisor_trainee WHERE supervisor_id = ?)
            try {
                String subquery = String.format(
                        "(SELECT trainee_id FROM sys_supervisor_trainee WHERE supervisor_id = %d)",
                        supervisorId
                );
                InExpression inExpression = new InExpression();
                inExpression.setLeftExpression(new Column("trainee_id"));
                inExpression.setRightExpression(CCJSqlParserUtil.parseExpression(subquery));
                return inExpression;
            } catch (Exception e) {
                log.error("构建导师权限条件失败", e);
                return null;
            }
        }

        return null;
    }

    /**
     * 构建个人权限条件
     */
    private Expression buildSelfCondition(String tableName, Long userId) {
        // 对sys_user表：WHERE id = ?
        if ("sys_user".equals(tableName)) {
            EqualsTo equalsTo = new EqualsTo();
            equalsTo.setLeftExpression(new Column("id"));
            equalsTo.setRightExpression(new LongValue(userId));
            return equalsTo;
        }

        // 对其他表：WHERE user_id = ? 或 WHERE trainee_id = ?
        if (hasColumn(tableName, "user_id")) {
            EqualsTo equalsTo = new EqualsTo();
            equalsTo.setLeftExpression(new Column("user_id"));
            equalsTo.setRightExpression(new LongValue(userId));
            return equalsTo;
        } else if (hasColumn(tableName, "trainee_id")) {
            EqualsTo equalsTo = new EqualsTo();
            equalsTo.setLeftExpression(new Column("trainee_id"));
            equalsTo.setRightExpression(new LongValue(userId));
            return equalsTo;
        }

        return null;
    }

    /**
     * 提取表名
     */
    private String extractTableName(FromItem fromItem) {
        if (fromItem instanceof Table) {
            return ((Table) fromItem).getName();
        }
        return null;
    }

    /**
     * 判断表是否包含某列（简化实现，实际应查询数据库元数据）
     */
    private boolean hasColumn(String tableName, String columnName) {
        // 简化实现：根据表名和列名的命名规范判断
        // 实际生产环境应该查询数据库元数据或使用配置
        return switch (columnName) {
            case "dept_id" -> !tableName.equals("sys_hospital") &&
                    !tableName.equals("sys_role") &&
                    !tableName.equals("sys_department");
            case "user_id", "trainee_id" -> !tableName.equals("sys_hospital") &&
                    !tableName.equals("sys_role") &&
                    !tableName.equals("sys_department") &&
                    !tableName.equals("sys_user");
            default -> false;
        };
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }
}
