package External;

import java.util.*;

public class Query {

    private static String listToString(List<String> list) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i < list.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    private List<String> selectFields = new ArrayList<>();
    private List<String> fromFields = new ArrayList<>();
    private List<String> whereFields = new ArrayList<>();
    private List<String> orderByFields = new ArrayList<>();
    private List<String> groupByFields = new ArrayList<>();
    private List<String> havingFields = new ArrayList<>();
    private List<String> joinFields = new ArrayList<>();
    private List<String> setFields = new ArrayList<>();
    private List<String> insertFields = new ArrayList<>();
    private List<String> updateFields = new ArrayList<>();
    private List<String> deleteFields = new ArrayList<>();
    private List<String> valuesFields = new ArrayList<>();

    public Query() {
    }

    public void select(String s) {
        selectFields.add(s);
    }

    public void insert(String s) {
        insertFields.add(s);
    }

    public void update(String s) {
        updateFields.add(s);
    }

    public void delete(String s) {
        deleteFields.add(s);
    }

    public void from(String s) {
        fromFields.add(s);
    }

    public void where(String s) {
        whereFields.add(s);
    }

    public void orderBy(String s) {
        orderByFields.add(s);
    }

    public void groupBy(String s) {
        groupByFields.add(s);
    }

    public void having(String s) {
        havingFields.add(s);
    }

    public void join(String s) {
        joinFields.add(s);
    }

    public void set(String s) {
        setFields.add(s);
    }

    public void values(String s) {
        valuesFields.add(s);
    }

    public String selectQuery() {
        String query = "SELECT " + listToString(selectFields);
        if (fromFields.size() > 0) {
            query += " FROM " + listToString(fromFields);
        }
        if (whereFields.size() > 0) {
            query += " WHERE " + listToString(whereFields);
        }
        if (joinFields.size() > 0) {
            query += listToString(joinFields);
        }
        if (groupByFields.size() > 0) {
            query += " GROUP BY " + listToString(groupByFields);
        }
        if (havingFields.size() > 0) {
            query += " HAVING " + listToString(havingFields);
        }
        if (orderByFields.size() > 0) {
            query += " ORDER BY " + listToString(orderByFields);
        }
        return query;
    }

    public String insertQuery() {
        String query = "INSERT INTO " + listToString(fromFields) + " (" + listToString(insertFields) + ") VALUES ("
                + listToString(valuesFields) + ")";
        return query;
    }

    public String updateQuery() {
        String query = "UPDATE " + listToString(updateFields) + " SET " + listToString(setFields);
        if (whereFields.size() > 0) {
            query += " WHERE " + listToString(whereFields);
        }
        return query;
    }

    public String deleteQuery() {
        String query = "DELETE FROM " + listToString(fromFields);
        if (whereFields.size() > 0) {
            query += " WHERE " + listToString(whereFields);
        }
        return query;
    }
}