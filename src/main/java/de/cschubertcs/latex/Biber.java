package de.cschubertcs.latex;

import java.io.File;

import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;

public class Biber extends MyAbstractTask {

  private static final String BIBER_INPUT_EXTENSION = ".bcf";
  private static final String BIBER_OUTPUT_EXTENSION = ".bbl";

  public Biber() {
    setOnlyIf(new Spec<Object>() {
      @Override
      public boolean isSatisfiedBy(Object arg0) {
        return getBiberInputFile().exists();
      }
    });
  }

  public String getBiberConfigPath() {
    return String.format("%s/%s%s", getTargetDirectory(), getDocumentBase(), BIBER_INPUT_EXTENSION);
  }

  @InputFile
  public File getBiberInputFile() {
    return getProject().file(getBiberConfigPath());
  }

  public String getBiberOutputPath() {
    return String.format("%s/%s%s", getTargetDirectory(), getDocumentBase(), BIBER_OUTPUT_EXTENSION);
  }

  @OutputFile
  public File getBiberOutputFile() {
    return getProject().file(getBiberOutputPath());
  }

  @Override
  public Object[] getCommandLine() {
    return new Object[] { getBinaryName(), "-D", "--input-directory", getSourceDirectory(), getBiberConfigPath() };
  }

}
