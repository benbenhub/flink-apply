package com.ben.core.modul;


public class SqlColumn {
    String columnName;
    String columnAlias;
    public SqlColumn(String columnName, String columnAlias) {
        this.columnName = columnName;
        this.columnAlias = columnAlias;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnAlias() {
        return columnAlias;
    }

    public void setColumnAlias(String columnAlias) {
        this.columnAlias = columnAlias;
    }

    @Override
    public String toString() {
        return "SqlColumn{" +
                "columnName='" + columnName + '\'' +
                ", columnAlias='" + columnAlias + '\'' +
                '}';
    }
}
