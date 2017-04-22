package de.cschubertcs.latex;

import java.io.OutputStream;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecResult;
import org.gradle.process.ExecSpec;
import org.gradle.process.internal.ExecException;

public abstract class MyAbstractTask extends DefaultTask {

  private String binaryName;
  private String documentBase;
  private String sourceDirectory;
  private String targetDirectory;
  private ExecResult execResult;
  private OutputStream standardOutput = System.out;
  private OutputStream errorOutput = System.err;

  protected abstract Object[] getCommandLine();
  
  public String getBinaryName() {
    return this.binaryName;
  }
  
  public void setBinaryName(String binaryName) {
    this.binaryName = binaryName;
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

  public OutputStream getStandardOutput() {
    return standardOutput;
  }

  public void setStandardOutput(OutputStream standardOutput) {
    this.standardOutput = standardOutput;
  }

  public OutputStream getErrorOutput() {
    return errorOutput;
  }

  public void setErrorOutput(OutputStream errorOutput) {
    this.errorOutput = errorOutput;
  }
  
  public ExecResult getExecResult() {
    return execResult;
  }
  
  @TaskAction
  protected void exec() {
    MyAction action = new MyAction();
    try {
      execResult = getProject().exec(action);
    } catch (ExecException e) {
      System.out.printf("%s standard output:", getBinaryName());
      System.out.println(getStandardOutput().toString());
      System.out.printf("%s error output:", getBinaryName());
      System.out.println(getErrorOutput().toString());
      String errorMessage = String.format(
          "There was an error while executing the command '%s'. See the output above for more information.",
          action.lastCommandLine);
      throw new ExecException(errorMessage, e);
    }
  }
  
  private class MyAction implements Action<ExecSpec> {
    
    public String lastCommandLine;
    
    @Override
    public void execute(ExecSpec spec) {
      spec.setCommandLine(getCommandLine());
      spec.setWorkingDir(".");
      spec.setStandardOutput(getStandardOutput());
      spec.setErrorOutput(getErrorOutput());
      
      this.lastCommandLine = String.join(" ", spec.getCommandLine());
    }
    
  }
  
}
