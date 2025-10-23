pipeline {
    agent any
    tools {
        maven 'mvn'
    }
    stages {
        stage('Checkout code')
        {
            steps {
                git branch: 'main', url: 'https://github.com/Raed-Bourouis/country-service.git'
            }
        }
        stage('Compile, test code, package in war file and store in maven repo')
        {
            steps {
                sh 'mvn clean install'
            }
            post{
                success{
                    junit allowEmptyResults: true,
                    testResults: '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('SonarQube Analysis')
        {
            steps{
                withSonarQubeEnv(installationName:'MySonarQubeServer', credentialsId:'country-service'){
                    sh 'mvn sonar:sonar -Dsonar.projectKey=country-service -Dsonar.projectName=country-service'
                }
            }
        }
        stage('Upload to Nexus') 
        {
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

        stage('Deploy to Tomcat') {
            steps {
                script {
                    def pom = readMavenPom file: 'pom.xml'
                    def version = pom.version
                    def artifactId = pom.artifactId
                    def groupId = pom.groupId
                    
                    // Download from Nexus first
                    sh """
                        curl -u admin:180203 -O http://localhost:8081/repository/country-service-maven-snapshots/${groupId.replace('.', '/')}/${artifactId}/${version}/${artifactId}-${version}.war
                    """
                    
                    // Then deploy the downloaded WAR
                    deploy adapters: [tomcat9(...)],
                    contextPath: '/country-service',
                    war: "${artifactId}-${version}.war"
                }
        }
}
    }

    post {
        success {
            echo 'Pipeline executed successfully!'
            echo 'Application deployed to: http://localhost:8090/country-service'
        }
        failure {
            echo 'Pipeline failed! Check console output for details.'
        }
    }
    
}
