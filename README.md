# kicker-clicker

DRIVER_LOCATION=C:\\Users\\pyros\\Desktop\\shoes\\geckodriver.exe

//run mysql
docker run --name mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD='' -d mysql:latest

//Users
SELECT * FROM `kicker`.`users`;
CREATE TABLE `kicker`.`users` (id VARCHAR(50), email VARCHAR(200), password VARCHAR(200), cv VARCHAR(5));
INSERT INTO `kicker`.`users`  (id, email, password, cv) VALUES ('', '', '', '');

//Drops
select * from `kicker`.`drops`;
CREATE TABLE `kicker`.`drops` (id VARCHAR(50), name VARCHAR(200), color VARCHAR(200), url VARCHAR(250), dateTime VARCHAR(100), wanted VARCHAR(2), monitorPeriod VARCHAR(12));
INSERT INTO `kicker`.`drops`  (id, name, color, url, dateTime, wanted, monitorPeriod)
VALUES ('', '', '', '', '', '', '');

//run local firefox
docker run -d -p 4444:4444 --shm-size 2g selenium/standalone-firefox:3.141.59-20200525






//run kicker clicker
docker run -d -v /home/siddhartha/selenium:/selenium --name kickerclicker -p 7000:7000 -e MYSQL_USER='' -e MYSQL_PASSWORD='' -e MYSQL_URL='' zeab/kickerclicker:latest
