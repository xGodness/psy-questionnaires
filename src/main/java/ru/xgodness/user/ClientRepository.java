package ru.xgodness.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import ru.xgodness.persistence.ConnectionManager;

@Repository
public class ClientRepository {

    private final ConnectionManager connectionManager;

    @Autowired
    public ClientRepository(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }



}
