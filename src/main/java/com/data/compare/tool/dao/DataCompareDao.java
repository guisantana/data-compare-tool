/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.data.compare.tool.dao;

import static com.data.compare.tool.DataCompare.LOGGER;
import com.data.compare.tool.bean.PrimaryKey;
import com.data.compare.tool.bean.PrimaryKeyValue;
import com.data.compare.tool.bean.Table;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author guilherme
 */
public class DataCompareDao {
    
    
    public static void getDatabaseTables(Connection connection, List<Table> tableList) throws SQLException {
        
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'");
        
        while (rs.next()) {
            Table table = new Table();
            table.setName(rs.getString(1));
            table.setCatalog(connection.getCatalog());
            tableList.add(table);
        }
        
    }
    
    public static void getPrimaryKeyField(Connection connection, Table table) throws SQLException {
        
        Statement statement = connection.createStatement();
        String query = "SELECT a.attname, format_type(a.atttypid, a.atttypmod) AS data_type FROM  "
                + "pg_index i JOIN   pg_attribute a ON a.attrelid = i.indrelid AND a.attnum = ANY(i.indkey) WHERE "
                + "i.indrelid = '"+table.getName()+"'::regclass AND i.indisprimary";
        ResultSet rs = statement.executeQuery(query);
        
        if (rs.next()) {
            PrimaryKey primaryKey = new PrimaryKey();
            primaryKey.setName(rs.getString(1));
            primaryKey.setType(rs.getString(2));
            
            if(!rs.getString(2).equals("bigint"))
                table.setStatus(false);
            
            table.setPrimaryKey(primaryKey);
        }
        else {
            PrimaryKey primaryKey = new PrimaryKey();
            primaryKey.setName("NO PRIMARY KEY");
            primaryKey.setType("NO DATA TYPE");
            
            table.setPrimaryKey(primaryKey);
            table.setStatus(false);
        }

    }
    
    public static void loadTableData(Connection connection, Table table, Table resultTable) throws SQLException {
        
        if(table.getStatus()) {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT "+table.getPrimaryKey().getName()+" FROM "+table.getName());
            int i = 0;           
            HashMap<Long,String> duplicatedMap = resultTable.getDuplicatedKeyValues();
            
            while (rs.next()) {
                
                boolean bNotExist = true;
                Long value = rs.getLong(1);
                i++;
                
                for(PrimaryKeyValue primaryKeyValue : resultTable.getPrimaryKeyValues()) {
                    if(primaryKeyValue != null && primaryKeyValue.getId().longValue() == value.longValue()) {
                        bNotExist = false;
                        primaryKeyValue.setMessage(primaryKeyValue.getMessage()+" - "+table.getCatalog());
                        primaryKeyValue.setDuplicated(true);
                        
                        duplicatedMap.put(value, primaryKeyValue.getMessage()+" - "+table.getCatalog());
                        
                        break;
                    }
                }
                
                if(bNotExist) {
                                    
                    PrimaryKeyValue pKV = new PrimaryKeyValue();
                    pKV.setId(value);
                    pKV.setMessage(table.getCatalog());
                    
                    resultTable.getPrimaryKeyValues().add(pKV);
                }
 
            }
            
            System.out.println("     Total rows of "+table.getCatalog()+"."+table.getName()+": "+i);
            LOGGER.info("     Total rows of "+table.getCatalog()+"."+table.getName()+": "+i);

        }
   
    }
}
