pipeline {
    agent any
    
    tools {
        maven 'mvn'
    }
    
    stages {
        stage('Checkout code') {
            steps {
                git branch: 'main', url: 'https://github.com/Raed-Bourouis/country-service.git'
            }
        }
        
        stage('Compile, test code, package in war file and store in maven repo') {
            steps {
                sh 'mvn clean install'
            }
            post {
                success {
                    junit allowEmptyResults: true,
                        testResults: '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Build Dockerfile') {
            steps {
                sh 'docker build . -t my-country-service:$BUILD_NUMBER'
                withCredentials([string(credentialsId: 'dockerhub-pwd', variable: 'dockerhubpwd')]) {
                    sh 'docker login -u raedbourouis -p ${dockerhubpwd}'
                }
                sh 'docker tag my-country-service:$BUILD_NUMBER raedbourouis/my-country-service:$BUILD_NUMBER'
                sh 'docker push raedbourouis/my-country-service:$BUILD_NUMBER'
            }
        }
        
        stage('Deploy to Kubernetes') {
            steps {
                script {
                    kubeconfig(credentialsId: 'kubeconfig-file', serverUrl: '') {
                        sh 'kubectl apply -f deployment.yaml'
                        sh 'kubectl apply -f service.yaml'
                    }
                }
            }
        }

        /*
        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv(installationName: 'MySonarQubeServer', credentialsId: 'country-service') {
                    sh 'mvn sonar:sonar -Dsonar.projectKey=country-service -Dsonar.projectName=country-service'
                }
            }
        }
        */
        
        /*
        stage('Upload to Nexus') {
            steps {
                script {
                    def pom = readMavenPom file: 'pom.xml'
                    def version = pom.version
                    def artifactId = pom.artifactId
                    def groupId = pom.groupId

                    def nexusRepo = version.endsWith('SNAPSHOT') ?
                        'country-service-maven-snapshots' :
                        'country-service-maven-releases'

                    nexusArtifactUploader(
                        nexusVersion: 'nexus3',
                        protocol: 'http',
                        nexusUrl: 'localhost:8081',
                        groupId: groupId,
                        version: version,
                        repository: nexusRepo,
                        credentialsId: 'nexus-credentials',
                        artifacts: [[
                            artifactId: artifactId,
                            classifier: '',
                            file: "target/${artifactId}-${version}.war",
                            type: 'war'
                        ]]
                    )
                }
            }
        }

        stage('Deploy from Nexus to Tomcat') {
            steps {
                script {
                    withCredentials([
                        usernamePassword(credentialsId: 'nexus-credentials', usernameVariable: 'NEXUS_USER', passwordVariable: 'NEXUS_PASS'),
                        usernamePassword(credentialsId: 'tomcat-credentials', usernameVariable: 'TOMCAT_USER', passwordVariable: 'TOMCAT_PASS')
                    ]) {
                        sh '''
                            # Get the latest snapshot version
                            METADATA_URL="http://localhost:8081/repository/country-service-maven-snapshots/com/ensi/countryservice/country-service/0.0.1-SNAPSHOT/maven-metadata.xml"
                            
                            # Download metadata
                            curl -s -u ${NEXUS_USER}:${NEXUS_PASS} $METADATA_URL -o maven-metadata.xml
                            
                            # Parse the latest snapshot version
                            SNAPSHOT_VERSION=$(grep -oP '<value>\\K[^<]+' maven-metadata.xml | head -1)
                            echo "Latest snapshot version: $SNAPSHOT_VERSION"
                            
                            # Download the WAR
                            WAR_URL="http://localhost:8081/repository/country-service-maven-snapshots/com/ensi/countryservice/country-service/0.0.1-SNAPSHOT/country-service-${SNAPSHOT_VERSION}.war"
                            curl -u ${NEXUS_USER}:${NEXUS_PASS} -L -o country-service.war "$WAR_URL"
                            
                            # Deploy to Tomcat
                            curl -v -u ${TOMCAT_USER}:${TOMCAT_PASS} \
                                --upload-file country-service.war \
                                "http://localhost:8090/manager/text/deploy?path=/country-service&update=true"
                        '''
                    }
                }
            }
        }
        */
    }
    
    post {
        success {
            echo 'Pipeline executed successfully!'
        }
        failure {
            echo 'Pipeline failed! Check console output for details.'
        }
    }
}