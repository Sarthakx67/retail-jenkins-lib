# 🛒 Retail Store — Jenkins Shared Library

> A reusable Jenkins Shared Library for automating CI/CD pipelines across microservices in the Retail Store platform. Handles version detection, Docker image builds, and Kubernetes deployments via Helm — all from a single pipeline definition.

---

## 📁 Repository Structure

```
vars/
├── microservicePipeline.groovy   # Main pipeline orchestrator
├── detectVersion.groovy          # Auto-detects service version
├── dockerBuildPush.groovy        # Builds & pushes Docker image
└── deployK8s.groovy              # Deploys to Kubernetes via Helm
```

---

## ⚡ Quick Start

### 1. Register the Library in Jenkins

Go to **Manage Jenkins → Configure System → Global Pipeline Libraries** and add:

| Field | Value |
|---|---|
| Name | `retail-store-lib` |
| Default Version | `main` |
| Source | Your Git repository URL |

### 2. Use in a `Jenkinsfile`

```groovy
@Library('retail-store-lib') _

microservicePipeline(
    service:   'cart',
    type:      'node',
    namespace: 'dev',
    env:       'staging',
    agent:     'AGENT-1'
)
```

That's it. The library handles the rest.

---

## 🔧 Shared Variables (vars/)

### `microservicePipeline(Map config)`

The top-level pipeline entry point. Chains all stages together.

**Parameters:**

| Parameter | Required | Description |
|---|---|---|
| `service` | ✅ | Microservice name (e.g. `cart`, `orders`) |
| `type` | ✅ | Build type: `maven`, `go`, or `node` |
| `namespace` | ✅ | Kubernetes namespace suffix |
| `env` | ✅ | Target environment (e.g. `staging`, `prod`) |
| `agent` | ❌ | Jenkins agent label (defaults to `AGENT-1`) |

**Pipeline Stages:**

```
Detect Version  →  Build & Push Image  →  Checkout Helm Repo  →  Deploy
```

---

### `detectVersion(Map config)`

Detects the service version from its source code and sets `env.VERSION` and `env.IMAGE`.

| Build Type | Detection Method |
|---|---|
| `maven` | Reads `project.version` via `mvn help:evaluate` |
| `go` | Parses `@version` annotation from `main.go` |
| `node` | Reads `version` field from `package.json` |

**Sets environment variables:**

```
env.VERSION  →  e.g. 1.4.2
env.IMAGE    →  e.g. sarthak6700/retail-store-cart:1.4.2
```

---

### `dockerBuildPush(Map config)`

Builds a Docker image from `src/<service>/` and pushes it to Docker Hub.

**Requires Jenkins credential:** `dockerhub-creds` *(Username + Password)*

```groovy
dockerBuildPush(service: 'cart')
```

---

### `deployK8s(Map config)`

Deploys the service to Kubernetes using Helm with environment-specific values files.

**Helm command executed:**

```bash
helm upgrade --install retail-store . \
    -n retail-store-<namespace> \
    --set <service>.image.tag=<version> \
    -f values.yaml \
    -f values/<env>/values-<namespace>-<env>.yaml \
    --create-namespace
```

**Parameters:**

| Parameter | Description |
|---|---|
| `service` | Microservice name |
| `version` | Docker image tag to deploy |
| `namespace` | Kubernetes namespace suffix |
| `env` | Target environment |

---

## 🏗️ How It All Fits Together

```
Jenkinsfile
    └── microservicePipeline(config)
            ├── detectVersion()       ── reads version from source
            ├── dockerBuildPush()     ── builds & pushes to Docker Hub
            ├── git checkout          ── clones Helm chart repo
            └── deployK8s()          ── helm upgrade --install
```

The Helm chart is fetched from:
```
https://github.com/Sarthakx67/retail-store-aws-deployment.git  (branch: main)
```

---

## 📋 Prerequisites

| Tool | Purpose |
|---|---|
| Jenkins | CI/CD runner with this library configured |
| Docker | Available on the Jenkins agent |
| `kubectl` | Configured with cluster access |
| `helm` | v3+ installed on the agent |
| `mvn` | Required for Maven services |

**Jenkins Credentials required:**

- `dockerhub-creds` — Docker Hub username & password

---

## 🗂️ Helm Values Convention

The library expects Helm values files in this layout inside the chart repo:

```
retail-store-helm-chart/
├── values.yaml                                  # Base values
└── values/
    └── <env>/
        └── values-<namespace>-<env>.yaml        # Per-environment overrides
```

**Example** for `namespace=dev`, `env=staging`:
```
values/staging/values-dev-staging.yaml
```

---

## 💡 Examples

### Node.js Service

```groovy
@Library('retail-store-lib') _

microservicePipeline(
    service:   'ui',
    type:      'node',
    namespace: 'frontend',
    env:       'prod'
)
```

### Java/Maven Service

```groovy
@Library('retail-store-lib') _

microservicePipeline(
    service:   'orders',
    type:      'maven',
    namespace: 'backend',
    env:       'staging'
)
```

### Go Service

```groovy
@Library('retail-store-lib') _

microservicePipeline(
    service:   'inventory',
    type:      'go',
    namespace: 'backend',
    env:       'prod',
    agent:     'AGENT-2'
)
```

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/my-change`
3. Make your changes and test in a sandbox Jenkins environment
4. Submit a Pull Request with a clear description

---

<p align="center">
  Built for the <strong>Retail Store</strong> platform · Maintained by <a href="https://github.com/Sarthakx67">@Sarthakx67</a>
</p>
