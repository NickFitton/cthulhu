package org.vitrivr.cthulhu.jobs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import org.apache.commons.io.IOUtils;


public class BashJob extends Job {

  private String action;

  protected BashJob() {
    super();
  }

  protected BashJob(String action) {
    this(action, 2);
  }

  protected BashJob(String action, int priority) {
    super();
    this.action = action;
    this.priority = priority;
  }

  public int execute() {
    ProcessBuilder pb = new ProcessBuilder("sh");
    Process p;
    try {
      p = pb.start();
      OutputStream os = p.getOutputStream();
      os.write(this.action.getBytes(Charset.forName("UTF-8")));
      os.flush();
      os.close();

      InputStream is = p.getInputStream();
      InputStream es = p.getErrorStream();

      this.stdOut = IOUtils.toString(is, "UTF-8");
      this.stdErr = IOUtils.toString(es, "UTF-8");
      int retVal = p.waitFor();
      if (retVal == 0) {
        status = Job.Status.SUCCEEDED;
      }
    } catch (IOException e) {
      status = Job.Status.FAILED;
      return status.getValue();
    } catch (InterruptedException e) {
      status = Job.Status.INTERRUPTED;
    }
    return status.getValue();
  }

  /**
   * Returns the action property of the job. The main action of it.
   * <p>
   *
   * @return the action property of the job
   */
  public String getAction() {
    return action;
  }
}
