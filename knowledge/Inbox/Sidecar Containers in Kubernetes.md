---
title: "Sidecar Containers in Kubernetes"
source: "https://praneethreddybilakanti.medium.com/sidecar-containers-in-kubernetes-0e7829c96446"
author: "Praneeth Bilakanti"
published: "2024-08-19T17:33:44Z"
created: "2026-10-19T21:34:20+05:30"
description: "Overview:"
tags:
  - clippings
category: "Clipping"
---

## Overview:

Sidecar containers are secondary containers that run alongside the main application container within the same Pod. They are designed to extend or enhance the functionality of the main application container by providing services like logging, monitoring, security, or data synchronization without altering the primary application code.

### Enabling Sidecar Containers:

Starting with Kubernetes 1.29, the feature gate `SidecarContainers` is enabled by default. This allows you to specify a `restartPolicy` for containers listed in a Pod's `initContainers` field. These sidecar containers operate independently of other init containers and the main application container within the same Pod. They can be started, stopped, or restarted without affecting the main application container or other init containers.

### Sidecar Containers and Pod Lifecycle:

- **Persistent Init Containers**:  
	If an init container is created with its `restartPolicy` set to `Always`, it will start and remain running throughout the life of the Pod. This is useful for running support services that need to be separate from the main application container.
- **Readiness Probe**:  
	If a `readinessProbe` is specified for a sidecar container, it will be used to determine the `Ready` state of the Pod.
- **Sequential Execution with Init Containers**:  
	Sidecar containers defined within `initContainers` continue running after they have started, unlike regular init containers that exit after completing their tasks. This is important when multiple init containers are defined. Once a sidecar-style init container starts (indicated by the kubelet setting its `started` status to true), the kubelet proceeds to start the next init container in the sequence.

### Example Deployment with Sidecar:

Here’s an example of a Deployment with a sidecar container:

```cs
apiVersion: apps/v1
kind: Deployment
metadata:
  name: myapp
  labels:
    app: myapp
spec:
  replicas: 1
  selector:
    matchLabels:
      app: myapp
  template:
    metadata:
      labels:
        app: myapp
    spec:
      containers:
        - name: myapp
          image: alpine:latest
          command: ['sh', '-c', 'while true; do echo "logging" >> /opt/logs.txt; sleep 1; done']
          volumeMounts:
            - name: data
              mountPath: /opt
      initContainers:
        - name: logshipper
          image: alpine:latest
          restartPolicy: Always
          command: ['sh', '-c', 'tail -F /opt/logs.txt']
          volumeMounts:
            - name: data
              mountPath: /opt
      volumes:
        - name: data
          emptyDir: {}
```

This feature is also useful for running Jobs with sidecars, as the sidecar container will not prevent the Job from completing after the main container has nished. Here’s an example of a Job with two containers, one of which is a sidecar

```cs
apiVersion: batch/v1
kind: Job
metadata:
 name: myjob
spec:
 template:
   spec:
     containers:
       - name: myjob
         image: alpine:latest
         command: ['sh', '-c', 'echo "logging" > /opt/logs.txt']
         volumeMounts:
           - name: data
             mountPath: /opt
     initContainers:
       - name: logshipper
         image: alpine:latest
         restartPolicy: Always
         command: ['sh', '-c', 'tail -F /opt/logs.txt']
         volumeMounts:
           - name: data
             mountPath: /opt
     restartPolicy: Never
     volumes:
       - name: data
         emptyDir: {}
```

### Differences from Regular Containers:

- **Supporting Role**:  
	Sidecar containers provide additional functionality to the main application but do not execute the primary application logic.
- **Independent Lifecycle**:  
	They can be started, stopped, and restarted independently of the main containers. This allows you to update, scale, or maintain sidecar containers without affecting the primary application.
- **Shared Namespace**:  
	Sidecar containers share the same network and storage namespaces with the primary container, enabling close interaction and resource sharing.

### Differences from Init Containers:

- **Concurrently Running**:  
	Sidecar containers work alongside the main container throughout the Pod’s lifecycle, providing continuous service, unlike init containers, which run to completion before the main container starts.
- **Interaction with Main Containers**:  
	Sidecar containers can interact directly with the main application containers, sharing the same network namespace, filesystem, and environment variables.

### Resource Sharing within Containers:

