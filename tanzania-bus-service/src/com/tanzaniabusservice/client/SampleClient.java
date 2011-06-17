package com.tanzaniabusservice.client;

import com.tanzaniabusservice.BusService;
import org.apache.avro.ipc.HttpTransceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;

import java.io.IOException;
import java.net.URL;

public class SampleClient {
    public static void main(String[] args) throws IOException {
        final HttpTransceiver client = new HttpTransceiver(new URL("http://tanzaniabusservice.local/tanzaniabusservice"));
        final BusService busService = SpecificRequestor.getClient(BusService.class, client);

        System.out.println("Fetched company by ID: " + busService.loadCompany(0L));

        System.out.println("Fetched company list: " + busService.listCompaniesAboveRating(5.0f));

        System.out.println("Checking null value: " + busService.loadCompany(-55L));
    }
}
