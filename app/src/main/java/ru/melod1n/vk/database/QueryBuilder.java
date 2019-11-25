package ru.melod1n.vk.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class QueryBuilder {

    private StringBuilder builder;

    private QueryBuilder() {
        builder = new StringBuilder();
    }

    public static QueryBuilder query() {
        return new QueryBuilder();
    }

    public QueryBuilder select(String column) {
        builder.append("SELECT ")
                .append(column)
                .append(" ");
        return this;
    }

    public QueryBuilder from(String table) {
        builder.append("FROM ")
                .append(table)
                .append(" ");
        return this;
    }

    public QueryBuilder where(String clause) {
        builder.append("WHERE ")
                .append(clause)
                .append(" ");
        return this;
    }

    public QueryBuilder leftJoin(String table) {
        builder.append("LEFT JOIN ")
                .append(table)
                .append(" ");
        return this;
    }

    public QueryBuilder on(String where) {
        builder.append("ON ")
                .append(where)
                .append(" ");
        return this;
    }

    public QueryBuilder and() {
        builder.append("AND ");
        return this;
    }

    public QueryBuilder or() {
        builder.append("OR ");
        return this;
    }

    public Cursor asCursor(SQLiteDatabase db) {
        return db.rawQuery(toString(), null);
    }

    @Override
    public String toString() {
        return builder.toString().trim();
    }
}