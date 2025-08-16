package org.unimelb.generate;

import java.sql.*;

public class EntityGenerator {

    public static void main(String[] args) throws SQLException {
        String url = "jdbc:mysql://localhost:3306/wjdb";
        String user = "root";
        String password = "123456";
        String tableName = "wj_base_param";
        String prefixToRemove = "wj";

        EntityGenerator generator = new EntityGenerator(url, user, password,  prefixToRemove);
        System.out.println();
        generator.generateEntityClass(tableName);
    }

    private final String url;
    private final String user;
    private final String password;
    private final String prefixToRemove;

    public EntityGenerator(String url, String user, String password,  String prefixToRemove) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.prefixToRemove = prefixToRemove;
    }

    public void generateEntityClass(String tableName) throws SQLException {
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, tableName, null);

            StringBuilder entityClassCode = new StringBuilder();
            entityClassCode.append("import lombok.Data;\n");
            entityClassCode.append("import com.baomidou.mybatisplus.annotation.TableId;\n");
            entityClassCode.append("import com.baomidou.mybatisplus.annotation.TableName;\n");
            entityClassCode.append("import com.baomidou.mybatisplus.annotation.IdType;\n");
            entityClassCode.append("import java.io.Serializable;\n\n");
            entityClassCode.append("@Data\n");
            entityClassCode.append("@TableName(\"").append(tableName).append("\")\n");
            entityClassCode.append("public class ").append(convertToCamelCase(tableName)).append(" implements Serializable {\n");

            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                String typeName = columns.getString("TYPE_NAME").toLowerCase();
                boolean isDateTime = typeName.contains("datetime");
                String javaType = convertToJavaType(typeName);
                if (columnName.equalsIgnoreCase("id")) {
                    entityClassCode.append("    @TableId(type = IdType.AUTO)\n");
                }
                entityClassCode.append("    private ").append(javaType).append(" ")
                        .append(underscoreToCamel(columnName)).append(";\n");
            }

            entityClassCode.append("}");

            System.out.println(entityClassCode.toString());
        }
    }

    private String convertToJavaType(String dbType) {
        if (dbType.startsWith("int")) {
            return "Integer";
        } else if (dbType.startsWith("varchar") || dbType.startsWith("text")) {
            return "String";
        } else if (dbType.startsWith("datetime")) {
            return "java.util.Date";
        } else {
            return "String";
        }
    }

    private String underscoreToCamel(String input) {
        StringBuilder result = new StringBuilder();
        boolean nextUpperCase = false;
        for (int i = 0; i < input.length(); i++) {
            char currentChar = input.charAt(i);
            if (currentChar == '_') {
                nextUpperCase = true;
            } else {
                if (nextUpperCase) {
                    result.append(Character.toUpperCase(currentChar));
                    nextUpperCase = false;
                } else {
                    result.append(currentChar);
                }
            }
        }
        return result.toString();
    }

    private String convertToCamelCase(String tableName) {
        String[] parts = tableName.toLowerCase().split("_");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (!part.equalsIgnoreCase(prefixToRemove)) {
                result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
            }
        }
        return result.toString();
    }


}
