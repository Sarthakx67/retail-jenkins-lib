def call(Map config) {

    sh """
    echo "🚀 Deploying ${config.service} version ${config.version}"

    cd helm-repo/retail-store-helm-chart

    helm upgrade --install retail-store . \
        -n retail-store-${config.namespace} \
        --set ${config.service}.image.tag=${config.version} \
        -f values.yaml \
        -f values/${config.env}/values-${config.namespace}-${config.env}.yaml \
        --create-namespace \
    
    """

    // kubectl rollout status deployment/src/${config.service} -n retail-store-${config.namespace}
    // """
}
