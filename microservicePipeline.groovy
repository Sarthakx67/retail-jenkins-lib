def call(Map config) {

    pipeline {
        agent { label config.agent ?: 'AGENT-1' }

        stages {
            stage('Detect Version') {
                steps {
                    detectVersion(service: config.service, type: config.type)
                }
            }

            stage('Build & Push Image') {
                steps {
                    dockerBuildPush(service: config.service)
                }
            }

            stage('Deploy') {
                steps {
                    deployK8s(
                        service: config.service,
                        version: env.VERSION,
                        namespace: config.namespace,
                        env: config.env
                    )
                }
            }
        }
    }
}
