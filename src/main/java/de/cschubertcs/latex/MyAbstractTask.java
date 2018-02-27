package de.cschubertcs.latex;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecResult;
import org.gradle.process.ExecSpec;
import org.gradle.process.internal.ExecException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import groovy.lang.GString;

public abstract class MyAbstractTask extends DefaultTask {

  private static final Logger logger = LoggerFactory.getLogger(MyAbstractTask.class);
  
  private GString binaryName;
  private GString documentBase;
  private GString sourceDirectory;
  private GString targetDirectory;
  private ExecResult execResult;
  private OutputStream standardOutput = System.out;
  private OutputStream errorOutput = System.err;

  protected abstract Object[] getCommandLine();
  
  public MyAbstractTask() {
    setStandardOutput(new ByteArrayOutputStream());
    setErrorOutput(new ByteArrayOutputStream());
    setGroup("Tex");
  }
  
  public GString getBinaryName() {
    return this.binaryName;
  }
  
  public void setBinaryName(GString binaryName) {
    this.binaryName = binaryName;
  }

  public GString getDocumentBase() {
    return documentBase;
  }

  public void setDocumentBase(GString documentBase) {
    this.documentBase = documentBase;
  }

  public GString getSourceDirectory() {
    return sourceDirectory;
  }

  public void setSourceDirectory(GString sourceDirectory) {
    this.sourceDirectory = sourceDirectory;
  }

  public GString getTargetDirectory() {
    return targetDirectory;
  }

  public void setTargetDirectory(GString targetDirectory) {
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
      logger.error("%s standard output:", getBinaryName());
      logger.error(getStandardOutput().toString());
      logger.error("%s error output:", getBinaryName());
      logger.error(getErrorOutput().toString());
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
      logger.warn("Executing command: '{}'", this.lastCommandLine);
    }
    
  }
  
}
