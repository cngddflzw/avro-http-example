package com.tanzaniabusservice.web;

import com.tanzaniabusservice.BusService;
import com.tanzaniabusservice.util.AvroBusServiceProvider;
import org.apache.avro.ipc.ResponderServlet;
import org.apache.avro.ipc.specific.SpecificResponder;

import java.io.IOException;

public class TanzaniaBusServiceServlet extends ResponderServlet {
    public TanzaniaBusServiceServlet() throws IOException {
        super(new SpecificResponder(BusService.class, AvroBusServiceProvider.getAvroBusService()));
    }
}