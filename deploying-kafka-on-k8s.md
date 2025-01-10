### k8s에 kafka 배포

#### 1. kafka 30.1.8 설치
```bash
$ helm install kafka -f values.yaml oci://registry-1.docker.io/bitnamicharts/kafka --version 30.1.8

Pulled: registry-1.docker.io/bitnamicharts/kafka:30.1.8
Digest: sha256:6714a9cb82b6fff281bb1fc8a7d8eea3b263ae802798bed8a9ebcc5f54366924
NAME: kafka
LAST DEPLOYED: Thu Jan  9 10:12:47 2025
NAMESPACE: default
STATUS: deployed
REVISION: 1
TEST SUITE: None
NOTES:
CHART NAME: kafka
CHART VERSION: 30.1.8
APP VERSION: 3.8.1

** Please be patient while the chart is being deployed **

Kafka can be accessed by consumers via port 9092 on the following DNS name from within your cluster:

    kafka.default.svc.cluster.local

Each Kafka broker can be accessed by producers via port 9092 on the following DNS name(s) from within your cluster:

    kafka-controller-0.kafka-controller-headless.default.svc.cluster.local:9092
    kafka-controller-1.kafka-controller-headless.default.svc.cluster.local:9092
    kafka-controller-2.kafka-controller-headless.default.svc.cluster.local:9092

To create a pod that you can use as a Kafka client run the following commands:

    kubectl run kafka-client --restart='Never' --image docker.io/bitnami/kafka:3.8.1-debian-12-r0 --namespace default --command -- sleep infinity
    kubectl exec --tty -i kafka-client --namespace default -- bash

    PRODUCER:
        kafka-console-producer.sh \
            --bootstrap-server kafka.default.svc.cluster.local:9092 \
            --topic test

    CONSUMER:
        kafka-console-consumer.sh \
            --bootstrap-server kafka.default.svc.cluster.local:9092 \
            --topic test \
            --from-beginning

WARNING: There are "resources" sections in the chart not set. Using "resourcesPreset" is not recommended for production. For production installations, please set the following values according to your workload needs:
  - volumePermissions.resources
+info https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/
```

#### 2. Persistent Volume 생성
- Persistent Volume Claim 목록 조회
  ```bash
  $ kubectl get pvc

  NAME                      STATUS    VOLUME   CAPACITY   ACCESS MODES   STORAGECLASS   AGE
  data-kafka-controller-0   Pending                                                     28h
  data-kafka-controller-1   Pending                                                     28h
  data-kafka-controller-2   Pending                                                     28h
  ```

- Persistent Volume 생성
  ```bash
  $ kubectl create -f kafka-pv-1.yaml
  $ kubectl create -f kafka-pv-2.yaml
  $ kubectl create -f kafka-pv-3.yaml
  ```

- Persistent Volume 생성
  ```bash
  $ kubectl get pv

  NAME         CAPACITY   ACCESS MODES   RECLAIM POLICY   STATUS   CLAIM                             STORAGECLASS   REASON   AGE
  kafka-pv-1   8Gi        RWO            Recycle          Bound    default/data-kafka-controller-0                           38h
  kafka-pv-2   8Gi        RWO            Recycle          Bound    default/data-kafka-controller-1                           37h
  kafka-pv-3   8Gi        RWO            Recycle          Bound    default/data-kafka-controller-2                           37h
  ```

- Persistent Volume Claim와 Persistent Volume 연결 확인
  ```bash
  $ kubectl get pvc

  NAME                      STATUS   VOLUME       CAPACITY   ACCESS MODES   STORAGECLASS   AGE
  data-kafka-controller-0   Bound    kafka-pv-1   8Gi        RWO                           10m
  data-kafka-controller-1   Bound    kafka-pv-2   8Gi        RWO                           10m
  data-kafka-controller-2   Bound    kafka-pv-3   8Gi        RWO                           10m
  ```

#### 3. [새로운 터미널] Producer pod 생성 (kafka 설치 시 출력된 명령어를 참고하여 사용)
```
$ kubectl run kafka-pro --restart='Never' --image docker.io/bitnami/kafka:3.8.1-debian-12-r0 --namespace default --command -- sleep infinity
$ kubectl exec --tty -i kafka-pro --namespace default -- bash
I have no name!@kafka-pro:/$ kafka-console-producer.sh --bootstrap-server kafka.default.svc.cluster.local:9092 --topic test
>123
>12
>
```

#### 4. [새로운 터미널] Consumer pod 생성 (kafka 설치 시 출력된 명령어를 참고하여 사용)
```
$ kubectl run kafka-con --restart='Never' --image docker.io/bitnami/kafka:3.8.1-debian-12-r0 --namespace default --command -- sleep infinity
$ kubectl exec --tty -i kafka-con --namespace default -- bash
I have no name!@kafka-con:/$ kafka-console-consumer.sh --bootstrap-server kafka.default.svc.cluster.local:9092 --topic test --from-beginning
123
12
```