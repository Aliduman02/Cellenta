package com.i2i.intern.cellenta;

import org.voltdb.client.Client;
import org.voltdb.client.ClientConfig;
import org.voltdb.client.ClientFactory;
import org.voltdb.client.ClientResponse;

public class VoltDbService {
    private Client client;

    public VoltDbService(String host, int port) throws Exception {
        ClientConfig config = new ClientConfig();
        client = ClientFactory.createClient(config);
        client.createConnection(host + ":" + port);
    }

    public ClientResponse callProcedure(String procedureName, Object... params) throws Exception {
        return client.callProcedure(procedureName, params);
    }

    public void close() {
        try {
            client.close();
        } catch (Exception ignored) {}
    }



}
