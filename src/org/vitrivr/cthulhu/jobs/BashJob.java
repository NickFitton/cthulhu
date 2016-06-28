package org.vitrivr.cthulhu.jobs;

import java.lang.ProcessBuilder;
import java.lang.Process;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import org.apache.commons.io.IOUtils;

import java.lang.InterruptedException;
import java.io.IOException;


public class BashJob extends Job {
    String stdOut;
    String stdErr;

    protected BashJob(){
        super();
    }

    protected BashJob(String action) {
        super();
        this.action = action;
        this.type = "BashJob";
    }
    protected BashJob(String action, int priority) {
        super();
        this.action = action;
        this.priority = priority;
        this.type = "BashJob";
    }

    public int execute() {
        ProcessBuilder pb = new ProcessBuilder("sh");
        Process p;
        int retVal = 1; //Error unless changed later on
        try {
            p = pb.start();
            OutputStream os = p.getOutputStream();
            os.write(this.action.getBytes(Charset.forName("UTF-8")));
            os.flush();
            os.close();

            InputStream is = p.getInputStream();
            InputStream es = p.getErrorStream();
            
            this.stdOut = IOUtils.toString(is,"UTF-8");
            this.stdErr = IOUtils.toString(es,"UTF-8");
        } catch (IOException e) {
            status = Job.Status.FAILED;
            return status.getValue();
        }
        try {
            retVal = p.waitFor();
            if(retVal == 0) status = Job.Status.SUCCEEDED;
        } catch (InterruptedException e) {
            status = Job.Status.INTERRUPTED;
        } finally {
            return status.getValue();
        }
    }

    /**
     * Returns the standard output of a job after it ran
     */
    public String getStdOut() { return stdOut; }
    /**
     * Returns the standard error stream contents of a job after it ran
     */
    public String getStdErr() { return stdErr; }
}
