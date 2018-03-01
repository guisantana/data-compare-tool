/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.data.compare.tool.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author guilherme
 */
public class Table {
    private String name;
    private PrimaryKey primaryKey;
    private String catalog;
    List<PrimaryKeyValue>primaryKeyValues = new ArrayList<PrimaryKeyValue>();
    HashMap<Long, String>duplicatedKeyValues = new HashMap<Long, String>();
    private boolean status = true;

    
    public Table() {
        super();
    }
    
    public Table(Table table) {
        this.name = table.getName();
        this.primaryKey = table.getPrimaryKey();
        this.catalog = table.getCatalog();
        this.status = table.getStatus();
        this.primaryKeyValues = new ArrayList<PrimaryKeyValue>();
        this.duplicatedKeyValues = new HashMap<Long, String>();
    }

    public String getName() {
        return name;
    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public PrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public void setPrimaryKey(PrimaryKey primaryKey) {
        this.primaryKey = primaryKey;
    }

    public List<PrimaryKeyValue> getPrimaryKeyValues() {
        return primaryKeyValues;
    }

    public void setPrimaryKeyValues(List<PrimaryKeyValue> primaryKeyValues) {
        this.primaryKeyValues = primaryKeyValues;
    }
 
    public HashMap<Long, String> getDuplicatedKeyValues() {
        return duplicatedKeyValues;
    }

    public void setDuplicatedKeyValues(HashMap<Long, String>duplicatedKeyValues) {
        this.duplicatedKeyValues = duplicatedKeyValues;
    }

}
