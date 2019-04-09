package org.vitrivr.cthulhu.jobs;

import static org.vitrivr.cthulhu.jobs.Job.Status.FAILED;
import static org.vitrivr.cthulhu.jobs.Job.Status.INTERRUPTED;
import static org.vitrivr.cthulhu.jobs.Job.Status.SUCCEEDED;
import static org.vitrivr.cthulhu.jobs.Job.Status.UNEXPECTED_ERROR;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import org.apache.commons.io.IOUtils;
import org.vitrivr.cineast.core.config.IngestConfig;

public class CineastJob extends Job {

  private final UUID id;
  private IngestConfig config;
  private String workDir;

  /**
   * Base constructor for the job, can be used by Jackson.
   */
  @JsonCreator
  public CineastJob(
      @JsonProperty("config") IngestConfig config,
      @JsonProperty("type") String type,
      @JsonProperty("name") String name,
      @JsonProperty(value = "priority", defaultValue = "2") int priority) {
    super();
    this.id = UUID.randomUUID();
    this.config = config;
    this.type = type;
    this.name = name;
    this.priority = priority;
  }

  /**
   * Saves the configuration to give to Cineast as a file.
   *
   * @param config the config to give to Cineast
   * @param outputPath where to save the file to
   * @param id the job id used to avoid save overwrites
   * @throws IOException if the file fails to save
   */
  private static void saveConfigurationFile(IngestConfig config, String outputPath, UUID id)
      throws IOException {
    String destination = getConfigPath(outputPath, id);
    ObjectMapper mapper = new ObjectMapper().registerModule(new Jdk8Module())
        .registerModule(new JavaTimeModule());
    mapper.writeValue(new File(destination), config);
  }

  private static String getConfigPath(String outputPath, UUID id) {
    return outputPath + id + "_config.json";
  }

  @Override
  public int execute() {
    tools.lg.info("Cineast job started");
    if (!getTools().isPresent() || config == null) {
      setStatus(UNEXPECTED_ERROR);
      return getStatusValue();
    }

    String dir = getOrSetWorkDir();
    try {
      saveConfigurationFile(config, dir, this.id);
    } catch (IOException e) {
      setStatus(UNEXPECTED_ERROR);
      return getStatusValue();
    }

    String cineastLocation = tools.getCineastLocation();
    String cineastConfig = tools.getCineastConfigLocation();
    Status executionStatus = executeCineast(cineastLocation, cineastConfig, getConfigPath(dir, id));

    setStatus(executionStatus);
    if (!this.stdErr.isEmpty()) {
      setStatus(FAILED);
    }
    return getStatusValue();

  }

  /**
   * If the work directory is not defined, it is set, then the work directory is returned.
   *
   * @return the working directory to save files in
   */
  private String getOrSetWorkDir() {
    if (workDir == null || workDir.isEmpty()) {
      tools.lg.info("Setting directory for Cineast job");
      workDir = tools.setWorkingDirectory(this);
    }
    return workDir;
  }

  /**
   * Runs the Cineast jar and returns the status of the process on completion.
   *
   * @param cineastLocation the location of the cineast jar
   * @param cineastConf the location of the cineast configuration
   * @param configFile the job to send to cineast
   * @return the status of the execution
   */
  private Status executeCineast(String cineastLocation, String cineastConf, String configFile) {
    tools.lg.info("{} - Preparing to execute cineast", name);
    if (cineastLocation == null || cineastLocation.isEmpty()) {
      return UNEXPECTED_ERROR;
    }
    String javaFlags = tools.getJavaFlags();
    String command = String
        .format("java %s -jar %s --job %s --config %s",
                javaFlags, cineastLocation, configFile, cineastConf);
    try {
      Process p = Runtime.getRuntime().exec(command);
      int retVal = waitForProcess(p);
      if (retVal == 0) {
        return SUCCEEDED;
      }
    } catch (InterruptedException e) {
      return INTERRUPTED;
    } catch (Exception e) {
      return FAILED;
    }
    tools.lg.info("{} - Execution of cineast finalized", name);
    return SUCCEEDED;
  }
}
