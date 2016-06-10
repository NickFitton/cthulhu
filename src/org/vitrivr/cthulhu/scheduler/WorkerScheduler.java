package org.vitrivr.cthulhu.scheduler;

import org.vitrivr.cthulhu.jobs.Job;
import org.vitrivr.cthulhu.jobs.JobFactory;
import org.vitrivr.cthulhu.jobs.JobQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import java.util.List;
import java.util.Hashtable;
import java.util.stream.*;

import java.util.Properties;

public class WorkerScheduler extends CthulhuScheduler {
    public WorkerScheduler(Properties props) {
        super(props);
        int port = Integer.parseInt(props.getProperty("port"));
        informCoordinator(props.getProperty("hostAddress"),
                          Integer.parseInt(props.getProperty("hostPort")),
                          props.getProperty("address"),
                          Integer.parseInt(props.getProperty("port")));
    }
    void informCoordinator(String coordAddress, int coordPort, String workerAddress, int workerPort) {
        conn.postWorker(coordAddress,coordPort,workerAddress,workerPort);
    }
}
