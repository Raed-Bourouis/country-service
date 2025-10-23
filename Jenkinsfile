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

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv(installationName:'MySonarQubeServer', credentialsId:'country-service') {
                    sh 'mvn sonar:sonar -Dsonar.projectKey=country-service -Dsonar.projectName=country-service'
                }
            }
        }
        
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

        stage('Deploy to Tomcat from Nexus') {
            steps {
                script {
                    def pom = readMavenPom file: 'pom.xml'
                    def version = pom.version
                    def artifactId = pom.artifactId
                    def groupId = pom.groupId
                    
                    // Determine which Nexus repository to use
                    def nexusRepo = version.endsWith('SNAPSHOT') ?
                        'country-service-maven-snapshots' :
                        'country-service-maven-releases'
                    
                    // Construct Nexus download URL
                    def nexusUrl = "http://localhost:8081/repository/${nexusRepo}/${groupId.replace('.', '/')}/${artifactId}/${version}/${artifactId}-${version}.war"
                    
                    echo "Nexus URL: ${nexusUrl}"
                    
                    withCredentials([
                        usernamePassword(
                            credentialsId: 'nexus-credentials',
                            usernameVariable: 'NEXUS_USER',
                            passwordVariable: 'NEXUS_PASS'
                        ),
                        usernamePassword(
                            credentialsId: 'tomcat-credentials',
                            usernameVariable: 'TOMCAT_USER',
                            passwordVariable: 'TOMCAT_PASS'
                        )
                    ]) {
                        sh """
                            echo "📦 Downloading WAR from Nexus..."
                            curl -u \${NEXUS_USER}:\${NEXUS_PASS} \
                            -o ${artifactId}-${version}.war \
                            -f "${nexusUrl}"
                            
                            echo "✓ Downloaded: ${artifactId}-${version}.war"
                            ls -lh ${artifactId}-${version}.war
                            
                            echo "🗑️  Undeploying existing application..."
                            curl -s -u \${TOMCAT_USER}:\${TOMCAT_PASS} \
                            "http://localhost:8090/manager/text/undeploy?path=/country-service" || echo "No existing deployment"
                            
                            echo "⏳ Waiting for undeploy to complete..."
                            sleep 5
                            
                            echo "🚀 Deploying WAR to Tomcat..."
                            RESPONSE=\$(curl -s -u \${TOMCAT_USER}:\${TOMCAT_PASS} \
                            --upload-file ${artifactId}-${version}.war \
                            "http://localhost:8090/manager/text/deploy?path=/country-service&update=true")
                            
                            echo "Tomcat Response: \$RESPONSE"
                            
                            if echo "\$RESPONSE" | grep -q "OK"; then
                                echo "✅ Deployment successful!"
                            else
                                echo "❌ Deployment failed!"
                                echo "\$RESPONSE"
                                exit 1
                            fi
                            
                            echo "🔍 Verifying deployment..."
                            curl -s -u \${TOMCAT_USER}:\${TOMCAT_PASS} \
                            "http://localhost:8090/manager/text/list" | grep country-service
                            
                            echo "✅ Application deployed successfully!"
                            echo "🌐 Access at: http://localhost:8090/country-service"
                        """
                    }
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