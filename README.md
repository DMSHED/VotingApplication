Чтобы запустить приложение необходимо

Пока без docker compose, запускаем ручками

1. Запустить докер контейнер с postgres:latest
port: 5432:5432
user: postgres
password: postgres
docker run -itd -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 -v /data:/var/lib/postgresql/data --name postgresql postgres

2. Запустить Jar файл в режиме сервера 

java -jar VotingApp-1.0-SNAPSHOT.jar --mode=server 
(mode можно не указывать тогда по дефолту запустит в режиме сервера)

3. Запустить Jar файл в режиме клиента
java -jar VotingApp-1.0-SNAPSHOT.jar --mode=client

4. Все команды выполняются в терминале, такое вот приложение
На данный момент доступно

в режиме клиента:
exit - выход из сессии (доступно и без добавления юзера в сессию)
login -u=<username> добавит юзера в текущую сессию сервера и позволит выполнять остальные команды
create topic -n=<topic_name> создат раздел



