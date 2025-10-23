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
            
            def nexusRepo = version.endsWith('SNAPSHOT') ?
                'country-service-maven-snapshots' :
                'country-service-maven-releases'
            
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
                    echo "📦 Downloading latest WAR from Nexus..."
                    
                    # Base path in Nexus
                    BASE_PATH="${groupId.replace('.', '/')}/${artifactId}/${version}"
                    
                    if [[ "${version}" == *"SNAPSHOT"* ]]; then
                        echo "Fetching SNAPSHOT metadata..."
                        
                        # Download maven-metadata.xml
                        curl -s -u \${NEXUS_USER}:\${NEXUS_PASS} \
                        -o maven-metadata.xml \
                        "http://localhost:8081/repository/${nexusRepo}/\${BASE_PATH}/maven-metadata.xml"
                        
                        if [ ! -f maven-metadata.xml ]; then
                            echo "❌ Failed to download maven-metadata.xml"
                            exit 1
                        fi
                        
                        # Parse metadata to get timestamp and buildNumber
                        TIMESTAMP=\$(grep -oP '(?<=<timestamp>)[^<]+' maven-metadata.xml | head -1)
                        BUILD_NUMBER=\$(grep -oP '(?<=<buildNumber>)[^<]+' maven-metadata.xml | head -1)
                        
                        echo "Latest SNAPSHOT: \${TIMESTAMP}-\${BUILD_NUMBER}"
                        
                        # Construct actual filename
                        BASE_VERSION=\${version%-SNAPSHOT}
                        ACTUAL_FILE="${artifactId}-\${BASE_VERSION}-\${TIMESTAMP}-\${BUILD_NUMBER}.war"
                        
                        DOWNLOAD_URL="http://localhost:8081/repository/${nexusRepo}/\${BASE_PATH}/\${ACTUAL_FILE}"
                    else
                        # For release versions
                        ACTUAL_FILE="${artifactId}-${version}.war"
                        DOWNLOAD_URL="http://localhost:8081/repository/${nexusRepo}/\${BASE_PATH}/\${ACTUAL_FILE}"
                    fi
                    
                    echo "Downloading from: \${DOWNLOAD_URL}"
                    
                    # Download the WAR
                    curl -u \${NEXUS_USER}:\${NEXUS_PASS} \
                    -o deployment.war \
                    -f -L "\${DOWNLOAD_URL}"
                    
                    if [ ! -f deployment.war ]; then
                        echo "❌ Failed to download WAR file"
                        exit 1
                    fi
                    
                    echo "✓ Downloaded successfully"
                    ls -lh deployment.war
                    
                    echo "🗑️  Undeploying existing application..."
                    curl -s -u \${TOMCAT_USER}:\${TOMCAT_PASS} \
                    "http://localhost:8090/manager/text/undeploy?path=/country-service" || echo "No existing deployment"
                    
                    echo "⏳ Waiting for undeploy to complete..."
                    sleep 5
                    
                    echo "🚀 Deploying WAR to Tomcat..."
                    RESPONSE=\$(curl -s -u \${TOMCAT_USER}:\${TOMCAT_PASS} \
                    --upload-file deployment.war \
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
