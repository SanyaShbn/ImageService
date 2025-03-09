## Установка и запуск приложения

В этом кратком руководстве описывается процесс настройки и запуска приложения локально.

### Установка

Сначала клонируйте репозиторий проекта:

```
https://github.com/SanyaShbn/ImageService.git
```

В корневом каталоге проекта создайте файл с именем .env. Этот файл будет содержать переменные окружения,
необходимые для работы приложения (убедитесь, что ваш текстовый редактор или IDE настроены для отслеживания изменений в файле .env).
Для запуска приложения необходимо настроить следующие переменные окружения:

- `MONGODB_URI`: URL базы данных MongoDB.
- `KAFKA_BOOTSTRAP_SERVERS`: Сервер Kafka.
- `MONGO_USERNAME`: Имя пользователя базы данных MongoDB.
- `MONGO_PASSWORD`: Пароль базы данных MongoDB.

Можете просто скопировать следующие настройки по умолчанию:

```
MONGODB_URI=mongodb://root:example@mongodb:27017/image_service_db?authSource=admin
KAFKA_BOOTSTRAP_SERVERS=kafka:9092
MONGO_USERNAME=root
MONGO_PASSWORD=example
```

Соберите проект локально, используя gradle:

```
gradle clean build
```

Перед запуском проекта через Docker Compose необходимо создать сеть (для корректной совместной работы контейнеров данного проекта и основного сервиса):

```
docker network create backend-network
```

Для запуска docker-compose:

```
docker-compose up
```

### Использование

Функционал данного микросервиса имеет смысл тестировать вместе с основным сервисом, для которого он был разработан. 
Ссылка на репозиторий основного проекта с подробным гайдом по сборке, запуску и тестированию обоих сервисов:

```
https://github.com/SanyaShbn/TicketBookingSystem.git
```

## Описание процесса деплоя в kubernetes (на данный момент, инструкция - только для Windows)

### Предварительные требования

Убедитесь, что установлены следующие инструменты:

- [Minikube](https://minikube.sigs.k8s.io/docs/start/) (для локального Kubernetes-кластера)
- [kubectl](https://kubernetes.io/docs/tasks/tools/) (для управления кластером)
- Docker (с поддержкой локального демона Minikube)
- В качестве виртуальной машины использовал Oracle VirtualBox(https://www.virtualbox.org/wiki/Downloads)

### Шаг 1: Запуск Minikube

Запустите Minikube, если он ещё не запущен (в PowerShell):

```
minikube start --no-vtx-check
```

Настройте Docker-клиент на использование Docker-демона Minikube:

```
minikube docker-env | Invoke-Expression
```

### Шаг 2: Построение Docker-образа

Перейдите в корневую директорию проекта и выполните:

```
docker build -t imageservice-image-service:latest .
```

### Шаг 3: Деплой MongoDB

Перейдите в директорию "kubernetes" в корне проекта, содержащую .yaml-файлы, необходимые для деплоя
проекта в kubernetes.
Примените файл persistent-volumes.yaml для настройки Persistent Volume в MongoDB:

```
kubectl apply -f persistent-volumes.yaml
```

Далее задеплойте MongoDB:

```
kubectl apply -f mongodb-deployment.yaml
```

### Шаг 4: Деплой Kafka

Сначала применим файл zookeeper-deployment.yaml:

```
kubectl apply -f zookeeper-deployment.yaml
```

Далее деплоим непосредственно Kafka:

```
kubectl apply -f kafka-deployment.yaml
```

Рекомендуется периодически осуществлять проверку статуса подов, чтобы удостовериться, что все они запущены и корректно
работают перед развертыванием ImageService:

```
kubectl get pods
```

### Шаг 5: Деплой ImageService

Примените файл image-service-deployment.yaml:

```
kubectl apply -f image-service-deployment.yaml
```

Перенаправьте порт для локального доступа после его запуска:

```
kubectl port-forward svc/image-service 8081:8081
```

Теперь сервис доступен по адресу http://localhost:8081.

### Шаг 6: Проверка взаимодействия

ImageService использует MongoDB и Kafka. Можете проверить их статус:

```
kubectl get pods
kubectl logs <имя-pod>
```

Дальнейшее полноценное тестирование сервиса рекомендуется производить вместе с основным, TicketBookingSystem:
https://github.com/SanyaShbn/TicketBookingSystem/tree/learning-ci-cd-and-k8s.
По данной ссылке доступна ветка с описанием деплоя в kubernetes уже основного сервиса (отдельно ImageService можно
протестировать, используя, например, Postman. Swagger здесь подключен не был)