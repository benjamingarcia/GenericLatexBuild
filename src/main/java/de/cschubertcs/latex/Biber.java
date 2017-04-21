package de.cschubertcs.latex;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecSpec;

public class Biber extends DefaultTask {

  private String biberBinary = "biber";
  private String documentBase;
  private final String biberConfigExtension = ".bcf";
  private String sourceDirectory;
  private String targetDirectory;

  public String getBiberBinary() {
    return biberBinary;
  }

  public void setBiberBinary(String biberBinary) {
    this.biberBinary = biberBinary;
  }

  public String getDocumentBase() {
    return documentBase;
  }
  
  public void setDocumentBase(String documentBase) {
    this.documentBase = documentBase;
  }
  
  public String getSourceDirectory() {
    return sourceDirectory;
  }

  public void setSourceDirectory(String sourceDirectory) {
    this.sourceDirectory = sourceDirectory;
  }

  public String getTargetDirectory() {
    return targetDirectory;
  }

  public void setTargetDirectory(String targetDirectory) {
    this.targetDirectory = targetDirectory;
  }
  
  @Input
  public String getBiberConfigFile() {
    return String.format("%s/%s%s", targetDirectory, documentBase, biberConfigExtension);
  }

  @TaskAction
  protected void exec() {
    OutputStream stdout = new ByteArrayOutputStream();
    OutputStream errout = new ByteArrayOutputStream();
    /*ExecResult result = */getProject().exec(new BiberAction(stdout, errout));
  }

  private class BiberAction implements Action<ExecSpec> {

    private final OutputStream stdout;
    private final OutputStream errout;

    public BiberAction(OutputStream stdout, OutputStream errout) {
      this.stdout = stdout;
      this.errout = errout;
    }
    
    @Override
    public void execute(ExecSpec spec) {
      String command = String.format("%s -D --input-directory %s %s", getBiberBinary(), getSourceDirectory(), getBiberConfigFile());
      spec.setCommandLine(command);
      spec.setWorkingDir(".");
      spec.setIgnoreExitValue(true);
      spec.setStandardOutput(stdout);
      spec.setErrorOutput(errout);
    }

  }

}