- **Effective Resource Requests and Limits**:
- The highest resource request or limit defined on all init containers becomes the effective init request/limit. If no limit is specified for a resource, it is considered the highest limit.
- The Pod’s effective request/limit for a resource is the sum of pod overhead and the higher of:
- The sum of all non-init containers (app and sidecar containers) request/limit for that resource.
- The effective init request/limit for that resource.
- **Scheduling**:  
	Scheduling is based on effective requests/limits, meaning init containers can reserve resources during initialization that may not be used during the life of the Pod.
- **Quality of Service (QoS)**:  
	The QoS tier for the Pod is determined by the highest QoS tier among all init, sidecar, and app containers.
- **Quota and Limits**:  
	Quotas and limits are applied based on the effective Pod request and limit.
- **Pod Control Groups (cgroups)**:  
	Pod-level cgroups are based on the effective Pod request and limit, similar to how the scheduler operates.

This configuration allows for greater flexibility and control over container lifecycles within a Pod, making it possible to separate concerns and responsibilities between the main application and supporting services.

## Regular Sidecar Container Example

Here’s an example of a Kubernetes Deployment with a sidecar container:

```cs
apiVersion: apps/v1
kind: Deployment
metadata:
  name: myapp
spec:
  replicas: 1
  selector:
    matchLabels:
      app: myapp
  template:
    metadata:
      labels:
        app: myapp
    spec:
      containers:
        - name: myapp
          image: alpine:latest
          command: ['sh', '-c', 'while true; do echo "logging" >> /opt/logs.txt; sleep 1; done']
          volumeMounts:
            - name: data
              mountPath: /opt
        - name: logshipper
          image: alpine:latest
          command: ['sh', '-c', 'tail -F /opt/logs.txt']
          volumeMounts:
            - name: data
              mountPath: /opt
      volumes:
        - name: data
          emptyDir: {}
```

In this example:

- `myapp` is the main application container.
- `logshipper` is the sidecar container running alongside `myapp`, providing log shipping functionality

## Comparison of Init Containers and Sidecar Containers

