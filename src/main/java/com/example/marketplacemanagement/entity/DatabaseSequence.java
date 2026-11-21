package com.example.marketplacemanagement.entity;

// stores the counter

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "database_sequences")
public class DatabaseSequence {

    @Id
    private String id;

    private long value;

    public DatabaseSequence()
    {

    }

    public String getId(){
        return id;
    }

    public void setId(String id){
        this.id = id;
    }
    public long getValue(){
        return value;
    }

    public void setValue(long value){
        this.value = value;
    }

}
