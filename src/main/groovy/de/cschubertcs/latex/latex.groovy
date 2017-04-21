package de.cschubertcs.latex

import org.gradle.api.GradleException
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.tasks.Exec

class LatexPlugin implements Plugin<Project> {

  private boolean isExecutableExisting(execName) {
    def exists = false
    def pathEntries = System.getenv("PATH").tokenize(File.pathSeparator)
    for(entry in pathEntries) {
      def execPath = new File(entry, execName)
      exists |= (execPath.exists() && execPath.isFile() && execPath.canExecute())
    }
    return exists
  }

  @Override
  void apply(Project project) {
    project.extensions.create("latex", LatexPluginExtension)

    project.defaultTasks 'cook'

    project.task('check') {
      doFirst{
        println("Checking that '${project.latex.latexBinary}' is in the path system variable...")
        if (!isExecutableExisting("${project.latex.latexBinary}")) {
          throw new InvalidUserDataException("The binary '${project.latex.latexBinary}' is not present in the path.")
        }

        println("Checking that '${project.latex.bibtexBinary}' is in the path system variable...")
        if (!isExecutableExisting("${project.latex.bibtexBinary}")) {
          throw new InvalidUserDataException("The binary '${project.latex.bibtexBinary}' is not present in the path.")
        }

        println("Checking that the primary source file exists...")
        if (!project.file("${project.latex.latexFilePath}").exists()) {
          throw new InvalidUserDataException("The file '${project.latex.latexFilePath}' does not exist.")
        }

        println("Checking that the target directory exists (or creating it if necessary)...")
        if (!project.file("${project.latex.tmpDirectory}").exists()) {
          project.mkdir project.latex.tmpDirectory
          println "Created target directory ${project.latex.tmpDirectory}"
        }
      }
    }

    (1..3).each { counter ->
      project.task("pdfLatexRun$counter", type: Exec, dependsOn: project.check) {
        doFirst {
          println "Building ${project.latex.latexFilePath} into ${project.latex.tmpDirectory} via pdflatex."
        }

        dependsOn project.check

        if( counter > 2 ) {
          // the third run is only needed in case that there is a biber file
          project."pdfLatexRun$counter".onlyIf {
            project.file("${->project.latex.biberConfigFilePath}").exists()
          }
        }

        standardOutput = new ByteArrayOutputStream() // stops output to STDOUT

        // TODO: check what this does
//        inputs.files
        outputs.upToDateWhen{false}

        workingDir '.'
        commandLine(
          "pdflatex",
          "-output-directory", "${->project.latex.tmpDirectory}",
          "${->project.latex.latexFilePath}"
        )

        ignoreExitValue true

        // TODO: check what this does
        outputs.file project.file("${->project.latex.pdfFilePath}")

        doLast {
          if (execResult.exitValue != 0) {
            println(standardOutput.toString())
            throw new GradleException("There was an error while executing the command '" +
              commandLine.join(" ") + "'. See the output above for more information.")
          } else {
            println "Built ${project.latex.latexFilePath} into ${project.latex.rawDirectory} via pdflatex."
          }
        }
      }
    }

    project.task('biber', type: Biber, dependsOn: project.check) {
      doFirst {
        println "Building the bibliography of ${project.latex.latexFilePath} into ${project.latex.tmpDirectory} via biber."
      }

      project.biber.onlyIf{
        project.file("${->project.latex.biberConfigFilePath}").exists()
      }

	  setDocumentBase("${project.latex.documentBase}")
	  setSourceDirectory("${project.latex.rawDirectory}")
	  setTargetDirectory("${project.latex.Directory}")

      doLast {
        if (execResult.exitValue != 0) {
          println(standardOutput.toString())
          throw new GradleException("There was an error while executing the command '" + commandLine.join(" ") + "'. See the output above for more information.")
        } else {
          println "Built the bibliography of ${project.latex.latexFilePath} into ${project.latex.tmpDirectory} via biber."
        }
      }
    }


    project.task('cook') {
      dependsOn project.pdfLatexRun1
      dependsOn project.pdfLatexRun2
      dependsOn project.biber
      dependsOn project.pdfLatexRun3

      project.pdfLatexRun2.mustRunAfter project.pdfLatexRun1
      project.biber.mustRunAfter project.pdfLatexRun2
      project.pdfLatexRun3.mustRunAfter project.biber

      doLast {
        println "Cooked ${project.latex.latexFilePath} to ${project.latex.pdfFilePath}."
      }
    }
  }
}

class LatexPluginExtension {
    def latexBinary = 'pdflatex'
    def bibtexBinary = 'biber'

    def documentBase = 'Test'
    def rawDirectory = 'src'
    def tmpDirectory = 'build'

    def latexFile = "${-> documentBase}.tex"
    def pdfFile = "${-> documentBase}.pdf"
    def biberConfigFile = "${-> documentBase}.bcf"
    def latexFilePath = "${-> rawDirectory}/${-> latexFile}"
    def pdfFilePath = "${-> tmpDirectory}/${-> pdfFile}"
    def biberConfigFilePath = "${-> tmpDirectory}/${-> biberConfigFile}"
}
