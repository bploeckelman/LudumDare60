#!groovy
import groovy.json.JsonOutput
import hudson.Util;

pipeline {
    agent any

//    tools {
//        jdk 'jdk-11' // Match what your libGDX project needs. Set this up in Manage Jenkins -> Tools
//    }

    options {
        timestamps()
        timeout(time: 30, unit: 'MINUTES') // kill hung GWT compiles
    }

    triggers {
        githubPush()
//        pollSCM('H/5 * * * *') //polling for changes, here every 5 min
    }

    stages {
        stage("Setup") {
            steps {
                script {
                    env.GIT_COMMIT_MSG = sh (script: 'git log -1 --pretty=%B ${GIT_COMMIT}', returnStdout: true).trim()
                    env.GIT_REPO_NAME = env.GIT_URL.replaceFirst(/^.*\/([^\/]+?).git$/, '$1')
                    env.REMOTE_DIR =  "inthelifeofdoug.com/LudumDareBuilds/${env.GIT_REPO_NAME}/${env.BRANCH_NAME}/${BUILD_NUMBER}"
                    env.BUILD_LINK = "http://${env.REMOTE_DIR}"

                    mqttNotification brokerUrl: 'tcp://home.inthelifeofdoug.com:1883',
                            credentialsId: 'mqttcreds',
                            message: getBeginMessage(),
                            qos: '2',
                            topic: "jenkins/${env.GIT_REPO_NAME}"
                    sh 'chmod +x ./gradlew' // Fix gradlew perms once up front

                }
            }
        }

        stage("Desktop") {
            steps {
                sh './gradlew lwjgl3:jar'
                archiveArtifacts artifacts: 'lwjgl3/build/libs/*.jar', allowEmptyArchive: true
            }
        }
//        stage("HTML - GWT") {
//            steps {
//                sh './gradlew html:dist'
//            }
//        }
        stage("HTML - TeaVM") {
            steps {
                sh './gradlew teavm:build'
            }
        }


        stage("Upload to Host") {
            when { // Only upload if builds succeeded
                expression { currentBuild.resultIsBetterOrEqualTo('SUCCESS') }
            }
            steps {
                sshPublisher(
                        publishers: [
                                sshPublisherDesc(
                                        configName: "wxpick",
                                        verbose: false, // set true only when debugging
                                        transfers: [
                                                sshTransfer(
                                                        execCommand: "mkdir -p ${env.REMOTE_DIR}/teavm", // ensure dirs exist
                                                        execTimeout: 120000
                                                ),
//                                                sshTransfer(
//                                                        sourceFiles: "html/build/dist/**",
//                                                        removePrefix: "html/build/dist/",
//                                                        remoteDirectory: "${env.REMOTE_DIR}",
//                                                ),
                                                sshTransfer(
                                                        sourceFiles: "teavm/build/dist/webapp/**",
                                                        removePrefix: "teavm/build/dist/webapp/",
                                                        remoteDirectory: "${env.REMOTE_DIR}/teavm",
                                                )
                                        ]
                                )
                        ]
                )
            }
        }

    }

    post{
        always {
            mqttNotification brokerUrl: 'tcp://home.inthelifeofdoug.com:1883',
                    credentialsId: 'mqttcreds',
                    message: getMessage(),
                    qos: '2',
                    topic: "jenkins/${env.GIT_REPO_NAME}"
        }
        success {
            echo "Deployed to: ${env.BUILD_LINK}"
        }
    }


}

def getMessageAttrib() {
    def changes = []
    def changeLogSets = currentBuild.changeSets
    for (int i = 0; i < changeLogSets.size(); i++) {
        def entries = changeLogSets[i].items
        for (int j = 0; j < entries.length; j++) {
            def entry = entries[j]
            def files = new ArrayList(entry.affectedFiles)
            echo "${entry.commitId} by ${entry.author} on ${new Date(entry.timestamp)}: ${entry.msg}"
            def commit = [commitId: "${entry.commitId}", author: "${entry.author}", message: "${entry.msg}", fileCount: "${files.size()}"]
            commit.files = [];
            for (int k = 0; k < files.size(); k++) {
                def file = files[k]
                echo "  ${file.editType.name} ${file.path}"
                commit.files << "${file.editType.name} ${file.path}"
            }
            changes << commit
        }
    }

    def message = [
            buildnumber: "${BUILD_NUMBER}",
            status: "${currentBuild.currentResult}",
            title: "${env.GIT_REPO_NAME}",
            project: "${currentBuild.projectName}",
            duration: "${Util.getTimeSpanString(System.currentTimeMillis() - currentBuild.startTimeInMillis)}",
            commitmessage: "${env.GIT_COMMIT_MSG}",
            buildURL: "${env.BUILD_URL}",
            changesets: changes
    ]

    return message
}

def getBeginMessage() {
    def message = getMessageAttrib()
    message.status = "STARTING"
    return JsonOutput.prettyPrint(JsonOutput.toJson(message))

}

def getMessage() {
    def message = getMessageAttrib()
    if (currentBuild.resultIsBetterOrEqualTo("SUCCESS")) {
        message.link = env.BUILD_LINK
    }
    return JsonOutput.prettyPrint(JsonOutput.toJson(message))
}
