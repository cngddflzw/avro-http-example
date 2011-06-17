package com.tanzaniabusservice.util;

import com.tanzaniabusservice.service.AvroBusService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * This class is a hack to get spring dependency injection going.  Since the service needs to extend a
 * servlet and that servlet needs references in the constructor, we can't inject the service into it.
 * This class is here to have spring handle the service creation and help get that into the servlet.  To make
 * the hack worse, spring does not support direct static field injection, so we need a public setter for it to use.
 * One should avoid looking directly into this class as it may cause seizures and mental trauma.
 */
@Component
public class AvroBusServiceProvider {
    private static AvroBusService avroBusService;

    public static AvroBusService getAvroBusService() {
        return avroBusService;
    }

    @Inject
    public void setAvroBusService(AvroBusService avroBusService) {
        AvroBusServiceProvider.avroBusService = avroBusService;
    }
}
