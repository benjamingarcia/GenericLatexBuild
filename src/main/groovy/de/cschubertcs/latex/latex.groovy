package de.cschubertcs.latex

import org.gradle.api.GradleException
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.tasks.Exec
import org.gradle.api.GradleException;

class LatexPlugin implements Plugin<Project> {

  private boolean isExecutableExisting(execName) {
    def exists = false
    def pathEntries = System.getenv("PATH").tokenize(File.pathSeparator)
    for (entry in pathEntries) {
      def execPath = new File(entry, execName)
      exists |= (execPath.exists() && execPath.isFile() && execPath.canExecute())
    }
    return exists
  }

  @Override
  void apply(Project project) {
    project.extensions.create("latex", LatexPluginExtension)

    project.defaultTasks 'pdfLatexRun3'

    project.task('check') {
      doFirst{
        logger.quiet("Checking that '${project.latex.latexBinary}' is in the path system variable...")
        if (!isExecutableExisting("${project.latex.latexBinary}")) {
          throw new GradleException("The binary '${project.latex.latexBinary}' is not present in the path.")
        }

        logger.quiet("Checking that '${project.latex.biberBinary}' is in the path system variable...")
        if (!isExecutableExisting("${project.latex.biberBinary}")) {
          throw new GradleException("The binary '${project.latex.biberBinary}' is not present in the path.")
        }

        logger.quiet("Checking that the primary source file exists...")
        if (!project.file("${project.latex.latexFilePath}").exists()) {
          throw new GradleException("The file '${project.latex.latexFilePath}' does not exist.")
        }

        logger.quiet("Checking that the target directory exists (or creating it if necessary)...")
        if (!project.file("${project.latex.tmpDirectory}").exists()) {
          project.mkdir project.latex.tmpDirectory
          logger.quiet "Created target directory ${project.latex.tmpDirectory}"
        }
      }
      
      inputs.property "PATH", System.getenv("PATH")
      outputs.upToDateWhen { 
        project.file("${project.latex.latexFilePath}").exists() && project.file("${project.latex.tmpDirectory}").exists()
      }
    }

    project.task('pdfLatexRun1', type: Latex, dependsOn: project.check)
    
    project.task('biber', type: Biber, dependsOn: project.pdfLatexRun1)
    
    project.task('pdfLatexRun2', type: Latex, dependsOn: project.biber)
    
    project.task('pdfLatexRun3', type: Latex, dependsOn: project.pdfLatexRun2) 
    
    (1..3).each { counter ->
      project."pdfLatexRun$counter" {
        // the needed parameter (lazy evaluated to reflect user changes)
        binaryName "${->project.latex.latexBinary}"
        documentBase "${->project.latex.documentBase}"
        sourceDirectory "${->project.latex.rawDirectory}"
        targetDirectory "${->project.latex.tmpDirectory}"
      }
    }
    
    project.'pdfLatexRun1' {
        inputs.file("${->project.latex.latexFilePath}")
        outputs.file("${->project.latex.tmpDirectory}/${->project.latex.documentBase}.aux")
        outputs.file("${->project.latex.tmpDirectory}/${->project.latex.documentBase}.log")
        outputs.file("${->project.latex.tmpDirectory}/${->project.latex.documentBase}.log")
    }
    
    project.'biber' {
        // set the needed parameter (lazy evaluated to reflect user changes)
        binaryName "${->project.latex.biberBinary}"
        documentBase "${->project.latex.documentBase}"
        sourceDirectory "${->project.latex.rawDirectory}"
        targetDirectory "${->project.latex.tmpDirectory}"
    }

    project.'pdfLatexRun2' {
        inputs.file("${->project.latex.latexFilePath}")
        outputs.file("${->project.latex.tmpDirectory}/${->project.latex.documentBase}.aux")
        outputs.file("${->project.latex.tmpDirectory}/${->project.latex.documentBase}.log")
    }
    
    project.'pdfLatexRun3' { 
        onlyIf {
            project.file("${->project.latex.biberConfigFilePath}").exists()
        }
        inputs.file("${->project.latex.latexFilePath}")
        outputs.file("${->project.latex.tmpDirectory}/${->project.latex.documentBase}.aux")
        outputs.file("${->project.latex.tmpDirectory}/${->project.latex.documentBase}.log")
    }

  }
}

class LatexPluginExtension {
    def latexBinary = 'pdflatex'
    def biberBinary = 'biber'

    def documentBase = 'main'
    def rawDirectory = 'src/main/latex'
    def tmpDirectory = 'build'

    def latexFile = "${-> documentBase}.tex"
    def pdfFile = "${-> documentBase}.pdf"
    def biberConfigFile = "${-> documentBase}.bcf"
    def latexFilePath = "${-> rawDirectory}/${-> latexFile}"
    def pdfFilePath = "${-> tmpDirectory}/${-> pdfFile}"
    def biberConfigFilePath = "${-> tmpDirectory}/${-> biberConfigFile}"
}
