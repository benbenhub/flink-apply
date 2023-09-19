package com.ben.core.util;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLObject;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.statement.*;
import com.ben.core.enums.JdbcConnectionType;
import com.ben.core.modul.SqlColumn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SQLParseUtil {

    //原始表字段
    private HashMap<String, List<String>> originalTables = new HashMap<>();
    //用于存储最终输出的字段
    private List<SqlColumn> SqlColumns = new ArrayList<>();

    public static List<SqlColumn> parseSelect(String sql, JdbcConnectionType dbType) throws Exception {
        return new SQLParseUtil().parseSelect(sql,dbType,false);
    }

    public List<SqlColumn> parseSelect(String sql,JdbcConnectionType dbType, boolean keepComments) throws Exception {
        String formatSql = SQLUtils.format(sql,DbType.of(dbType.name()));
        SQLStatement sqlStatement = SQLUtils.parseSingleStatement(formatSql, DbType.of(dbType.name()), keepComments);
        if (sqlStatement instanceof SQLSelectStatement
                && ((SQLSelectStatement) sqlStatement).getSelect() != null
                && ((SQLSelectStatement) sqlStatement).getSelect().getQuery() != null) {
            SQLSelectQuery select = ((SQLSelectStatement) sqlStatement).getSelect().getQuery();
            //总体查询分两种，select和union
            if (select instanceof SQLSelectQueryBlock) {
                getTopColumnsFromSelectQuery(select);
            } else if (select instanceof SQLUnionQuery) {
                getTopColumnsFromUnionQuery(select);
            }
        }
        return SqlColumns;
    }

    //获取最外层的输出字段
    private void getTopColumnsFromSelectQuery(SQLSelectQuery query) throws Exception {
        for (SQLSelectItem item : ((SQLSelectQueryBlock) query).getSelectList()) {
            SQLExpr expr = item.getExpr();
            if (expr instanceof SQLPropertyExpr) {
                SQLPropertyExpr s = (SQLPropertyExpr) expr;
                String columnOwner = s.getOwnerName();
                String columnName = s.getName();
                if (originalTables.containsKey(columnOwner)) {
                    //场景1:输出实体表所有字段
                    if (columnName.equals("*")) {
                        for (String tableColumn : originalTables.get(columnOwner)) {
                            SqlColumns.add(new SqlColumn(
                                    String.format("%s.%s", columnOwner, tableColumn), tableColumn));
                        }
                    } else {
                        //场景2:输出实体表单个字段
                        SqlColumns.add(
                                new SqlColumn(s.toString(), item.getAlias() == null ? columnName : item.getAlias()));
                    }
                } else if (columnName.equals("*")) {
                    //场景3:输出子查询所有字段
                    getColumnsFromSubQuery(query, columnOwner, false);
                } else {
                    //场景4:输出单个字段
                    SqlColumns.add(new SqlColumn(s.toString(), item.getAlias() == null ? columnName : item.getAlias()));
                }
            } else {
                String columnName = expr.toString();
                if (columnName.equals("*")) {
                    //场景5:输出该查询所有字段
                    getColumnsFromQuery(query);
                } else {
                    //场景6:输出单个计算或者单个字段
                    SqlColumns.add(new SqlColumn(columnName, item.getAlias() == null ? columnName : item.getAlias()));
                }
            }
        }
    }

    //获取最外层的输出字段
    private void getTopColumnsFromUnionQuery(SQLSelectQuery query) throws Exception {
        //union查询只要获取某一边的字段即可
        SQLSelectQuery selectQuery = ((SQLUnionQuery) query).getLeft();
        if (selectQuery instanceof SQLSelectQueryBlock) {
            getTopColumnsFromSelectQuery(selectQuery);
        } else {
            getTopColumnsFromUnionQuery(selectQuery);
        }
    }

    private void getColumnsFromQuery(SQLObject o) throws Exception {
        if (o instanceof SQLSelectQueryBlock) {
            SQLSelectQueryBlock select = (SQLSelectQueryBlock) o;
            SQLTableSource from = select.getFrom();
            if (from instanceof SQLExprTableSource) {
                String tableName = ((SQLExprTableSource) from).getTableName();
                for (SQLSelectItem item : select.getSelectList()) {
                    String columnExpr = item.getExpr().toString();
                    if (columnExpr.contains("*")) {
                        if (originalTables.containsKey(tableName)) {
                            for (String tableColumn : originalTables.get(tableName)) {
                                SqlColumns.add(new SqlColumn(String.format("%s.%s", tableName, tableColumn), tableColumn));
                            }
                        } else {
                            throw new Exception(String.format("SQL代码中读取的表%s不在该组件前置节点中", tableName));
                        }
                    } else {
                        SqlColumns.add(new SqlColumn(columnExpr, item.getAlias() == null ? columnExpr : item.getAlias()));
                    }
                }
            } else {
                getColumnsFromQuery(from);
            }
        } else if (o instanceof SQLSelect) {
            getColumnsFromQuery(((SQLSelect) o).getQuery());
        } else if (o instanceof SQLJoinTableSource) {
            getColumnsFromQuery(((SQLJoinTableSource) o).getLeft());
            getColumnsFromQuery(((SQLJoinTableSource) o).getRight());
        } else if (o instanceof SQLSubqueryTableSource) {
            getColumnsFromQuery(((SQLSubqueryTableSource) o).getSelect());
        } else if (o instanceof SQLExprTableSource) {
            String tableName = ((SQLExprTableSource) o).getTableName();
            if (originalTables.containsKey(tableName)) {
                for (String tableColumn : originalTables.get(tableName)) {
                    SqlColumns.add(new SqlColumn(String.format("%s.%s", tableName, tableColumn), tableColumn));
                }
            } else {
                throw new Exception(String.format("SQL代码中读取的表%s不在该组件前置节点中", tableName));
            }
        }
    }

    private void getColumnsFromSubQuery(SQLObject o, String subQueryName, boolean matchSubQueryName) throws Exception {
        if (o instanceof SQLSelectQueryBlock) {
            SQLSelectQueryBlock select = (SQLSelectQueryBlock) o;
            SQLTableSource from = select.getFrom();
            //matchSubQueryName用于在该查询的父查询，进行子查询别名的判断
            if (from instanceof SQLExprTableSource && matchSubQueryName) {
                String tableName = ((SQLExprTableSource) from).getTableName();
                for (SQLSelectItem item : select.getSelectList()) {
                    String columnExpr = item.getExpr().toString();
                    if (columnExpr.contains("*")) {
                        if (originalTables.containsKey(tableName)) {
                            for (String tableColumn : originalTables.get(tableName)) {
                                SqlColumns.add(new SqlColumn(String.format("%s.%s", tableName, tableColumn), tableColumn));
                            }
                        } else {
                            throw new Exception(String.format("SQL代码中读取的表%s不在该组件前置节点中", tableName));
                        }
                    } else {
                        SqlColumns.add(
                                new SqlColumn(String.format("%s.%s", subQueryName, columnExpr), item.getAlias() == null ? columnExpr : item.getAlias()));
                    }
                }
            } else {
                getColumnsFromSubQuery(from, subQueryName, false);
            }
        } else if (o instanceof SQLSelect) {
            getColumnsFromSubQuery(((SQLSelect) o).getQuery(), subQueryName, matchSubQueryName);
        } else if (o instanceof SQLJoinTableSource) {
            SQLTableSource left = ((SQLJoinTableSource) o).getLeft();
            getColumnsFromSubQuery(left, subQueryName, subQueryName.equals(left.getAlias()));
            SQLTableSource right = ((SQLJoinTableSource) o).getRight();
            getColumnsFromSubQuery(right, subQueryName, subQueryName.equals(right.getAlias()));
        } else if (o instanceof SQLSubqueryTableSource) {
            SQLSubqueryTableSource subQuery = (SQLSubqueryTableSource) o;
            getColumnsFromSubQuery(subQuery.getSelect(), subQueryName, subQueryName.equals(subQuery.getAlias()));
        } else if (o instanceof SQLExprTableSource && matchSubQueryName) {
            String tableName = ((SQLExprTableSource) o).getTableName();
            if (originalTables.containsKey(tableName)) {
                for (String tableColumn : originalTables.get(tableName)) {
                    SqlColumns.add(new SqlColumn(String.format("%s.%s", tableName, tableColumn), tableColumn));
                }
            } else {
                throw new Exception(String.format("SQL代码中读取的表%s不在该组件前置节点中", tableName));
            }
        }
    }
}
