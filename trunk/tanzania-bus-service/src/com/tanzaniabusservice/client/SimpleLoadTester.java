package com.tanzaniabusservice.client;

import com.tanzaniabusservice.BusService;
import com.tanzaniabusservice.Company;
import org.apache.avro.AvroRemoteException;
import org.apache.avro.ipc.HttpTransceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;
import org.apache.commons.cli.*;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

public class SimpleLoadTester {
    private static final Logger logger = Logger.getLogger(SimpleLoadTester.class);
    private static final String OPT_URL = "url";
    private static final String OPT_THREADS = "threads";
    private static final String OPT_ITERATIONS = "iterations";

    private URL serviceUrl;
    private int threadCount;
    private int maxIterations;

    public SimpleLoadTester(URL serviceUrl, int threadCount, int maxIterations) {
        this.serviceUrl = serviceUrl;
        this.threadCount = threadCount;
        this.maxIterations = maxIterations;
    }

    public static void main(String[] args) {
        Options options = new Options();
        options.addOption(OPT_THREADS, true, "number of concurrent threads to use");
        options.addOption(OPT_URL, true, "url of service");
        options.addOption(OPT_ITERATIONS, true, "number of calls to execute on each thread");

        CommandLineParser parser = new GnuParser();
        try {
            CommandLine commandLine = parser.parse(options, args);

            if (!commandLine.hasOption(OPT_URL)) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("SimpleLoadTester", options);
            }

            int clThreadCount = 1;
            if (commandLine.hasOption(OPT_THREADS)) {
                try {
                    clThreadCount = Integer.parseInt(commandLine.getOptionValue(OPT_THREADS));
                } catch (NumberFormatException e) {
                    System.out.println("Invalid thread count entered.");
                    return;
                }
            }

            int clIterations = 1000;
            if(commandLine.hasOption(OPT_ITERATIONS)) {
                try {
                    clIterations = Integer.parseInt(commandLine.getOptionValue(OPT_ITERATIONS));
                } catch (NumberFormatException e) {
                    System.out.println("Invalid iteration count entered.");
                    return;
                }
            }

            try {
                URL serviceUrl = new URL(commandLine.getOptionValue(OPT_URL));
                SimpleLoadTester tester = new SimpleLoadTester(serviceUrl, clThreadCount, clIterations);
                tester.runTest();
            } catch (MalformedURLException e) {
                System.out.println("Invalid url entered.");
                return;
            }

        } catch (ParseException e) {
            System.out.println("Bad times parsing options: " + e);
        }

    }

    public void runTest() {
        logger.info(String.format("Starting test of %s with %d thread(s)...", serviceUrl, threadCount));
        ExecutorService executorService = Executors.newCachedThreadPool(new ThreadFactory() {
            public Thread newThread(Runnable r) {
                final Thread thread = new Thread(r);
                thread.setDaemon(true);
                return thread;
            }
        });

        List<Future> futures = new ArrayList<Future>(threadCount);

        for(int i=0; i< threadCount; i++) {
            futures.add(executorService.submit(new ServiceTestRunner(maxIterations, serviceUrl)));
        }

        for(Future future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                logger.error("Error waiting for threads to complete.", e);
            }
        }
    }

    class ServiceTestRunner implements Runnable {
        private Random random = new Random();
        private int iterationCount = 0;
        private int maxIterations;
        private long totalOperatingMills;
        HttpTransceiver client;
        BusService busService;

        ServiceTestRunner(int maxIterations, URL serviceUrl) {
            this.maxIterations = maxIterations;
            client = new HttpTransceiver(serviceUrl);
            try {
                busService = SpecificRequestor.getClient(BusService.class, client);
            } catch (IOException e) {
                logger.error("Error setting up client.", e);
            }
        }

        public void run() {
            while (iterationCount < maxIterations) {
                iterationCount++;
                long startMills = System.currentTimeMillis();

                try {
                    final Company company = busService.loadCompany(random.nextInt(4));
                    if(company != null) {
                        if(company.name == null) {
                            logger.warn("Name was null.  Probably not good.");
                        }
                    }
                } catch (AvroRemoteException e) {
                    logger.error("Error making service call", e);
                }

                totalOperatingMills += System.currentTimeMillis() - startMills;

                if(iterationCount % 100 == 0) {
                    logger.info(String.format("%d iterations performed in %d milliseconds.  avg: %f",
                            iterationCount, totalOperatingMills, (totalOperatingMills/(double)iterationCount)));
                }
            }
        }
    }
}
