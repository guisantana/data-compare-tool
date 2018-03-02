package com.data.compare.tool;

import com.data.compare.tool.configuration.ConnectionConfiguration;
import com.data.compare.tool.configuration.Configuration;
import com.data.compare.tool.bean.PrimaryKeyValue;
import com.data.compare.tool.bean.Table;
import com.data.compare.tool.configuration.MyFormatter;
import com.data.compare.tool.dao.DataCompareDao;
import com.google.gson.Gson;

import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class DataCompare {

    public final static Logger LOGGER = Logger.getLogger(DataCompare.class.getName());
    private static DataCompareDao dataCompareDao = new DataCompareDao();

    public static void main(String[] args) throws IOException {

        try {
            LOGGER.setUseParentHandlers(false);
            FileHandler fh = new FileHandler("resultFile.log");
            LOGGER.addHandler(fh);
            MyFormatter formatter = new MyFormatter();
            fh.setFormatter(formatter);

            Gson gson = new Gson();
            System.out.println("Reading configuration file");

            Configuration configuration = gson.fromJson(new FileReader("configuration.json"), Configuration.class);

            if (configuration.connections.size() < 2) {
                LOGGER.info("Please define at least two connections");
                System.out.println("Please define at least two connections");
                System.exit(0);
            }
            
            Class.forName("org.postgresql.Driver");
            
            List<Table> tableResultList = new ArrayList<Table>();
            
            for(ConnectionConfiguration connectionConfiguration : configuration.connections) {
                System.out.println("Connecting database: "+connectionConfiguration.name);
                Connection connection = DriverManager.getConnection(connectionConfiguration.url, connectionConfiguration.user, connectionConfiguration.password);
                
                List<Table> tableList = new ArrayList<Table>();
                getDatabaseTables(connection, tableList); 
                
                mergeDatabases(tableResultList, connection, tableList);
            }
                 
            System.out.println("------------------------------------------------------------------------");
            LOGGER.info("------------------------------------------------------------------------");
            System.out.println("Total of tables: "+tableResultList.size());
            LOGGER.info("Total of tables: "+tableResultList.size());
            
            for(Table table : tableResultList) {

                System.out.println("Total merged rows to "+table.getName()+" table: "+table.getPrimaryKeyValues().size());
                LOGGER.info("Total merged rows to "+table.getName()+" table: "+table.getPrimaryKeyValues().size());
                
                int t = 0;
                String duplicatedValues = "";
                for (Map.Entry map : table.getDuplicatedKeyValues().entrySet()) {
                        t++;
                        
                        if(!"".equals(duplicatedValues))
                            duplicatedValues += ",";
                        duplicatedValues += map.getKey()+"("+map.getValue()+")";

                }
                
                if(t > 0) {
                    System.out.println("   Duplicated total rows of "+table.getName()+" table: "+t);
                    LOGGER.info("   Duplicated total rows of "+table.getName()+" table: "+t);

                    System.out.println("   Duplicated values: "+duplicatedValues);
                    LOGGER.info("   Duplicated values: "+duplicatedValues);
                }
                else {
                    System.out.println("   No duplicated rows to "+table.getName()+": "+t);
                    LOGGER.info("   No duplicated rows of "+table.getName()+" table: "+t);                    
                }
            }
            
            System.out.println("FINISHED");
            LOGGER.info("FINISHED");        
        } 
        catch (Exception e) {


            e.printStackTrace();
        }
        
    }
    
    private static void getDatabaseTables(Connection connection, List<Table> tableList) throws SQLException, IOException {

        dataCompareDao.getDatabaseTables(connection, tableList);
        System.out.println("   Number of tables to "+connection.getCatalog()+": "+tableList.size());
        LOGGER.info("   Number of tables to "+connection.getCatalog()+": "+tableList.size());
        
        for(Table table : tableList) {
            dataCompareDao.getPrimaryKeyField(connection, table);
            System.out.println("     Table: "+table.getName()+" - Primary Key field: "+table.getPrimaryKey().getName() +" ("+table.getPrimaryKey().getType()+")");
            LOGGER.info("     Table: "+table.getName()+" - Primary Key field: "+table.getPrimaryKey().getName() +" ("+table.getPrimaryKey().getType()+")");
        }
        
    }
    
    private static Table getTableCompare(List<Table> tableList, String tableName) {
        for(Table t : tableList) {
            if(t != null && t.getName().equals(tableName)) {
                return t;
            }
        }
        return null;
    }
    
    
    private static List<Table> mergeDatabases(List<Table> resultTableList, Connection connection, List<Table> tableList) throws SQLException, IOException {
        
        for (Table table : tableList) {
            
            Table resultTable = getTableCompare(resultTableList, table.getName());
            
            if(resultTable == null) {
                resultTable = new Table(table);
                resultTableList.add(resultTable);
            }

            dataCompareDao.loadTableData(connection, table, resultTable);

        }

        return resultTableList;
    }


}
