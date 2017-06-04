package de.cschubertcs.latex;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFiles;

public class Latex extends MyAbstractTask {

  private static final String LATEX_EXTENSION = ".tex";
  private static final String AUX_EXTENSION = ".aux";
  private static final String BBL_EXTENSION = ".bbl";
  private static final String BCF_EXTENSION = ".bcf";
  private static final String BLG_EXTENSION = ".blg";
  private static final String RUN_XML_EXTENSION = ".run.xml";

  private static final String[] TARGET_INPUT_EXTENSIONS = { AUX_EXTENSION, BBL_EXTENSION, BLG_EXTENSION,
      RUN_XML_EXTENSION };
  /*
   * The pdf extension is not part of the output, as the pdf is always newly
   * generated and as such would always be the cause of a new latex run
   */
  private static final String[] OUTPUT_EXTENSIONS = { AUX_EXTENSION, BCF_EXTENSION, BLG_EXTENSION, RUN_XML_EXTENSION };

  public String getLatexFilePath() {
    return String.format("%s/%s%s", getSourceDirectory(), getDocumentBase(), LATEX_EXTENSION);
  }

  //@InputFiles
  public List<File> getInputFiles() {
    List<File> files = new ArrayList<>(TARGET_INPUT_EXTENSIONS.length + 1);
    files.add(getFile(getLatexFilePath()));
    for (String extension : TARGET_INPUT_EXTENSIONS) {
      String path = String.format("%s/%s%s", getTargetDirectory(), getDocumentBase(), extension);
      files.add(getFile(path));
    }
    return files;
  }

  //@OutputFiles
  public List<File> getOutputFiles() {
    List<File> files = new ArrayList<>(OUTPUT_EXTENSIONS.length);
    for (String extension : OUTPUT_EXTENSIONS) {
      String path = String.format("%s/%s%s", getTargetDirectory(), getDocumentBase(), extension);
      files.add(getFile(path));
    }
    return files;
  }

  private File getFile(String path) {
    return getProject().file(path);
  }

  @Override
  protected Object[] getCommandLine() {
    return new Object[] { getBinaryName(), "-output-directory", getTargetDirectory(), getLatexFilePath() };
  }

}