![](https://miro.medium.com/v2/resize:fit:1400/format:webp/1*Qy5pDUZZXBiWBupFVho0Pw.png)

![](https://miro.medium.com/v2/resize:fit:1400/format:webp/1*7nEmr47hbNl-ha6MN_z5tA.png)

## Init Containers Example

```cs
apiVersion: apps/v1
kind: Deployment
metadata:
  name: myapp-init
  labels:
    app: myapp
spec:
  replicas: 1
  selector:
    matchLabels:
      app: myapp
  template:
    metadata:
      labels:
        app: myapp
    spec:
      containers:
        - name: myapp
          image: alpine:latest
          command: ['sh', '-c', 'while true; do echo "logging" >> /opt/logs.txt; sleep 1; done']
          volumeMounts:
            - name: data
              mountPath: /opt
      initContainers:
        - name: init-setup
          image: alpine:latest
          command: ['sh', '-c', 'echo "Setting up environment" > /opt/setup.txt']
          volumeMounts:
            - name: data
              mountPath: /opt
      volumes:
        - name: data
          emptyDir: {}
```

## Get Praneeth Bilakanti’s stories in your inbox

Join Medium for free to get updates from this writer.

**Description:**

- The `init-setup` container runs first, performing setup tasks (e.g., writing to a file) before the main `myapp` container starts.
- The Pod is not considered ready until `init-setup` completes successfully.

## Sidecar Containers Example

```cs
apiVersion: apps/v1
kind: Deployment
metadata:
  name: myapp-sidecar
  labels:
    app: myapp
spec:
  replicas: 1
  selector:
    matchLabels:
      app: myapp
  template:
    metadata:
      labels:
        app: myapp
    spec:
      containers:
        - name: myapp
          image: alpine:latest
          command: ['sh', '-c', 'while true; do echo "logging" >> /opt/logs.txt; sleep 1; done']
          volumeMounts:
            - name: data
              mountPath: /opt
        - name: logshipper
          image: alpine:latest
          command: ['sh', '-c', 'tail -F /opt/logs.txt']
          volumeMounts:
            - name: data
              mountPath: /opt
      volumes:
        - name: data
          emptyDir: {}
```

**Description:**

- The `logshipper` sidecar container runs alongside the main `myapp` container, continuously processing logs.
- Both containers share the same network and storage namespaces and can interact directly with each other.

## Init Containers as Sidecar Containers Example

```cs
apiVersion: apps/v1
kind: Deployment
metadata:
  name: myapp-init-sidecar
  labels:
    app: myapp
spec:
  replicas: 1
  selector:
    matchLabels:
      app: myapp
  template:
    metadata:
      labels:
        app: myapp
    spec:
      containers:
        - name: myapp
          image: alpine:latest
          command: ['sh', '-c', 'while true; do echo "logging" >> /opt/logs.txt; sleep 1; done']
          volumeMounts:
            - name: data
              mountPath: /opt
      initContainers:
        - name: persistent-init
          image: alpine:latest
          restartPolicy: Always
          command: ['sh', '-c', 'tail -F /opt/logs.txt']
          volumeMounts:
            - name: data
              mountPath: /opt
      volumes:
        - name: data
          emptyDir: {}
```

**Description:**

- The `persistent-init` container is configured to run with `restartPolicy: Always`, making it act similarly to a sidecar container but as part of the init containers.
- This container will continuously run and monitor logs, extending its role beyond traditional init tasks.

## Summary

- **Init Containers**: Perform one-time setup before the main container starts.
- **Sidecar Containers**: Provide additional, ongoing functionality alongside the main container.
- **Init Containers as Sidecar Containers**: Combine the roles, allowing the init container to offer ongoing functionality beyond initial setup.

These examples demonstrate how different types of containers can be used in Kubernetes to manage tasks and provide functionality, depending on the requirements of the application.

## 1\. Init Containers as Sidecar Containers

**Purpose**:

- This approach allows init containers to run with a `restartPolicy` of `Always`, so they continue running throughout the lifecycle of the Pod. This can be useful for certain persistent setup tasks or supporting services that should run alongside the main application container but still benefit from the init container ordering guarantees.

**Example**:

```cs
apiVersion: apps/v1
kind: Deployment
metadata:
  name: myapp
spec:
  replicas: 1
  selector:
    matchLabels:
      app: myapp
  template:
    metadata:
      labels:
        app: myapp
    spec:
      containers:
        - name: myapp
          image: alpine:latest
          command: ['sh', '-c', 'while true; do echo "logging" >> /opt/logs.txt; sleep 1; done']
          volumeMounts:
            - name: data
              mountPath: /opt
      initContainers:
        - name: logshipper
          image: alpine:latest
          restartPolicy: Always
          command: ['sh', '-c', 'tail -F /opt/logs.txt']
          volumeMounts:
            - name: data
              mountPath: /opt
      volumes:
        - name: data
          emptyDir: {}
```

**Pros**:

- **Consistent Ordering**: Benefits from the init container’s ordering guarantees.
- **Ongoing Support**: Can run continuously and provide support services throughout the Pod’s lifecycle.

**Cons**:

- **Limited Flexibility**: Less flexible than regular sidecar containers in terms of lifecycle management.
- **Complexity**: May introduce complexity by mixing init container semantics with sidecar behavior.

## 2\. Regular Sidecar Containers

**Purpose**:

- Designed for tasks that need to run alongside the main application container for its entire lifecycle. They are useful for adding ongoing functionality like logging, monitoring, or data synchronization.

**Example**:

```cs
apiVersion: apps/v1
kind: Deployment
metadata:
  name: myapp
spec:
  replicas: 1
  selector:
    matchLabels:
      app: myapp
  template:
    metadata:
      labels:
        app: myapp
    spec:
      containers:
        - name: myapp
          image: alpine:latest
          command: ['sh', '-c', 'while true; do echo "logging" >> /opt/logs.txt; sleep 1; done']
          volumeMounts:
            - name: data
              mountPath: /opt
        - name: logshipper
          image: alpine:latest
          command: ['sh', '-c', 'tail -F /opt/logs.txt']
          volumeMounts:
            - name: data
              mountPath: /opt
      volumes:
        - name: data
          emptyDir: {}
```

**Pros**:

- **Flexibility**: Can be started, stopped, and restarted independently of the main container.
- **Lifecycle Management**: Regular sidecar containers can be managed separately from the main container.

**Cons**:

- **Resource Usage**: Can increase the resource consumption of the Pod.
- **Complexity**: Requires careful coordination to ensure that the sidecar containers fulfill their purpose effectively without interfering with the main container.

## Recommendation

- **Use Regular Sidecar Containers** when you need ongoing, continuous support tasks that run alongside the main application container and can be independently managed.
- **Use Init Containers as Sidecar Containers** if you need the ordering guarantees and the preconditions provided by init containers but also want them to persist and provide ongoing support throughout the lifecycle of the Pod.

In most cases, **Regular Sidecar Containers** are recommended for their simplicity and flexibility unless you have specific requirements that benefit from the init container guaran